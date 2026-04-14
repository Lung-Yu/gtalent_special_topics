package com.example.application.command;

import java.time.LocalDate;

import com.example.domain.model.User;

public class ExpenditureQueryCommand {

    private User user;
    private LocalDate date;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
