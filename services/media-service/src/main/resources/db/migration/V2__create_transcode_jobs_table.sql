CREATE TABLE IF NOT EXISTS transcode_jobs (
    id VARCHAR(255) PRIMARY KEY,
    file_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress INT DEFAULT 0,
    requested_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message VARCHAR(1000),
    FOREIGN KEY (file_id) REFERENCES media_files(id) ON DELETE CASCADE
);

CREATE INDEX idx_transcode_jobs_file_id ON transcode_jobs(file_id);
CREATE INDEX idx_transcode_jobs_status ON transcode_jobs(status);
