package com.gtalent.helloworld.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Tracks an in-progress chunked upload session.
 * The client initiates a session, uploads chunks in any order using the
 * uploadId, then calls the "complete" endpoint to finalise the file.
 */
@Entity
@Table(name = "upload_sessions")
public class UploadSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique token handed to the client after init. */
    @Column(nullable = false, unique = true, length = 36)
    private String uploadId;

    @Column(nullable = false)
    private String originalName;

    private String contentType;

    /** Total expected file size in bytes, declared by the client at init time. */
    @Column(nullable = false)
    private Long totalSize;

    /** Cumulative bytes written so far (upper-watermark of sequential writes). */
    @Column(nullable = false)
    private Long receivedBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UploadStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    /** After this timestamp the session and its staging file are eligible for cleanup. */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /**
     * MinIO multipart upload ID returned by createMultipartUpload().
     * Null when using filesystem storage or before multipart is initiated.
     */
    @Column(length = 512)
    private String minioUploadId;

    /**
     * JSON array of completed part ETags: [{"part":1,"etag":"xxx"}, ...].
     * Accumulated during storeChunk() calls, consumed by completeUpload().
     * Stored as TEXT to accommodate large numbers of parts.
     */
    @Column(columnDefinition = "TEXT")
    private String partEtags;

    // ── getters / setters ────────────────────────────────────────

    public Long getId() { return id; }

    public String getUploadId() { return uploadId; }
    public void setUploadId(String uploadId) { this.uploadId = uploadId; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTotalSize() { return totalSize; }
    public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

    public Long getReceivedBytes() { return receivedBytes; }
    public void setReceivedBytes(Long receivedBytes) { this.receivedBytes = receivedBytes; }

    public UploadStatus getStatus() { return status; }
    public void setStatus(UploadStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }

    public String getMinioUploadId() { return minioUploadId; }
    public void setMinioUploadId(String minioUploadId) { this.minioUploadId = minioUploadId; }

    public String getPartEtags() { return partEtags; }
    public void setPartEtags(String partEtags) { this.partEtags = partEtags; }
}
