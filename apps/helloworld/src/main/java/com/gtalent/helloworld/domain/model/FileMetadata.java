package com.gtalent.helloworld.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stored_file_id", nullable = false)
    private StoredFile storedFile;

    @Column(nullable = false)
    private String originalName;

    private String contentType;

    private Long fileSize;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    private String uploadedBy;

    public Long getId() { return id; }
    public StoredFile getStoredFile() { return storedFile; }
    public void setStoredFile(StoredFile storedFile) { this.storedFile = storedFile; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
