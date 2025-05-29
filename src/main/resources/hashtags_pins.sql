CREATE TABLE IF NOT EXISTS hashtags_pins(
    hashtag_id INT,
    pin_id INT,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE,
    FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE
);