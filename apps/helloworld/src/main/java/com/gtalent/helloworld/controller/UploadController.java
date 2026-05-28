package com.gtalent.helloworld.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.service.StorageService;

@RestController
@RequestMapping("v1")
public class UploadController {

    @Autowired
    private StorageService storageService;

    // ── Phase 1: single-request streaming upload ──────────────────────
    // Unchanged API; the service layer now streams to disk instead of
    // loading the entire file into a byte array.

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> upload(@RequestParam("file") MultipartFile file) {
        FileMetadata metadata = storageService.store(file);
        return ResponseEntity.ok(metadata);
    }

    // ── Phase 2: chunked / resumable upload ───────────────────────────

    /**
     * Step 1 – Initialise an upload session.
     *
     * Request body (JSON):
     * <pre>
     * {
     *   "originalName": "movie.mp4",
     *   "contentType": "video/mp4",
     *   "totalSize": 107374182400
     * }
     * </pre>
     * Response: {@link UploadSession} JSON containing {@code uploadId} and
     * {@code expiredAt} that the client must retain for subsequent calls.
     */
    @PostMapping("/upload/init")
    public ResponseEntity<UploadSession> initUpload(@Valid @RequestBody InitUploadRequest req) {
        UploadSession session = storageService.initUpload(
                req.getOriginalName(), req.getContentType(), req.getTotalSize());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    /**
     * Step 2 – Upload a single chunk.
     *
     * The request body must be {@code application/octet-stream}.
     * Query parameter {@code offset} is the byte position within the
     * final file where this chunk starts (0-based).  Chunks may be sent
     * in any order; the client is responsible for tracking which chunks
     * have been successfully acknowledged.
     *
     * Returns {@code 204 No Content} on success.
     */
    @PutMapping(value = "/upload/{uploadId}/chunk",
                consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> uploadChunk(
            @PathVariable String uploadId,
            @RequestParam long offset,
            HttpServletRequest request) throws Exception {
        storageService.storeChunk(uploadId, offset, request.getInputStream());
        return ResponseEntity.noContent().build();
    }

    /**
     * Step 3 – Finalise the upload.
     *
     * The server verifies the total file size, computes SHA-256, performs
     * deduplication, and commits the file to permanent storage.
     *
     * Returns the resulting {@link FileMetadata}.
     */
    @PostMapping("/upload/{uploadId}/complete")
    public ResponseEntity<FileMetadata> completeUpload(@PathVariable String uploadId) {
        FileMetadata metadata = storageService.completeUpload(uploadId);
        return ResponseEntity.ok(metadata);
    }

    // ── Inner DTO ─────────────────────────────────────────────────────

    public static class InitUploadRequest {

        @NotBlank
        private String originalName;

        private String contentType;

        @Positive
        private long totalSize;

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
    }
}

