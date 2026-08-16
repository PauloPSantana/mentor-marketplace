# Prompts para usar no Cursor IDE

## Prompt mestre
Você é um arquiteto de software sênior especializado em Java, Spring Boot, Next.js e SaaS.

Estamos construindo o MentorHub AI, um marketplace de mentorias.

Antes de gerar código:
1. leia README.md;
2. leia docs/PRODUCT.md;
3. leia docs/ARCHITECTURE.md;
4. leia docs/API.md;
5. leia docs/DATABASE.md;
6. mantenha arquitetura de monólito modular;
7. aplique SOLID;
8. não coloque regra de negócio em controllers;
9. escreva testes;
10. mantenha compatibilidade com Java 21+ e Spring Boot 3.

Sempre apresente:
- arquivos que serão criados/alterados;
- justificativa arquitetural;
- implementação;
- testes;
- como executar e validar.

## Prompt 1 — criar backend
Crie o projeto backend Spring Boot conforme os documentos do repositório. Use Maven, Java 21+, PostgreSQL, Flyway, Spring Security, JWT, Validation e OpenAPI. Crie os pacotes por módulo e um endpoint /actuator/health.

## Prompt 2 — autenticação
Implemente o módulo identity com cadastro, login JWT, roles MENTOR/MENTEE/ADMIN e endpoint /api/v1/auth/me. Inclua migrations, testes unitários e testes de integração.

## Prompt 3 — mentor
Implemente o módulo mentors com perfil, bio, headline, anos de experiência, skills e selo de verificação. Usuário MENTOR só pode editar o próprio perfil.

## Prompt 4 — mentorias
Implemente o módulo mentorships. O mentor pode criar produto em DRAFT, publicar, pausar, editar e arquivar. Valide preço, vagas, duração e sessões.

## Prompt 5 — marketplace
Implemente catálogo público com paginação, pesquisa por texto e filtros por categoria, nível, faixa de preço, avaliação e skill.

## Prompt 6 — contratação
Implemente enrollments com estados PENDING, ACTIVE, COMPLETED e CANCELLED. Armazene snapshot financeiro no momento da contratação.

## Prompt 7 — avaliações
Implemente avaliações somente para enrollments COMPLETED. Cada enrollment permite apenas uma avaliação.

## Prompt 8 — frontend
Crie frontend Next.js + TypeScript + Tailwind com:
- home;
- busca;
- página do mentor;
- página da mentoria;
- login/cadastro;
- dashboard mentor;
- dashboard mentorado;
- criação/edição de mentoria.

## Prompt 9 — qualidade
Revise todo o código verificando SOLID, segurança, validações, N+1 queries, transações, tratamento global de erros, logs, cobertura de testes e consistência REST.
