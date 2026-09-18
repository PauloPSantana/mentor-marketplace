ALTER TABLE mentorship_sessions
    ADD COLUMN reminder_10m_sent_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE mentorship_sessions
    ADD COLUMN zoom_meeting_id VARCHAR(64);

ALTER TABLE mentorship_sessions
    ADD COLUMN zoom_start_url VARCHAR(1000);

ALTER TABLE mentorship_sessions
    ADD COLUMN zoom_status VARCHAR(20);

ALTER TABLE mentorship_sessions
    ADD COLUMN zoom_started_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE mentorship_sessions
    ADD COLUMN zoom_ended_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_mentorship_sessions_zoom_meeting
    ON mentorship_sessions (zoom_meeting_id);

CREATE TABLE zoom_connections (
    user_id UUID PRIMARY KEY REFERENCES users (id),
    zoom_user_id VARCHAR(64) NOT NULL,
    zoom_email VARCHAR(180),
    access_token VARCHAR(2000) NOT NULL,
    refresh_token VARCHAR(2000) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
