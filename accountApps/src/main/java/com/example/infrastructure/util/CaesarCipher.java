package com.example.infrastructure.util;

/**
 * 凱薩加密工具類
 * 提供密碼加密和解密功能
 */
public class CaesarCipher {
    
    private static final int START_NUM = 65; // 'A' 的 ASCII 值
    private static final int SIZE = 26; // 字母表大小
    private static final int DEFAULT_SHIFT = 3; // 預設位移量
    
    /**
     * 加密文字
     * 
     * @param text 原始文字
     * @return 加密後的文字
     */
    public static String encrypt(String text) {
        return encrypt(text, DEFAULT_SHIFT);
    }
    
    /**
     * 加密文字（使用指定位移量）
     * 
     * @param text 原始文字
     * @param shift 位移量
     * @return 加密後的文字
     */
    public static String encrypt(String text, int shift) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String key = generateKey(shift);
        StringBuilder cipher = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char encryptedChar = encryptChar(c, key);
            cipher.append(encryptedChar);
        }
        
        return cipher.toString();
    }
    
    /**
     * 解密文字
     * 
     * @param cipher 加密後的文字
     * @return 原始文字
     */
    public static String decrypt(String cipher) {
        return decrypt(cipher, DEFAULT_SHIFT);
    }
    
    /**
     * 解密文字（使用指定位移量）
     * 
     * @param cipher 加密後的文字
     * @param shift 位移量
     * @return 原始文字
     */
    public static String decrypt(String cipher, int shift) {
        if (cipher == null || cipher.isEmpty()) {
            return cipher;
        }
        
        String key = generateKey(shift);
        StringBuilder text = new StringBuilder();
        
        for (int i = 0; i < cipher.length(); i++) {
            char c = cipher.charAt(i);
            char decryptedChar = decryptChar(c, key);
            text.append(decryptedChar);
        }
        
        return text.toString();
    }
    
    /**
     * 加密單一字元
     * 
     * @param c 原始字元
     * @param key 密鑰
     * @return 加密後的字元
     */
    private static char encryptChar(char c, String key) {
        // 處理大寫字母
        if (c >= 'A' && c <= 'Z') {
            int idx = c - START_NUM;
            return key.charAt(idx);
        }
        // 處理小寫字母
        else if (c >= 'a' && c <= 'z') {
            int idx = c - 'a';
            char upperEncrypted = key.charAt(idx);
            return Character.toLowerCase(upperEncrypted);
        }
        // 其他字元不變
        return c;
    }
    
    /**
     * 解密單一字元
     * 
     * @param c 加密後的字元
     * @param key 密鑰
     * @return 原始字元
     */
    private static char decryptChar(char c, String key) {
        // 處理大寫字母
        if (c >= 'A' && c <= 'Z') {
            int idx = key.indexOf(c);
            if (idx != -1) {
                return (char) (START_NUM + idx);
            }
        }
        // 處理小寫字母
        else if (c >= 'a' && c <= 'z') {
            char upper = Character.toUpperCase(c);
            int idx = key.indexOf(upper);
            if (idx != -1) {
                return (char) ('a' + idx);
            }
        }
        // 其他字元不變
        return c;
    }
    
    /**
     * 生成密鑰
     * 
     * @param shift 位移量
     * @return 密鑰字串
     */
    private static String generateKey(int shift) {
        StringBuilder key = new StringBuilder();
        
        for (int i = 0; i < SIZE; i++) {
            int charValue = START_NUM + ((shift + i) % SIZE);
            key.append((char) charValue);
        }
        
        return key.toString();
    }
}
