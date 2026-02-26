package com.example.application.exception;

/**
 * 資料庫連接例外
 * 當資料庫操作失敗時拋出此例外
 */
public class DatabaseConnectionException extends RuntimeException {
    
    /**
     * 建構子
     * 
     * @param message 錯誤訊息
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    /**
     * 建構子
     * 
     * @param message 錯誤訊息
     * @param cause 原始例外
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
