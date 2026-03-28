CREATE TABLE recycle_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    analysis_id BIGINT,
    disposal_category VARCHAR(255),
    disposal_method VARCHAR(255),
    category VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id)
);