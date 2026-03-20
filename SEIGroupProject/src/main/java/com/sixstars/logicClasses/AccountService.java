package com.sixstars.logicClasses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class AccountService {
    private static final String FILE_PATH = "accounts.json";
    private final Gson gson;

    public AccountService() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Account createGuestAccount(String firstName, String lastName, String email, String password) {
        ArrayList<Account> accounts = loadAccounts();

        for (Account acc : accounts) {
            if (acc.getEmail().equalsIgnoreCase(email)) {
                throw new RuntimeException("An account with that email already exists.");
            }
        }

        String passwordHash = hashPassword(password);

        Account guestAccount = new Account(
                firstName,
                lastName,
                email,
                passwordHash,
                Role.GUEST
        );

        accounts.add(guestAccount);
        saveAccounts(accounts);

        return guestAccount;
    }

    private ArrayList<Account> loadAccounts() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Account>>() {}.getType();
            ArrayList<Account> accounts = gson.fromJson(reader, listType);

            if (accounts == null) {
                return new ArrayList<>();
            }

            return accounts;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveAccounts(ArrayList<Account> accounts) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(accounts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not hash password.", e);
        }
    }
}
