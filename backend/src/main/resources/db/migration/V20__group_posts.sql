CREATE TABLE group_announcements (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES mentorship_groups (id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(180),
    body VARCHAR(5000) NOT NULL,
    audience_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_group_announcements_group ON group_announcements (group_id, created_at);

CREATE TABLE group_announcement_recipients (
    id UUID PRIMARY KEY,
    announcement_id UUID NOT NULL REFERENCES group_announcements (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    UNIQUE (announcement_id, user_id)
);

CREATE INDEX idx_group_announcement_recipients_user ON group_announcement_recipients (user_id);

CREATE TABLE group_announcement_likes (
    id UUID PRIMARY KEY,
    announcement_id UUID NOT NULL REFERENCES group_announcements (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (announcement_id, user_id)
);

CREATE TABLE group_announcement_comments (
    id UUID PRIMARY KEY,
    announcement_id UUID NOT NULL REFERENCES group_announcements (id) ON DELETE CASCADE,
    parent_comment_id UUID REFERENCES group_announcement_comments (id),
    author_user_id UUID NOT NULL REFERENCES users (id),
    content VARCHAR(1000) NOT NULL,
    author_name VARCHAR(120) NOT NULL,
    author_photo_url VARCHAR(500),
    author_role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_group_announcement_comments_announcement ON group_announcement_comments (announcement_id, created_at);
