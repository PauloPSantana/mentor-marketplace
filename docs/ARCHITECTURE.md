# Arquitetura

## Estratégia
Monólito Modular com separação por domínio.

```text
Frontend (Next.js)
        |
        v
REST API (Spring Boot)
        |
        +--> Identity/Auth
        +--> Mentors
        +--> Mentees
        +--> Mentorships
        +--> Enrollments
        +--> Scheduling
        +--> Reviews
        +--> Subscriptions
        +--> Payments
        +--> Search
        +--> Notifications
        +--> AI
        +--> Admin
        |
        v
PostgreSQL
```

## Padrão interno por módulo

```text
module
├── api
├── application
├── domain
└── infrastructure
```

### api
Controllers e DTOs.

### application
Use cases / serviços de aplicação.

### domain
Entidades, value objects e regras.

### infrastructure
JPA, gateways, clients e configurações.

## Princípios
- SOLID;
- Clean Architecture pragmática;
- domínio sem dependência de framework quando possível;
- DTOs não são entidades;
- controllers não contêm regra de negócio;
- repositories ficam atrás de interfaces;
- migrations versionadas com Flyway;
- testes de unidade e integração.
