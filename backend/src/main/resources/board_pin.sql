CREATE TABLE IF NOT EXISTS board_pin (
    board_id INT,
    pin_id INT,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE
);