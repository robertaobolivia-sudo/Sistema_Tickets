# Contratos de API, autenticação e QA manual

Documento de consolidação (Sprint 64). Projeto: **suporte-tickets** (Spring Boot 3.3, SPA em `src/main/resources/static`).

---

## 1. Estado atual da autenticação

- Login: `POST /api/analistas/login` (público). Retorna `AnalistaResponseDTO` com `authToken` **apenas nesta resposta**.
- Sessão nas demais chamadas: headers obrigatórios:
  - `X-Analista-Id` (Long)
  - `X-Analista-Token` (string)
- Validação: `PerfilAcessoAutorizacaoService.validarSessao` / `exigirSessaoValida` / `exigirAdmin`.
- Token com expiração (`authTokenExpiraEm`). Logout: `POST /api/analistas/logout` (exige sessão).
- Senha: BCrypt no cadastro/atualização; login aceita legado em texto e migra para hash. Politica minima (Sprint 70) em cadastro e troca: mínimo 8 caracteres, 1 letra e 1 número (`AnalistaSenhaPoliticaService`); senhas já armazenadas seguem válidas até troca.
- **Mensagens de erro (UI):** frontend (`app.js`, Sprint 71) padroniza 401/403, validação e falhas via `MSG_ERRO` e `mensagemErroApi` / `mensagemErroLogin`; não exibir textos técnicos de header/token ao usuário.
- Frontend: `apiFetch` injeta headers quando `loggedAnalyst` tem `authToken`. Sessão persistida em `localStorage` (`suporteTicketsAnalista`).
- Reload: `restoreSessionFromServer()` restaura cache e valida com `GET /api/analistas/{id}`.

**Não usar** Spring Security/JWT neste documento — modelo atual é header + token em banco.

---

## 2. Endpoints públicos legítimos

| Método | URL | Justificativa |
|--------|-----|----------------|
| POST | `/api/analistas/login` | Entrada da aplicação; emite token de sessão |
| POST | `/api/webhooks/tickets` | Integração externa (webhook); cria ticket sem sessão |
| GET | `/`, `/index.html`, estáticos | SPA antes do login |

**Respostas sensíveis:** GET de analistas **não** inclui `senha` nem `authToken` (DTO + `JsonInclude.NON_NULL`). Entidade `Analista` ignora `senha`/`authToken` em JSON (`@JsonIgnore`).

---

## 3. Endpoints protegidos por sessão (qualquer perfil autenticado)

Regra: `exigirSessaoValida` — ANALISTA, SUPERVISOR ou ADMIN com token válido.

Principais grupos:

- **Tickets:** `GET/PUT` `/api/tickets`, busca, alertas, PDF, status, encerrar, reabrir, escalonar, interações, **`POST /api/tickets`** (abertura pela UI).
- **Dashboard:** `GET /api/dashboard/resumo`, `gerencial`, `sla`, `filas-analistas`, `conexoes-pendencias`.
- **Notificações:** `GET/PUT/POST` em `/api/notificacoes` (lista, marcar lida, verificar SLA).
- **Clientes e contatos:** `/api/clientes`, `/api/contatos-clientes`, contatos por cliente.
- **Categorias:** `/api/grupos-categoria`, `/api/subgrupos-categoria`.
- **Analistas (parcial):** `GET /api/analistas/online`, `GET /api/analistas/filas`, `GET /api/analistas/{id}` (próprio ou ADMIN), `GET /api/analistas/{id}/tickets` (próprio ou ADMIN), `PUT` status/foto do próprio ou ADMIN conforme regra, `POST /api/analistas/logout`.
- **Webhook consulta:** `GET /api/webhooks/tickets/{numeroTicket}` (sessão; não é criação pública).

---

## 4. Endpoints ADMIN (sessão + perfil ADMIN)

Regra: `exigirAdmin` após validar token.

