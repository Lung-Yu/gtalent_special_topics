package com.example.application.command;

import com.example.domain.model.User;

public class TagCreateCommand {
    private User user;
    private String name;
    private String type;
    private String icon;

    public TagCreateCommand() {}

    public TagCreateCommand(User user, String name, String type, String icon) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
