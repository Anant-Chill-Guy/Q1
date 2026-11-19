package com.example.orders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class OrderManagementApp {
    private static final List<String> STATUSES = List.of("Pending", "Shipped", "Delivered", "Cancelled");

    public static void main(String[] args) {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/ecommerce_db");
        String username = System.getenv().getOrDefault("DB_USER", "user");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "123456");

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Scanner scanner = new Scanner(System.in)) {
            OrderRepository repository = new OrderRepository(connection);
            while (true) {
                menu();
                int choice = readInt(scanner, "Choice: ");
                try {
                    switch (choice) {
                        case 1 -> seed(repository);
                        case 2 -> insert(repository, scanner);
                        case 3 -> search(repository, scanner);
                        case 4 -> increase(repository, scanner);
                        case 5 -> changeStatus(repository, scanner);
                        case 6 -> System.out.println("Total order value: " + repository.totalValue());
                        case 7 -> delete(repository, scanner);
                        case 8 -> show(repository.findAll());
                        case 0 -> { return; }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException exception) {
                    System.err.println(exception.getMessage());
                }
            }
        } catch (SQLException exception) {
            System.err.println("Database connection failed: " + exception.getMessage());
        }
    }

    private static void menu() {
        System.out.println("\n1. Insert 3 sample orders");
        System.out.println("2. Insert an order");
        System.out.println("3. Filter by status, quantity > 2, price descending");
        System.out.println("4. Increase quantity");
        System.out.println("5. Update status");
        System.out.println("6. Total order value");
        System.out.println("7. Delete cancelled order");
        System.out.println("8. Show all orders");
        System.out.println("0. Exit");
    }

    private static void seed(OrderRepository repository) throws SQLException {
        List<Order> samples = List.of(
                new Order(101, "Aisha", "Keyboard", 3, 1200, "Pending"),
                new Order(102, "Rohit", "Mouse", 5, 650, "Shipped"),
                new Order(103, "Neha", "Monitor", 1, 9500, "Cancelled")
        );
        for (Order order : samples) {
            System.out.println(repository.insert(order)
                    ? "Inserted order " + order.id()
                    : "Duplicate order ID " + order.id());
        }
    }

    private static void insert(OrderRepository repository, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Order ID: ");
        String customer = readText(scanner, "Customer name: ");
        String product = readText(scanner, "Product name: ");
        int quantity = readInt(scanner, "Quantity: ");
        double price = readDouble(scanner, "Unit price: ");
        String status = validStatus(readText(scanner, "Status: "));
        if (id <= 0 || customer.isBlank() || product.isBlank() || quantity < 0 || price < 0 || status == null) {
            System.out.println("Invalid order details.");
            return;
        }
        Order order = new Order(id, customer, product, quantity, price, status);
        System.out.println(repository.insert(order) ? "Order inserted." : "Duplicate order ID.");
    }

    private static void search(OrderRepository repository, Scanner scanner) throws SQLException {
        String status = validStatus(readText(scanner, "Status: "));
        if (status == null) {
            System.out.println("Invalid status.");
            return;
        }
        show(repository.findByStatus(status));
    }

    private static void increase(OrderRepository repository, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Order ID: ");
        int extra = readInt(scanner, "Additional quantity: ");
        if (extra <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }
        if (repository.increaseQuantity(id, extra)) {
            System.out.println(repository.findById(id));
        } else {
            System.out.println("Order not found.");
        }
    }

    private static void changeStatus(OrderRepository repository, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Order ID: ");
        String status = validStatus(readText(scanner, "New status: "));
        if (status == null) {
            System.out.println("Invalid status.");
            return;
        }
        if (repository.findById(id) == null) {
            System.out.println("Order not found.");
            return;
        }
        repository.updateStatus(id, status);
        System.out.println(repository.findById(id));
    }

    private static void delete(OrderRepository repository, Scanner scanner) throws SQLException {
        int id = readInt(scanner, "Order ID: ");
        Order order = repository.findById(id);
        if (order == null) {
            System.out.println("Order not found.");
        } else if (!order.status().equals("Cancelled")) {
            System.out.println("Only cancelled orders can be deleted.");
        } else {
            System.out.println(repository.deleteCancelled(id) ? "Order deleted." : "Could not delete order.");
        }
    }

    private static void show(List<Order> orders) {
        if (orders.isEmpty()) {
            System.out.println("No matching orders.");
            return;
        }
        orders.forEach(System.out::println);
    }

    private static String validStatus(String input) {
        return STATUSES.stream()
                .filter(status -> status.equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(scanner, prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readText(scanner, prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static String readText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
