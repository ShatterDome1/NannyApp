package com.example.nannyapp.entity;

import com.example.nannyapp.utils.Role;

public class User {
    private Role role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String noChildren;
    private String address;
    private String description;

    public User() {}

    public User(Role role, String firstName, String lastName, String phoneNumber, String noChildren, String address, String description) {
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.noChildren = noChildren;
        this.address = address;
        this.description = description;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNoChildren() {
        return noChildren;
    }

    public void setNoChildren(String noChildren) {
        this.noChildren = noChildren;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
