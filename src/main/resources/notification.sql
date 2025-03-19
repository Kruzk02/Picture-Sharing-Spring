CREATE TABLE IF NOT EXISTS notifications(
    id INT auto_increment PRIMARY KEY,
    user_id INT NOT NULL,
    message VARCHAR(512) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX(user_id)
);