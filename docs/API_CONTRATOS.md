# Contratos básicos de API — suporte-tickets

Documentação objetiva dos principais endpoints (Sprint 77 — contrato básico de API).  
Base URL: `http://localhost:8080` (contexto `/`).

## Convenções gerais

| Item | Descrição |
|------|-----------|
| Autenticação UI | Headers `X-Analista-Id` (Long) e `X-Analista-Token` (string) em rotas protegidas |
| Público | Sem headers; sem validação de sessão |
| Perfil ADMIN | `exigirAdmin` no backend — somente administradores |
| Perfil sessão | `exigirSessaoValida` — qualquer analista autenticado e ativo |
| Perfil próprio ou ADMIN | `exigirProprioAnalistaOuAdmin` — próprio cadastro ou ADMIN |
| Erros | JSON `{ "status": <http>, "erro": "<mensagem>" }` (padrão global) |
| Datas em query | ISO `YYYY-MM-DD` ou `YYYY-MM-DDTHH:mm:ss` conforme endpoint |

**UI x backend:** a interface oculta menus por perfil (`PAGE_ACCESS_BY_PERFIL` em `app.js`). O backend ainda valida sessão/perfil nos controllers; não confiar só no frontend.

---

## 1. Autenticação (`/api/analistas`)

### POST `/api/analistas/login`

| Campo | Valor |
|-------|--------|
| Método | POST |
| Rota | `/api/analistas/login` |
| Autenticação | **Público** (sem token) |
| Perfil | — |
| Uso | Login da aplicação; retorna `authToken` |
| Payload | `{ "email": "...", "senha": "..." }` |
| Retorno | `200` — `AnalistaResponseDTO` com `authToken`, `perfilAcesso`, etc. |
| Observações | Credencial inválida → `401` com mensagem amigável (não 500) |

### POST `/api/analistas/logout`

| Campo | Valor |
|-------|--------|
| Método | POST |
| Rota | `/api/analistas/logout` |
| Autenticação | Sessão obrigatória |
| Perfil | Sessão válida |
| Uso | Encerra sessão (invalida token no servidor) |
| Payload | — |
| Retorno | `204` sem corpo |

---

## 2. Tickets (`/api/tickets`)

### POST `/api/tickets` (UI autenticada)

| Campo | Valor |
|-------|--------|
| Método | POST |
| Rota | `/api/tickets` |
| Autenticação | Sessão obrigatória |
| Perfil | Sessão válida |
| Uso | Abertura de ticket pela **tela logada** (`apiFetch`) |
| Payload | `TicketWebhookRequestDTO` (mesmo contrato do webhook) |
| Retorno | `201` — `TicketResponseDTO` |
| Observações | Mesma regra de negócio que webhook; registra auditoria `TICKET_CRIAR_UI` |

### GET `/api/tickets`

| Campo | Valor |
|-------|--------|
| Método | GET |
| Rota | `/api/tickets` |
| Autenticação | Sessão |
| Perfil | Sessão |
| Uso | Lista tickets (ordenados por abertura) |
| Retorno | `200` — lista de `TicketResponseDTO` |

### GET `/api/tickets/busca`

| Campo | Valor |
|-------|--------|
| Método | GET |
| Rota | `/api/tickets/busca` |
| Autenticação | Sessão |
| Perfil | Sessão |
| Uso | Busca avançada (`TicketFiltroDTO` em query) |
| Retorno | `200` — lista |

### GET `/api/tickets/{numeroTicket}`

| Campo | Valor |
|-------|--------|
| Método | GET |
| Rota | `/api/tickets/{numeroTicket}` |
| Autenticação | Sessão |
| Perfil | Sessão |
| Uso | Detalhe do ticket (modal) |
| Retorno | `200` — `TicketResponseDTO` |

### PUT `/api/tickets/{numeroTicket}/status`

| Payload | `{ "status": "ABERTO\|EM_ATENDIMENTO\|..." }` |
| Retorno | `200` — ticket atualizado |

### PUT `/api/tickets/{numeroTicket}/encerrar`

| Payload | `EncerrarTicketRequestDTO` (comentário, etc.) |
| Retorno | `200` — ticket encerrado (status RESOLVIDO) |

