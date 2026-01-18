package com.example.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class InCSVCategoryRepository implements CategoryRepository {

    private final File file;
    private static final String HEADER = "name,icon,type,createdAt,createdBy";
    private static final String NULL_MARKER = "<NULL>";

    public InCSVCategoryRepository(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            filepath = "data/categories.csv";
        }
        this.file = new File(filepath);
    }

    public InCSVCategoryRepository() {
        String filepath = "data/categories.csv";
        this.file = new File(filepath);
    }

    public InCSVCategoryRepository(File file) {
        this.file = file;
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
            
            boolean fileExists = file.exists();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                // Write header if file is new or empty
                if (!fileExists || file.length() == 0) {
                    writer.write(HEADER);
                    writer.newLine();
                }
                
                // Write category data
                String csvLine = toCsvLine(category);
                writer.write(csvLine);
                writer.newLine();
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

            List<Category> categories = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                
                while ((line = reader.readLine()) != null) {
                    // Skip header
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    Category category = parseCsvLine(line);
                    if (category != null) {
                        categories.add(category);
                    }
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

    private String toCsvLine(Category category) {
        String name = escapeCsvValue(category.getName());
        String icon = category.getIcon() == null ? NULL_MARKER : escapeCsvValue(category.getIcon());
        String type = category.getType().name();
        String createdAt = category.getCreatedAt().toString();
        String createdBy = escapeCsvValue(category.getCreatedBy().getUsername());
        
        return String.format("%s,%s,%s,%s,%s", name, icon, type, createdAt, createdBy);
    }

    private Category parseCsvLine(String line) {
        try {
            String[] fields = parseCsvFields(line);
            
            if (fields.length != 5) {
                return null;
            }
            
            String name = unescapeCsvValue(fields[0]);
            String icon = fields[1].equals(NULL_MARKER) ? null : unescapeCsvValue(fields[1]);
            String typeStr = fields[2];
            String createdAtStr = fields[3];
            String createdByStr = unescapeCsvValue(fields[4]);
            
            TypeCategory type = TypeCategory.fromString(typeStr);
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
            User createdBy = new User(createdByStr);
            
            return new Category(name, icon, type, createdAt, createdBy);
        } catch (Exception e) {
            return null;
        }
    }

    private String[] parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // Check for escaped quote
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private String unescapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // Remove surrounding quotes if present
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            // Unescape doubled quotes
            value = value.replace("\"\"", "\"");
        }
        
        return value;
    }
}
