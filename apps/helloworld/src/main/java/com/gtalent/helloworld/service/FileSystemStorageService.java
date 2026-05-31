package com.gtalent.helloworld.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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

    private final Path rootLocation;
    private final StoredFileRepository storedFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final Semaphore uploadSemaphore;
    private final int uploadSessionExpireHours;

    @Autowired
    public FileSystemStorageService(StorageProperties properties,
                                    StoredFileRepository storedFileRepository,
                                    FileMetadataRepository fileMetadataRepository,
                                    UploadSessionRepository uploadSessionRepository,
                                    @Value("${storage.max-concurrent-uploads:3}") int maxConcurrentUploads,
                                    @Value("${storage.upload-session-expire-hours:24}") int uploadSessionExpireHours) {
        if (properties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }
        this.rootLocation = Paths.get(properties.getLocation());
        this.storedFileRepository = storedFileRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadSessionRepository = uploadSessionRepository;
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

        String uploadId = UUID.randomUUID().toString();
        Path stagingFile = stagingPath(uploadId);
        try (FileChannel fc = FileChannel.open(stagingFile,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            // Create empty file; OS will allocate space lazily (sparse file).
        } catch (IOException e) {
            throw new StorageException("Could not create staging file for upload " + uploadId, e);
        }

        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setOriginalName(originalName);
        session.setContentType(contentType);
        session.setTotalSize(totalSize);
        session.setReceivedBytes(0L);
        session.setStatus(UploadStatus.PENDING);
        session.setExpiredAt(LocalDateTime.now().plusHours(uploadSessionExpireHours));
        return uploadSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void storeChunk(String uploadId, long offset, InputStream chunkStream) {
        UploadSession session = uploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found: " + uploadId));
        if (session.getStatus() != UploadStatus.PENDING) {
            throw new StorageException("Session is not PENDING: " + uploadId);
        }
        if (offset < 0 || offset >= session.getTotalSize()) {
            throw new StorageException("Offset out of bounds: " + offset);
        }

        Path stagingFile = stagingPath(uploadId);
        try (RandomAccessFile raf = new RandomAccessFile(stagingFile.toFile(), "rw")) {
            raf.seek(offset);
            byte[] buffer = new byte[256 * 1024]; // 256 KB write buffer
            int bytesRead;
            long written = 0L;
            while ((bytesRead = chunkStream.read(buffer)) != -1) {
                raf.write(buffer, 0, bytesRead);
                written += bytesRead;
            }
            // Track sequential progress (upper-watermark of written range)
            long newHighWater = Math.min(session.getTotalSize(), offset + written);
            if (newHighWater > session.getReceivedBytes()) {
                session.setReceivedBytes(newHighWater);
                uploadSessionRepository.save(session);
            }
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
        if (session.getStatus() != UploadStatus.PENDING) {
            throw new StorageException("Session is not PENDING: " + uploadId);
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
        byte[] buffer = new byte[256 * 1024];
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

