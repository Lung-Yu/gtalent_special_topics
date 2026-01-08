package com.example.application;

import java.util.List;

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

        // do something
        //case 1
        List<Tag> conditions = tagsRepository.findByName(command.getName());
        for (Tag tag : conditions) {
            if (tag.getType().equals(type))
                throw new IllegalArgumentException("Tag with same name and type already exists");
        }

        //case 2
        if (tagsRepository.existsByTypeAndName(command.getName(), command.getType())) {
            throw new IllegalArgumentException("Tag with same name and type already exists");
        }

        //case 3
        if (tagsRepository.findByTypeAndName(command.getName(), command.getType()) != null) {
            throw new IllegalArgumentException("Tag with same name and type already exists");
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
