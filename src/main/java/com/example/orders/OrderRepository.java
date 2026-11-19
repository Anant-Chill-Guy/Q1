package com.example.orders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final Connection connection;

    public OrderRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean insert(Order order) throws SQLException {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, order.id());
            statement.setString(2, order.customerName());
            statement.setString(3, order.productName());
            statement.setInt(4, order.quantity());
            statement.setDouble(5, order.unitPrice());
            statement.setString(6, order.status());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                return false;
            }
            throw exception;
        }
    }

    public List<Order> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet result = statement.executeQuery()) {
                return readAll(result);
            }
        }
    }

    public List<Order> findAll() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Orders ORDER BY order_id");
             ResultSet result = statement.executeQuery()) {
            return readAll(result);
        }
    }

    public Order findById(int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Orders WHERE order_id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readOrder(result) : null;
            }
        }
    }

    public boolean increaseQuantity(int id, int additionalQuantity) throws SQLException {
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, additionalQuantity);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE Orders SET status = ? WHERE order_id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public double totalValue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM Orders";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getDouble(1);
        }
    }

    public boolean deleteCancelled(int id) throws SQLException {
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private List<Order> readAll(ResultSet result) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (result.next()) {
            orders.add(readOrder(result));
        }
        return orders;
    }

    private Order readOrder(ResultSet result) throws SQLException {
        return new Order(
                result.getInt("order_id"),
                result.getString("customer_name"),
                result.getString("product_name"),
                result.getInt("quantity"),
                result.getDouble("unit_price"),
                result.getString("status")
        );
    }
}
