CREATE TABLE IF NOT EXISTS users_roles(
	role_id int,
    user_id int,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);