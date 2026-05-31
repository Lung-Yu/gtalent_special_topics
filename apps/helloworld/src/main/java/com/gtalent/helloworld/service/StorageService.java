package com.gtalent.helloworld.service;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.UploadSession;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    void init();

    // ── Phase 1: streaming single-request upload ──────────────────

    /** Streams the multipart file to disk without buffering in heap. */
    FileMetadata store(MultipartFile file);

    // ── Phase 2: chunked / resumable upload ───────────────────────

    /**
     * Creates an {@link UploadSession} and the corresponding staging file.
     * The client should declare the total file size so the server can
     * pre-check available disk space.
     */
    UploadSession initUpload(String originalName, String contentType, long totalSize);

    /**
     * Writes a single chunk starting at {@code offset} bytes into the
     * staging file.  Chunks may arrive in any order.
     */
    void storeChunk(String uploadId, long offset, InputStream chunkStream);

    /**
     * Validates the staging file size, computes SHA-256, performs
     * deduplication, commits the file to permanent storage, and
     * returns the resulting {@link FileMetadata}.
     */
    FileMetadata completeUpload(String uploadId);

    // ── Common ────────────────────────────────────────────────────

    Resource loadAsResource(String filename);

    void deleteAll();

    void deleteFile(Long metadataId);

    // ── Phase 3: pre-signed URL upload (MinIO only) ───────────────────

    /**
     * Generates a time-limited pre-signed PUT URL so the client can upload a file
     * directly to object storage without routing bytes through this server.
     *
     * <p>The returned {@code uploadToken} must be passed to {@link #confirmPresignedUpload}
     * after the client has PUT the file bytes to the presigned URL.
     *
     * <p><b>Not supported</b> by the filesystem storage provider — throws
     * {@link StorageException} when {@code storage.provider=filesystem}.
     */
    PresignedUploadResult generatePresignedUpload(String originalName, String contentType, long fileSize);

    /**
     * Verifies that the object was uploaded to the presigned URL, streams it to compute
     * SHA-256, performs deduplication, persists {@link com.gtalent.helloworld.domain.model.FileMetadata},
     * and marks the upload session as COMPLETED.
     *
     * <p><b>Not supported</b> by the filesystem storage provider — throws
     * {@link StorageException} when {@code storage.provider=filesystem}.
     */
    FileMetadata confirmPresignedUpload(String uploadToken);
}
