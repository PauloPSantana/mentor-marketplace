ALTER TABLE mentor_profiles ADD COLUMN photo_url VARCHAR(500);
ALTER TABLE mentor_profiles ADD COLUMN linkedin_url VARCHAR(500);
ALTER TABLE mentor_profiles ADD COLUMN github_url VARCHAR(500);
ALTER TABLE mentor_profiles ADD COLUMN session_price NUMERIC(12, 2);
ALTER TABLE mentor_profiles ADD COLUMN modality VARCHAR(30);
ALTER TABLE mentor_profiles ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE mentor_profiles ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE mentor_profiles ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE TABLE mentor_technologies (
    mentor_id UUID NOT NULL REFERENCES mentor_profiles (id),
    technology VARCHAR(100) NOT NULL,
    PRIMARY KEY (mentor_id, technology)
);
