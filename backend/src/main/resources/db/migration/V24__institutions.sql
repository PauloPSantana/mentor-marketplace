CREATE TABLE institutions (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    owner_user_id UUID NOT NULL UNIQUE REFERENCES users (id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE mentor_invitations (
    id UUID PRIMARY KEY,
    institution_id UUID NOT NULL REFERENCES institutions (id) ON DELETE CASCADE,
    invited_by_user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    specialty VARCHAR(160),
    program VARCHAR(160),
    token VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_user_id UUID REFERENCES users (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentor_invitations_institution ON mentor_invitations (institution_id);
CREATE INDEX idx_mentor_invitations_email ON mentor_invitations (email);

ALTER TABLE mentor_profiles
    ADD COLUMN institution_id UUID REFERENCES institutions (id);

CREATE INDEX idx_mentor_profiles_institution ON mentor_profiles (institution_id);
