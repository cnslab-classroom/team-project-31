CREATE DATABASE IF NOT EXISTS stockAnalysis;
USE stockAnalysis;

CREATE TABLE IF NOT EXISTS article_evaluations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    headline TEXT NOT NULL,
    contents TEXT NOT NULL,
    url VARCHAR(255) NOT NULL,
    estimate_value VARCHAR(10) NOT NULL,
    evaluation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_url (url)
);