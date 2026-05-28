package com.gtalent.helloworld.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.UploadSession;
import com.gtalent.helloworld.domain.model.UploadStatus;
import com.gtalent.helloworld.repository.UploadSessionRepository;

/**
 * Periodically removes expired or failed chunked-upload sessions and their
 * associated staging files so that disk space is not leaked by abandoned uploads.
 *
 * Runs every hour by default (configurable via {@code storage.cleanup-cron}).
 */
@Component
public class UploadCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(UploadCleanupScheduler.class);

    private final UploadSessionRepository uploadSessionRepository;
    private final Path stagingDir;

    public UploadCleanupScheduler(
            UploadSessionRepository uploadSessionRepository,
            @Value("${storage.location:upload-dir}") String storageLocation) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.stagingDir = Paths.get(storageLocation).resolve("tmp");
    }

    /**
     * Cleans up sessions that are still PENDING but have passed their
     * {@code expiredAt} timestamp, as well as any sessions in FAILED state.
     * Runs every hour (at the top of the hour).
     */
    @Scheduled(cron = "${storage.cleanup-cron:0 0 * * * *}")
    @Transactional
    public void cleanupExpiredSessions() {
        List<UploadSession> expired = uploadSessionRepository
                .findByStatusAndExpiredAtBefore(UploadStatus.PENDING, LocalDateTime.now());

        if (expired.isEmpty()) {
            return;
        }

        log.info("Upload cleanup: found {} expired session(s) to remove.", expired.size());

        for (UploadSession session : expired) {
            deleteStagingFile(session.getUploadId());
            session.setStatus(UploadStatus.FAILED);
            uploadSessionRepository.save(session);
            log.debug("Marked session {} as FAILED and deleted its staging file.", session.getUploadId());
        }
    }

    private void deleteStagingFile(String uploadId) {
        Path stagingFile = stagingDir.resolve(uploadId).normalize().toAbsolutePath();
        try {
            Files.deleteIfExists(stagingFile);
        } catch (IOException e) {
            // Log but do not abort the transaction – other sessions should still be cleaned up.
            log.warn("Could not delete staging file for session {}: {}", uploadId, e.getMessage());
        }
    }
}
