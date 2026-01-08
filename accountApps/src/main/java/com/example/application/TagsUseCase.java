package com.example.application;

import com.example.application.command.TagCreateCommand;
import com.example.application.exception.TagTypeNotExists;
import com.example.domain.model.Tag;
import com.example.domain.repository.TagsRepository;
import com.example.domain.valueobject.TypeTag;

public class TagsUseCase {
    private final TagsRepository tagsRepository;

    public TagsUseCase(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    public void execute(TagCreateCommand command) throws TagTypeNotExists {
        validate(command);

        TypeTag type;
        try {
            type = TypeTag.fromString(command.getType());
        } catch (IllegalArgumentException ex) {
            throw new TagTypeNotExists(command.getType());
        }

        Tag tag = new Tag(command.getName(), command.getIcon(), type);
        tagsRepository.save(tag);
    }

    private void validate(TagCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag name cannot be null or empty");
        }
        if (command.getType() == null || command.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag type cannot be null or empty");
        }
    }
}
