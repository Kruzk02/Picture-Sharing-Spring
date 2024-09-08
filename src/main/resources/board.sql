CREATE TABLE IF NOT EXISTS boards (
	id int auto_increment primary key,
    user_id int,
    board_name varchar(255) not null,
    create_at timestamp default current_timestamp,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);