CREATE DATABASE IF NOT EXISTS stock_analysis;
USE stock_analysis;

CREATE TABLE IF NOT EXISTS stock_evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(10) NOT NULL,
    headline TEXT NOT NULL,
    contents TEXT NOT NULL,
    url VARCHAR(255) NOT NULL,
    estimate_value DOUBLE NOT NULL,
    evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stock_code (stock_code)
);