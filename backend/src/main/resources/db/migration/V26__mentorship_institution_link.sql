ALTER TABLE mentorships
    ALTER COLUMN enrollment_id DROP NOT NULL;

ALTER TABLE mentorships
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE mentorships
    ADD COLUMN institution_id UUID REFERENCES institutions (id);

ALTER TABLE mentorships
    ADD COLUMN program VARCHAR(160);

CREATE INDEX idx_mentorships_institution ON mentorships (institution_id);

UPDATE mentorships m
SET institution_id = (
    SELECT p.institution_id
    FROM mentor_profiles p
    WHERE p.id = m.mentor_profile_id
);
