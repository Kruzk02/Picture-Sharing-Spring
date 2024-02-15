create table if not exists  Boards (
	id int auto_increment primary key,
    user_id int,
    board_name varchar(255) not null,
    create_at timestamp default current_timestamp,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);