package com.sixstars.app;

import com.sixstars.database.DatabaseManager;
import com.sixstars.service.AccountService;
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
    public static GuestReservationsPage guestReservationsPage;

    public static void createAndShowUI() {
        // create the .db file and tables if they don't already exist
        DatabaseManager.initializeDatabase();

        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService();
        AccountService accountService = new AccountService();

        JFrame frame = new JFrame("6 Stars Hotel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);

        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);

        HomeLandingPage homeLandingPage = new HomeLandingPage(pages, cardLayout);
        WelcomePage welcomePage = new WelcomePage(pages, cardLayout);
        LoginPage loginPage = new LoginPage(pages, cardLayout, accountService);
        createAccountPage = new CreateAccountPage(pages, cardLayout);
        menuPage = new MenuPage(pages, cardLayout);
        makeReservationPage = new MakeReservationPage(pages, cardLayout, reservationService, roomService);
        roomManagementPage = new RoomManagementPage(pages, cardLayout, roomService);
        guestReservationsPage = new GuestReservationsPage(pages, cardLayout, reservationService);
        AccountDetailsPage accountDetailsPage = new AccountDetailsPage(pages, cardLayout);

        pages.add(homeLandingPage, "home");
        pages.add(welcomePage, "welcome");
        pages.add(loginPage, "login");
        pages.add(new AdminPage(pages, cardLayout), "admin");
        pages.add(createAccountPage, "create account");
        pages.add(menuPage, "menu page");
        pages.add(makeReservationPage, "make reservation");
        pages.add(roomManagementPage, "room management");
        pages.add(guestReservationsPage, "guest reservations");
        pages.add(accountDetailsPage, "account details");

        frame.add(pages);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }
}
