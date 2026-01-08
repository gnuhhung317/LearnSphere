CREATE TABLE IF NOT EXISTS video_variants (
    id VARCHAR(255) PRIMARY KEY,
    job_id VARCHAR(255) NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    resolution VARCHAR(50),
    codec VARCHAR(50),
    bitrate INT,
    stored_filename VARCHAR(500),
    bucket_name VARCHAR(255),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES transcode_jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES media_files(id) ON DELETE CASCADE
);

CREATE INDEX idx_video_variants_job_id ON video_variants(job_id);
CREATE INDEX idx_video_variants_file_id ON video_variants(file_id);
CREATE INDEX idx_video_variants_status ON video_variants(status);
