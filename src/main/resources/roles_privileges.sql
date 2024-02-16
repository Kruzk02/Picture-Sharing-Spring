Create Table if not exists roles_privileges (
	privileges_id int,
    roles_id int,
    FOREIGN KEY (privileges_id) REFERENCES privileges(id) ON DELETE CASCADE,
    FOREIGN KEY (roles_id) REFERENCES roles(id) ON DELETE CASCADE
);