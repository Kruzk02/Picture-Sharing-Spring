create table if not exists Pins (
	id int auto_increment primary key,
    user_id int,
    board_id int,
    file_name varchar(255) not null,
    image_url varchar(255) not null,
    description text,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (board_id) REFERENCES Boards(id) ON DELETE CASCADE
);