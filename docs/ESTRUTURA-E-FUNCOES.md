# MentorHub AI — Estrutura e funções do sistema

Documento de referência da plataforma: o que existe, como está organizado e o que cada papel consegue fazer.

**Stack:** monólito modular Java 21 + Spring Boot 3 (backend na porta 8080) e Next.js 15 (frontend na porta 3000).  
**Banco:** H2 local (`backend/data/mentorhub`) ou PostgreSQL. Schema via Flyway.  
**Auth:** JWT + papéis. Senhas com BCrypt. IDs UUID.

---

## 1. Visão geral

O MentorHub une **marketplace de mentorias** (mentor publica produto, mentorado solicita e contrata) e **operação institucional** (instituição convida mentores, vincula mentorados a programas e acompanha sessões).

Há três painéis:

| Papel | Rota inicial | Foco |
|---|---|---|
| Instituição | `/dashboard/instituicao` | Resumo executivo e gestão |
| Mentor | `/dashboard/mentor` | Trabalho com mentorados |
| Mentorado | `/dashboard/mentorado` | Jornada e progresso |

Regra de navegação: **Dashboard = resumo**. Cadastro, agenda, convites, mentorias e relatórios ficam em telas próprias.

---

## 2. Arquitetura

Repositório em monorepo:

```
mentor-marketplace/
├── backend/     Spring Boot (API REST /api/v1)
├── frontend/    Next.js App Router
├── docs/        Documentação
└── docker-compose.yml   PostgreSQL opcional
```

Cada módulo de domínio no backend segue:

```
api/            Controllers e DTOs HTTP
application/    Casos de uso e eventos
domain/         Entidades, regras e repositórios
infrastructure/ JPA, integrações externas
```

Controllers só orquestram HTTP. Regras ficam em domain/application. Entidades JPA não saem na API.

### Módulos de domínio

| Módulo | Função |
|---|---|
| `identity` | Usuários, cadastro, login, foto, LinkedIn |
| `mentors` | Perfil profissional do mentor |
| `institutions` | Instituição, convites de mentor |
| `mentorships` | Produto de mentoria, vínculo Mentor ↔ Mentorado, sessões, agenda |
| `enrollments` | Solicitações/matrículas do marketplace |
| `availability` | Disponibilidade e reserva de horários |
| `studyplans` | Plano de estudos e tarefas da sala |
| `payments` | Pagamentos da mentoria |
| `reviews` | Avaliações após conclusão |
| `feed` | Posts, likes, comentários |
| `social` | Seguir e bloquear usuários |
| `groups` | Grupos de mentoria |
| `announcements` | Comunicados do grupo |
| `notifications` | Notificações in-app |
| `scheduling` | Google Calendar/Meet e Zoom |
| `shared` | Segurança JWT, erros, CORS |

---

## 3. Papéis e permissões

| Papel | Quem é | O que pode |
|---|---|---|
| `MENTEE` | Mentorado | Buscar mentores, solicitar mentoria, acompanhar jornada, sessões, plano e tarefas |
| `MENTOR` | Mentor | Editar perfil, aceitar pedidos, vincular mentorados, agendar, concluir sessões, publicar no feed |
| `INSTITUTION` | Gestor da instituição | Convidar mentores, vincular mentorado a mentor/programa, ver agenda e indicadores da instituição |
| `ADMIN` | Interno | Não é criado pelo cadastro público |

Regras importantes:

- Mentor não edita dados de outro mentor.
- Mentorado não cria produto de mentoria.
- Instituição só vê mentorias da própria instituição.
- Participantes da mentoria (mentor/mentorado) acessam a sala; instituição também pode consultar o vínculo se for o dono.

---

## 4. Entidade central: Mentoria (vínculo)

`Mentorship` é o elo operacional **Mentor ↔ Mentorado**.

Pode nascer de duas formas:

1. **Marketplace:** pedido do mentorado + produto → `Mentorship.start(...)` com enrollment e product.
2. **Instituição/mentor:** `Mentorship.assign(...)` com e-mail do mentorado, programa e (no caso da instituição) mentor escolhido. Enrollment/product podem ser nulos.

Campos relevantes: `institutionId`, `program`, status, datas de início/pausa/conclusão/cancelamento, próxima sessão.

**Status da mentoria:** `PENDING` → `ACTIVE` → `PAUSED` | `COMPLETED` | `CANCELLED`