- `GET /api/analistas` — lista completa de analistas.
- `POST /api/analistas`, `PUT /api/analistas/{id}` — cadastro/edição.
- `PUT /api/analistas/{id}/perfil-acesso`.
- **Configurações (somente ADMIN):** `GET/PUT /api/horarios-uteis/padrao`, CRUD/seed feriados, `GET/PUT /api/sla-metas`, seeds.
- **Ferramenta SLA:** `GET /api/sla/calcular-vencimento-teste` (ADMIN).
- **Auditoria (read-only):** `GET /api/auditoria/eventos` — lista paginada de `auditoria_eventos` (filtros: dataInicio, dataFim, analistaId, acao, entidade, entidadeId; params pagina, limite padrão 50 máx 200).
- **Auditoria (retenção manual, ADMIN):** `GET /api/auditoria/eventos/contar-antigos?antesDe=YYYY-MM-DD` — conta eventos com `dataHora` anterior ao início do dia informado; `DELETE /api/auditoria/eventos/antigos?antesDe=YYYY-MM-DD&confirmar=true` — exclusão física somente com `confirmar=true`; data limite de exclusão deve ser anterior a hoje − 30 dias (proteção dos registros recentes). Sem scheduler nem exclusão automática. Ver `docs/AUDITORIA_RETENCAO.md`.

Mutations de clientes/categorias exigem sessão (não necessariamente ADMIN), salvo regras já existentes em controllers.

---

## 4.1. Etiquetas — sessão e perfil ADMIN ou SUPERVISOR

Controller: `/api/etiquetas`.

| Operação | Regra backend |
|----------|----------------|
| `GET /api/etiquetas`, `GET /api/etiquetas/ativas` | `exigirSessaoValida` (qualquer perfil autenticado) |
| `POST`, `PUT /{id}`, `PATCH /{id}/ativar`, `PATCH /{id}/inativar` | `exigirAdminOuSupervisor` |

**UI:** página Configurações no menu para ADMIN e SUPERVISOR. Seções horário útil, feriados e metas SLA (classe `.config-admin-only`) visíveis e editáveis apenas para ADMIN. SUPERVISOR vê e gerencia somente a seção **Etiquetas**. ANALISTA não acessa a página Configurações (`PAGE_ACCESS_BY_PERFIL`); no Chats, seleção de novas etiquetas usa apenas ativas (`/api/etiquetas/ativas`).

---

## 5. Webhook público x abertura autenticada pela UI

| Aspecto | POST `/api/webhooks/tickets` | POST `/api/tickets` |
|---------|------------------------------|---------------------|
| Autenticação | Nenhuma | `exigirSessaoValida` |
| Uso | Integrações externas | Tela **Abrir Ticket** (UI logada) |
| Payload | `TicketWebhookRequestDTO` | Mesmo DTO |
| Service | `ticketService.criarTicketPorWebhook` | **Mesmo método** |
| Resposta | 201 + `TicketResponseDTO` | 201 + `TicketResponseDTO` |
| Frontend | `fetch` direto (sem token) | `apiFetch` |

**Importante:** não duplicar regra de negócio; apenas separar portas de entrada. SLA na criação, prioridade padrão MEDIA e demais regras permanecem no service.

---

## 6. Perfis e acessos principais (UI)

Definido em `PAGE_ACCESS_BY_PERFIL` (`js/core/permissions.js`):

| Página / área | ADMIN | SUPERVISOR | ANALISTA |
|---------------|:-----:|:----------:|:--------:|
| Dashboard | Sim | Sim | Sim |
| Tickets | Sim | Sim | Sim |
| Chats | Sim | Sim | Sim |
| Abrir Ticket | Sim | Não | Sim |
| Relatórios | Sim | Sim | Não |
| Indicadores | Sim | Sim | Não |
| Atendentes | Sim | Sim | Não |
| Perfil | Sim | Sim | Sim |
| Clientes | Sim | Não | Não |
| Auditoria | Sim | Não | Não |
| Configurações (menu) | Sim | Sim | Não |

### 6.1. Configurações — conteúdo por perfil

A mesma rota/página `configuracoes` concentra blocos com permissões diferentes:

| Bloco na tela | ADMIN | SUPERVISOR | ANALISTA |
|---------------|:-----:|:----------:|:--------:|
| Horário útil | Sim | Não (oculto) | Sem acesso |
| Feriados | Sim | Não (oculto) | Sem acesso |
| Metas SLA | Sim | Não (oculto) | Sem acesso |
| Etiquetas (gestão visual) | Sim | Sim | Sem acesso |

