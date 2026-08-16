CREATE TABLE plans (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    billing_cycle VARCHAR(30) NOT NULL,
    price NUMERIC(12,2) NOT NULL
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    mentor_id UUID NOT NULL REFERENCES mentor_profiles(id),
    plan_id UUID NOT NULL REFERENCES plans(id),
    status VARCHAR(30) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE
);

INSERT INTO plans (id, code, name, billing_cycle, price) VALUES
    ('11111111-1111-1111-1111-111111111111', 'FREE', 'Free', 'NONE', 0.00),
    ('22222222-2222-2222-2222-222222222222', 'PRO_MONTHLY', 'Pro Mensal', 'MONTHLY', 79.90),
    ('33333333-3333-3333-3333-333333333333', 'PARTNER_YEARLY', 'Parceiro Anual', 'YEARLY', 999.00);
