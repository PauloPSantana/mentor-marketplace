# MentorHub AI

Marketplace inteligente de mentorias onde mentores publicam produtos e mentorados pesquisam, contratam, agendam e acompanham evolução.

## Estado atual

Implementado (Sprint 0 + Sprint 1 + Sprint 2):
- backend Spring Boot modular;
- H2 (dev local) + PostgreSQL opcional via perfil + Flyway;
- OpenAPI/Swagger;
- autenticação JWT (register/login/me);
- perfil do mentor (skills, tecnologias, modalidade, valor da sessão, links);
- frontend Next.js com home, busca, login, cadastro e dashboard do mentor com edição de perfil.

## Stack
- Backend: Java 17+ (preferível 21), Spring Boot 3.5, Security, JPA, Flyway, JWT, springdoc
- Frontend: Next.js 15, TypeScript, Tailwind CSS 4
- Banco local: H2 (arquivo em `backend/data/`)
- Infra opcional: Docker Compose (PostgreSQL 16)

## Subir o ambiente

### 1. Backend (H2 por padrão — sem Docker)
```bash
cd backend
# Windows PowerShell (ajuste o path do JDK):
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
mvn spring-boot:run
```

- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/mentorhub`
  - User: `sa` / senha vazia

#### PostgreSQL (opcional)
```bash
docker compose up -d
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 2. Frontend
Requer Node.js 20+.
```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:3000

## Auth (API)

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
```

Exemplo de cadastro:
```json
{
  "name": "Ana Mentor",
  "email": "ana@email.com",
  "password": "senha12345",
  "role": "MENTOR"
}
```

Roles públicas: `MENTOR`, `MENTEE`. `ADMIN` não pode ser criado pelo cadastro público.

## Próximos passos (roadmap)
1. Sprint 3 — produtos de mentoria
2. Sprint 4 — marketplace/busca
3. Sprint 5+ — contratação, agenda, avaliações, planos e IA

### Mentors (API)
```http
GET  /api/v1/mentors
GET  /api/v1/mentors/{id}
GET  /api/v1/mentors/me
PUT  /api/v1/mentors/me
```

Use `docs/CURSOR_PROMPTS.md` para continuar a implementação no Cursor.
