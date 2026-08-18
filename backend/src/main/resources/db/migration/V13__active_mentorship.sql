ALTER TABLE enrollments
    ADD COLUMN responded_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE enrollments
    ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE enrollments
    ALTER COLUMN message VARCHAR(2000);

UPDATE enrollments
SET status = 'ACCEPTED',
    responded_at = COALESCE(responded_at, updated_at)
WHERE status = 'ACTIVE';

CREATE TABLE mentorships (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL UNIQUE REFERENCES enrollments (id),
    mentee_user_id UUID NOT NULL REFERENCES users (id),
    mentor_profile_id UUID NOT NULL REFERENCES mentor_profiles (id),
    mentor_user_id UUID NOT NULL REFERENCES users (id),
    product_id UUID NOT NULL REFERENCES mentorship_products (id),
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentorships_mentee ON mentorships (mentee_user_id);
CREATE INDEX idx_mentorships_mentor_user ON mentorships (mentor_user_id);

INSERT INTO mentorships (
    id,
    enrollment_id,
    mentee_user_id,
    mentor_profile_id,
    mentor_user_id,
    product_id,
    status,
    started_at,
    completed_at,
    created_at,
    updated_at
)
SELECT
    RANDOM_UUID(),
    e.id,
    e.mentee_user_id,
    p.mentor_id,
    mp.user_id,
    e.mentorship_id,
    CASE WHEN e.status = 'COMPLETED' THEN 'COMPLETED' ELSE 'ACTIVE' END,
    COALESCE(e.responded_at, e.updated_at),
    CASE WHEN e.status = 'COMPLETED' THEN e.updated_at ELSE NULL END,
    COALESCE(e.responded_at, e.updated_at),
    e.updated_at
FROM enrollments e
JOIN mentorship_products p ON p.id = e.mentorship_id
JOIN mentor_profiles mp ON mp.id = p.mentor_id
WHERE e.status IN ('ACCEPTED', 'COMPLETED');

