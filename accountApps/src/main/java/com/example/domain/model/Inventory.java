package com.example.domain.model;

public class Inventory {
    
    private String id;
    private String name;

    public Inventory(String id , String name){
        setId(id);
        setName(name);
    }

    public void setId(String id){
        // AF001 格式：2個英文3個數字，否則違法，透過正規表達法檢查
        if (!id.matches("[a-zA-Z]{2}\\d{3}")) {
            throw new IllegalArgumentException("Invalid ID format. Expected format: 2 letters followed by 3 digits");
        }
        this.id = id;        
    }

    public void setName(String name){
        this.name = name;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

}
