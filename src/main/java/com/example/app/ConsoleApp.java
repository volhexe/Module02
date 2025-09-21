package com.example.app;

import com.example.dao.UserDaoImpl;
import com.example.model.User;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import com.example.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleApp.class);

    private final UserService userService;

    public ConsoleApp() {
        this.userService = new UserServiceImpl(new UserDaoImpl());
    }

    public static void main(String[] args) {
        new ConsoleApp().run();
    }

    private void run() {
        LOGGER.info("Starting user-service application");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createUser(scanner);
                    case "2" -> listUsers();
                    case "3" -> getUser(scanner);
                    case "4" -> updateUser(scanner);
                    case "5" -> deleteUser(scanner);
                    case "0" -> {
                        shutdown();
                        return;
                    }
                    default -> System.out.println("Unknown option. Try again.");
                }
            } catch (Exception e) {
                LOGGER.error("Operation failed", e);
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1) Create user");
        System.out.println("2) List all users");
        System.out.println("3) Get user by id");
        System.out.println("4) Update user");
        System.out.println("5) Delete user");
        System.out.println("0) Exit");
        System.out.print("Select: ");
    }

    private void createUser(Scanner scanner) throws Exception {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Age: ");
        Integer age = Integer.valueOf(scanner.nextLine().trim());

        User user = userService.createUser(name, email, age);
        System.out.println("Created: " + user);
    }

    private void listUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) System.out.println("No users found.");
        else users.forEach(System.out::println);
    }

    private void getUser(Scanner scanner) {
        System.out.print("Id: ");
        Long id = Long.valueOf(scanner.nextLine().trim());
        Optional<User> u = userService.getUserById(id);
        u.ifPresentOrElse(System.out::println, () -> System.out.println("User not found"));
    }

    private void updateUser(Scanner scanner) throws Exception {
        System.out.print("Id: ");
        Long id = Long.valueOf(scanner.nextLine().trim());
        Optional<User> opt = userService.getUserById(id);
        if (opt.isEmpty()) {
            System.out.println("User not found");
            return;
        }
        User current = opt.get();
        System.out.println("Current: " + current);

        System.out.print("New name (blank to keep): ");
        String name = scanner.nextLine();
        if (name.isBlank()) name = current.getName();

        System.out.print("New email (blank to keep): ");
        String email = scanner.nextLine();
        if (email.isBlank()) email = current.getEmail();

        System.out.print("New age (blank to keep): ");
        String ageStr = scanner.nextLine();
        Integer age = ageStr.isBlank() ? current.getAge() : Integer.valueOf(ageStr.trim());

        User updated = userService.updateUser(id, name, email, age);
        System.out.println("Updated: " + updated);
    }

    private void deleteUser(Scanner scanner) throws Exception {
        System.out.print("Id: ");
        Long id = Long.valueOf(scanner.nextLine().trim());
        boolean deleted = userService.deleteUser(id);
        System.out.println(deleted ? "Deleted" : "User not found");
    }

    private void shutdown() {
        System.out.println("Shutting down...");
        HibernateUtil.shutdown();
        LOGGER.info("Application stopped");
    }
}
