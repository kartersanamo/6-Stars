package com.sixstars.app;

import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.ui.*;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static CreateAccountPage createAccountPage;
    public static MenuPage menuPage;
    public static MakeReservationPage makeReservationPage;
    public static RoomManagementPage roomManagementPage;

    public static void createAndShowUI() {
        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService();

        JFrame frame = new JFrame("6 Stars Hotel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);

        WelcomePage welcomePage = new WelcomePage(pages, cardLayout);
        LoginPage loginPage = new LoginPage(pages, cardLayout);
        createAccountPage = new CreateAccountPage(pages, cardLayout);
        menuPage = new MenuPage(pages, cardLayout);
        makeReservationPage = new MakeReservationPage(pages, cardLayout, reservationService, roomService);
        roomManagementPage = new RoomManagementPage(pages, cardLayout, roomService);
        AccountDetailsPage accountDetailsPage = new AccountDetailsPage(pages, cardLayout);

        pages.add(welcomePage, "welcome");
        pages.add(loginPage, "login");
        pages.add(new AdminPage(pages, cardLayout), "admin");
        pages.add(createAccountPage, "create account");
        pages.add(menuPage, "menu page");
        pages.add(makeReservationPage, "make reservation");
        pages.add(roomManagementPage, "room management");
        pages.add(accountDetailsPage, "account details");

        frame.add(pages);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }
}
