# 6 Stars Hotel Management System

Welcome to the 6 Stars Hotel Management System! This Java-based desktop application allows hotel staff and guests to manage reservations, accounts, and administrative tasks through a user-friendly graphical interface.

For more information, visit: [6stars.xyz](https://6stars.xyz)

## Features

- **User Authentication**: Login system for Guests, Clerks, and Admins. Admins are regular accounts with the `ADMIN` role in `accounts.json`.
- **Account Management**: Create new accounts with different roles (Guest, Clerk, Admin).
- **Reservation System**: Make and manage room reservations.
- **Admin Dashboard**: Special admin page for users with the `ADMIN` role.
- **Role-Based Access**: Features and pages are shown based on the user's role.
- **Data Persistence**: User accounts and roles are stored in `accounts.json`.

## Project Structure

```
SEIGroupProject/
├── pom.xml
├── accounts.json
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── sixstars/
│                   ├── logicClasses/
│                   │   ├── Account.java
│                   │   ├── AccountController.java
│                   │   ├── AccountService.java
│                   │   ├── BedType.java
│                   │   ├── LoginController.java
│                   │   ├── Main.java
│                   │   ├── Reservation.java
│                   │   ├── ReservationService.java
│                   │   ├── Role.java
│                   │   ├── Room.java
│                   │   └── RoomService.java
│                   └── ui/
│                       ├── CreateAccountPage.java
│                       ├── LoginPage.java
│                       ├── MakeReservationPage.java
│                       ├── WelcomePage.java
│                       └── AdminPage.java
```

## Getting Started

### Prerequisites
- Java 17 or higher (Java 25 recommended)
- Maven

### Build & Run
1. **Clone the repository** and open the `SEIGroupProject` folder in your IDE.
2. **Install dependencies and build:**
   ```sh
   mvn clean install
   ```
3. **Run the application:**
   ```sh
   mvn exec:java -Dexec.mainClass="com.sixstars.logicClasses.Main"
   ```
   Or use your IDE's run configuration for `Main.java`.

### Usage
- **Login:** Use your email and password. Admins log in like any other user.
- **Admin Access:** If your account has the `ADMIN` role in `accounts.json`, you will see the Admin dashboard after login.
- **Create Account:** Use the Create Account page to register new users.
- **Make Reservation:** Guests and clerks can make reservations after logging in.

## Customization
- **Add Admins:** To make a user an admin, set their `role` to `ADMIN` in `accounts.json`.
- **Add/Remove Roles:** Edit the `Role.java` enum and update logic as needed.

## Documentation
- See the `documents/Software Engineering Iterations Documentation.pdf` for detailed design and iteration notes.
- Visit [6stars.xyz](https://6stars.xyz) for more information and updates.

## License
This project is for educational purposes. See [6stars.xyz](https://6stars.xyz) for more details.
