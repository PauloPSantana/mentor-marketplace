CREATE TABLE mentorship_payments (
    id UUID PRIMARY KEY,
    mentorship_id UUID NOT NULL REFERENCES mentorships (id),
    payer_user_id UUID NOT NULL REFERENCES users (id),
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_transaction_id VARCHAR(120) UNIQUE,
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL UNIQUE,
    paid_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_mentorship_payments_mentorship ON mentorship_payments (mentorship_id, status);

CREATE TABLE mentorship_reviews (
    id UUID PRIMARY KEY,
    mentorship_id UUID NOT NULL REFERENCES mentorships (id),
    reviewer_user_id UUID NOT NULL REFERENCES users (id),
    reviewed_user_id UUID NOT NULL REFERENCES users (id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_mentorship_reviewer UNIQUE (mentorship_id, reviewer_user_id)
);

CREATE INDEX idx_mentorship_reviews_reviewed ON mentorship_reviews (reviewed_user_id, status);
CREATE INDEX idx_mentorship_reviews_mentorship ON mentorship_reviews (mentorship_id);
