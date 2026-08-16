# Contrato inicial da API

## Auth
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me

## Mentors
GET  /api/v1/mentors
GET  /api/v1/mentors/{id}
GET  /api/v1/mentors/me
PUT  /api/v1/mentors/me

## Mentorships
GET    /api/v1/mentorships
GET    /api/v1/mentorships/{id}
POST   /api/v1/mentorships
PUT    /api/v1/mentorships/{id}
PATCH  /api/v1/mentorships/{id}/publish
PATCH  /api/v1/mentorships/{id}/pause
DELETE /api/v1/mentorships/{id}

## Enrollments
POST  /api/v1/mentorships/{id}/enrollments
GET   /api/v1/enrollments/me
PATCH /api/v1/enrollments/{id}/accept
PATCH /api/v1/enrollments/{id}/reject
PATCH /api/v1/enrollments/{id}/complete

## Sessions
GET  /api/v1/enrollments/{id}/sessions
POST /api/v1/enrollments/{id}/sessions
PUT  /api/v1/sessions/{id}

## Reviews
POST /api/v1/enrollments/{id}/reviews
GET  /api/v1/mentors/{id}/reviews

## Subscriptions
GET  /api/v1/plans
POST /api/v1/subscriptions

## Admin
GET   /api/v1/admin/metrics
GET   /api/v1/admin/users
PATCH /api/v1/admin/mentors/{id}/verify
