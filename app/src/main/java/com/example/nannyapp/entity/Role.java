package com.example.nannyapp.entity;

public enum Role {
    PARENT("PARENT"),
    NANNY("NANNY");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
