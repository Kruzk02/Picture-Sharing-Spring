CREATE TABLE IF NOT EXISTS Board_Pin (
    board_id INT,
    pin_id INT,
    FOREIGN KEY (board_id) REFERENCES Boards(id) ON DELETE CASCADE,
    FOREIGN KEY (pin_id) REFERENCES Pins(id) ON DELETE CASCADE
);