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
}
