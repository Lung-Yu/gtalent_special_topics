package com.example.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.example.application.command.TagCreateCommand;
import com.example.application.exception.TagTypeNotExists;
import com.example.domain.model.Tag;
import com.example.domain.model.User;
import com.example.domain.repository.TagsRepository;
import com.example.domain.valueobject.TypeTag;
import com.example.infrastructure.persistence.InMemoryTagsRepository;

public class TagsUseCaseTest {

    private TagsRepository tagsRepository;
    private TagsUseCase tagsUseCase;
    private User user1;

    @Before
    public void setUp() {
        tagsRepository = new InMemoryTagsRepository();
        tagsUseCase = new TagsUseCase(tagsRepository);
        user1 = new User("alice");
    }

    @Test
    public void execute_WithValidIncomeTag_ShouldSaveSuccessfully() throws TagTypeNotExists {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "薪水",
            "income",
            "💰"
        );

        tagsUseCase.execute(command);

        assertEquals(1, tagsRepository.findAll().size());
        
        Tag savedTag = tagsRepository.findAll().get(0);
        assertEquals("薪水", savedTag.getName());
        assertEquals("💰", savedTag.getIcon());
        assertEquals(TypeTag.INCOME, savedTag.getType());
    }

    @Test
    public void execute_WithValidOutcomeTag_ShouldSaveSuccessfully() throws TagTypeNotExists {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "午餐",
            "OUTCOME",
            "🍱"
        );

        tagsUseCase.execute(command);

        assertEquals(1, tagsRepository.findAll().size());
        
        Tag savedTag = tagsRepository.findAll().get(0);
        assertEquals("午餐", savedTag.getName());
        assertEquals("🍱", savedTag.getIcon());
        assertEquals(TypeTag.OUTCOME, savedTag.getType());
    }

    @Test
    public void execute_WithCaseInsensitiveType_ShouldParseCorrectly() throws TagTypeNotExists {
        String[] variations = {"income", "INCOME", "Income", "InCoMe"};
        
        for (String typeVariation : variations) {
            tagsRepository = new InMemoryTagsRepository();
            tagsUseCase = new TagsUseCase(tagsRepository);
            
            TagCreateCommand command = new TagCreateCommand(
                user1,
                "測試",
                typeVariation,
                "✓"
            );

            tagsUseCase.execute(command);

            assertEquals(1, tagsRepository.findAll().size());
            assertEquals(TypeTag.INCOME, tagsRepository.findAll().get(0).getType());
        }
    }

    @Test
    public void execute_WithMultipleTags_ShouldSaveAll() throws TagTypeNotExists {
        TagCreateCommand command1 = new TagCreateCommand(user1, "薪水", "income", "💰");
        TagCreateCommand command2 = new TagCreateCommand(user1, "午餐", "outcome", "🍱");
        TagCreateCommand command3 = new TagCreateCommand(user1, "獎金", "income", "🎁");

        tagsUseCase.execute(command1);
        tagsUseCase.execute(command2);
        tagsUseCase.execute(command3);

        assertEquals(3, tagsRepository.findAll().size());
    }

    @Test
    public void execute_WithInvalidType_ShouldThrowTagTypeNotExists() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "測試",
            "invalid_type",
            "❌"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw TagTypeNotExists");
        } catch (TagTypeNotExists e) {
            assertEquals("Tag type not exists: invalid_type", e.getMessage());
        }
    }

    @Test
    public void execute_WithEmptyType_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "測試",
            "",
            "❌"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag type cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullType_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "測試",
            null,
            "❌"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag type cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithWhitespaceType_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "測試",
            "   ",
            "❌"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag type cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullCommand_ShouldThrowIllegalArgumentException() {
        try {
            tagsUseCase.execute(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Command cannot be null", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullName_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            null,
            "income",
            "💰"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag name cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithEmptyName_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "",
            "income",
            "💰"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag name cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithWhitespaceName_ShouldThrowIllegalArgumentException() {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "   ",
            "income",
            "💰"
        );

        try {
            tagsUseCase.execute(command);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Tag name cannot be null or empty", e.getMessage());
        } catch (TagTypeNotExists e) {
            fail("Should throw IllegalArgumentException, not TagTypeNotExists");
        }
    }

    @Test
    public void execute_WithNullIcon_ShouldStillSave() throws TagTypeNotExists {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "無圖示標籤",
            "income",
            null
        );

        tagsUseCase.execute(command);

        assertEquals(1, tagsRepository.findAll().size());
        
        Tag savedTag = tagsRepository.findAll().get(0);
        assertEquals("無圖示標籤", savedTag.getName());
        assertEquals(null, savedTag.getIcon());
        assertEquals(TypeTag.INCOME, savedTag.getType());
    }

    @Test
    public void execute_CanQueryByName() throws TagTypeNotExists {
        TagCreateCommand command = new TagCreateCommand(
            user1,
            "薪水",
            "income",
            "💰"
        );

        tagsUseCase.execute(command);

        assertEquals(1, tagsRepository.findByName("薪水").size());
        assertEquals(0, tagsRepository.findByName("不存在").size());
    }

    @Test
    public void execute_CanQueryByType() throws TagTypeNotExists {
        TagCreateCommand command1 = new TagCreateCommand(user1, "薪水", "income", "💰");
        TagCreateCommand command2 = new TagCreateCommand(user1, "午餐", "outcome", "🍱");
        TagCreateCommand command3 = new TagCreateCommand(user1, "獎金", "income", "🎁");

        tagsUseCase.execute(command1);
        tagsUseCase.execute(command2);
        tagsUseCase.execute(command3);

        assertEquals(2, tagsRepository.findByType("INCOME").size());
        assertEquals(1, tagsRepository.findByType("OUTCOME").size());
    }
}
