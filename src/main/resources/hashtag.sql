CREATE TABLE IF NOT EXISTS hashtags(
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tag VARCHAR(69) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);