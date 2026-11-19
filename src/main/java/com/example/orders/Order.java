package com.example.orders;

public record Order(
        int id,
        String customerName,
        String productName,
        int quantity,
        double unitPrice,
        String status
) {
    @Override
    public String toString() {
        return id + " | " + customerName + " | " + productName + " | "
                + quantity + " | " + unitPrice + " | " + status;
    }
}
