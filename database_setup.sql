CREATE DATABASE IF NOT EXISTS ecommerce_db;
CREATE USER IF NOT EXISTS 'user'@'localhost' IDENTIFIED BY '123456';
ALTER USER 'user'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'user'@'localhost';
CREATE USER IF NOT EXISTS 'user'@'127.0.0.1' IDENTIFIED BY '123456';
ALTER USER 'user'@'127.0.0.1' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'user'@'127.0.0.1';
USE ecommerce_db;
CREATE TABLE IF NOT EXISTS Orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL
);
