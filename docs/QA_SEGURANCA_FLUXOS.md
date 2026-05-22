# QA manual guiado — Fase segurança (Sprint 74)

Documento de fechamento da fase: autenticação por token, perfis, auditoria, política de senha, mensagens padronizadas e login 401.

Referências: `docs/QA_PERFIS.md`, `docs/AUDITORIA_RETENCAO.md`, `docs/CONTRATOS_E_QA.md`, `MSG_ERRO` / `mensagemErroApi` em `app.js`.

---

## Credenciais (ambiente local seed)

| Uso        | E-mail                      | Senha          | Perfil esperado |
|-----------|-----------------------------|----------------|-----------------|
| ADMIN     | robertaobolivia@gmail.com   | @Hipcom123789  | ADMIN           |
| SUPERVISOR| wesley.silva@suporte.local  | Wesley@123       | SUPERVISOR *    |
| ANALISTA smoke | smoke.analista72@suporte.local | Valida72xy | ANALISTA (criada em QA) |

\* Confirmar perfil no cadastro se necessário.

---

## 1. Autenticação e sessão

| # | Teste | Passos | Esperado | Manual | API (Sprint 74) |
|---|--------|--------|----------|--------|------------------|
| 1.1 | Login válido | POST login com ADMIN | HTTP **200**, corpo com `authToken` | [ ] | [x] |
| 1.2 | Login inválido (senha) | Senha incorreta | HTTP **401**, `erro`: E-mail ou senha inválidos — **não 500** | [ ] | [x] |
| 1.3 | Login inválido (e-mail) | E-mail inexistente | HTTP **401**, mesma mensagem | [ ] | [x] |
| 1.4 | UI login inválido | Tela login, credencial errada | Alerta: **E-mail ou senha inválidos** (sem texto de header/token) | [ ] | — |
| 1.5 | Logout | Menu sair / logout | Volta à tela de login; token limpo | [ ] | [x] POST logout com sessão |
| 1.6 | Reload com sessão | Login → F5 | Permanece logado (localStorage + validação servidor) | [ ] | — |
| 1.7 | Sessão inválida | Token adulterado em chamada API | 403/401 amigável (Sprint 71) | [ ] | — |

---

## 2. Fluxos ADMIN (após login ADMIN)

| # | Área | Passos | Esperado | Manual | API |
|---|------|--------|----------|--------|-----|
| 2.1 | Dashboard | Abrir menu Dashboard | Página carrega; resumo sem erro | [ ] | [x] GET `/api/dashboard/resumo` 200 |
| 2.2 | Tickets | Abrir Tickets | Listagem carrega | [ ] | [x] GET `/api/tickets` 200 |
| 2.3 | Abrir Ticket | Abrir Abrir Ticket | Formulário disponível | [ ] | — |
| 2.4 | Atendentes | Abrir Atendentes | Kanban + bloco Administração de analistas | [ ] | [x] GET `/api/analistas` 200 |
| 2.5 | Auditoria | Abrir Auditoria | Lista eventos; filtros | [ ] | [x] GET `/api/auditoria/eventos` 200 |
| 2.6 | Configurações | Abrir Configurações | Horário útil / feriados / SLA editáveis (ADMIN) | [ ] | [x] GET horários/metas conforme uso |
| 2.7 | Clientes | Abrir Clientes | Página admin clientes | [ ] | — |
| 2.8 | Relatórios | Abrir Relatórios | Página relatórios | [ ] | — |
| 2.9 | Política senha | Cadastrar analista senha fraca | Bloqueio + mensagem regra (8 chars, letra, número) | [ ] | [x] Sprint 70 |
| 2.10 | Retenção auditoria | Contar/excluir antigos (ADMIN) | Confirmação na exclusão; proteção 30 dias | [ ] | — |

---

## 3. Não-ADMIN (bloqueios)

| # | Perfil | Teste | Esperado | Manual | API |
|---|--------|--------|----------|--------|-----|
| 3.1 | ANALISTA | GET `/api/auditoria/eventos` | **403** | [ ] | [x] smoke.analista72 |
| 3.2 | ANALISTA | GET `/api/analistas` | **403** | [ ] | [x] |
| 3.3 | ANALISTA | Menu Auditoria / Config / Clientes | Itens **ocultos** ou mensagem ao forçar URL | [ ] | — |
| 3.4 | SUPERVISOR | Auditoria e Configurações | Menu oculto; API auditoria **403** | [ ] | [x] wesley SUPERVISOR |
| 3.5 | Qualquer | Mensagem acesso negado | Sem JSON técnico; MSG_ERRO (Sprint 71) | [ ] | — |

---

## 4. Auditoria de ações críticas

| # | Ação | Como verificar | Esperado |
|---|------|----------------|----------|
| 4.1 | LOGIN_SUCESSO | Login ADMIN → Auditoria filtrar ação | Evento registrado com analista e IP |
| 4.2 | LOGOUT | Logout → consultar auditoria | Evento LOGOUT (se sessão válida no logout) |
| 4.3 | Alteração perfil / ticket | Operação real → auditoria | Evento com entidade e descrição legível |

---

## 5. Registro de execução automatizada (Sprint 74 — 2026-05-20)

- [x] `mvn clean install` — BUILD SUCCESS
- [x] `http://localhost:8080/` — HTTP 200
- [x] Login válido ADMIN — 200 + authToken
- [x] Login inválido — 401 (senha errada e e-mail inexistente)
- [x] ADMIN: dashboard, tickets, auditoria, analistas — 200
- [x] Não-ADMIN: auditoria/analistas — 403 (conta smoke ANALISTA)
- [ ] Itens marcados **Manual** — validar no navegador (checklist guiado)

## 6. Falhas encontradas nesta sprint

- Nenhuma correção de código na Sprint 74 (somente documentação + smoke API).
- Pendência: rodada **manual** completa (logout, reload F5, cliques em todas as telas ADMIN).

## 7. Histórico da fase segurança (sprints)

| Sprint | Entrega resumida |
|--------|------------------|
| 52–53 | Cadastro analistas + BCrypt |
| 65–68 | Auditoria consulta, UI, CSV |
| 69 | Retenção manual auditoria |
| 70 | Política mínima de senha |
| 71 | Mensagens de erro padronizadas |
| 72 | QA perfis (QA_PERFIS.md) |
| 73 | Login inválido → 401 |
| 74 | Checklist fase segurança (este documento) |
