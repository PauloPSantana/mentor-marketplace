ALTER TABLE enrollments
    ADD COLUMN message VARCHAR(1000);

ALTER TABLE enrollments
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE INDEX idx_enrollments_mentee ON enrollments (mentee_user_id);
CREATE INDEX idx_enrollments_mentorship ON enrollments (mentorship_id);
CREATE INDEX idx_mentorship_products_mentor ON mentorship_products (mentor_id);
