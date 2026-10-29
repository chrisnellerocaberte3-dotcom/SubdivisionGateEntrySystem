package com.gateentry.ui;

import com.gateentry.dao.ActivityLogDao;
import com.gateentry.dao.UserDao;
import com.gateentry.model.Role;
import com.gateentry.model.Status;
import com.gateentry.model.User;
import java.util.List;
import java.util.Scanner;

/** Menu shown after a successful login. Options depend on the user's role. */
public class UserMenu {

    private final User user;
    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao logDao;

    public UserMenu(User user, Scanner scanner, UserDao userDao, ActivityLogDao logDao) {
        this.user = user;
        this.scanner = scanner;
        this.userDao = userDao;
        this.logDao = logDao;
    }

    public void run() {
        System.out.println("\nWelcome, " + user.fullName() + " [" + user.role() + "]");
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- " + user.role() + " MENU ---");
            if (user.role() == Role.ADMIN) {
                System.out.println("1. Review pending accounts");
            }
            System.out.println("0. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) {
                System.out.println("Logged out. Returning to main menu...");
                loggedIn = false;
            } else if (choice.equals("1") && user.role() == Role.ADMIN) {
                reviewPending();
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void reviewPending() {
        List<User> pending = userDao.findByStatus(Status.PENDING);
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.");
            return;
        }

        System.out.println("\nID | Username | Full name | Role");
        for (User u : pending) {
            System.out.printf("%d | %s | %s | %s%n", u.id(), u.username(), u.fullName(), u.role());
        }

        System.out.print("Enter user ID to review (blank to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        int id;
        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }
        if (pending.stream().noneMatch(u -> u.id() == id)) {
            System.out.println("That ID is not in the pending list.");
            return;
        }

        System.out.print("A = approve, R = reject: ");
        String decision = scanner.nextLine().trim().toUpperCase();
        Status newStatus = switch (decision) {
            case "A" -> Status.ACTIVE;
            case "R" -> Status.REJECTED;
            default -> null;
        };
        if (newStatus == null) {
            System.out.println("Cancelled.");
            return;
        }

        userDao.updateStatus(id, newStatus);
        logDao.log(user.id(), "ACCOUNT_" + newStatus, "User ID " + id + " set to " + newStatus);
        System.out.println("User " + id + " is now " + newStatus + ".");
    }
}