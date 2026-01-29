package com.example.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.domain.model.User;
import com.example.domain.repository.UserRepository;
import com.example.infrastructure.util.CaesarCipher;

/**
 * CSV 檔案實作的使用者儲存庫
 * 負責將使用者資料儲存至 CSV 檔案並從中讀取
 */
public class InCSVUserRepository implements UserRepository {

    private final File file;
    private static final String HEADER = "username,password";

    /**
     * 建構子：使用指定的檔案路徑
     * 
     * @param filepath CSV 檔案路徑
     */
    public InCSVUserRepository(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            filepath = "accountApps/data/users.csv";
        }
        this.file = new File(filepath);
        initializeFileIfNeeded();
    }

    /**
     * 建構子：使用預設路徑 accountApps/data/users.csv
     */
    public InCSVUserRepository() {
        this("accountApps/data/users.csv");
    }

    /**
     * 建構子：使用指定的 File 物件
     * 
     * @param file CSV 檔案物件
     */
    public InCSVUserRepository(File file) {
        this.file = file;
        initializeFileIfNeeded();
    }

    /**
     * 初始化檔案，如果檔案不存在則創建並寫入預設使用者
     */
    private void initializeFileIfNeeded() {
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                
                // 創建檔案並寫入預設使用者（密碼已加密）
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(HEADER);
                    writer.newLine();
                    
                    // 寫入預設使用者（密碼使用凱薩加密）
                    writer.write("admin," + CaesarCipher.encrypt("admin"));
                    writer.newLine();
                    writer.write("user," + CaesarCipher.encrypt("user"));
                    writer.newLine();
                    writer.write("test," + CaesarCipher.encrypt("test"));
                    writer.newLine();
                    writer.write("test2," + CaesarCipher.encrypt("test2"));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize user file", e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            if (!file.exists()) {
                return List.of();
            }

            List<User> users = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // 跳過標題列
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    User user = parseCsvLine(line);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            
            return users;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users", e);
        }
    }

    @Override
    public void save(User user) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            boolean fileExists = file.exists();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                // 如果檔案不存在或為空，寫入標題列
                if (!fileExists || file.length() == 0) {
                    writer.write(HEADER);
                    writer.newLine();
                }
                
                // 寫入使用者資料
                String csvLine = toCsvLine(user);
                writer.write(csvLine);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    /**
     * 將使用者物件轉換為 CSV 格式的字串
     * 密碼會使用凱薩加密後再儲存
     * 
     * @param user 使用者物件
     * @return CSV 格式的字串
     */
    private String toCsvLine(User user) {
        String username = escapeCsvValue(user.getUsername());
        // 將密碼加密後再儲存
        String encryptedPassword = CaesarCipher.encrypt(user.getPassword());
        String password = escapeCsvValue(encryptedPassword);
        
        return String.format("%s,%s", username, password);
    }

    /**
     * 將 CSV 格式的字串解析為使用者物件
     * 密碼會從加密狀態解密
     * 
     * @param line CSV 格式的字串
     * @return 使用者物件，解析失敗則返回 null
     */
    private User parseCsvLine(String line) {
        try {
            String[] fields = parseCsvFields(line);
            
            if (fields.length != 2) {
                return null;
            }
            
            String username = unescapeCsvValue(fields[0]);
            String encryptedPassword = unescapeCsvValue(fields[1]);
            // 將儲存的加密密碼解密
            String password = CaesarCipher.decrypt(encryptedPassword);
            
            return new User(username, password);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 CSV 欄位，處理引號和逗號
     * 
     * @param line CSV 行
     * @return 欄位陣列
     */
    private String[] parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 檢查是否為跳脫的引號
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // 跳過下一個引號
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * 跳脫 CSV 值中的特殊字元
     * 
     * @param value 原始值
     * @return 跳脫後的值
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果值包含逗號、引號或換行，則用引號包裹並跳脫引號
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * 解除 CSV 值的跳脫
     * 
     * @param value 跳脫後的值
     * @return 原始值
     */
    private String unescapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // 移除前後的引號
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            // 解除雙引號的跳脫
            value = value.replace("\"\"", "\"");
        }
        
        return value;
    }
}
