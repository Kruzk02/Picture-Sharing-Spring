CREATE TABLE IF NOT EXISTS pins (
	id int auto_increment primary key,
    user_id int,
    file_name varchar(255) not null,
    image_url varchar(255) not null,
    description text,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);