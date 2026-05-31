package com.gtalent.helloworld.domain.model;

public enum UploadStatus {
    /** Session created; staging file allocated; no chunk received yet. */
    PENDING,
    /** At least one chunk has been received; upload is in progress. */
    IN_PROGRESS,
    /** All chunks received and the file has been committed to storage. */
    COMPLETED,
    /** The session expired or an unrecoverable error occurred. */
    FAILED
}
