package com.gateentry.ui;

import com.gateentry.dao.ActivityLogDao;
import com.gateentry.dao.UserDao;
import com.gateentry.model.Role;
import com.gateentry.model.Status;
import com.gateentry.model.User;
import java.util.Optional;
import java.util.Scanner;

/** Main menu shown before login: Register, Login, Exit. */
public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== SUBDIVISION GATE ENTRY SYSTEM ===");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register Account ---");
        String fullName = prompt("Full name: ");
        String username = prompt("Username: ");
        String password = prompt("Password (min 6 chars): ");

        if (fullName.isEmpty() || username.isEmpty()) {
            System.out.println("Full name and username are required.");
            return;
        }
        if (password.length() < 6) {
            System.out.println("Password must be at least 6 characters.");
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        if (userDao.register(username, password, fullName, role, Status.PENDING)) {
            logDao.log(null, "REGISTER", "New " + role + " account '" + username + "' (PENDING)");
            System.out.println("Registered! Your account is PENDING until an admin approves it.");
        } else {
            System.out.println("Username already taken.");
        }
    }

    private Role chooseRole() {
        System.out.println("Select role: 1) GUARD  2) RESIDENT  3) ADMIN");
        System.out.print("Role: ");
        return switch (scanner.nextLine().trim()) {
            case "1" -> Role.GUARD;
            case "2" -> Role.RESIDENT;
            case "3" -> Role.ADMIN;
            default -> null;
        };
    }

    private void login() {
        System.out.println("\n--- Login ---");
        String username = prompt("Username: ");
        String password = prompt("Password: ");

        Optional<User> result = userDao.authenticate(username, password);
        if (result.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }

        User user = result.get();
        switch (user.status()) {
            case PENDING -> System.out.println("Your account is still PENDING admin approval.");
            case REJECTED -> System.out.println("Your account was REJECTED. Contact the HOA office.");
            case ACTIVE -> {
                logDao.log(user.id(), "LOGIN", user.username() + " logged in");
                new UserMenu(user, scanner, userDao, logDao).run();
                logDao.log(user.id(), "LOGOUT", user.username() + " logged out");
            }
        }
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}