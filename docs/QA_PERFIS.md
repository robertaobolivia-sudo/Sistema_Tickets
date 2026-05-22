# Smoke QA por perfil

Referência de menus: `PAGE_ACCESS_BY_PERFIL` em `src/main/resources/static/js/core/permissions.js`.

Funções auxiliares de Configurações: `canManageConfiguracoes()` (somente ADMIN), `canManageEtiquetas()` (ADMIN e SUPERVISOR), `applyConfiguracoesAdminSectionsVisibility()` (oculta horário, feriados e metas SLA para não-ADMIN).

Mensagem esperada ao abrir página sem permissão: **Você não tem permissão para acessar esta página.**

## Configurações — regras por perfil (Sprint 148+)

A página **Configurações** no menu é acessível a **ADMIN** e **SUPERVISOR**. O conteúdo interno varia por perfil:

| Área na tela | ADMIN | SUPERVISOR | ANALISTA |
|--------------|:-----:|:----------:|:--------:|
| Horário útil | Sim | Não (seção oculta) | Sem acesso à página |
| Feriados | Sim | Não (seção oculta) | Sem acesso à página |
| Metas SLA | Sim | Não (seção oculta) | Sem acesso à página |
| Etiquetas (CRUD visual) | Sim | Sim | Sem acesso à página |

**API etiquetas** (`/api/etiquetas`): leitura com sessão válida; criar, editar, ativar e inativar exigem **ADMIN** ou **SUPERVISOR**. Horário útil, feriados e metas SLA: mutações somente **ADMIN**.

**Chats:** opções de etiqueta ativa vêm de `GET /api/etiquetas/ativas` (sessão). Inativas não entram como nova seleção.

## Credenciais sugeridas (ambiente seed)

| Perfil     | E-mail                         | Senha           |
|-----------|--------------------------------|-----------------|
| ADMIN     | robertaobolivia@gmail.com      | @Hipcom123789   |
| ANALISTA  | analista.teste@suporte.local   | Teste@123       |
| SUPERVISOR| wesley.silva@suporte.local *   | Wesley@123      |

\* Confirmar `perfilAcesso=SUPERVISOR` no cadastro (ADMIN) se o login não refletir o menu esperado.

---

## ADMIN

| Item            | Menu visível | Página / API                         | Resultado esperado |
|-----------------|-------------|--------------------------------------|--------------------|
| Dashboard       | Sim         | `/` dashboard + GET `/api/dashboard/resumo` | OK |
| Tickets         | Sim         | tickets + GET `/api/tickets`         | OK |
| Clientes        | Sim         | clientes                             | OK |
| Atendentes      | Sim         | atendentes + gestão analistas        | OK |
| Auditoria       | Sim         | GET `/api/auditoria/eventos`         | 200 |
| Configurações   | Sim         | horário útil, feriados, metas SLA, etiquetas | OK |
| Relatórios      | Sim         | relatorios                           | OK |
| Indicadores     | Sim         | indicadores (subpáginas)           | OK |
| Abrir ticket    | Sim         | abrir-ticket                         | OK |
| Perfil          | Sim         | perfil                               | OK |
| Login/logout    | —           | login → reload (F5) mantém sessão    | OK |
| Logout          | —           | volta à tela de login                | OK |

**Configurações (ADMIN):** listar/criar/editar/ativar/inativar etiquetas; salvar horário útil; feriados; metas SLA.

---

## SUPERVISOR

| Item            | Menu visível | Bloqueio UI / API                    | Resultado esperado |
|-----------------|-------------|--------------------------------------|--------------------|
| Dashboard       | Sim         | —                                    | OK |
| Tickets         | Sim         | —                                    | OK |
| Chats           | Sim         | —                                    | OK |
| Relatórios      | Sim         | —                                    | OK |
| Indicadores     | Sim         | —                                    | OK |
| Atendentes      | Sim         | sem bloco “Administração de analistas” | OK |
| Perfil          | Sim         | —                                    | OK |
| Configurações   | Sim         | somente seção **Etiquetas** visível; horário/feriados/SLA ocultos (`.config-admin-only`) | OK |
| Auditoria       | **Não**     | menu oculto; API auditoria → 403     | OK |
| Clientes        | **Não**     | menu oculto                          | OK |
| Abrir ticket    | **Não**     | menu oculto                          | OK |

**Configurações (SUPERVISOR):** CRUD de etiquetas via UI e `POST/PUT/PATCH /api/etiquetas/*`. `PUT /api/horarios-uteis/padrao`, feriados e `PUT /api/sla-metas` → **403**.

---

## ANALISTA

| Item            | Menu visível | Bloqueio UI / API                    | Resultado esperado |
|-----------------|-------------|--------------------------------------|--------------------|
| Dashboard       | Sim         | —                                    | OK |
| Abrir ticket    | Sim         | —                                    | OK |
| Tickets         | Sim         | —                                    | OK |
| Chats           | Sim         | —                                    | OK |
| Perfil          | Sim         | —                                    | OK |
| Clientes        | **Não**     | menu oculto                          | OK |
| Atendentes      | **Não**     | menu oculto                          | OK |
| Relatórios      | **Não**     | menu oculto                          | OK |
| Indicadores     | **Não**     | menu oculto                          | OK |
| Configurações   | **Não**     | menu oculto; `showPage('configuracoes')` → mensagem amigável | OK |
| Auditoria       | **Não**     | GET `/api/auditoria/eventos` → 403   | OK |
| Listar analistas| —           | GET `/api/analistas` → 403           | OK |

**Etiquetas:** não gerencia em Configurações; no Chats pode usar etiquetas **ativas** já vinculadas ao ticket (leitura/salvamento de vínculos conforme fluxo do ticket).

---

## Registro da execução (2026-05-20 — Sprint 72)

- [x] `mvn clean install` — BUILD SUCCESS
- [x] HTTP 200 em `http://localhost:8080/`
- [x] ADMIN (`robertaobolivia@gmail.com`): login OK; dashboard 200; auditoria 200; GET `/api/analistas` 200
- [x] ANALISTA (`smoke.analista72@suporte.local` / `Valida72xy`, criado no smoke): login OK; dashboard 200; auditoria 403; analistas 403
- [x] SUPERVISOR (`wesley.silva@suporte.local`, perfil ajustado para SUPERVISOR no teste): login OK; dashboard 200; auditoria 403; analistas 403
- [x] UI: `PAGE_ACCESS_BY_PERFIL` alinhado ao checklist (menus ocultos + `showPage` com mensagem amigável)

**Atualização Sprint 150:** matriz de Configurações e etiquetas alinhada à Sprint 148/149 (SUPERVISOR com acesso parcial a Configurações).

## Falhas reais encontradas

- Nenhum bug corrigido nesta sprint: permissões UI/API coerentes com o desenho atual.
- Pendência: smoke visual no navegador (clique menu a menu) não automatizado aqui.

## Falhas conhecidas / pendências

- Login inválido: HTTP 401 no backend (Sprint 73); mensagem amigável no frontend (Sprint 71).
- Validação visual completa no navegador recomendada após cada release (incluir Configurações → Etiquetas para SUPERVISOR).
