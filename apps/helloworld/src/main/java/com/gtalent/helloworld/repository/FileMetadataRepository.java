package com.gtalent.helloworld.repository;

import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    long countByStoredFile(StoredFile storedFile);
}
