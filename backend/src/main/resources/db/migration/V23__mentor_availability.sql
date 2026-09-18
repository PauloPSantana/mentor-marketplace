CREATE TABLE mentor_availability_rules (
    id UUID PRIMARY KEY,
    mentor_profile_id UUID NOT NULL REFERENCES mentor_profiles (id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,
    buffer_minutes INTEGER NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentor_availability_profile ON mentor_availability_rules (mentor_profile_id);
