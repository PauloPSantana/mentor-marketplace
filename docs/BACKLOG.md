# Backlog de Produto

## EPIC 01 — Cadastro e autenticação
**US-001** Como usuário, quero me cadastrar como mentor ou mentorado.
Critérios:
- email único;
- senha criptografada;
- papel obrigatório;
- validações retornam 400;
- cadastro válido retorna 201.

**US-002** Como usuário, quero autenticar e receber token.
Critérios:
- credenciais válidas retornam JWT;
- inválidas retornam 401.

## EPIC 02 — Perfil do mentor
**US-010** Como mentor, quero montar meu perfil público.
Critérios:
- headline;
- bio;
- skills;
- experiência;
- avaliação agregada.

## EPIC 03 — Mentorias
**US-020** Como mentor, quero cadastrar um produto de mentoria.
Critérios:
- título e descrição obrigatórios;
- preço > 0;
- sessões > 0;
- vagas > 0;
- inicia em DRAFT.

**US-021** Como mentor, quero publicar minha mentoria.
Critérios:
- somente o proprietário;
- produto válido;
- status muda para PUBLISHED.

## EPIC 04 — Marketplace
**US-030** Como mentorado, quero encontrar mentorias.
Critérios:
- paginação;
- filtro por categoria;
- filtro por preço;
- filtro por nível;
- busca por texto.

## EPIC 05 — Contratação
**US-040** Como mentorado, quero solicitar uma mentoria.
Critérios:
- produto publicado;
- vagas disponíveis;
- snapshot de preço;
- status PENDING.

## EPIC 06 — Avaliação
**US-050** Como mentorado, quero avaliar após concluir.
Critérios:
- enrollment COMPLETED;
- nota de 1 a 5;
- uma avaliação por enrollment.
