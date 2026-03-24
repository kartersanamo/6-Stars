package com.sixstars.controller;

import java.io.FileReader;
import java.util.List;
import java.lang.reflect.Type;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sixstars.model.Account;
import com.sixstars.service.AccountService;
import com.sixstars.ui.AccountDetailsPage;

public class LoginController {
    public static Account checkLogin(String username, String password) {
        AccountService aService = new AccountService();
        String hashPassword = aService.hashPassword(password);


        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Account>>(){}.getType();

            List<Account> users = gson.fromJson(
                    new FileReader("accounts.json"), listType
            );

            for (Account user : users) {
                if (user.getEmail().equals(username) &&
                        user.getPasswordHash().equals(hashPassword)) {
                    AccountController.currentAccount = user;
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
