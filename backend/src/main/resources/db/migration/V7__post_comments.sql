CREATE TABLE post_comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    parent_comment_id UUID REFERENCES post_comments (id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES users (id),
    content TEXT NOT NULL,
    author_name VARCHAR(120) NOT NULL,
    author_photo_url VARCHAR(500),
    author_headline VARCHAR(180),
    author_role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_post_comments_post_id ON post_comments (post_id);
CREATE INDEX idx_post_comments_parent_id ON post_comments (parent_comment_id);
CREATE INDEX idx_post_comments_created_at ON post_comments (created_at);
