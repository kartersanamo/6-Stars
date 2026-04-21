package com.sixstars.model;

public class Account {
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private double totalBill;

    public Account(String firstName, String lastName, String email, String passwordHash, Role role, double totalBill) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.totalBill = totalBill;
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

    public double getTotalBill() {
        return totalBill;
    }
}