Não pode existir outro vínculo aberto (`PENDING`/`ACTIVE`/`PAUSED`) para o mesmo par mentor + mentorado.

---

## 5. Funções por papel

### 5.1 Instituição

**Dashboard** (`/dashboard/instituicao`)  
Cartões: mentores ativos, mentorados, mentorias ativas, sessões da semana, convites pendentes. Abaixo: próximas sessões, convites recentes, atividades pendentes, indicadores.

| Tela | Rota | Funções |
|---|---|---|
| Mentores | `/dashboard/instituicao/mentores` | Listar nome, programa, mentorados, próxima sessão, status. Ver perfil, ver mentorados, agenda. Desativar mentor |
| Mentorados | `/dashboard/instituicao/mentorados` | Listar mentorados com mentor, programa, próxima sessão e status |
| Programas | `/dashboard/instituicao/programas` | Ver programas derivados de convites e mentorias (ainda sem cadastro próprio de Programa) |
| Agenda | `/dashboard/instituicao/agenda` | Calendário mensal/semanal; sessões com mentor, mentorado, programa, Meet/Zoom, status |
| Convites | `/dashboard/instituicao/convites` | Convidar mentor; filtrar Pendentes / Aceitos / Expirados / Cancelados; reenviar; cancelar |
| Mentorias | `/dashboard/instituicao/mentorias` | Vincular mentorado (e-mail + mentor + programa); listar relações |
| Relatórios | `/dashboard/instituicao/relatorios` | Indicadores agregados e estatísticas de sessão |
| Configurações | `/dashboard/instituicao/configuracoes` | Nome da instituição, responsável, status do e-mail de convite |

Fluxo de convite: instituição envia convite → mentor abre `/convites/mentor/[token]` → cria senha → fica vinculado à instituição.

### 5.2 Mentor

**Dashboard** (`/dashboard/mentor`)  
Resumo: quantidade de mentorados, sessões da semana, próximas sessões e atalhos.

| Tela | Rota | Funções |
|---|---|---|
| Meus Mentorados | `/dashboard/mentor/mentorados` | Aceitar/recusar solicitações do marketplace; vincular mentorado por e-mail; abrir sala |
| Agenda | `/dashboard/mentor/agenda` | Ver, reagendar, concluir, marcar ausência e cancelar sessões |
| Plano / Tarefas / Questionários | `/dashboard/mentor/plano` etc. | Escolher a mentoria e abrir a sala na aba correspondente |
| Mensagens | `/dashboard/mentor/mensagens` | Atalho para notificações (chat ainda não existe) |
| Perfil | `/dashboard/mentor/perfil` | Headline, bio, especialidades, preço, modalidade, LinkedIn/GitHub, foto, Google, Zoom, disponibilidade |

### 5.3 Mentorado

**Dashboard** (`/dashboard/mentorado`)  
Meu mentor, próxima sessão, progresso e atalhos.

| Tela | Rota | Funções |
|---|---|---|
| Minha Jornada | `/dashboard/mentorado/jornada` | Solicitações enviadas e mentorias aceitas |
| Agenda | `/dashboard/mentorado/agenda` | Sessões; reagendar/cancelar quando permitido |
| Plano / Tarefas / Questionários | `/dashboard/mentorado/plano` etc. | Abrir a sala da mentoria |
| Progresso | `/dashboard/mentorado/progresso` | Evolução da mentoria |

### 5.4 Funções comuns (todos os logados)

| Área | Rota | Funções |
|---|---|---|
| Marketplace | `/mentorias`, `/mentors/[id]` | Buscar mentores/produtos, ver perfil público, solicitar mentoria, avaliar |
| Feed | `/feed` | Publicar, curtir, comentar, seguir, bloquear |
| Grupos | `/grupos`, `/grupos/[id]` | Criar grupo, membros, comunicados, likes e comentários |
| Notificações | `/notifications` | Listar, marcar como lida |
| Ajuda | `/ajuda`, `/ajuda/[slug]` | Central de ajuda |
| Sala da mentoria | `/dashboard/mentorships/[id]` | Visão, sessões, plano, tarefas, questionários, materiais, evolução, pagamento e avaliação |

`/agenda` redireciona para a agenda do papel logado.

---

## 6. Sala da mentoria

Rota: `/dashboard/mentorships/[id]?tab=`

