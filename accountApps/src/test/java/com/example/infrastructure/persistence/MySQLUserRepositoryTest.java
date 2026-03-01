package com.example.infrastructure.persistence;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.application.exception.DatabaseConnectionException;
import com.example.domain.model.User;
import com.example.infrastructure.util.CaesarCipher;
import com.example.infrastructure.util.DatabaseConnectionFactory;

/**
 * MySQL 使用者儲存庫整合測試
 * 
 * 此測試需要 Docker MySQL 容器運行
 * 如果容器未啟動或連線失敗，測試將自動跳過
 * 
 * 前置條件：
 * 1. Docker MySQL 容器必須運行 (northwind_mysql)
 * 2. 資料庫中應存在測試帳號：admin, user, test
 * 
 * 執行測試前準備：
 * <pre>
 * # 啟動容器
 * docker-compose up -d
 * 
 * # 新增測試帳號（如果不存在）
 * docker exec -it northwind_mysql mysql -ustudent -pstudent123 accountapps -e "
 * INSERT IGNORE INTO users (username, password) VALUES 
 * ('admin', 'dgplq'),
 * ('user', 'xvhu'), 
 * ('test', 'whvw');"
 * </pre>
 */
public class MySQLUserRepositoryTest {

    private static boolean isDatabaseAvailable = false;
    private MySQLUserRepository repository;
    private static final String TEST_USER_PREFIX = "testuser_";
    
    /**
     * 在所有測試開始前檢查資料庫是否可用
     * 如果無法連線，所有測試將被跳過
     */
    @BeforeClass
    public static void checkDatabaseAvailability() {
        try (Connection conn = DatabaseConnectionFactory.getConnection()) {
            isDatabaseAvailable = true;
            System.out.println("✓ 資料庫連線成功，測試將正常執行");
        } catch (Exception e) {
            isDatabaseAvailable = false;
            System.err.println("⚠ 資料庫連線失敗，測試將被跳過");
            System.err.println("  請確認 Docker MySQL 容器已啟動: docker-compose up -d");
            System.err.println("  錯誤訊息: " + e.getMessage());
        }
    }
    
    /**
     * 每個測試前初始化 repository
     */
    @Before
    public void setUp() {
        assumeTrue("資料庫未啟動，跳過測試", isDatabaseAvailable);
        repository = new MySQLUserRepository();
    }
    
