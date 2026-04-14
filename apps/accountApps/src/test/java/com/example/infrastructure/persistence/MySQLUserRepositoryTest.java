package com.example.infrastructure.persistence;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.sql.Connection;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.example.domain.model.User;
import com.example.infrastructure.util.DatabaseConnectionFactory;

/**
 * MySQL 使用者儲存庫整合測試
 * 
 * 測試範圍：
 * 1. 資料庫連線有效性
 * 2. 登入驗證成功情境
 * 3. 登入驗證失敗情境
 * 
 * 前置條件：
 * 1. Docker MySQL 容器運行 (northwind_mysql)
 * 2. 資料庫存在測試帳號：admin, user, test
 * 
 * 執行: mvn test -Dtest=MySQLUserRepositoryTest
 */
public class MySQLUserRepositoryTest {

    private static boolean isDatabaseAvailable = false;
    private MySQLUserRepository repository;
    
    @BeforeClass
    public static void checkDatabaseAvailability() {
        try (Connection conn = DatabaseConnectionFactory.getConnection()) {
            isDatabaseAvailable = true;
            System.out.println("✓ 資料庫連線成功");
        } catch (Exception e) {
            System.err.println("⚠ 資料庫連線失敗，測試將被跳過");
            System.err.println("  請執行: docker-compose up -d");
        }
    }
    
    @Before
    public void setUp() {
        assumeTrue("資料庫未啟動", isDatabaseAvailable);
        repository = new MySQLUserRepository();
    }
    
    /**
     * 測試：資料庫連線有效性
     * 驗證：能成功連線並查詢測試帳號（admin, user, test）
     */
    @Test
    public void testDatabaseConnection_ShouldBeValid() {
        // 驗證能查詢到測試帳號
        Optional<User> admin = repository.findByUsername("admin");
        Optional<User> user = repository.findByUsername("user");
        Optional<User> test = repository.findByUsername("test");
        
        assertTrue("應該找到 admin 帳號", admin.isPresent());
        assertTrue("應該找到 user 帳號", user.isPresent());
        assertTrue("應該找到 test 帳號", test.isPresent());
        
        // 驗證密碼正確解密
        assertEquals("admin 密碼應被正確解密", "admin", admin.get().getPassword());
        assertEquals("user 密碼應被正確解密", "user", user.get().getPassword());
        assertEquals("test 密碼應被正確解密", "test", test.get().getPassword());
        
        System.out.println("✓ 資料庫連線有效，測試帳號正常");
    }
    
    /**
     * 測試：登入驗證成功情境
     * 涵蓋情境：
     * 1. 正確的使用者名稱和密碼
     * 2. 密碼大小寫不敏感（符合 LoginController 行為）
     * 3. 不同測試帳號都能成功登入
     */
    @Test
    public void testLoginAuthentication_SuccessScenarios() {
        // 情境1: admin 使用正確密碼登入
        Optional<User> adminUser = repository.findByUsername("admin");
        boolean adminAuth = adminUser
            .map(u -> u.getPassword().equalsIgnoreCase("admin"))
            .orElse(false);
        assertTrue("admin 正確密碼應驗證成功", adminAuth);
        
        // 情境2: admin 使用大寫密碼登入（大小寫不敏感）
        boolean adminAuthUpperCase = adminUser
            .map(u -> u.getPassword().equalsIgnoreCase("ADMIN"))
            .orElse(false);
        assertTrue("密碼應不區分大小寫", adminAuthUpperCase);
        
        // 情境3: user 帳號登入
        Optional<User> testUser = repository.findByUsername("user");
        boolean userAuth = testUser
            .map(u -> u.getPassword().equalsIgnoreCase("user"))
            .orElse(false);
        assertTrue("user 正確密碼應驗證成功", userAuth);
        
        // 情境4: test 帳號登入
        Optional<User> testAccount = repository.findByUsername("test");
        boolean testAuth = testAccount
            .map(u -> u.getPassword().equalsIgnoreCase("test"))
            .orElse(false);
        assertTrue("test 正確密碼應驗證成功", testAuth);
        
        System.out.println("✓ 所有登入成功情境測試通過");
    }
    
    /**
     * 測試：登入驗證失敗情境
     * 涵蓋情境：
     * 1. 錯誤的密碼
     * 2. 不存在的使用者
     * 3. null 使用者名稱
     * 4. 空字串使用者名稱
     * 5. 空白使用者名稱
     */
    @Test
    public void testLoginAuthentication_FailureScenarios() {
        // 情境1: 正確使用者但錯誤密碼
        Optional<User> adminUser = repository.findByUsername("admin");
        boolean wrongPasswordAuth = adminUser
            .map(u -> u.getPassword().equalsIgnoreCase("wrongpassword"))
            .orElse(false);
        assertFalse("錯誤密碼應驗證失敗", wrongPasswordAuth);
        
        // 情境2: 不存在的使用者
        Optional<User> nonExistentUser = repository.findByUsername("nonexistent_user_xyz");
        boolean nonExistentAuth = nonExistentUser
            .map(u -> u.getPassword().equalsIgnoreCase("anypassword"))
            .orElse(false);
        assertFalse("不存在的使用者應無法登入", nonExistentAuth);
        
        // 情境3: null 使用者名稱
        Optional<User> nullUser = repository.findByUsername(null);
        assertFalse("null 使用者名稱應回傳空值", nullUser.isPresent());
        
        // 情境4: 空字串使用者名稱
        Optional<User> emptyUser = repository.findByUsername("");
        assertFalse("空字串使用者名稱應回傳空值", emptyUser.isPresent());
        
        // 情境5: 空白使用者名稱
        Optional<User> blankUser = repository.findByUsername("   ");
        assertFalse("空白使用者名稱應回傳空值", blankUser.isPresent());
        
        System.out.println("✓ 所有登入失敗情境測試通過");
    }
}
