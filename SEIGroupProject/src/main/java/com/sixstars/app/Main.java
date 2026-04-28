package com.sixstars.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.time.LocalDate;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.cdimascio.dotenv.Dotenv;

import com.sixstars.controller.AccountController;
import com.sixstars.database.DatabaseManager;
import com.sixstars.model.Room;
import com.sixstars.service.AccountService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.ui.*;

public class Main {

    public static CreateAccountPage createAccountPage;
    public static ClerkPage clerkPage;
    public static MakeReservationPage makeReservationPage;
    public static ReservationConfirmationPage reservationConfirmationPage;
    public static RoomManagementPage roomManagementPage;
    public static ReservationsPage reservationsPage;
    public static AccountDetailsPage accountDetailsPage;
    public static HomeLandingPage homeLandingPage;
    public static HeaderBar headerBar;
    private static PendingReservation pendingReservation;
    public static ShopPage shopPage;
    public static BillingPage billingPage;
    public static CheckInPage checkInPage;
    public static ChangePasswordPage changePasswordPage;
    public static ClerkBillingSearchPage clerkBillingSearchPage;

    public static void createAndShowUI() {
        // create the .db file and tables if they don't already exist
        DatabaseManager.initializeDatabase();

        RoomService roomService = new RoomService();
        ReservationService reservationService = new ReservationService();
        AccountService accountService = new AccountService();
        AccountController accountController = new AccountController();

        JFrame frame = new JFrame("6 Stars Hotel");
        applyAppIcon(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        
        CardLayout cardLayout = new CardLayout();
        JPanel pages = new JPanel(cardLayout);
        headerBar = new HeaderBar(pages, cardLayout);
        homeLandingPage = new HomeLandingPage(pages, cardLayout);
        WelcomePage welcomePage = new WelcomePage(pages, cardLayout);
        LoginPage loginPage = new LoginPage(pages, cardLayout, accountService);
        createAccountPage = new CreateAccountPage(pages, cardLayout);
        clerkPage = new ClerkPage(pages, cardLayout);
        makeReservationPage = new MakeReservationPage(pages, cardLayout, reservationService, roomService);
        reservationConfirmationPage = new ReservationConfirmationPage(pages, cardLayout, reservationService, roomService);
        roomManagementPage = new RoomManagementPage(pages, cardLayout, roomService, reservationService);
        reservationsPage = new ReservationsPage(pages, cardLayout, reservationService);
        accountDetailsPage = new AccountDetailsPage(pages, cardLayout, accountController);
        changePasswordPage = new ChangePasswordPage(pages, cardLayout, accountService);
        shopPage = new ShopPage(pages, cardLayout);
        billingPage = new BillingPage();
        checkInPage = new CheckInPage(pages, cardLayout, reservationService);
        clerkBillingSearchPage = new ClerkBillingSearchPage(pages, cardLayout);


        pages.add(homeLandingPage, "home");
        pages.add(welcomePage, "welcome");
        pages.add(loginPage, "login");
        pages.add(new AdminPage(pages, cardLayout), "admin page");
        pages.add(createAccountPage, "create account");
        pages.add(changePasswordPage, "reset password");
        pages.add(clerkPage, "clerk page");
        pages.add(makeReservationPage, "make reservation");
        pages.add(reservationConfirmationPage, "reservation confirmation");
        pages.add(roomManagementPage, "room management");
        pages.add(reservationsPage, "reservations");
        pages.add(accountDetailsPage, "account details");
        pages.add(shopPage, "shop");
        pages.add(billingPage, "billing page");
        pages.add(checkInPage, "check in");
        pages.add(clerkBillingSearchPage, "clerk billing");

        frame.setLayout(new BorderLayout());
  

        frame.add(headerBar, BorderLayout.NORTH);
        frame.add(pages, BorderLayout.CENTER);
        frame.setVisible(true);

        // Clean up expired reservations immediately on startup
        reservationService.processAutomaticCheckOuts();
    }

    // Load the application icon robustly (classpath first, then filesystem) and attempt to set
    // it for both the JFrame and the OS taskbar/dock when possible.
    private static void applyAppIcon(JFrame frame) {
        java.awt.Image image = null;

        // 1) Try classpath resource (recommended: put Logo.png in src/main/resources/assets/)
        try {
            java.net.URL res = Main.class.getResource("/assets/Logo.png");
            if (res != null) {
                image = new ImageIcon(res).getImage();
            }
        } catch (Throwable ignored) {
        }

        // 2) Fallback to file path (useful during development)
        if (image == null) {
            try {
                java.io.File f = new java.io.File("assets/Logo.png");
                if (f.exists()) {
                    image = new ImageIcon(f.getAbsolutePath()).getImage();
                }
            } catch (Throwable ignored) {
            }
        }

        if (image == null) {
            return; // nothing to apply
        }

        try {
            // Create a rounded version of the image for a nicer Dock/Taskbar appearance
            int preferredSize = 512; // size to render the rounded icon (keeps it crisp on HiDPI)
            java.awt.Image roundedImage = makeRounded(image, preferredSize, Math.round(preferredSize * 0.18f));

            // Always set the JFrame icon (use rounded image so it matches other icons)
            frame.setIconImage(roundedImage != null ? roundedImage : image);

            // Try java.awt.Taskbar (Java 9+) via reflection so code compiles on older JDKs
            try {
                Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
                java.lang.reflect.Method getTaskbar = taskbarClass.getMethod("getTaskbar");
                Object taskbar = getTaskbar.invoke(null);
                java.lang.reflect.Method setIconImage = taskbarClass.getMethod("setIconImage", java.awt.Image.class);
                setIconImage.invoke(taskbar, roundedImage != null ? roundedImage : image);
            } catch (Throwable t) {
                // If Taskbar isn't available, try macOS-specific API (com.apple.eawt.Application)
                try {
                    Class<?> appClass = Class.forName("com.apple.eawt.Application");
                    java.lang.reflect.Method getApplication = appClass.getMethod("getApplication");
                    Object application = getApplication.invoke(null);
                    java.lang.reflect.Method setDockIconImage = appClass.getMethod("setDockIconImage", java.awt.Image.class);
                    setDockIconImage.invoke(application, roundedImage != null ? roundedImage : image);
                } catch (Throwable ignored2) {
                    // Last fallback: nothing else we can do
                }
            }
        } catch (Throwable ignore) {
            // Do not prevent app startup on icon errors
        }
    }

    // Create a rounded BufferedImage from the provided image. Returns null on failure.
    private static java.awt.Image makeRounded(java.awt.Image src, int size, int arc) {
        try {
            if (src == null) return null;

            java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = out.createGraphics();
            try {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(java.awt.AlphaComposite.SrcOver);

                // Draw rounded clipping area
                java.awt.geom.RoundRectangle2D round = new java.awt.geom.RoundRectangle2D.Float(0, 0, size, size, arc, arc);
                g2.setClip(round);

                // Draw the source image scaled to the target size
                g2.drawImage(src, 0, 0, size, size, null);
            } finally {
                g2.dispose();
            }
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    public static void main(String[] args) {
        // Load .env file if it exists
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            // Apply loaded environment variables to System properties so they can be accessed
            // by the rest of the application (System.getenv() is read-only)
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            // .env file not found or failed to load, continue with system environment variables
        }
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
