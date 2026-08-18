CREATE TABLE user_follows (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    followed_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_user_follows_pair UNIQUE (follower_id, followed_id),
    CONSTRAINT ck_user_follows_not_self CHECK (follower_id <> followed_id)
);

CREATE INDEX idx_user_follows_follower ON user_follows (follower_id, created_at DESC);
CREATE INDEX idx_user_follows_followed ON user_follows (followed_id, created_at DESC);

ALTER TABLE notifications ALTER COLUMN post_id DROP NOT NULL;
