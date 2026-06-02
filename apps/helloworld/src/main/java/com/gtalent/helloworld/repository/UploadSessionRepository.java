package com.gtalent.helloworld.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.domain.model.UploadStatus;

public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {

    Optional<UploadSession> findByUploadId(String uploadId);

    // 結合兩個欄位做查詢，並回傳 Optional 以確保優雅地處理空值
    Optional<UploadSession> findOneByUploadIdAndStatus(String uploadId, UploadStatus status);

    /** Used by the cleanup scheduler to find stale sessions. */
    List<UploadSession> findByStatusAndExpiredAtBefore(UploadStatus status, LocalDateTime cutoff);
}
