# Modelo de Dados Inicial

## users
- id UUID PK
- name
- email UNIQUE
- password_hash
- role
- status
- created_at
- updated_at

## mentor_profiles
- id UUID PK
- user_id FK
- headline
- bio
- years_experience
- verified
- rating_avg
- rating_count

## mentor_skills
- mentor_id FK
- skill

## mentorship_products
- id UUID PK
- mentor_id FK
- title
- slug UNIQUE
- description
- category
- level
- duration_weeks
- sessions_count
- max_students
- price
- currency
- status
- created_at
- updated_at

## enrollments
- id UUID PK
- mentorship_id FK
- mentee_user_id FK
- status
- price_snapshot
- platform_fee
- mentor_amount
- created_at

## sessions
- id UUID PK
- enrollment_id FK
- scheduled_at
- duration_minutes
- meeting_url
- status
- mentor_notes
- mentee_notes

## reviews
- id UUID PK
- enrollment_id UNIQUE FK
- mentor_id FK
- mentee_user_id FK
- rating
- comment
- created_at

## plans
- id UUID PK
- code
- name
- billing_cycle
- price

## subscriptions
- id UUID PK
- mentor_id FK
- plan_id FK
- status
- starts_at
- expires_at
