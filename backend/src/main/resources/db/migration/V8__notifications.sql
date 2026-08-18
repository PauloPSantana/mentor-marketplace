CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES users (id),
    actor_user_id UUID NOT NULL REFERENCES users (id),
    type VARCHAR(30) NOT NULL,
    post_id UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    comment_id UUID REFERENCES post_comments (id) ON DELETE CASCADE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notifications_recipient_created ON notifications (recipient_user_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_read ON notifications (recipient_user_id, read_at);
