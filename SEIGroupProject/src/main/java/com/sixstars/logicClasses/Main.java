package com.sixstars.logicClasses;

import com.sixstars.ui.CreateAccountPage;
import com.sixstars.ui.LoginPage;
import com.sixstars.ui.WelcomePage;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void createAndShowUI() {
        JFrame frame = new JFrame("6 Stars Hotel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);

        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);

        WelcomePage welcomePage = new WelcomePage(pages, cardLayout);
        LoginPage loginPage = new LoginPage(pages, cardLayout);
        CreateAccountPage createAccountPage = new CreateAccountPage(pages, cardLayout);

        pages.add(welcomePage, "welcome");
        pages.add(loginPage, "login");
        pages.add(createAccountPage, "create account");

        frame.add(pages);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowUI());
    }
}
