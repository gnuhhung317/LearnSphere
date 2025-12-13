-- Add room_type column to rooms table
ALTER TABLE rooms ADD COLUMN room_type VARCHAR(20) NOT NULL DEFAULT 'GROUP';

-- Create index for quick DM lookup
CREATE INDEX idx_rooms_type_members ON rooms(room_type);

-- Add unique constraint for DM rooms to prevent duplicates
-- We'll use application logic to ensure DM rooms are unique per user pair

COMMENT ON COLUMN rooms.room_type IS 'Types: GROUP, DIRECT_MESSAGE';
