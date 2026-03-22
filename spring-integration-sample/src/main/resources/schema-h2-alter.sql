-- Migration: drop validation_status (single status column)
-- For existing DBs, run these before dropping: UPDATE file_metadata SET status='DOWNLOADED' WHERE status='COMPLETED';
-- UPDATE file_metadata SET status='DOWNLOAD_FAILED' WHERE status='FAILED'; UPDATE file_metadata SET status='VALIDATED' WHERE validation_status='VALIDATED'; etc.
ALTER TABLE file_metadata DROP COLUMN IF EXISTS validation_status;

-- Migration: add last_modified for optimistic locking (timestamp @Version)
ALTER TABLE file_metadata ADD COLUMN IF NOT EXISTS last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE file_metadata DROP COLUMN IF EXISTS version;
