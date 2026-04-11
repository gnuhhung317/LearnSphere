-- Add parent_id column if not exists
ALTER TABLE media_files ADD COLUMN IF NOT EXISTS parent_id VARCHAR(255);

-- Drop existing check constraint if exists
ALTER TABLE media_files DROP CONSTRAINT IF EXISTS media_files_file_type_check;

-- Add updated check constraint with FOLDER
ALTER TABLE media_files ADD CONSTRAINT media_files_file_type_check 
    CHECK (file_type IN ('IMAGE', 'DOCUMENT', 'VIDEO', 'AUDIO', 'OTHER', 'FOLDER'));

-- Create index on parent_id for faster folder lookups
CREATE INDEX IF NOT EXISTS idx_media_files_parent_id ON media_files(parent_id);
