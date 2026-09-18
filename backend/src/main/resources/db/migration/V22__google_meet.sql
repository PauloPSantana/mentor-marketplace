CREATE TABLE google_connections (
    user_id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    google_user_id VARCHAR(128) NOT NULL,
    google_email VARCHAR(180),
    access_token VARCHAR(4000) NOT NULL,
    refresh_token VARCHAR(4000) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE mentorship_sessions ADD COLUMN meeting_provider VARCHAR(20);
ALTER TABLE mentorship_sessions ALTER COLUMN zoom_meeting_id VARCHAR(128);

UPDATE mentorship_sessions
SET meeting_provider = 'ZOOM'
WHERE zoom_meeting_id IS NOT NULL;
