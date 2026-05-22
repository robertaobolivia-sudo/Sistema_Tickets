# Sprint 246 — Visual corporativo telas complementares e menu

## Objetivo

Alinhar Atendentes, Auditoria, Abrir Ticket e Perfil ao padrão `--corp-*` e simplificar o menu lateral (Perfil só pela topbar).

## Backup

`Sistemas_BKP\BKP_Sprint_246_Telas_Complementares_Menu`

## Arquivos

| Arquivo | Alteração |
|---------|-----------|
| `css/pages/atendentes.css` | Bloco `#page-atendentes` |
| `css/pages/auditoria.css` | Bloco `#page-auditoria` |
| `css/pages/perfil.css` | Bloco `#page-perfil` |
| `css/pages/abrir-ticket.css` | Bloco `#page-abrir-ticket` |
| `css/layout.css` | Ocultar `.nav-item[data-page="perfil"]` na sidebar |
| `index.html` | Cache `?v=sprint246` nas folhas acima |

**Inalterados:** backend, schema, JS, IDs, `data-testid`. Página `#page-perfil` e rota `showPage('perfil')` via topbar **Meu Perfil**.

## Menu

- Item lateral **Perfil** oculto (`display: none !important`).
- Acesso equivalente: avatar/menu topbar → **Meu Perfil** (`data-user-menu="perfil"`).

## Smoke sugerido

Atendentes (kanban/admin) · Auditoria (filtros, paginação, CSV) · Abrir Ticket (busca cliente, submit se seguro) · Perfil via topbar · tema claro/escuro.

## GitHub

`Sprint 246 — Visual corporativo telas complementares e menu`

## Próximo passo

Validação E2E/smoke geral ou fechamento da fase visual do blueprint; documentar **REESTRUTURAÇÃO CONCLUIDA** apenas quando o último sprint real de domínio/visual for aprovado pelo produto.
