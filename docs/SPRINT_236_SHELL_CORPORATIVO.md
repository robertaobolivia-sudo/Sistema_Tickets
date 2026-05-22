# Sprint 236 — Shell corporativo (sidebar, topbar, navegação)

## Objetivo

Aplicar tokens `--corp-*` no shell autenticado sem redesenhar páginas internas nem o login.

## Backup

`Sistemas_BKP\BKP_Sprint_236_Shell_Corporativo` (`layout.css`, `theme.css`)

## Arquivos alterados

| Arquivo | Mudança |
|---------|---------|
| `css/layout.css` | Sidebar teal, nav hover/ativo neon, topbar borda/sombra, notificações e menu usuário |
| `css/theme.css` | Overrides tema escuro do shell alinhados a `--corp-*` |

**Inalterados:** `login.css`, HTML, JS, backend, CSS de páginas (`dashboard.css`, etc.).

## Mudanças visuais

- **Sidebar:** fundo `#0F2F3A` (`--corp-sidebar-bg`), borda neon sutil, expansão com sombra corporativa.
- **Nav:** hover com fundo neon translúcido; ativo com texto/barra `--corp-accent-neon` e `inset` neon; submenu com borda neon; foco com `--corp-focus-ring`.
- **Topbar:** borda inferior teal (`--corp-border-strong`), sombra suave; botão notificações com superfície muted corporativa; avatar/menu com foco neon; menu dropdown com radius/sombra corp.

## Tema claro / escuro

| Área | Claro | Escuro |
|------|-------|--------|
| Sidebar | Teal corporativo (igual) | Teal corporativo (remove azul legado `#1e3a5f`) |
| Topbar | `var(--surface)` + bordas corp | `var(--corp-surface-elevated)` + bordas corp |
| Nav ativo | Neon em fundo teal | Idem (tokens RGB) |
| Conteúdo `.page` | Sem alteração | Sem alteração |

## Smoke visual (manual)

| Tela | Esperado |
|------|----------|
| Login | Igual Sprint 229 (isolado em `login.css`) |
| Dashboard | Shell novo; cards/conteúdo legado |
| Clientes | Idem |
| Chats | Idem |
| Relatórios | Idem |

## Testes automatizados

Não executados (somente CSS).

## Próximo passo

**Sprint 237** — Login polish + Avaliação pública com tokens (blueprint).
