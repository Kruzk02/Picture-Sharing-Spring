CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profilePicture VARCHAR(255) NOT NULL ,
    bio TEXT,
    gender ENUM('male', 'female', 'other') NOT NULL,
    enable BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);