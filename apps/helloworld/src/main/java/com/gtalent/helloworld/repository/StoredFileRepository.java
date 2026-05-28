package com.gtalent.helloworld.repository;

import com.gtalent.helloworld.domain.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    Optional<StoredFile> findByHash(String hash);
}
