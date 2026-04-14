package com.example.infrastructure.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.domain.model.Category;
import com.example.domain.model.User;
import com.example.domain.repository.CategoryRepository;
import com.example.domain.valueobject.TypeCategory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InJsonLibCategoryRepository implements CategoryRepository {

    private final File file;
    private final Gson gson;

    public InJsonLibCategoryRepository(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            filepath = "data/categories_lib.json";
        }
        this.file = new File(filepath);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public InJsonLibCategoryRepository() {
        this("data/categories_lib.json");
    }

    public InJsonLibCategoryRepository(File file) {
        this.file = file;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
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
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            // Read existing categories
            JsonArray jsonArray;
            if (file.exists() && file.length() > 0) {
                try (FileReader reader = new FileReader(file)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    jsonArray = element.getAsJsonArray();
                }
            } else {
                jsonArray = new JsonArray();
            }
            
            // Create JSON object for new category
            JsonObject categoryJson = new JsonObject();
            categoryJson.addProperty("name", category.getName());
            
            if (category.getIcon() == null) {
                categoryJson.add("icon", JsonNull.INSTANCE);
            } else {
                categoryJson.addProperty("icon", category.getIcon());
            }
            
            categoryJson.addProperty("type", category.getType().name());
            categoryJson.addProperty("createdAt", category.getCreatedAt().toString());
            categoryJson.addProperty("createdBy", category.getCreatedBy().getUsername());
            
            // Add to array
            jsonArray.add(categoryJson);
            
            // Write back to file
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(jsonArray, writer);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save category", e);
        }
    }

    @Override
    public List<Category> findAll() {
        try {
            if (!file.exists() || file.length() == 0) {
                return List.of();
            }

            List<Category> categories = new ArrayList<>();
            
            try (FileReader reader = new FileReader(file)) {
                JsonElement element = JsonParser.parseReader(reader);
                JsonArray jsonArray = element.getAsJsonArray();
                
                for (JsonElement categoryElement : jsonArray) {
                    JsonObject categoryJson = categoryElement.getAsJsonObject();
                    
                    String name = categoryJson.get("name").getAsString();
                    JsonElement iconElement = categoryJson.get("icon");
                    String icon = (iconElement == null || iconElement.isJsonNull()) ? 
                                  null : iconElement.getAsString();
                    String typeStr = categoryJson.get("type").getAsString();
                    String createdAtStr = categoryJson.get("createdAt").getAsString();
                    String createdByStr = categoryJson.get("createdBy").getAsString();
                    
                    TypeCategory type = TypeCategory.fromString(typeStr);
                    LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
                    User createdBy = new User(createdByStr);
                    
                    Category category = new Category(name, icon, type, createdAt, createdBy);
                    categories.add(category);
                }
            }
            
            return categories;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read categories", e);
        }
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
