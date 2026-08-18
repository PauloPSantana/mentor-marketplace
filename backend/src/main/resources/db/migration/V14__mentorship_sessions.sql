ALTER TABLE mentorships
    ADD COLUMN paused_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE mentorships
    ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE mentorships
    ADD COLUMN status_changed_by_user_id UUID REFERENCES users (id);

UPDATE mentorships
SET status_changed_by_user_id = COALESCE(status_changed_by_user_id, mentor_user_id);

CREATE TABLE mentorship_sessions (
    id UUID PRIMARY KEY,
    mentorship_id UUID NOT NULL REFERENCES mentorships (id),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    meeting_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(2000),
    created_by_user_id UUID NOT NULL REFERENCES users (id),
    completed_at TIMESTAMP WITH TIME ZONE,
    completed_by_user_id UUID REFERENCES users (id),
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancelled_by_user_id UUID REFERENCES users (id),
    cancel_reason VARCHAR(500),
    reminder_24h_sent_at TIMESTAMP WITH TIME ZONE,
    reminder_1h_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentorship_sessions_mentorship ON mentorship_sessions (mentorship_id);
CREATE INDEX idx_mentorship_sessions_schedule ON mentorship_sessions (scheduled_at, status);
