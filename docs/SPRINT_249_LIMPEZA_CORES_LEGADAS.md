# Sprint 249 — Limpeza global de cores legadas no corpo

Log: **Sprint 249 — Limpeza global de cores legadas no corpo**.

## Objetivo

Remover azul legado (#1e40af, #2563eb, #dbeafe, etc.) como identidade principal do corpo das páginas; alinhar títulos, avatares, badges, cards e focos aos tokens `--corp-*`.

## Arquivos

- `css/theme.css` — `--primary` teal; prioridade MÉDIA e `--corp-info-soft`
- `css/layout.css`, `css/base.css`, `css/components.css`, `css/modals.css`
- `css/pages/perfil.css`, `atendentes.css`, `chats.css`
- `index.html` — cache `?v=sprint249`; logo SVG teal
- `js/pages/etiquetasConfigSection.js` — cor padrão etiqueta

## Smoke visual sugerido

Perfil, Dashboard, Clientes, Chats, Tickets, Relatórios, Indicadores, Configurações — tema claro e escuro.
