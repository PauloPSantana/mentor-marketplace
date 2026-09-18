CREATE TABLE study_plans (
    id UUID PRIMARY KEY,
    mentorship_id UUID NOT NULL UNIQUE REFERENCES mentorships (id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE study_tasks (
    id UUID PRIMARY KEY,
    study_plan_id UUID NOT NULL REFERENCES study_plans (id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(2000),
    task_type VARCHAR(30) NOT NULL,
    order_number INT NOT NULL,
    due_date DATE,
    required BOOLEAN NOT NULL,
    resource_title VARCHAR(180),
    resource_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_study_tasks_plan ON study_tasks (study_plan_id, order_number);

CREATE TABLE study_task_progress (
    id UUID PRIMARY KEY,
    study_task_id UUID NOT NULL REFERENCES study_tasks (id) ON DELETE CASCADE,
    mentee_user_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (study_task_id, mentee_user_id)
);

CREATE INDEX idx_study_task_progress_mentee ON study_task_progress (mentee_user_id);
