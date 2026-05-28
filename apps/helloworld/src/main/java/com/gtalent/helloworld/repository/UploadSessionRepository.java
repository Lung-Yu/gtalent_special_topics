package com.gtalent.helloworld.repository;

import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.domain.model.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {

    Optional<UploadSession> findByUploadId(String uploadId);

    /** Used by the cleanup scheduler to find stale sessions. */
    List<UploadSession> findByStatusAndExpiredAtBefore(UploadStatus status, LocalDateTime cutoff);
}
