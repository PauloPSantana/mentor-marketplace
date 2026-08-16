CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE mentor_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    headline VARCHAR(180),
    bio TEXT,
    years_experience INTEGER,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating_avg NUMERIC(3,2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE mentor_skills (
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id),
    skill VARCHAR(100) NOT NULL,
    PRIMARY KEY (mentor_id, skill)
);

CREATE TABLE mentorship_products (
    id UUID PRIMARY KEY,
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id),
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    level VARCHAR(50),
    duration_weeks INTEGER NOT NULL,
    sessions_count INTEGER NOT NULL,
    max_students INTEGER NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    mentorship_id UUID NOT NULL REFERENCES mentorship_products(id),
    mentee_user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    price_snapshot NUMERIC(12,2) NOT NULL,
    platform_fee NUMERIC(12,2) NOT NULL,
    mentor_amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    meeting_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    mentor_notes TEXT,
    mentee_notes TEXT
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL UNIQUE REFERENCES enrollments(id),
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id),
    mentee_user_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