Backend pode retornar 403 em APIs restritas (ex.: horário/feriados/SLA para SUPERVISOR; listagem de analistas; auditoria) mesmo que o menu esteja visível — não contornar sem perfil adequado.

---

## 7. Checklist manual — ADMIN

Pré-requisito: usuário ADMIN (ex.: credencial de ambiente de teste).

1. [ ] `mvn clean install` — BUILD SUCCESS.
2. [ ] Subir app; abrir `http://localhost:8080/` — HTTP 200.
3. [ ] Login com e-mail/senha válidos — entra no Dashboard.
4. [ ] Recarregar página (F5) — sessão restaurada, permanece logado.
5. [ ] Dashboard — cards/indicadores carregam sem erro no console.
6. [ ] Tickets — listagem abre; abrir detalhe de um ticket.
7. [ ] Abrir Ticket — buscar cliente, criar ticket — sucesso; ticket aparece na listagem.
8. [ ] Alerta Ticket (preferência ON) — após criar ticket, alerta ou verificação sem erro crítico.
9. [ ] Clientes — listar, buscar, abrir edição (se aplicável).
10. [ ] Atendentes — kanban/filas; administração de analistas (se visível).
11. [ ] Configurações — horário útil, feriados, metas SLA e etiquetas — carregar e salvar alteração simples em cada bloco.
12. [ ] Relatórios — filtros e gerar relatório (ou CSV se usado).
13. [ ] Notificações — sino/lista; marcar uma como lida.
14. [ ] Perfil — alterar status operador; foto (opcional).
15. [ ] Logout — volta à tela de login; `GET /api/tickets` sem token → 403.
16. [ ] API: `POST /api/webhooks/tickets` sem token — não retorna 403 (pode 400/201 conforme payload).

---

## 8. Checklist manual — ANALISTA ou SUPERVISOR

Pré-requisito: credencial não-ADMIN no ambiente (se indisponível, validar menu oculto com usuário ADMIN em aba anônima não se aplica — usar conta real).

**ANALISTA (típico):**

1. [ ] Login — OK.
2. [ ] Dashboard e Tickets — OK.
3. [ ] Abrir Ticket — OK (ANALISTA tem acesso).
4. [ ] Menu **Clientes**, **Configurações**, **Indicadores** — ocultos ou inacessíveis.
5. [ ] Relatórios — oculto para ANALISTA.
6. [ ] Atendentes — oculto para ANALISTA.
7. [ ] Perfil — OK.

**SUPERVISOR (típico):**

1. [ ] Login — OK.
2. [ ] Dashboard, Tickets, Chats, Relatórios, Indicadores, Atendentes — OK.
3. [ ] Abrir Ticket — menu oculto (somente ADMIN e ANALISTA).
4. [ ] Clientes e Auditoria — ocultos.
5. [ ] Configurações — menu visível; somente seção **Etiquetas** (listar, criar, editar, ativar/inativar); horário útil, feriados e metas SLA não aparecem ou APIs retornam 403.
6. [ ] `GET /api/analistas` com token SUPERVISOR — esperado 403 (somente ADMIN).
7. [ ] `POST /api/etiquetas` com token SUPERVISOR — esperado 201 (payload válido).

---

## 9. Pendências conhecidas

| Prioridade | Item |
|------------|------|
| Média | Rate limit / token de integração no webhook público |
| Baixa | Atualizar smoke automatizado E2E para Configurações parcial (SUPERVISOR) |
| Média | Testes E2E automatizados (login + fluxos críticos) |
| Baixa | Documentar payload completo do webhook em exemplo JSON |
| Baixa | Rotação/período de expiração de token configurável via properties |

---

## Referência rápida — frontend

- Público: `fetch` em login e webhook de criação.
- Autenticado: `apiFetch` em todo o restante.
- Erros 401/403: função `mensagemErroSessaoApi` (sessão expirada / acesso restrito a administradores).

---

*Última atualização: Sprint 150 — Configurações parcial para SUPERVISOR (Etiquetas); matriz ADMIN/SUPERVISOR/ANALISTA.*