    /**
     * 每個測試後清理測試資料
     * 只刪除測試期間建立的使用者（testuser_ 開頭）
     * 保留原有的測試帳號（admin, user, test）
     */
    @After
    public void tearDown() {
        if (!isDatabaseAvailable) {
            return;
        }
        
        try {
            // 清理測試期間建立的使用者
            List<User> allUsers = repository.findAll();
            for (User user : allUsers) {
                if (user.getUsername().startsWith(TEST_USER_PREFIX)) {
                    // 注意：Repository 沒有 delete 方法，這裡只是示意
                    // 實際上測試帳號會保留在資料庫中
                    System.out.println("測試帳號將保留: " + user.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("清理測試資料失敗: " + e.getMessage());
        }
    }
    
    // ==================== findByUsername 測試 ====================
    
    /**
     * 測試查詢存在的使用者
     * 驗證能正確查詢到使用者並解密密碼
     */
    @Test
    public void testFindByUsername_ExistingUser() {
        // Arrange - 使用預設的測試帳號
        String username = "admin";
        String expectedPassword = "admin"; // 資料庫中儲存的是加密後的 "dgplq"
        
        // Act
        Optional<User> result = repository.findByUsername(username);
        
        // Assert
        assertTrue("應該找到使用者 admin", result.isPresent());
        assertEquals("使用者名稱應該是 admin", username, result.get().getUsername());
        assertEquals("密碼應該被正確解密", expectedPassword, result.get().getPassword());
    }
    
    /**
     * 測試查詢不存在的使用者
     * 應回傳 Optional.empty()
     */
    @Test
    public void testFindByUsername_NonExistentUser() {
        // Arrange
        String nonExistentUsername = "nonexistent_user_12345";
        
        // Act
        Optional<User> result = repository.findByUsername(nonExistentUsername);
        
        // Assert
        assertFalse("不應該找到不存在的使用者", result.isPresent());
    }
    
    /**
     * 測試使用 null 使用者名稱查詢
     * 應回傳 Optional.empty() 而不拋出例外
     */
    @Test
    public void testFindByUsername_NullUsername() {
        // Act
        Optional<User> result = repository.findByUsername(null);
        
        // Assert
        assertFalse("null 使用者名稱應回傳空值", result.isPresent());
    }
    
    /**
     * 測試使用空字串查詢
     * 應回傳 Optional.empty()
     */
    @Test
    public void testFindByUsername_EmptyUsername() {
        // Act
        Optional<User> result = repository.findByUsername("");
        
        // Assert
        assertFalse("空字串使用者名稱應回傳空值", result.isPresent());
    }
    
    /**
     * 測試使用者名稱的大小寫敏感性
     * MySQL 預設對字串比較是大小寫不敏感的（取決於 collation）
     */
    @Test
    public void testFindByUsername_CaseSensitivity() {
        // Act - 使用大寫查詢
        Optional<User> result = repository.findByUsername("ADMIN");
        
        // Assert - 根據資料庫設定，可能找到也可能找不到
        // 這個測試主要是確認行為一致性
        System.out.println("大小寫查詢結果: " + (result.isPresent() ? "找到" : "未找到"));
    }
    
    // ==================== findAll 測試 ====================
    
    /**
     * 測試查詢所有使用者
     * 至少應該包含三個測試帳號：admin, user, test
     */
    @Test
    public void testFindAll_ShouldReturnTestAccounts() {
        // Act
        List<User> users = repository.findAll();
        
        // Assert
        assertNotNull("使用者列表不應為 null", users);
        assertTrue("至少應該有 3 個測試帳號", users.size() >= 3);
        
        // 驗證測試帳號存在
        boolean hasAdmin = users.stream().anyMatch(u -> "admin".equals(u.getUsername()));
        boolean hasUser = users.stream().anyMatch(u -> "user".equals(u.getUsername()));
        boolean hasTest = users.stream().anyMatch(u -> "test".equals(u.getUsername()));
        
        assertTrue("應該包含 admin 帳號", hasAdmin);
        assertTrue("應該包含 user 帳號", hasUser);
        assertTrue("應該包含 test 帳號", hasTest);
        
        System.out.println("找到 " + users.size() + " 個使用者");
    }
    
    /**
     * 測試 findAll 回傳的使用者密碼都已解密
     */
    @Test
    public void testFindAll_PasswordsAreDecrypted() {
        // Act
        List<User> users = repository.findAll();
        
        // Assert
        for (User user : users) {
            assertNotNull("密碼不應為 null", user.getPassword());
            assertFalse("密碼不應為空", user.getPassword().isEmpty());
            
            // 驗證密碼已解密（加密後的密碼不應等於原始密碼）
            // 例如：admin 的原始密碼是 "admin"，加密後是 "dgplq"
            if ("admin".equals(user.getUsername())) {
                assertEquals("admin 密碼應該被解密", "admin", user.getPassword());
            }
        }
    }
    
    // ==================== save 測試 ====================
    
    /**
     * 測試儲存新使用者
     * 驗證能成功新增使用者且密碼被正確加密
     */
    @Test
    public void testSave_NewUser() {
        // Arrange
        String username = TEST_USER_PREFIX + System.currentTimeMillis();
        String password = "testpass123";
        User newUser = new User(username, password);
        
        // Act
        repository.save(newUser);
        
        // Assert - 查詢剛儲存的使用者
        Optional<User> saved = repository.findByUsername(username);
        assertTrue("應該能找到剛儲存的使用者", saved.isPresent());
        assertEquals("使用者名稱應該正確", username, saved.get().getUsername());
        assertEquals("密碼應該被正確加密後解密", password, saved.get().getPassword());
        
        System.out.println("✓ 成功儲存新使用者: " + username);
    }
    
    /**
     * 測試更新現有使用者的密碼
     * 使用 ON DUPLICATE KEY UPDATE 機制
     */
    @Test
    public void testSave_UpdateExistingUserPassword() {
        // Arrange - 先建立一個使用者
        String username = TEST_USER_PREFIX + "update_" + System.currentTimeMillis();
        String oldPassword = "oldpass";
        String newPassword = "newpass";
        
        User user = new User(username, oldPassword);
        repository.save(user);
        
        // Act - 更新密碼
        User updatedUser = new User(username, newPassword);
        repository.save(updatedUser);
        
        // Assert
        Optional<User> result = repository.findByUsername(username);
        assertTrue("應該找到使用者", result.isPresent());
        assertEquals("密碼應該被更新", newPassword, result.get().getPassword());
        
        System.out.println("✓ 成功更新使用者密碼: " + username);
    }
    
    /**
     * 測試儲存 null 使用者
     * 應拋出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSave_NullUser_ShouldThrowException() {
        // Act & Assert
        repository.save(null);
    }
    
    /**
     * 測試儲存使用者名稱為 null 的使用者
     * 應拋出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSave_NullUsername_ShouldThrowException() {
        // Arrange
        User user = new User(null, "password");
        
        // Act & Assert
        repository.save(user);
    }
    
    /**
     * 測試儲存使用者名稱為空字串的使用者
     * 應拋出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSave_EmptyUsername_ShouldThrowException() {
        // Arrange
        User user = new User("", "password");
        
        // Act & Assert
        repository.save(user);
    }
    
    /**
     * 測試儲存使用者名稱只有空白的使用者
     * 應拋出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSave_WhitespaceUsername_ShouldThrowException() {
        // Arrange
        User user = new User("   ", "password");
        
        // Act & Assert
        repository.save(user);
    }
    
    // ==================== 密碼加密驗證測試 ====================
    
    /**
     * 測試密碼加密機制
     * 驗證密碼在資料庫中是加密儲存的，查詢時會自動解密
     */
    @Test
    public void testPasswordEncryption() {
        // Arrange
        String username = TEST_USER_PREFIX + "encrypt_" + System.currentTimeMillis();
        String plainPassword = "mypassword";
        User user = new User(username, plainPassword);
        
        // Act - 儲存使用者
        repository.save(user);
        
        // Assert - 查詢使用者應得到解密後的密碼
        Optional<User> retrieved = repository.findByUsername(username);
        assertTrue("應該找到使用者", retrieved.isPresent());
        assertEquals("密碼應該正確解密", plainPassword, retrieved.get().getPassword());
        
        // 驗證加密邏輯
        String encrypted = CaesarCipher.encrypt(plainPassword);
        assertNotEquals("加密後的密碼應該與原始密碼不同", plainPassword, encrypted);
        
        String decrypted = CaesarCipher.decrypt(encrypted);
        assertEquals("解密後應該等於原始密碼", plainPassword, decrypted);
        
        System.out.println("密碼加密測試: " + plainPassword + " -> " + encrypted + " -> " + decrypted);
    }
    
    // ==================== 登入流程驗證測試 ====================
    
    /**
     * 測試完整的登入流程：正確的密碼
     * 模擬 LoginController 的驗證邏輯
     */
    @Test
    public void testLoginFlow_CorrectPassword() {
        // Arrange
        String username = "admin";
        String correctPassword = "admin";
        
        // Act - 模擬登入流程
        Optional<User> userOpt = repository.findByUsername(username);
        boolean isAuthenticated = userOpt
            .map(user -> user.getPassword().equalsIgnoreCase(correctPassword))
            .orElse(false);
        
        // Assert
        assertTrue("使用者應該存在", userOpt.isPresent());
        assertTrue("正確密碼應該驗證成功", isAuthenticated);
        
        System.out.println("✓ 登入驗證成功: " + username);
    }
    
    /**
     * 測試完整的登入流程：錯誤的密碼
     */
    @Test
    public void testLoginFlow_WrongPassword() {
        // Arrange
        String username = "admin";
        String wrongPassword = "wrongpassword";
        
        // Act
        Optional<User> userOpt = repository.findByUsername(username);
        boolean isAuthenticated = userOpt
            .map(user -> user.getPassword().equalsIgnoreCase(wrongPassword))
            .orElse(false);
        
        // Assert
        assertTrue("使用者應該存在", userOpt.isPresent());
        assertFalse("錯誤密碼應該驗證失敗", isAuthenticated);
        
        System.out.println("✓ 錯誤密碼被正確拒絕");
    }
    
    /**
     * 測試登入流程：不存在的使用者
     */
    @Test
    public void testLoginFlow_NonExistentUser() {
        // Arrange
        String username = "nonexistent_" + System.currentTimeMillis();
        String password = "anypassword";
        
        // Act
        Optional<User> userOpt = repository.findByUsername(username);
        boolean isAuthenticated = userOpt
            .map(user -> user.getPassword().equalsIgnoreCase(password))
            .orElse(false);
        
        // Assert
        assertFalse("不存在的使用者應該無法找到", userOpt.isPresent());
        assertFalse("不存在的使用者應該驗證失敗", isAuthenticated);
        
        System.out.println("✓ 不存在的使用者被正確拒絕");
    }
    
    /**
     * 測試登入流程：密碼大小寫不敏感（符合 LoginController 的行為）
     */
    @Test
    public void testLoginFlow_PasswordCaseInsensitive() {
        // Arrange
        String username = "admin";
        String passwordUpperCase = "ADMIN";
        
        // Act
        Optional<User> userOpt = repository.findByUsername(username);
        boolean isAuthenticated = userOpt
            .map(user -> user.getPassword().equalsIgnoreCase(passwordUpperCase))
            .orElse(false);
        
        // Assert
        assertTrue("使用者應該存在", userOpt.isPresent());
        assertTrue("密碼比對應該不區分大小寫", isAuthenticated);
        
        System.out.println("✓ 密碼大小寫不敏感驗證成功");
    }
    
    // ==================== 例外處理測試 ====================
    
    /**
     * 測試資料庫連線異常處理
     * 注意：此測試難以在正常環境下執行，因為需要模擬資料庫故障
     * 這裡主要是文檔化預期行為
     */
    @Test
    public void testDatabaseExceptionHandling_Documentation() {
        // 這個測試主要是記錄預期行為
        // 實際的例外測試需要使用 Mock 或測試資料庫
        
        try {
            repository.findAll();
            // 正常情況下不應拋出例外
            assertTrue("資料庫連線正常", true);
        } catch (DatabaseConnectionException e) {
            // 如果拋出例外，應該是 DatabaseConnectionException
            fail("不應該在正常情況下拋出例外: " + e.getMessage());
        }
    }
}
