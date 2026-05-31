package com.gtalent.helloworld.service;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.StoredFile;
import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.domain.model.UploadStatus;
import com.gtalent.helloworld.repository.FileMetadataRepository;
import com.gtalent.helloworld.repository.StoredFileRepository;
import com.gtalent.helloworld.repository.UploadSessionRepository;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO-backed implementation of {@link StorageService}.
 * Activated when {@code storage.provider=minio}.
 *
 * <h3>Chunked upload strategy</h3>
 * Chunks are staged to a local temp file (under {@code storage.location/staging/}).
 * On {@link #completeUpload}, the fully-assembled file is streamed to MinIO via
 * {@code putObject}; the MinIO SDK handles multipart internally for large objects.
 * This avoids the internal (non-public) multipart API of the MinIO Java SDK 8.5.x.
 *
 * <h3>Deduplication</h3>
 * MinIO object key format: {@code <sha256hex>.<ext>}.
 * {@link StoredFile#path} stores the object key instead of a local file path.
 */
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
public class MinioStorageService implements StorageService {

    /** Multipart part size passed to SDK (10 MiB); SDK uses multipart for objects > this. */
    private static final long PART_SIZE = 10 * 1024 * 1024L;

    private final MinioClient minioClient;
    private final int presignExpiryMinutes;
    private final MinioProperties minioProperties;
    private final StoredFileRepository storedFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final int uploadSessionExpireHours;
    private final Path stagingRoot;

    @Autowired
    public MinioStorageService(MinioClient minioClient,
                                MinioProperties minioProperties,
                                StorageProperties storageProperties,
                                StoredFileRepository storedFileRepository,
                                FileMetadataRepository fileMetadataRepository,
                                UploadSessionRepository uploadSessionRepository,
                                @Value("${storage.upload-session-expire-hours:24}") int uploadSessionExpireHours,
                                @Value("${storage.presign-expiry-minutes:15}") int presignExpiryMinutes) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.storedFileRepository = storedFileRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadSessionRepository = uploadSessionRepository;
        this.uploadSessionExpireHours = uploadSessionExpireHours;
        this.presignExpiryMinutes = presignExpiryMinutes;
        this.stagingRoot = Paths.get(storageProperties.getLocation()).resolve("staging");
    }

    // ── Lifecycle ────────────────────────────────────────────────

    @Override
    public void init() {
        try {
            Files.createDirectories(stagingRoot);
            String bucket = minioProperties.getBucketName();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new StorageException("Could not initialise MinIO storage", e);
        }
    }

    // ── Phase 1: single-request streaming upload ─────────────────

    @Override
    @Transactional
    public FileMetadata store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Cannot store empty file.");
        }
        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);
        String contentType = file.getContentType();

        try {
            // First pass: compute SHA-256 for deduplication check
            String hash;
            try (InputStream is = file.getInputStream()) {
                hash = computeHash(is);
            }
            String objectKey = hash + (ext.isEmpty() ? "" : "." + ext);

            // Deduplication: reuse StoredFile record if the same hash already exists
            StoredFile storedFile = storedFileRepository.findByHash(hash)
                    .orElseGet(() -> {
                        // Second pass: upload to MinIO
                        try (InputStream is = file.getInputStream()) {
                            putObject(objectKey, is, file.getSize(), contentType);
                        } catch (IOException e) {
                            throw new StorageException("Failed to read file for upload", e);
                        }
                        return persistStoredFile(hash, objectKey);
                    });

            return saveMetadata(storedFile, originalName, contentType, file.getSize());

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException("Failed to store file via MinIO", e);
        }
    }

    // ── Phase 2: chunked / resumable upload ───────────────────────

    @Override
    @Transactional
    public UploadSession initUpload(String originalName, String contentType, long totalSize) {
        String uploadId = UUID.randomUUID().toString();
        Path stagingFile = stagingPath(uploadId);

        // Create an empty staging file; chunks will be written at their byte offsets.
        try (FileChannel fc = FileChannel.open(stagingFile,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            // intentionally empty – OS allocates space lazily
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
        // Reuse minioUploadId field to carry the local staging file path
        session.setMinioUploadId(stagingFile.toString());
        return uploadSessionRepository.save(session);
    }

    private static final int BUFFER_SIZE = 256 * 1024;

    @Override
    @Transactional
    public void storeChunk(String uploadId, long offset, InputStream chunkStream) {
        UploadSession session = loadActiveSession(uploadId);
        long totalSize = session.getTotalSize();
        if (offset < 0 || offset >= totalSize) {
            throw new StorageException("Offset out of bounds: " + offset);
        }
        if (session.getReceivedBytes() >= totalSize) {
            throw new StorageException("Upload already complete; no more chunks accepted: " + uploadId);
        }

        long maxChunkBytes = totalSize - offset;  // prevent writing beyond declared size
        Path stagingFile = stagingPath(uploadId);
        try (RandomAccessFile raf = new RandomAccessFile(stagingFile.toFile(), "rw")) {
            raf.seek(offset);
            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead;
            long written = 0;
            while ((bytesRead = chunkStream.read(buf)) != -1) {
                if (written + bytesRead > maxChunkBytes) {
                    throw new StorageException(
                            "Chunk exceeds remaining file size at offset " + offset);
                }
                raf.write(buf, 0, bytesRead);
                written += bytesRead;
            }
            // Cumulative sum (not high-watermark) to detect sparse-file spoofing
            session.setReceivedBytes(session.getReceivedBytes() + written);
            // Transition PENDING → IN_PROGRESS on first successful chunk
            if (session.getStatus() == UploadStatus.PENDING) {
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
        UploadSession session = loadInProgressSession(uploadId);
        Path stagingFile = stagingPath(uploadId);

        try {
            // Validate cumulative receivedBytes BEFORE touching the filesystem
            if (session.getReceivedBytes() != session.getTotalSize()) {
                throw new StorageException(
                        "Upload incomplete: received " + session.getReceivedBytes()
                        + " of " + session.getTotalSize() + " bytes");
            }
            long actualSize = Files.size(stagingFile);
            if (actualSize != session.getTotalSize()) {
                throw new StorageException(
                        "File size mismatch: expected " + session.getTotalSize()
                        + " bytes, got " + actualSize);
            }

            String hash = hashFile(stagingFile);
            String ext = extractExtension(session.getOriginalName());
            String objectKey = hash + (ext.isEmpty() ? "" : "." + ext);

            StoredFile storedFile = storedFileRepository.findByHash(hash)
                    .orElseGet(() -> {
                        // Stream the staging file to MinIO
                        try (InputStream is = Files.newInputStream(stagingFile)) {
                            putObject(objectKey, is, session.getTotalSize(), session.getContentType());
                        } catch (IOException e) {
                            throw new StorageException("Failed to upload staging file to MinIO", e);
                        }
                        return persistStoredFile(hash, objectKey);
                    });

            FileMetadata meta = saveMetadata(storedFile, session.getOriginalName(),
                    session.getContentType(), session.getTotalSize());

            session.setStatus(UploadStatus.COMPLETED);
            uploadSessionRepository.save(session);

            Files.deleteIfExists(stagingFile);
            return meta;

        } catch (StorageException se) {
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            throw se;
        } catch (IOException | NoSuchAlgorithmException e) {
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            throw new StorageException("Failed to complete upload " + uploadId, e);
        }
    }

    // ── Phase 3: pre-signed URL upload ───────────────────────────────

    @Override
    @Transactional
    public PresignedUploadResult generatePresignedUpload(String originalName, String contentType, long fileSize) {
        String uploadId = UUID.randomUUID().toString();
        String ext = extractExtension(originalName);
        String objectKey = uploadId + (ext.isEmpty() ? "" : "." + ext);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(presignExpiryMinutes);

        String presignedUrl;
        try {
            presignedUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .expiry(presignExpiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new StorageException("Failed to generate presigned URL", e);
        }

        UploadSession session = new UploadSession();
        session.setUploadId(uploadId);
        session.setOriginalName(originalName);
        session.setContentType(contentType != null ? contentType : "application/octet-stream");
        session.setTotalSize(fileSize);
        session.setReceivedBytes(0L);
        session.setStatus(UploadStatus.PENDING);
        session.setExpiredAt(expiresAt);
        // Reuse minioUploadId field to carry the UUID-based object key
        session.setMinioUploadId(objectKey);
        uploadSessionRepository.save(session);

        return new PresignedUploadResult(uploadId, presignedUrl, expiresAt.toString());
    }

    @Override
    @Transactional
    public FileMetadata confirmPresignedUpload(String uploadToken) {
        UploadSession session = uploadSessionRepository.findByUploadId(uploadToken)
                .filter(s -> s.getStatus() == UploadStatus.PENDING)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found or not PENDING: " + uploadToken));

        String objectKey = session.getMinioUploadId();
        String bucket = minioProperties.getBucketName();

        try {
            // 1. Verify the object exists in MinIO and its size matches the declared totalSize
            StatObjectResponse stats;
            try {
                stats = minioClient.statObject(
                        StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            } catch (Exception e) {
                throw new StorageException(
                        "Object not found in MinIO – upload the file to the presigned URL first: "
                        + uploadToken, e);
            }
            if (stats.size() != session.getTotalSize()) {
                throw new StorageException("Object size mismatch: expected "
                        + session.getTotalSize() + " bytes, got " + stats.size());
            }

            // 2. Stream from MinIO to compute SHA-256 for deduplication
            String hash;
            try (InputStream is = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
                hash = computeHash(is);
            }

            // 3. Dedup: if hash already in DB, delete the newly uploaded duplicate and reuse
            StoredFile storedFile = storedFileRepository.findByHash(hash)
                    .map(existing -> {
                        try {
                            minioClient.removeObject(RemoveObjectArgs.builder()
                                    .bucket(bucket).object(objectKey).build());
                        } catch (Exception ex) {
                            throw new StorageException(
                                    "Failed to remove duplicate object: " + objectKey, ex);
                        }
                        return existing;
                    })
                    .orElseGet(() -> persistStoredFile(hash, objectKey));

            // 4. Persist metadata and complete the session
            FileMetadata meta = saveMetadata(storedFile,
                    session.getOriginalName(), session.getContentType(), session.getTotalSize());
            session.setStatus(UploadStatus.COMPLETED);
            uploadSessionRepository.save(session);
            return meta;

        } catch (StorageException se) {
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            throw se;
        } catch (Exception e) {
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            throw new StorageException("Failed to confirm presigned upload: " + uploadToken, e);
        }
    }

    // ── Common ────────────────────────────────────────────────────

    @Override
    public Resource loadAsResource(String filename) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(filename)
                            .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    @Transactional
    public void deleteAll() {
        try {
            List<DeleteObject> objects = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .recursive(true)
                            .build());
            for (Result<Item> result : results) {
                objects.add(new DeleteObject(result.get().objectName()));
            }
            if (!objects.isEmpty()) {
                Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .objects(objects)
                                .build());
                for (Result<DeleteError> err : errors) {
                    err.get(); // surface any delete errors
                }
            }
            fileMetadataRepository.deleteAll();
            storedFileRepository.deleteAll();
        } catch (Exception e) {
            throw new StorageException("Failed to delete all objects from MinIO", e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long metadataId) {
        FileMetadata meta = fileMetadataRepository.findById(metadataId)
                .orElseThrow(() -> new StorageFileNotFoundException("File not found: id=" + metadataId));
        StoredFile storedFile = meta.getStoredFile();

        fileMetadataRepository.delete(meta);
        long remaining = fileMetadataRepository.countByStoredFile(storedFile);
        if (remaining == 0) {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(storedFile.getPath())
                        .build());
            } catch (Exception e) {
                throw new StorageException("Failed to delete object from MinIO: " + storedFile.getPath(), e);
            }
            storedFileRepository.delete(storedFile);
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private void putObject(String objectKey, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .stream(stream, size, PART_SIZE)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to MinIO: " + objectKey, e);
        }
    }

    private StoredFile persistStoredFile(String hash, String objectKey) {
        StoredFile sf = new StoredFile();
        sf.setHash(hash);
        sf.setPath(objectKey);
        return storedFileRepository.save(sf);
    }

    private FileMetadata saveMetadata(StoredFile storedFile, String originalName,
                                       String contentType, long fileSize) {
        FileMetadata meta = new FileMetadata();
        meta.setStoredFile(storedFile);
        meta.setOriginalName(originalName);
        meta.setContentType(contentType);
        meta.setFileSize(fileSize);
        return fileMetadataRepository.save(meta);
    }

    /** Accepts PENDING or IN_PROGRESS — used by storeChunk. */
    private UploadSession loadActiveSession(String uploadId) {
        return uploadSessionRepository.findByUploadId(uploadId)
                .filter(s -> s.getStatus() == UploadStatus.PENDING
                          || s.getStatus() == UploadStatus.IN_PROGRESS)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found or not active: " + uploadId));
    }

    /** Requires IN_PROGRESS — used by completeUpload (guards zero-chunk complete). */
    private UploadSession loadInProgressSession(String uploadId) {
        return uploadSessionRepository.findByUploadId(uploadId)
                .filter(s -> s.getStatus() == UploadStatus.IN_PROGRESS)
                .orElseThrow(() -> new StorageFileNotFoundException(
                        "Upload session not found or not IN_PROGRESS: " + uploadId));
    }

    private Path stagingPath(String uploadId) {
        return stagingRoot.resolve(uploadId);
    }

    private static String computeHash(InputStream stream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (DigestInputStream dis = new DigestInputStream(stream, digest)) {
            dis.readAllBytes();
        }
        return bytesToHex(digest.digest());
    }

    private static String hashFile(Path file) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = Files.newInputStream(file)) {
            return computeHash(is);
        }
    }

    private static String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1) : "";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
