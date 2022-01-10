package com.example.nannyapp.entity;

public class Nanny extends User {
    private String dateOfBirth;
    private String skills;
    private String experience;

    public Nanny() {}

    public Nanny(Role role, String firstName, String lastName, String phoneNumber, String address, String dateOfBirth, String skills, String experience) {
        super(role, firstName, lastName, phoneNumber, address);
        this.dateOfBirth = dateOfBirth;
        this.skills = skills;
        this.experience = experience;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }
}
