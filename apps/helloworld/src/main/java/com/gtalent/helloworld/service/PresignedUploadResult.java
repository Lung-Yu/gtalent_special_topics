package com.gtalent.helloworld.service;

/**
 * Value returned by {@link StorageService#generatePresignedUpload}.
 *
 * <ul>
 *   <li>{@code uploadToken} – opaque token the client passes to the confirm endpoint.</li>
 *   <li>{@code presignedUrl} – time-limited PUT URL pointing directly at object storage.</li>
 *   <li>{@code expiresAt} – ISO-8601 timestamp when the URL expires.</li>
 * </ul>
 */
public record PresignedUploadResult(
        String uploadToken,
        String presignedUrl,
        String expiresAt
) {}
