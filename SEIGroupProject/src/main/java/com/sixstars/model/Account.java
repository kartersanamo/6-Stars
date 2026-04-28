package com.sixstars.model;

public class Account {
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private Boolean emailVerified;
    private String verificationCodeHash;
    private String verificationExpiresAt;
    private String profileImagePath;

    public Account(String firstName, String lastName, String email, String passwordHash, Role role, String profileImagePath) {
        this(firstName, lastName, email, passwordHash, role, null, null, null);
    }

    public Account(String firstName, String lastName, String email, String passwordHash, Role role,
                   Boolean emailVerified, String verificationCodeHash, String verificationExpiresAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = emailVerified;
        this.verificationCodeHash = verificationCodeHash;
        this.verificationExpiresAt = verificationExpiresAt;
        this.profileImagePath = profileImagePath;
    }

    public Account(String firstName, String lastName, String email, String passwordHash, Role role, boolean emailVerified, String verificationCodeHash, String verificationExpiresAt, Role role1, String profileImagePath) {
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

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public String getVerificationCodeHash() {
        return verificationCodeHash;
    }

    public String getVerificationExpiresAt() {
        return verificationExpiresAt;
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
