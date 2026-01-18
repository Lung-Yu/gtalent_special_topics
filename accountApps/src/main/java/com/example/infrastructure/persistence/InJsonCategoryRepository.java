package com.example.infrastructure.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.domain.model.Category;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.valueobject.TypeCategory;

public class InJsonCategoryRepository implements CategoryRepository {

    private final File file;

    public InJsonCategoryRepository(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            filepath = "data/categories.json";
        }
        this.file = new File(filepath);
    }

    public InJsonCategoryRepository() {
        String filepath = "data/categories.json";
        this.file = new File(filepath);
    }

    public InJsonCategoryRepository(File jsonFile) {
        this.file = jsonFile;
    }

    @Override
    public List<Category> findByName(String name) {
        List<Category> allCategories = findAll();
        List<Category> result = new ArrayList<>();
        
        for (Category category : allCategories) {
            if (category.getName().equals(name)) {
                result.add(category);
            }
        }
        
        return result;
    }

    @Override
    public List<Category> findByType(String type) {
        List<Category> allCategories = findAll();
        List<Category> result = new ArrayList<>();
        TypeCategory typeCategory = TypeCategory.fromString(type);
        
        for (Category category : allCategories) {
            if (category.getType() == typeCategory) {
                result.add(category);
            }
        }
        
        return result;
    }

    @Override
    public void save(Category category) {
        String icon = category.getIcon() == null ? "null" : "\"" + category.getIcon() + "\"";
        
        String jsonObj = 
        String.format("{\"name\":\"%s\",\"icon\":%s,\"type\":\"%s\",\"createdAt\":\"%s\",\"createdBy\":\"%s\"}", 
            category.getName(), 
            icon, 
            category.getType().name(), 
            category.getCreatedAt().toString(), 
            category.getCreatedBy().getUsername());

        // jsonObj append into file
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write("[]");
                }
            }

            // Read existing content
            String content = new String(Files.readAllBytes(file.toPath()));

            // Parse as JSON array and append new object
            if (content.trim().equals("[]")) {
                content = "[" + jsonObj + "]";
            } else {
                content = content.substring(0, content.lastIndexOf("]")) + "," + jsonObj + "]";
            }

            // Write back to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save category", e);
        }


    }

    @Override
    public List<Category> findAll() {
        try {
            if (!file.exists()) {
                return List.of();
            }

            String content = new String(Files.readAllBytes(file.toPath()));
            
            if (content.trim().isEmpty() || content.trim().equals("[]")) {
                return List.of();
            }

            // Parse JSON array manually
            List<Category> categories = new ArrayList<>();
            content = content.trim();
            
            // Remove outer brackets
            content = content.substring(1, content.length() - 1);
            
            // Split by objects (simple parsing)
            int braceCount = 0;
            int start = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') {
                    braceCount++;
                    if (braceCount == 1) {
                        start = i;
                    }
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        String jsonObj = content.substring(start, i + 1);
                        Category category = parseCategory(jsonObj);
                        if (category != null) {
                            categories.add(category);
                        }
                    }
                }
            }
            
            return categories;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read categories", e);
        }
    }

    private Category parseCategory(String jsonObj) {
        try {
            String name = extractValue(jsonObj, "name");
            String icon = extractValue(jsonObj, "icon");
            String typeStr = extractValue(jsonObj, "type");
            String createdAtStr = extractValue(jsonObj, "createdAt");
            String createdByStr = extractValue(jsonObj, "createdBy");
            
            TypeCategory type = TypeCategory.fromString(typeStr);
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
            User createdBy = new User(createdByStr);
            
            return new Category(name, icon, type, createdAt, createdBy);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractValue(String json, String key) {
        // Check for null value first
        String nullKey = "\"" + key + "\":null";
        if (json.contains(nullKey)) {
            return null;
        }
        
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return "";
        }
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return "";
        }
        return json.substring(startIndex, endIndex);
    }

    @Override
    public Category findByTypeAndName(String name, String type) {
        List<Category> allCategories = findAll();
        TypeCategory typeCategory = TypeCategory.fromString(type);
        
        for (Category category : allCategories) {
            if (category.getName().equals(name) && category.getType() == typeCategory) {
                return category;
            }
        }
        
        return null;
    }

    @Override
    public boolean existsByTypeAndName(String name, String type) {
        return findByTypeAndName(name, type) != null;
    }

}