| Aba | Função |
|---|---|
| `visao` | Status, progresso de sessões, mentor/mentorado, pagamento |
| `sessoes` | Agendar (horário livre ou slot de disponibilidade), Meet/Zoom/link manual, lista de sessões |
| `plano` | Título e descrição do plano de estudos |
| `tarefas` | Criar, editar, concluir e acompanhar tarefas |
| `questionarios` | Aba prevista na navegação (conteúdo ainda via plano/tarefas do tipo quiz) |
| `materiais` | Tarefas com URL de recurso |
| `evolucao` | Progresso consolidado |

Ações de ciclo de vida: concluir mentoria, cancelar mentoria, pagar (quando o produto exige), avaliar após conclusão.

**Status da sessão:** `SCHEDULED` · `COMPLETED` · `CANCELLED` · `NO_SHOW`

---

## 7. Telas públicas e autenticação

| Rota | Função |
|---|---|
| `/` | Home |
| `/login` | Entrar |
| `/cadastro` | Criar conta (`MENTOR`, `MENTEE` ou `INSTITUTION`) |
| `/convites/mentor/[token]` | Aceitar convite institucional e criar senha |
| `/mentorias` | Catálogo |
| `/mentors/[id]` | Perfil público do mentor |

---

## 8. API REST (`/api/v1`)

### Identidade

| Método | Caminho | Função |
|---|---|---|
| POST | `/auth/register` | Cadastro |
| POST | `/auth/login` | Login JWT |
| GET | `/auth/me` | Usuário atual |
| PUT | `/auth/me` | Atualizar nome |
| POST | `/auth/me/photo` | Upload de foto |
| GET/POST | `/auth/linkedin/*` | Importação/OAuth LinkedIn |

### Mentores e disponibilidade

| Método | Caminho | Função |
|---|---|---|
| GET | `/mentors` | Catálogo |
| GET/PUT | `/mentors/me` | Perfil do mentor logado |
| GET | `/mentors/{id}` | Perfil público |
| GET | `/mentors/{id}/reviews` | Avaliações |
| GET | `/mentors/{id}/rating` | Nota média |
| GET/PUT | `/mentors/{mentorId}/availability` | Ver/salvar horários |
| POST | `/mentors/{mentorId}/bookings` | Reservar slot |

### Instituição e convites

| Método | Caminho | Função |
|---|---|---|
| GET | `/institutions/me` | Dashboard (mentores, mentorados, convites, indicadores) |
| GET | `/institutions/me/mail-status` | Se o envio de e-mail está ativo |
| POST | `/institutions/me/invitations` | Convidar mentor |
| POST | `/institutions/me/invitations/{id}/resend` | Reenviar |
| POST | `/institutions/me/invitations/{id}/cancel` | Cancelar (status `CANCELLED`) |
| DELETE | `/institutions/me/invitations/{id}` | Remover convite |
| POST | `/institutions/me/mentors/{id}/deactivate` | Desativar mentor |
| GET | `/mentor-invitations/{token}` | Dados públicos do convite |
| POST | `/mentor-invitations/{token}/accept` | Aceitar convite |

**Status do convite:** `PENDING` · `ACCEPTED` · `EXPIRED` · `CANCELLED`

### Mentorias e sessões

| Método | Caminho | Função |
|---|---|---|
| GET | `/mentorships` | Produtos de mentoria (catálogo) |
| GET | `/mentorships/{id}` | Produto |
| GET | `/mentorships/as-mentor` | Vínculos do mentor |
| GET | `/mentorships/as-mentee` | Vínculos do mentorado |
| GET | `/mentorships/as-institution` | Vínculos da instituição |
| POST | `/mentorships/assignments` | Vincular mentorado a mentor/programa |
| GET | `/mentorships/relationships/{id}` | Detalhe do vínculo |
| PATCH | `/mentorships/relationships/{id}/complete` | Concluir |
| PATCH | `/mentorships/relationships/{id}/cancel` | Cancelar |
| GET/POST | `/mentorships/relationships/{id}/sessions` | Listar / criar sessão |
| GET | `/agenda` | Sessões no intervalo |
| GET | `/agenda/next` | Próxima sessão |
| GET | `/sessions/stats` | Indicadores de sessão (inclui instituição) |
| PATCH | `/sessions/{id}/reschedule` | Reagendar |
| PATCH | `/sessions/{id}/complete` | Concluir |
| PATCH | `/sessions/{id}/cancel` | Cancelar |
| PATCH | `/sessions/{id}/no-show` | Marcar ausência |

