ALTER TABLE post_comments
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE post_comments
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE post_comments
SET updated_at = created_at
WHERE updated_at IS NULL;

CREATE INDEX idx_post_comments_status ON post_comments (status);
