CREATE TABLE IF NOT EXISTS media(
    id INT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500) NOT NULL,
    media_type ENUM('VIDEO', 'IMAGE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);