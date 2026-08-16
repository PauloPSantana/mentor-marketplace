CREATE TABLE posts (
    id UUID PRIMARY KEY,
    author_user_id UUID NOT NULL REFERENCES users (id),
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    author_name VARCHAR(120) NOT NULL,
    author_photo_url VARCHAR(500),
    author_headline VARCHAR(180),
    author_role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
