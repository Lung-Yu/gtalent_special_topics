package com.example.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.example.domain.model.Tag;
import com.example.domain.repository.TagsRepository;

public class InMemoryTagsRepository implements TagsRepository {

    private final List<Tag> tags;

    public InMemoryTagsRepository() {
        this.tags = new ArrayList<>();
    }

    public InMemoryTagsRepository(List<Tag> seed) {
        this.tags = new ArrayList<>(seed == null ? Collections.emptyList() : seed);
    }

    @Override
    public List<Tag> findByName(String name) {
        if (name == null) {
            return Collections.emptyList();
        }
        return tags.stream()
                .filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Tag> findByType(String type) {
        if (type == null) {
            return Collections.emptyList();
        }
        String normalized = type.trim().toUpperCase();
        return tags.stream()
                .filter(t -> t.getType() != null && t.getType().name().equalsIgnoreCase(normalized))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        this.tags.add(tag);
    }

    @Override
    public List<Tag> findAll() {
        return new ArrayList<>(tags);
    }

    @Override
    public Tag findByTypeAndName(String name, String type) {
        if (name == null || type == null) {
            return null;
        }
        String normalizedType = type.trim().toUpperCase();
        return tags.stream()
                .filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(name)
                        && t.getType() != null && t.getType().name().equalsIgnoreCase(normalizedType))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsByTypeAndName(String name, String type) {
        return findByTypeAndName(name, type) != null;
    }
}
