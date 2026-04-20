package com.sixstars.app;

import java.awt.CardLayout;
import java.time.LocalDate;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sixstars.database.DatabaseManager;
import com.sixstars.service.AccountService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.ui.*;
import com.sixstars.model.Room;

public class Main {

    public static CreateAccountPage createAccountPage;
    public static MenuPage menuPage;
    public static MakeReservationPage makeReservationPage;
    public static RoomManagementPage roomManagementPage;
    public static GuestReservationsPage guestReservationsPage;
    public static AccountDetailsPage accountDetailsPage;
    public static HomeLandingPage homeLandingPage;
    public static HeaderBar headerBar;
    public static HeaderBar headerBar2;
    private static PendingReservation pendingReservation;
    public static ShopPage shopPage;

    public static void createAndShowUI() {
        // create the .db file and tables if they don't already exist
        DatabaseManager.initializeDatabase();

        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService();
        AccountService accountService = new AccountService();

        JFrame frame = new JFrame("6 Stars Hotel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLocationRelativeTo(null);
        
        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);
        
        headerBar = new HeaderBar(pages, cardLayout);
        headerBar2 = new HeaderBar(pages, cardLayout);
        homeLandingPage = new HomeLandingPage(pages, cardLayout);
        WelcomePage welcomePage = new WelcomePage(pages, cardLayout);
        LoginPage loginPage = new LoginPage(pages, cardLayout, accountService);
        createAccountPage = new CreateAccountPage(pages, cardLayout);
        menuPage = new MenuPage(pages, cardLayout);
        makeReservationPage = new MakeReservationPage(pages, cardLayout, reservationService, roomService);
        roomManagementPage = new RoomManagementPage(pages, cardLayout, roomService);
        guestReservationsPage = new GuestReservationsPage(pages, cardLayout, reservationService);
        accountDetailsPage = new AccountDetailsPage(pages, cardLayout);
        shopPage = new ShopPage(pages, cardLayout);

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
        pages.add(shopPage, "shop");

        frame.add(pages);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShowUI);
    }

    public static void setPendingReservation(Room room, LocalDate startDate, LocalDate endDate) {
        pendingReservation = new PendingReservation(room, startDate, endDate);
    }

    public static PendingReservation consumePendingReservation() {
        PendingReservation reservation = pendingReservation;
        pendingReservation = null;
        return reservation;
    }

    public static class PendingReservation {
        private final Room room;
        private final LocalDate startDate;
        private final LocalDate endDate;

        public PendingReservation(Room room, LocalDate startDate, LocalDate endDate) {
            this.room = room;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Room getRoom() {
            return room;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
