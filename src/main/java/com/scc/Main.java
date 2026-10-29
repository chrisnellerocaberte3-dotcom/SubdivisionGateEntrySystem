package com.scc;

import com.gateentry.dao.DatabaseConnection;
import com.gateentry.dao.UserDao;
import com.gateentry.model.Role;
import com.gateentry.model.Status;
import com.gateentry.ui.MainMenu;

/** Application entry point. */
public class Main {

    public static void main(String[] args) {
        DatabaseConnection.initialize();
        seedDefaultAdmin();
        new MainMenu().run();
    }

    /** Creates a first ACTIVE admin so someone can approve other accounts. */
    private static void seedDefaultAdmin() {
        UserDao userDao = new UserDao();
        if (userDao.findByUsername("admin").isEmpty()) {
            userDao.register("admin", "admin123", "HOA Administrator", Role.ADMIN, Status.ACTIVE);
            System.out.println("[Setup] Default admin created -> username: admin / password: admin123");
        }
    }
}