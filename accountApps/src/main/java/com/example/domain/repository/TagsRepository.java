package com.example.domain.repository;

import java.util.List;

import com.example.domain.model.Tag;

public interface TagsRepository {
    List<Tag> findByName(String name);
    List<Tag> findByType(String type);
    void save(Tag tag);
    List<Tag> findAll();
    Tag findByTypeAndName(String name, String type);
    boolean existsByTypeAndName(String name, String type);
}
