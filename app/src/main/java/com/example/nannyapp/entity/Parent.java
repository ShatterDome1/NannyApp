package com.example.nannyapp.entity;

public class Parent extends User {
    private String noChildren;
    private String description;

    public Parent() {}

    public String getNoChildren() {
        return noChildren;
    }

    public void setNoChildren(String noChildren) {
        this.noChildren = noChildren;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
