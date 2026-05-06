package com.sixstars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void fiveArgumentConstructorSetsBasicFields() {
        Account account = new Account(
                "Nolan",
                "Schirripa",
                "nolan@test.com",
                "hashed-password",
                Role.GUEST
        );

        assertEquals("Nolan", account.getFirstName());
        assertEquals("Schirripa", account.getLastName());
        assertEquals("nolan@test.com", account.getEmail());
        assertEquals("hashed-password", account.getPasswordHash());
        assertEquals(Role.GUEST, account.getRole());
    }

    @Test
    void fullConstructorSetsVerificationAndProfileImageFields() {
        Account account = new Account(
                "Admin",
                "User",
                "admin@test.com",
                "hash",
                Role.ADMIN,
                true,
                "verification-hash",
                "2026-05-10T12:00",
                "assets/profile/admin.png"
        );

        assertTrue(account.isEmailVerified());
        assertEquals("verification-hash", account.getVerificationCodeHash());
        assertEquals("2026-05-10T12:00", account.getVerificationExpiresAt());
        assertEquals("assets/profile/admin.png", account.getProfileImagePath());
    }

    @Test
    void nullEmailVerifiedReturnsFalse() {
        Account account = new Account(
                "Clerk",
                "User",
                "clerk@test.com",
                "hash",
                Role.CLERK
        );

        assertFalse(account.isEmailVerified());
    }

    @Test
    void settersUpdateEditableFields() {
        Account account = new Account(
                "Old",
                "Name",
                "user@test.com",
                "oldHash",
                Role.GUEST
        );

        account.setFirstName("New");
        account.setLastName("User");
        account.setPasswordHash("newHash");
        account.setRole(Role.CLERK);
        account.setProfileImagePath("assets/profile/new.png");

        assertEquals("New", account.getFirstName());
        assertEquals("User", account.getLastName());
        assertEquals("newHash", account.getPasswordHash());
        assertEquals(Role.CLERK, account.getRole());
        assertEquals("assets/profile/new.png", account.getProfileImagePath());
    }
}