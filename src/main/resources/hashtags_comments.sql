CREATE TABLE IF NOT EXISTS hashtags_comments(
    hashtag_id INT,
    comment_id INT,
    FOREIGN KEY (hashtag_id) REFERENCES hashtags(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);