### PUT `/api/tickets/{numeroTicket}/reabrir`

| Retorno | `200` — ticket reaberto |

### PUT `/api/tickets/{numeroTicket}/escalonar` / `remover-escalonamento`

| Payload escalonar | `TicketEscalonamentoRequestDTO` |
| Retorno | `200` |

### GET `/api/tickets/{numeroTicket}/pdf`

| Retorno | `200` — PDF (bytes), `Content-Disposition` attachment |

### GET `/api/tickets/relatorios/csv`

| Query | Filtros de `TicketFiltroDTO` |
| Retorno | `200` — CSV relatório de tickets |

### GET `/api/tickets/alerta-referencia` / `novos-alerta`

| Uso | Alertas de novos tickets na UI |
| Retorno | `200` — DTO/lista conforme endpoint |

### Interações — `/api/tickets/{numeroTicket}/interacoes`

| Método | Rota | Auth | Payload / retorno |
|--------|------|------|-------------------|
| POST | `.../interacoes` | Sessão | `TicketInteracaoRequestDTO` → `201` |
| GET | `.../interacoes` | Sessão | → `200` lista |

*(Demais rotas auxiliares: `GET /api/tickets/status/{status}` — sessão.)*

---

## 3. Webhook externo (`/api/webhooks`) — **público x UI**

### POST `/api/webhooks/tickets` — **PÚBLICO**

| Campo | Valor |
|-------|--------|
| Método | POST |
| Rota | `/api/webhooks/tickets` |
| Autenticação | **Nenhuma** |
| Perfil | — |
| Uso | Integrações externas (CRM, automação, etc.) |
| Payload | `TicketWebhookRequestDTO` |
| Retorno | `201` — `TicketResponseDTO` |
| Observações | **Não** usar na UI logada; UI deve usar `POST /api/tickets` com token. Mesmo service de criação (`criarTicketPorWebhook`). |

### GET `/api/webhooks/tickets/{numeroTicket}`

| Autenticação | Sessão obrigatória |
| Uso | Consulta ticket via rota webhook (legado/integração com auth) |

---

## 4. Clientes e contatos

### Clientes — `/api/clientes`

| Método | Rota | Auth | Perfil backend | Observação UI |
|--------|------|------|----------------|---------------|
| POST | `/api/clientes` | Sessão | Sessão | Menu Clientes: ADMIN |
| GET | `/api/clientes` | Sessão | Sessão | |
| GET | `/api/clientes/buscar`, `/busca` | Sessão | Sessão | Query nome/termo |
| GET | `/api/clientes/{id}` | Sessão | Sessão | |
| PUT | `/api/clientes/{id}` | Sessão | Sessão | |
| PATCH | `/{id}/ativar`, `/{id}/inativar` | Sessão | Sessão | |
| DELETE | `/api/clientes/{id}` | Sessão | Sessão | |

### Contatos por cliente — `/api/clientes/{clienteId}/contatos`

| POST | Criar contato vinculado ao cliente |
| GET | Listar / listar ativos |

### Contatos globais — `/api/contatos-clientes`

| GET `/busca` | Busca |
| GET/PUT/PATCH `/{id}` | Detalhe, edição, ativar/inativar, principal |

---

## 5. Categorias

### Grupos — `/api/grupos-categoria`

| CRUD | POST, GET, GET `/{id}`, PUT, DELETE — sessão |

### Subgrupos — `/api/subgrupos-categoria`

| CRUD + GET `/grupo/{grupoId}` — sessão |

---

## 6. Dashboard — `/api/dashboard`

| Método | Rota | Auth | Uso |
|--------|------|------|-----|
| GET | `/resumo` | Sessão | Cards resumo operacional |
| GET | `/gerencial` | Sessão | Indicadores gerenciais |
| GET | `/sla` | Sessão | Indicadores SLA |
| GET | `/filas-analistas` | Sessão | Kanban / filas |
| GET | `/conexoes-pendencias` | Sessão | Pendências por conexão |

---

## 7. Configurações — **ADMIN** (backend)

### Horário útil — `/api/horarios-uteis`

