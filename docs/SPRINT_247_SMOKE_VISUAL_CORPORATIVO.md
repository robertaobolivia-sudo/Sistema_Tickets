# Sprint 247 — Smoke geral visual corporativo (235–246)

## Objetivo

Validar no navegador a repaginação teal/neon após as sprints 235–246: consistência, contraste, navegação e ausência de regressão funcional.

## Tipo

Sprint de validação (documental + smoke). Sem alteração de regra de negócio ou backend.

## Ambiente

- URL: `http://localhost:8080/`
- HTTP: **200 OK**
- App: JAR em execução (estáticos embutidos podem estar **anteriores** ao último `index.html` do repositório — ver pendências)

## Smoke executado (automação + amostra manual)

| Área | Resultado |
|------|-----------|
| Login | Tela corporativa carrega; sessão ADMIN OK |
| Dashboard | Dados e filtros período/cliente; cards e seções visíveis |
| Clientes | Navegação OK; lista/busca renderiza |
| Topbar | Avatar + menu usuário OK |
| **Meu Perfil (topbar)** | Abre `#page-perfil` (foto, grid) — **OK** |
| Sidebar Perfil | Item ainda **visível** no JAR atual (regra Sprint 246 não no pacote servido) |
| Tema | `data-theme=dark` no smoke; menu oferece alternância (Tema Claro/Escuro) |
| Console | Sem erros críticos observados na sessão smoke |

Telas não clicadas uma a uma nesta automação, mas presentes no app e com CSS `--corp-*` no **código-fonte**: Tickets, Chats, Relatórios, Indicadores, Configurações, Atendentes, Auditoria, Abrir Ticket.

## Cache CSS (`index.html` — repositório)

| Folha | `?v=` |
|-------|--------|
| layout | sprint246 |
| login | sprint237 |
| modals | sprint241 |
| dashboard / indicadores | sprint242 |
| clientes | sprint239 |
| tickets | sprint241 |
| chats | sprint240 *(ajuste 247)* |
| relatorios | sprint244 |
| configuracoes | sprint245 |
| atendentes / auditoria / perfil / abrir-ticket | sprint246 |
| components | sprint243 *(ajuste 247)* |

## Correções nesta sprint (somente cache)

- `chats.css?v=sprint240` (antes sprint138 no HTML)
- `components.css?v=sprint243` (antes sprint112 — inclui paginação Sprint 243)

## Pendências / riscos

1. **`mvn package -DskipTests` + reiniciar JAR** para refletir layout Sprint 246 (ocultar Perfil na sidebar) e todos os bumps de CSS no browser.
2. Após rebuild: smoke manual completo em **claro e escuro** em todas as rotas do checklist.
3. **REESTRUTURAÇÃO CONCLUIDA** — registrar só após aprovação de produto com JAR atualizado e smoke assinado.

## GitHub

`Sprint 247 — Smoke geral visual corporativo`

## Próximo passo

Rebuild/deploy estáticos → smoke manual final → decisão de fechamento da fase visual (235–247).
