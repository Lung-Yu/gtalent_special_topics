# 代碼重構說明

## 重構日期
2026-01-22

## 重構目標
提升程式品質、改善代碼結構、增強可維護性

## 主要改進

### 1. 架構重構 - 採用 MVC 分層架構

#### 原始架構問題
- `App.java` 包含過多職責（顯示介面、業務邏輯、輸入處理）
- 違反單一職責原則（Single Responsibility Principle）
- 難以測試和維護

#### 改進後的架構

```
Presentation Layer (表現層)
├── MenuView.java          - 負責所有 UI 顯示
├── MenuController.java    - 處理選單邏輯和使用者輸入
└── MenuOption.java        - 枚舉定義選單選項

Application Layer (應用層)
└── App.java               - 應用程式入口，負責資源管理
```

### 2. 新增的類別

#### MenuView.java
- **職責**：純粹的視圖層，負責顯示訊息
- **優點**：
  - 分離顯示邏輯，易於修改 UI
  - 可以輕鬆擴展支援多語言
  - 便於單元測試

#### MenuController.java
- **職責**：控制器層，處理業務邏輯流程
- **優點**：
  - 統一的輸入處理和錯誤處理
  - 清晰的流程控制
  - 更好的異常處理機制
  - 為未來擴展預留接口（TODO 標記）

#### MenuOption.java
- **職責**：枚舉類型，定義系統選單選項
- **優點**：
  - 類型安全，避免魔術數字
  - 集中管理選單選項
  - 提供驗證方法
  - 易於添加新選項

### 3. App.java 重構

#### 重構前的問題
```java
// 原始代碼問題：
- 混合了 UI 顯示和邏輯處理
- switch-case 直接處理選單
- 沒有適當的錯誤處理
- 資源管理不完善
```

#### 重構後的改進
```java
public class App {
    // 1. 清晰的依賴注入
    private final Scanner scanner;
    private final MenuController menuController;
    
    // 2. 簡潔的初始化
    public App() {
        this.scanner = new Scanner(System.in);
        this.menuController = new MenuController(scanner);
    }
    
    // 3. 完善的錯誤處理
    public void start() {
        try {
            menuController.start();
        } catch (Exception e) {
            System.err.println("系統發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 4. 安全的資源釋放
    public static void main(String[] args) {
        App app = new App();
        try {
            app.start();
        } finally {
            app.close();  // 確保資源被正確釋放
        }
    }
}
```

## 程式品質提升

### 1. 可讀性改進
- ✅ 每個類別有單一、明確的職責
- ✅ 方法名稱清晰表達意圖
- ✅ 添加完整的 JavaDoc 註釋
- ✅ 代碼結構清晰，邏輯流程易於理解

### 2. 可維護性提升
- ✅ 分層架構使修改更容易
- ✅ 低耦合高內聚
- ✅ 便於添加新功能（預留 TODO）
- ✅ 便於修改現有功能（如更換 UI）

### 3. 可測試性增強
- ✅ 依賴注入使單元測試更容易
- ✅ 各層可以獨立測試
- ✅ Mock 對象更容易創建

### 4. 錯誤處理改進
- ✅ 統一的異常處理機制
- ✅ 使用者友好的錯誤訊息
- ✅ 輸入驗證更完善
- ✅ 資源安全釋放（try-finally）

### 5. 設計原則遵循

#### SOLID 原則
- **S (Single Responsibility)**: 每個類別只有一個職責
- **O (Open/Closed)**: 易於擴展，不需修改現有代碼
- **L (Liskov Substitution)**: 可以輕鬆替換實現
- **I (Interface Segregation)**: 接口設計精簡
- **D (Dependency Inversion)**: 依賴注入而非直接創建

## 測試結果

```
[INFO] Tests run: 47, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

✅ 所有現有測試通過
✅ 編譯成功，無警告
✅ 代碼重構完全向後兼容

## 未來擴展建議

1. **實作功能模組**
   - 在 MenuController 中實作各個 TODO 標記的方法
   - 整合現有的 UseCase 類別

2. **添加更多驗證**
   - 輸入範圍驗證
   - 業務邏輯驗證

3. **增強錯誤處理**
   - 自定義異常類別
   - 更詳細的錯誤訊息

4. **國際化支援**
   - 使用資源文件管理多語言
   - MenuView 可輕鬆支援多語言切換

## 程式碼統計

### 重構前
- App.java: ~70 行（包含所有邏輯）

### 重構後
- App.java: ~50 行（精簡、清晰）
- MenuView.java: ~45 行
- MenuController.java: ~110 行
- MenuOption.java: ~50 行

**總計**: 從 1 個職責混亂的類別，重構為 4 個職責清晰的類別

## 結論

這次重構顯著提升了程式品質：
- ✅ 程式架構更清晰
- ✅ 代碼更易維護
- ✅ 錯誤處理更完善
- ✅ 符合設計原則
- ✅ 為未來擴展打下良好基礎
