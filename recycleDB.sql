CREATE TABLE recycle_analysis_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    confidence DOUBLE NOT NULL,
    disposal_method VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);