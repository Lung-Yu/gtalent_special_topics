package com.example.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.CategoryCreateCommand;
import com.example.application.exception.DuplicateCategoryException;
import com.example.application.exception.CategoryTypeNotExists;
import com.example.domain.model.Category;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.valueobject.TypeCategory;
import com.example.infrastructure.persistence.InMemoryCategoryRepository;

public class CategoryUseCaseTest {

    private CategoryRepository categoryRepository;
    private CategoryUseCase categoryUseCase;
    private User user1;

    @Before
    public void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        categoryUseCase = new CategoryUseCase(categoryRepository);
        user1 = new User("alice");
    }

    @Test
    public void execute_WithValidIncomeCategory_ShouldSaveSuccessfully() throws CategoryTypeNotExists {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "薪水",
            "income",
            "💰"
        );

        categoryUseCase.execute(command);

        assertEquals(1, categoryRepository.findAll().size());
        
        Category savedCategory = categoryRepository.findAll().get(0);
        assertEquals("薪水", savedCategory.getName());
        assertEquals("💰", savedCategory.getIcon());
        assertEquals(TypeCategory.INCOME, savedCategory.getType());
        assertNotNull("createdAt should not be null", savedCategory.getCreatedAt());
        assertEquals("createdBy should be user1", user1, savedCategory.getCreatedBy());
    }

    @Test
    public void execute_WithValidOutcomeCategory_ShouldSaveSuccessfully() throws CategoryTypeNotExists {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "午餐",
            "OUTCOME",
            "🍱"
        );

        categoryUseCase.execute(command);

        assertEquals(1, categoryRepository.findAll().size());
        
        Category savedCategory = categoryRepository.findAll().get(0);
        assertEquals("午餐", savedCategory.getName());
        assertEquals("🍱", savedCategory.getIcon());
        assertEquals(TypeCategory.OUTCOME, savedCategory.getType());
    }

    @Test
    public void execute_WithCaseInsensitiveType_ShouldParseCorrectly() throws CategoryTypeNotExists {
        String[] variations = {"income", "INCOME", "Income", "InCoMe"};
        
        for (String typeVariation : variations) {
            categoryRepository = new InMemoryCategoryRepository();
            categoryUseCase = new CategoryUseCase(categoryRepository);
            
            CategoryCreateCommand command = new CategoryCreateCommand(
                user1,
                "測試",
                typeVariation,
                "✓"
            );

            categoryUseCase.execute(command);

            assertEquals(1, categoryRepository.findAll().size());
            assertEquals(TypeCategory.INCOME, categoryRepository.findAll().get(0).getType());
        }
    }

    @Test
    public void execute_WithMultipleCategories_ShouldSaveAll() throws CategoryTypeNotExists {
        CategoryCreateCommand command1 = new CategoryCreateCommand(user1, "薪水", "income", "💰");
        CategoryCreateCommand command2 = new CategoryCreateCommand(user1, "午餐", "outcome", "🍱");
        CategoryCreateCommand command3 = new CategoryCreateCommand(user1, "獎金", "income", "🎁");

        categoryUseCase.execute(command1);
        categoryUseCase.execute(command2);
        categoryUseCase.execute(command3);

        assertEquals(3, categoryRepository.findAll().size());
    }

    @Test
    public void execute_WithInvalidType_ShouldThrowCategoryTypeNotExists() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "測試",
            "invalid_type",
            "❌"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw CategoryTypeNotExists");
        } catch (CategoryTypeNotExists e) {
            assertEquals("Category type not exists: invalid_type", e.getMessage());
        }
    }

    @Test
    public void execute_WithEmptyType_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "測試",
            "",
            "❌"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category type cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullType_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "測試",
            null,
            "❌"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category type cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithWhitespaceType_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "測試",
            "   ",
            "❌"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category type cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullCommand_ShouldThrowIllegalArgumentException() {
        try {
            categoryUseCase.execute(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Command cannot be null", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullName_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            null,
            "income",
            "💰"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category name cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithEmptyName_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "",
            "income",
            "💰"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category name cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithWhitespaceName_ShouldThrowIllegalArgumentException() {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "   ",
            "income",
            "💰"
        );

        try {
            categoryUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Category name cannot be null or empty", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not CategoryTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullIcon_ShouldStillSave() throws CategoryTypeNotExists {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "無圖示標籤",
            "income",
            null
        );

        categoryUseCase.execute(command);

        assertEquals(1, categoryRepository.findAll().size());
        
        Category savedCategory = categoryRepository.findAll().get(0);
        assertEquals("無圖示標籤", savedCategory.getName());
        assertEquals(null, savedCategory.getIcon());
        assertEquals(TypeCategory.INCOME, savedCategory.getType());
    }

    @Test
    public void execute_CanQueryByName() throws CategoryTypeNotExists {
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "薪水",
            "income",
            "💰"
        );

        categoryUseCase.execute(command);

        assertEquals(1, categoryRepository.findByName("薪水").size());
        assertEquals(0, categoryRepository.findByName("不存在").size());
    }

    @Test
    public void execute_CanQueryByType() throws CategoryTypeNotExists {
        CategoryCreateCommand command1 = new CategoryCreateCommand(user1, "薪水", "income", "💰");
        CategoryCreateCommand command2 = new CategoryCreateCommand(user1, "午餐", "outcome", "🍱");
        CategoryCreateCommand command3 = new CategoryCreateCommand(user1, "獎金", "income", "🎁");

        categoryUseCase.execute(command1);
        categoryUseCase.execute(command2);
        categoryUseCase.execute(command3);

        assertEquals(2, categoryRepository.findByType("INCOME").size());
        assertEquals(1, categoryRepository.findByType("OUTCOME").size());
    }

    @Test
    public void execute_WithDuplicateNameAndType_ShouldThrowException() throws CategoryTypeNotExists {
        CategoryCreateCommand command1 = new CategoryCreateCommand(user1, "薪水", "income", "💰");
        CategoryCreateCommand command2 = new CategoryCreateCommand(user1, "薪水", "income", "💵");

        categoryUseCase.execute(command1);
        
        try {
            categoryUseCase.execute(command2);
            fail("Should throw exception for duplicate category name with same type");
        } catch (DuplicateCategoryException e) {
            assertEquals("Category with name '薪水' and type 'income' already exists", e.getMessage());
        }
    }

    @Test
    public void execute_WithSameNameDifferentType_ShouldSaveSuccessfully() throws CategoryTypeNotExists {
        CategoryCreateCommand command1 = new CategoryCreateCommand(user1, "薪水", "income", "💰");
        CategoryCreateCommand command2 = new CategoryCreateCommand(user1, "薪水", "outcome", "💵");

        categoryUseCase.execute(command1);
        categoryUseCase.execute(command2);

        assertEquals(2, categoryRepository.findAll().size());
        assertEquals(2, categoryRepository.findByName("薪水").size());
    }

    @Test
    public void execute_WithPresetCategories_ShouldPreventDuplicateCreation() {
        // 模擬系統預設標籤
        List<Category> presetCategories = Arrays.asList(
            new Category("食物", "🍔", TypeCategory.OUTCOME, user1),
            new Category("薪水", "💰", TypeCategory.INCOME, user1),
            new Category("交通", "🚗", TypeCategory.OUTCOME, user1)
        );
        
        // 使用帶預設標籤的 repository
        categoryRepository = new InMemoryCategoryRepository(presetCategories);
        categoryUseCase = new CategoryUseCase(categoryRepository);
        
        // 驗證預設標籤已存在
        assertEquals(3, categoryRepository.findAll().size());
        
        // 嘗試建立與預設標籤相同的標籤
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "食物",
            "outcome",
            "🍕"  // 即使圖示不同
        );
        
        try {
            categoryUseCase.execute(command);
            fail("Should throw DuplicateCategoryException when creating category with same name and type as preset");
        } catch (DuplicateCategoryException e) {
            assertEquals("Category with name '食物' and type 'outcome' already exists", e.getMessage());
        } catch (CategoryTypeNotExists e) {
            fail("Should throw DuplicateCategoryException, not CategoryTypeNotExists");
        }
        
        // 確認沒有新增標籤
        assertEquals(3, categoryRepository.findAll().size());
    }

    @Test
    public void execute_WithPresetCategories_AllowDifferentType() throws CategoryTypeNotExists {
        // 模擬系統預設標籤
        List<Category> presetCategories = Arrays.asList(
            new Category("薪水", "💰", TypeCategory.INCOME, user1)
        );
        
        categoryRepository = new InMemoryCategoryRepository(presetCategories);
        categoryUseCase = new CategoryUseCase(categoryRepository);
        
        // 建立相同名稱但不同類型的標籤應該成功
        CategoryCreateCommand command = new CategoryCreateCommand(
            user1,
            "薪水",
            "outcome",  // 不同類型
            "💵"
        );
        
        categoryUseCase.execute(command);
        
        // 應該有 2 個標籤
        assertEquals(2, categoryRepository.findAll().size());
    }
}
