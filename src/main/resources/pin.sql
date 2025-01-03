CREATE TABLE IF NOT EXISTS pins (
	id int auto_increment primary key,
    user_id int,
    description text,
    media_id int,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN kEY (media_id) REFERENCES media(id) ON DELETE CASCADE
);