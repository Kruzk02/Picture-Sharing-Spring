CREATE TABLE IF NOT EXISTS roles_privileges (
    role_id int,
    privilege_id int,
    FOREIGN KEY (privilege_id) REFERENCES privileges(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);