| GET/PUT | `/padrao` | ADMIN |

### Feriados — `/api/feriados`

| GET, GET `/ativos`, GET `/verificar`, POST, PUT `/{id}`, PATCH ativar/inativar, POST `/seed/2026-sao-paulo` | ADMIN |

### Metas SLA — `/api/sla-metas`

| GET, GET `/ativas`, GET `/prioridade/{prioridade}`, PUT prioridade, POST `/seed-default` | ADMIN |

### Ferramenta — `/api/sla/calcular-vencimento-teste`

| GET | ADMIN | Teste de cálculo SLA |

---

## 8. Auditoria — **ADMIN**

Base: `/api/auditoria`

| Método | Rota | Uso |
|--------|------|-----|
| GET | `/eventos` | Lista paginada (filtros: datas, analista, ação, entidade, pagina, limite) |
| GET | `/eventos/csv` | Exportação CSV (máx. registros) |
| GET | `/eventos/contar-antigos?antesDe=YYYY-MM-DD` | Contagem para retenção |
| DELETE | `/eventos/antigos?antesDe=...&confirmar=true` | Limpeza manual (proteção últimos 30 dias na exclusão) |

---

## 9. Satisfação

### Por ticket — `/api/tickets/{numeroTicket}/satisfacao`

| Método | Rota | Auth | Uso |
|--------|------|------|-----|
| GET | (base) | Sessão | Consulta avaliação; `204` se não houver |
| POST | (base) | Sessão | Registra nota 1–5 + comentário opcional; ticket RESOLVIDO/CANCELADO; uma avaliação por ticket |

### Gerencial — `/api/tickets/satisfacao`

| GET | `/resumo?dataInicio=&dataFim=` | Sessão | Total, média, distribuição 1–5, % positivas/negativas |
| GET | `/csv?dataInicio=&dataFim=` | Sessão | Exportação avaliações (UTF-8 BOM, `;`) → `satisfacao-tickets.csv` |

---

## 10. Notificações — `/api/notificacoes`

| Método | Rota | Auth | Retorno |
|--------|------|------|---------|
| GET | `/` | Sessão | Lista notificações |
| GET | `/nao-lidas` | Sessão | Não lidas |
| GET | `/contador-nao-lidas` | Sessão | Contador (sino) |
| PUT | `/{id}/marcar-lida` | Sessão | `200` |
| PUT | `/marcar-todas-lidas` | Sessão | `200` |
| POST | `/sla/verificar` | Sessão | Verificação SLA crítico (job manual UI) |

---

## 11. Analistas (administração) — `/api/analistas`

| Método | Rota | Auth | Perfil |
|--------|------|------|--------|
| GET | `/` | Sessão | **ADMIN** — lista todos |
| GET | `/online` | Sessão | Online |
| GET | `/{id}` | Sessão | Próprio ou ADMIN |
| GET | `/{id}/tickets` | Sessão | Próprio ou ADMIN |
| GET | `/filas` | Sessão | Filas analistas |
| POST | `/` | Sessão | **ADMIN** — criar analista |
| PUT | `/{id}` | Sessão | **ADMIN** — cadastro/senha |
| PUT | `/{id}/status` | Sessão | Status operador |
| PUT | `/{id}/perfil-acesso` | Sessão | **ADMIN** |
| POST/DELETE | `/{id}/foto` | Sessão | Foto perfil |

---

## Resumo: público vs autenticado vs ADMIN

| Tipo | Exemplos |
|------|----------|
| **Público** | `POST /api/analistas/login`, `POST /api/webhooks/tickets` |
| **Sessão** | Tickets, dashboard, notificações, satisfação, clientes (API), categorias |
| **ADMIN** | Auditoria, feriados, horário útil, metas SLA, listar/criar/editar analistas (admin), SLA teste |

---

## Referências

- Detalhes de produto/QA: `docs/CONTRATOS_E_QA.md`, `docs/QA_PERFIS.md`, `docs/QA_SEGURANCA_FLUXOS.md`, `docs/AUDITORIA_RETENCAO.md`
- Frontend estático: `src/main/resources/static/`

*Última revisão: Sprint 77 — contrato básico de API.*
