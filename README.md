# Six Stars Hotel

Single-property hotel desktop client: Swing UI, SQLite, role-based flows for guests, clerks, and administrators.

## Requirements and run

- **JDK 25**, **Maven 3.x**
- Optional **`.env`** (or JVM system properties) for Stripe, Mailgun, and related keys.

```bash
mvn compile exec:java
```

**Data:** `hotel_reservation.db` in the process working directory, created or opened on launch; a copy may be unpacked from `src/main/resources/data` when no local file exists. **Branding:** `src/main/resources/assets/Logo.png` or `assets/Logo.png`. **Shop images:** `src/main/resources/assets/shopImages/` plus classpath-aware resolution in `ShopImageLoader`.

**Package:** `mvn package` produces a runnable jar-with-dependencies (see `pom.xml`).

---

## Roles

| Role  | Access summary |
|-------|----------------|
| Guest | Book and view stays, account center, guest billing, shop (cart and checkout when checked in). |
| Clerk | Clerk dashboard, reservations, room management, check-in/out, clerk billing search, shop catalog (view only). |
| Admin | Admin command center, create-account flow for staff, global password reset tool, shortcuts into clerk and billing surfaces. |

---

## Feature layout

### Platform and session

- Card-based navigation between full-screen views; persistent **header** (brand home, book, shop, reservations when applicable, login/create account, profile menu with account center and role home).
- **Logout** clears session, persists shop cart where applicable, refreshes chrome, returns home (`AppSession`).
- **Automatic check-outs** for stays past end date on application start and from clerk/admin actions.

### Authentication and identity

- **Login** with role routing (guest home, clerk page, admin page).
- **Create account** (guest or clerk per flow); **password strength** meter and policy checks; **email verification** path and dialogs where required.
- **Forgot password** self-service page.
- **Change password** page (wired from account flows).
- **Admin reset user password:** pick any account from a list and set a new password (separate from self-service change password).

### Guest-facing

- **Home landing:** entry points into booking and shop.
- **Make reservation:** date and room selection, pricing context, handoff to **reservation confirmation**.
- **Reservations list** (guest): own stays.
- **Billing page:** reservation charges, shop charges, totals, amount due, Stripe pay path when configured, saved-card demo pay, link into Account Center payment area, **payment history** with **PDF receipt** open in browser (PDFBox).
- **Shop:** catalog with search and sort, product cards, **bag** with quantity steppers, checkout (enforced checked-in guest and stock rules), cart persistence in SQLite; notifications on successful orders; clerk mode is browse-only.

### Property operations (staff)

- **Room management:** CRUD-style inventory (bed type, theme, quality, smoking, nightly rate) backed by `RoomService` / `RoomDAO`.
- **Reservations page (clerk):** search and manage reservations for the property.
- **Check-in page:** arrival and departure handling tied to reservation status.
- **Clerk dashboard:** live metrics (reservations, in-house, arrivals today, occupancy vs room count, low-stock count, guest profile count); **quick folio** email field opening **clerk billing search** with folio load; **shift tiles** (new reservation, reservation desk, check-in, rooms, billing, shop, account center, night audit dialog, auto check-out, hotel directory); scroll lists for **today’s arrivals**, **in-house**, **low-stock SKUs** with folio shortcut; **briefing** copy to clipboard, guest home preview, sign out.

### Billing and payments (system)

- **Clerk billing search:** guest email search, folio generation, **hotel-wide billing summary** and metrics for clerks; admin path returns to admin when appropriate.
- **Guest ledger** and **saved payment methods** (persisted); simulated card payments for demos; **Stripe Checkout** integration and **Stripe Connect** OAuth (local hosted callback) where keys are present; billing validation helpers.
- **Invoices** surfaced in Account Center billing tab; receipt PDF generation for ledger records.

### Account Center (guest primary; staff may open from dashboards)

- **Account information:** name, email display, role, **profile photo** upload/remove with managed storage path, save profile.
- **Security tab** (extracted panel): change password, sign-in and alert preferences, session/device hints, **sign-in audit history** (local preferences), forgot-password entry point, activity-style copy.
- **Notifications:** per-**NotificationType** toggles for email and in-app, grouped by category (reservations, charges, shop, offers, messaging, security, on-property, experiences, system).
- **My reservations** snapshot inside account center.
- **Billing** tab: reservation and shop rollups, invoice list, receipt actions.
- **Payment** tab: Stripe Connect banner and actions, balance snapshot, quick pay paths, **add/remove saved cards**, deep link support from guest billing page.
- **Danger zone:** account deletion with email verification step.
- **Back** navigation respects role (guest home, clerk page, admin page).

### Administration

- **Admin dashboard:** executive KPI strip (accounts, clerks, guests, active reservations, rooms, ledger total, shop order count, low-stock SKUs); large **toolkit** grid (create staff, reset password, billing workspace, open clerk console, room editor, reservation ledger, check-in, shop preview, booking funnel, forgot-password preview, night audit snapshot, auto check-out, operations directory, integrations dialog); **staff roster**; **recent reservations** list; **retail spotlight** (shop revenue line, low-stock list); **command strip** (refresh, copy executive summary, guest home preview, sign out).
- **Integrations dialog:** Java version, working directory, SQLite file presence, Mailgun and Stripe configuration flags.

### Services and domain (backend-facing)

- **Accounts:** DAO-backed CRUD, hashing, verification fields, Mailgun-backed email when configured.
- **Reservations:** DAO, status lifecycle, join with rooms, nightly totals.
- **Shop:** inventory DAO, orders DAO, checkout with stock decrement and **NotificationService** publishes (purchase, order status, dining, promos on threshold, special amenity keywords).
- **BillingService** / **GuestLedgerService** / **GuestPaymentDAO** / **SavedPaymentMethodDAO** for money and cards.
- **SignInAuditService:** install device id, sign-in history lines, optional security notification hooks.
- **PasswordStrengthEvaluator** shared with UI formatters.

### Tests

- JUnit 5 unit tests under `src/test/java` (e.g. cart and item model behavior).

---

## Stack (Maven coordinates)

SQLite JDBC, JCalendar, java-dotenv, Stripe Java SDK, Apache PDFBox, SLF4J simple, JUnit Jupiter.

## Configuration note

Stripe and Mailgun are optional. The UI branches on `StripeConfig` and environment presence so missing keys do not hard-fail the client.

---

*Demonstration / coursework codebase. Not a production property-management system.*
