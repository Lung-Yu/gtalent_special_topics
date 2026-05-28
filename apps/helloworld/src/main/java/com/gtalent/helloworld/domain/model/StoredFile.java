package com.gtalent.helloworld.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Column(nullable = false)
    private String path;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
