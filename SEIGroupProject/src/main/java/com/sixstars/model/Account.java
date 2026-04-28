package com.sixstars.model;

public class Account {
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private String profileImagePath;

    public Account(String firstName, String lastName, String email, String passwordHash, Role role) {
        this(firstName, lastName, email, passwordHash, role, null);
    }

    public Account(String firstName, String lastName, String email, String passwordHash, Role role, String profileImagePath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.profileImagePath = profileImagePath;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
