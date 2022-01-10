package com.example.nannyapp.entity;

public class Parent extends User {
    private String noChildren;
    private String description;

    public Parent() {}

    public Parent(Role role, String firstName, String lastName, String phoneNumber, String address, String noChildren, String description) {
        super(role, firstName, lastName, phoneNumber, address);
        this.noChildren = noChildren;
        this.description = description;
    }

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
