package com.gtalent.helloworld.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.StoredFile;
import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.domain.model.UploadStatus;
import com.gtalent.helloworld.repository.FileMetadataRepository;
import com.gtalent.helloworld.repository.StoredFileRepository;
import com.gtalent.helloworld.repository.UploadSessionRepository;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemStorageService implements StorageService {

    /** Shared I/O buffer size for chunk writes and SHA-256 hashing (256 KB). */
    private static final int BUFFER_SIZE = 256 * 1024;

    private final Path rootLocation;
    private final StoredFileRepository storedFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final Semaphore uploadSemaphore;
    private final int uploadSessionExpireHours;
    private final UploadIdAllocator uploadIdAllocator;

    @Autowired
    public FileSystemStorageService(StorageProperties properties,
                                    StoredFileRepository storedFileRepository,
                                    FileMetadataRepository fileMetadataRepository,
                                    UploadSessionRepository uploadSessionRepository,
                                    UploadIdAllocator uploadIdAllocator,
                                    @Value("${storage.max-concurrent-uploads:3}") int maxConcurrentUploads,
                                    @Value("${storage.upload-session-expire-hours:24}") int uploadSessionExpireHours) {
        if (properties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = Paths.get(properties.getLocation());
        this.storedFileRepository = storedFileRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.uploadIdAllocator = uploadIdAllocator;
        this.uploadSemaphore = new Semaphore(maxConcurrentUploads);
        this.uploadSessionExpireHours = uploadSessionExpireHours;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            // Staging area for in-flight uploads; separate from final storage
            Files.createDirectories(rootLocation.resolve("tmp"));
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Phase 1 – Streaming single-request upload
    //
    // Key fix: replaced file.getBytes() (loads 100 GB into heap) with
    // file.getInputStream() fed through DigestInputStream → disk copy.
    // SHA-256 is computed on-the-fly; the final file is placed via an
    // atomic rename so there is never a partial visible file.
    // ══════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public FileMetadata store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        // Cap the number of simultaneous in-progress uploads
        if (!uploadSemaphore.tryAcquire()) {
            throw new StorageException("Too many concurrent uploads. Please try again later.");
        }
        try {
            Path tmpPath = newTmpPath();
            String hash = streamToFile(file.getInputStream(), tmpPath);
            String ext  = extractExtension(file.getOriginalFilename());
            StoredFile storedFile = deduplicateOrMove(tmpPath, hash, ext);
            return saveMetadata(storedFile, file.getOriginalFilename(),
                    file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        } finally {
            uploadSemaphore.release();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Phase 2 – Chunked / resumable upload
    //
    // Flow:
    //   1. POST /v1/upload/init        → returns uploadId + expiredAt
    //   2. PUT  /v1/upload/{id}/chunk  → repeat for each chunk
    //   3. POST /v1/upload/{id}/complete → SHA-256, dedup, persist
    //
    // Chunks may arrive in any order; RandomAccessFile.seek() places
    // each one at the correct byte offset.  The staging file lives at
    // upload-dir/tmp/{uploadId} until the session is completed or
    // cleaned up by the scheduler.
    // ══════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public UploadSession initUpload(String originalName, String contentType, long totalSize) {
        checkDiskSpace(totalSize);

        // UploadIdAllocator (獨立 bean) 負責以 CREATE_NEW 原子建立 staging 佔位檔並 retry。
        // 參見 UploadIdAllocator Javadoc 了解雙重防線設計。
        String uploadId;
        try {
            uploadId = uploadIdAllocator.allocate(rootLocation.resolve("tmp"));
        } catch (IOException e) {
            throw new StorageException("Could not allocate upload ID", e);
        }

        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setOriginalName(originalName);
        session.setContentType(contentType);
        session.setTotalSize(totalSize);
        session.setReceivedBytes(0L);
        session.setStatus(UploadStatus.PENDING);
        session.setExpiredAt(LocalDateTime.now().plusHours(uploadSessionExpireHours));
        try {
            return uploadSessionRepository.save(session);
        } catch (DataIntegrityViolationException e) {
            // 第二道防線：DB UNIQUE constraint 衝突（極罕見）
            // 清理已建立的 staging 佔位檔，避免 filesystem 洩漏
            try { Files.deleteIfExists(stagingPath(uploadId)); } catch (IOException ignored) { /* best-effort */ }
            throw new StorageException("Upload ID collision detected in DB for " + uploadId, e);
        }
    }

    @Override
    @Transactional
    public void storeChunk(String uploadId, long offset, InputStream chunkStream) {
        UploadSession session = uploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found: " + uploadId));

        // 接受 PENDING（第一個 chunk）或 IN_PROGRESS（後續 chunk）
        UploadStatus currentStatus = session.getStatus();
        if (currentStatus != UploadStatus.PENDING && currentStatus != UploadStatus.IN_PROGRESS) {
            throw new StorageException(
                    "Session is not accepting chunks (status=" + currentStatus + "): " + uploadId);
        }

        // offset 起點越界（含等於 totalSize 的情況）
        if (offset < 0 || offset >= session.getTotalSize()) {
            throw new StorageException("Offset out of bounds: " + offset);
        }

        // 已累積滿額，不再接受任何資料
        if (session.getReceivedBytes() >= session.getTotalSize()) {
            throw new StorageException(
                    "Upload already at capacity; no further chunks accepted for: " + uploadId);
        }

        // 此 chunk 從 offset 開始最多可寫入的 bytes 數
        long maxChunkBytes = session.getTotalSize() - offset;

        Path stagingFile = stagingPath(uploadId);
        try (RandomAccessFile raf = new RandomAccessFile(stagingFile.toFile(), "rw")) {
            raf.seek(offset);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long written = 0L;
            while ((bytesRead = chunkStream.read(buffer)) != -1) {
                // chunk 末端越界防護：拒絕寫入超出宣告大小的資料
                if (written + bytesRead > maxChunkBytes) {
                    throw new StorageException(
                            "Chunk extends beyond declared file size at offset " + offset);
                }
                raf.write(buffer, 0, bytesRead);
                written += bytesRead;
            }

            // 累積式計算：記錄本次實際寫入的 bytes，防止 sparse file 偽造進度
            long newReceived = session.getReceivedBytes() + written;
            if (newReceived > session.getTotalSize()) {
                throw new StorageException(
                        "Total received bytes exceed declared total size for: " + uploadId);
            }
            session.setReceivedBytes(newReceived);

            // 第一個 chunk 成功後將狀態從 PENDING 推進至 IN_PROGRESS
            if (currentStatus == UploadStatus.PENDING) {
                session.setStatus(UploadStatus.IN_PROGRESS);
            }
            uploadSessionRepository.save(session);
        } catch (IOException e) {
            throw new StorageException("Failed to write chunk at offset " + offset, e);
        }
    }

    @Override
    @Transactional
    public FileMetadata completeUpload(String uploadId) {
        UploadSession session = uploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found: " + uploadId));

        // 只有已收到至少一個 chunk 的 session 才允許 complete
        if (session.getStatus() != UploadStatus.IN_PROGRESS) {
            throw new StorageException(
                    "Session must be IN_PROGRESS to complete (status="
                    + session.getStatus() + "): " + uploadId);
        }

        // 累積 receivedBytes 必須與宣告大小完全一致，防止部分上傳即呼叫 complete
        if (!session.getReceivedBytes().equals(session.getTotalSize())) {
            throw new StorageException(
                    "Incomplete upload: received " + session.getReceivedBytes()
                    + " of " + session.getTotalSize() + " bytes for: " + uploadId);
        }

        Path stagingFile = stagingPath(uploadId);
        try {
            long actualSize = Files.size(stagingFile);
            if (actualSize != session.getTotalSize()) {
                throw new StorageException(
                        "File size mismatch. Expected " + session.getTotalSize()
                                + " bytes, got " + actualSize);
            }
            // Hash the staging file on-the-fly; dedup / move it to final storage
            String hash = hashFile(stagingFile);
            String ext  = extractExtension(session.getOriginalName());
            StoredFile storedFile = deduplicateOrMove(stagingFile, hash, ext);
            FileMetadata metadata = saveMetadata(storedFile, session.getOriginalName(),
                    session.getContentType(), session.getTotalSize());

            session.setStatus(UploadStatus.COMPLETED);
            uploadSessionRepository.save(session);
            // stagingFile was either moved or deleted by deduplicateOrMove
            return metadata;
        } catch (IOException e) {
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            throw new StorageException("Failed to complete upload " + uploadId, e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Common CRUD
    // ══════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public void deleteFile(Long metadataId) {
        FileMetadata metadata = fileMetadataRepository.findById(metadataId)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found: " + metadataId));
        StoredFile storedFile = metadata.getStoredFile();
        fileMetadataRepository.delete(metadata);

        if (fileMetadataRepository.countByStoredFile(storedFile) == 0) {
            Path filePath = rootLocation.resolve(storedFile.getPath()).normalize().toAbsolutePath();
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new StorageException("Could not delete physical file: " + storedFile.getPath(), e);
            }
            storedFileRepository.delete(storedFile);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    // ══════════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════════

    /**
     * Streams {@code inputStream} to {@code destPath} while computing SHA-256
     * on-the-fly via {@link DigestInputStream}.  No intermediate byte array
     * is ever held in the JVM heap — the OS page cache handles buffering.
     *
     * @return hex-encoded SHA-256 hash of the written content
     */
    private String streamToFile(InputStream inputStream, Path destPath) throws IOException {
        MessageDigest digest = newSha256();
        try (DigestInputStream dis = new DigestInputStream(inputStream, digest)) {
            Files.copy(dis, destPath);
        }
        return bytesToHex(digest.digest());
    }

    /**
     * Reads an existing file and computes its SHA-256 hash without copying it.
     * Used in {@link #completeUpload} where the staging file is already on disk.
     *
     * @return hex-encoded SHA-256 hash
     */
    private String hashFile(Path filePath) throws IOException {
        MessageDigest digest = newSha256();
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream is = Files.newInputStream(filePath);
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            //noinspection StatementWithEmptyBody
            while (dis.read(buffer) != -1) { /* drain */ }
        }
        return bytesToHex(digest.digest());
    }

    /**
     * If the hash is already stored, deletes {@code tmpPath} (deduplication).
     * Otherwise atomically moves {@code tmpPath} to the final hash-based path
     * and persists a new {@link StoredFile} record.
     */
    private StoredFile deduplicateOrMove(Path tmpPath, String hash, String extension) {
        return storedFileRepository.findByHash(hash)
                .map(existing -> {
                    // Same content already on disk — discard the duplicate
                    try { Files.deleteIfExists(tmpPath); } catch (IOException ignored) { /* best-effort */ }
                    return existing;
                })
                .orElseGet(() -> {
                    String relPath = hash + extension;
                    Path dest = rootLocation.resolve(relPath).normalize().toAbsolutePath();
                    // Path-traversal guard
                    if (!dest.getParent().equals(rootLocation.toAbsolutePath())) {
                        try { Files.deleteIfExists(tmpPath); } catch (IOException ignored) { /* best-effort */ }
                        throw new StorageException("Cannot store file outside upload directory.");
                    }
                    try {
                        try {
                            Files.move(tmpPath, dest, StandardCopyOption.ATOMIC_MOVE);
                        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                            Files.move(tmpPath, dest);
                        }
                    } catch (IOException e) {
                        throw new StorageException("Failed to move file to final location.", e);
                    }
                    StoredFile sf = new StoredFile();
                    sf.setHash(hash);
                    sf.setPath(relPath);
                    return storedFileRepository.save(sf);
                });
    }

    private FileMetadata saveMetadata(StoredFile storedFile, String originalName,
                                      String contentType, long fileSize) {
        FileMetadata metadata = new FileMetadata();
        metadata.setStoredFile(storedFile);
        metadata.setOriginalName(originalName);
        metadata.setContentType(contentType);
        metadata.setFileSize(fileSize);
        return fileMetadataRepository.save(metadata);
    }

    private void checkDiskSpace(long required) {
        try {
            long usable = Files.getFileStore(rootLocation).getUsableSpace();
            if (usable < required) {
                throw new StorageException(
                        "Insufficient disk space. Required: " + required + " bytes, available: " + usable);
            }
        } catch (IOException e) {
            throw new StorageException("Cannot determine available disk space", e);
        }
    }

    private Path stagingPath(String uploadId) {
        return rootLocation.resolve("tmp").resolve(uploadId).normalize().toAbsolutePath();
    }

    private Path newTmpPath() {
        return rootLocation.resolve("tmp")
                .resolve(UUID.randomUUID().toString() + ".tmp")
                .normalize().toAbsolutePath();
    }

    private static String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ── Phase 3: pre-signed URL upload (not supported) ───────────────

    @Override
    public PresignedUploadResult generatePresignedUpload(String originalName, String contentType, long fileSize) {
        throw new StorageException(
                "Presigned URL upload is not supported by the filesystem storage provider. " +
                "Set storage.provider=minio to enable this feature.");
    }

    @Override
    public FileMetadata confirmPresignedUpload(String uploadToken) {
        throw new StorageException(
                "Presigned URL upload is not supported by the filesystem storage provider. " +
                "Set storage.provider=minio to enable this feature.");
    }
}

