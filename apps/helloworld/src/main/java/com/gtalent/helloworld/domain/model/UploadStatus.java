package com.gtalent.helloworld.domain.model;

public enum UploadStatus {
    /** Session created; chunks are being accepted. */
    PENDING,
    /** All chunks received and the file has been committed to storage. */
    COMPLETED,
    /** The session expired or an unrecoverable error occurred. */
    FAILED
}
