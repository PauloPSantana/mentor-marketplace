CREATE TABLE mentorship_groups (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL REFERENCES users (id),
    mentor_user_id UUID NOT NULL REFERENCES users (id),
    product_id UUID REFERENCES mentorship_products (id),
    title VARCHAR(180) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentorship_groups_owner ON mentorship_groups (owner_user_id);
CREATE INDEX idx_mentorship_groups_mentor ON mentorship_groups (mentor_user_id);

CREATE TABLE mentorship_group_members (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES mentorship_groups (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id),
    role VARCHAR(20) NOT NULL,
    mentorship_id UUID REFERENCES mentorships (id),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (group_id, user_id)
);

CREATE INDEX idx_mentorship_group_members_user ON mentorship_group_members (user_id);
