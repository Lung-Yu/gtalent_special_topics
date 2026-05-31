package com.gtalent.helloworld.service;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * 負責為 chunked upload session 分配唯一的 uploadId 並在 staging 目錄建立佔位檔案。
 *
 * <p>獨立為 bean 的原因與 {@link BudgetAlertExecutor} 相同：
 * Spring AOP self-invocation 無法被代理攔截，因此 {@code @Retryable} 必須套用在
 * 由外部呼叫的獨立 bean 方法上，才能正確觸發重試機制。
 *
 * <p>唯一性保障策略（雙重防線）：
 * <ol>
 *   <li>以 {@code FileChannel.open(..., CREATE_NEW)} 作為 OS 層原子鎖 —
 *       只有一個執行緒能在相同路徑成功建立新檔案，天然消除 TOCTOU race condition。</li>
 *   <li>{@code upload_sessions.upload_id} DB UNIQUE constraint 作為第二道防線，
 *       由呼叫方 {@link FileSystemStorageService#initUpload} 捕捉
 *       {@code DataIntegrityViolationException} 處理。</li>
 * </ol>
 *
 * <p>裝飾器思維：retry 行為完全透過 {@code @Retryable} annotation 宣告，
 * 未來若需調整重試次數或退避策略，只需修改 annotation 參數，分配邏輯本身不受影響。
 */
@Service
public class UploadIdAllocator {

    /**
     * 在 {@code stagingDir} 下分配一個唯一的 uploadId，並以 CREATE_NEW 建立佔位檔案。
     *
     * <p>若 {@link FileAlreadyExistsException} 發生（UUID 碰撞，機率極低），
     * {@code @Retryable} 會自動重新呼叫此方法並產生新的 UUID，最多重試 3 次。
     * 其他 {@link IOException}（如磁碟錯誤）不在 retryFor 範圍內，會直接向上拋出。
     *
     * @param stagingDir staging 目錄路徑（通常為 upload-dir/tmp/）
     * @return 唯一的 uploadId 字串（UUID 格式），對應的空白佔位檔案已存在於 stagingDir
     * @throws FileAlreadyExistsException 若所有重試皆因 UUID 碰撞失敗（由 @Recover 攔截）
     * @throws IOException                若遇到非碰撞性 I/O 錯誤（不重試，直接拋出）
     */
    @Retryable(retryFor = FileAlreadyExistsException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 0))
    public String allocate(Path stagingDir) throws IOException {
        String candidate = UUID.randomUUID().toString();
        // CREATE_NEW 是 OS 層原子操作：成功代表此 candidate 在 filesystem 上唯一
        try (FileChannel fc = FileChannel.open(
                stagingDir.resolve(candidate),
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)) {
            // 建立空白佔位檔案；OS 將以 sparse file 延遲配置實際磁碟空間
        }
        return candidate;
    }

    /**
     * 3 次重試皆因 UUID 碰撞失敗後的補救方法。
     * 實際上觸發機率趨近於零（UUID v4 碰撞概率約 1/2^122），
     * 此方法存在主要作為安全網與明確的錯誤訊息。
     */
    @Recover
    public String recoverAllocate(FileAlreadyExistsException ex, Path stagingDir) {
        throw new StorageException(
                "Failed to generate unique upload ID after 3 retries. " +
                "Staging dir: " + stagingDir, ex);
    }
}
