create table if not exists  Users (
	id int auto_increment primary key,
    username varchar(255) not null,
    email varchar(255) not null unique,
    password varchar(255) not null,
    create_at timestamp default current_timestamp
);