### Solicitações (marketplace)

| Método | Caminho | Função |
|---|---|---|
| POST | `/mentorship-requests` | Mentorado solicita |
| GET | `/mentorship-requests/sent` | Enviadas |
| GET | `/mentorship-requests/received` | Recebidas pelo mentor |
| PATCH | `.../{id}/accept` · `/reject` · `/cancel` | Decidir pedido |
| GET | `/enrollments/me` | Matrículas do usuário |
| PATCH | `/enrollments/{id}/accept\|reject\|complete\|cancel` | Ciclo da matrícula |

### Plano de estudos

Base: `/mentorships/relationships/{mentorshipId}/study-plan`

| Método | Função |
|---|---|
| GET | Carregar plano |
| PUT | Criar/atualizar plano |
| POST `/tasks` | Nova tarefa |
| PATCH `/tasks/{id}` | Editar tarefa |
| PATCH `/tasks/{id}/progress` | Atualizar progresso |
| DELETE `/tasks/{id}` | Remover tarefa |

### Pagamentos e avaliações

| Método | Caminho | Função |
|---|---|---|
| POST/GET | `/mentorships/relationships/{id}/payments` | Cobrar / listar |
| GET | `/payments/{id}` | Detalhe |
| POST | `/payments/{id}/confirm` | Confirmar |
| POST | `/payments/webhook` e `/webhooks/{provider}` | Webhook |
| POST/GET | `/mentorships/relationships/{id}/reviews` | Avaliar / listar |

### Feed, social, grupos e comunicados

| Área | Endpoints principais |
|---|---|
| Feed | `GET /feed` · `POST/GET/PUT/DELETE /posts` · likes e comentários |
| Social | `POST/DELETE /users/{id}/follow` · `block` · followers/following |
| Grupos | `GET/POST /groups` · membros · candidatos · fechar |
| Comunicados | `GET/POST /groups/{id}/announcements` · likes · comentários |
| Notificações | `GET /notifications` · unread-count · marcar lida |

### Integrações

| Área | Função |
|---|---|
| Google | Conectar conta, status, desconectar, eventos de calendário, callback OAuth |
| Zoom | Conectar, status, desconectar, callback, webhook de reunião |

Ao criar sessão, o sistema pode gerar Meet ou Zoom conforme a conexão do mentor.

---

## 9. Modelo de dados (conceitual)

```
User (MENTOR | MENTEE | INSTITUTION | ADMIN)
 ├── MentorProfile
 ├── Institution
 │    ├── MentorInvitation
 │    └── MentorProfile.institutionId
 └── Mentorship (mentorUser + menteeUser + program + institutionId?)
      ├── MentorshipSession (Google Meet | Zoom | link)
      ├── StudyPlan → StudyTask
      ├── Payment
      └── Review
```

Marketplace paralelo: `MentorshipProduct` → `Enrollment` / `MentorshipRequest` → pode gerar `Mentorship`.

---

## 10. Frontend — mapa de rotas

```
/                              Home
/login  /cadastro
/convites/mentor/[token]
/mentorias  /mentors/[id]
/feed  /grupos  /grupos/[id]
/notifications  /ajuda  /ajuda/[slug]
/agenda                        → redireciona pela role

/dashboard/instituicao
  /mentores  /mentorados  /programas
  /agenda  /convites  /mentorias
  /relatorios  /configuracoes

/dashboard/mentor
  /mentorados  /agenda  /plano  /tarefas
  /questionarios  /mensagens  /perfil

/dashboard/mentorado
  /jornada  /agenda  /plano  /tarefas
  /questionarios  /progresso

/dashboard/mentorships/[id]    Sala (compartilhada)
```

Cada dashboard tem layout com menu lateral (`DashboardShell`) que restringe o papel.

---

## 11. O que ainda não é entidade própria

- **Programa:** texto compartilhado entre convite e mentoria; não há cadastro de Programa no banco.
- **Chat/mensagens 1:1:** a tela de mensagens aponta para notificações.
- **Questionários:** aba na sala; não há módulo separado de questionário além de tarefas do plano.
- **Admin UI:** role existe; não há painel admin no frontend.
- **Rate limit de login:** previsto nas regras de segurança, ainda não aplicado.

---

## 12. Como subir localmente

Backend (H2):

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080  
- Swagger: http://localhost:8080/swagger-ui.html  
- Health: http://localhost:8080/actuator/health  

Frontend:

```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:3000
