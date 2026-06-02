package com.gtalent.helloworld.domain.model;

public enum UploadStatus {
    /** Session initialised; no bytes received yet. */
    PENDING,
    /** At least one chunk successfully written; upload in progress. */
    IN_PROGRESS,
    /** All chunks received and the file has been committed to storage. */
    COMPLETED,
    /** The session expired or an unrecoverable error occurred. */
    FAILED
}
