# Sprint 240 — Ajuste de cores Chats (padrão corporativo)

## Objetivo

Alinhar **cores**, contrastes, bordas, sombras e estados da tela Chats aos tokens `--corp-*`, sem mudar layout nem comportamento.

## Backup

`Sistemas_BKP\BKP_Sprint_240_Chats_Cores`

## Arquivo alterado

- `src/main/resources/static/css/pages/chats.css` — somente propriedades de cor/borda/sombra/foco (variáveis locais `--chats-*` de radius/tamanho intactas)

**Não alterados:** `index.html`, JS, backend.

## Mudanças de cor (resumo)

| Área | Antes (azul legado) | Depois |
|------|---------------------|--------|
| Abas ativas / foco | `--primary` / `#2563eb` | `--corp-accent-neon` / `--corp-bg-deep` (texto) |
| Lista — item ativo | azul 6% mix | mix neon/teal |
| Busca lateral | foco azul | `--corp-focus-ring` |
| Layout shell Chats | `--border` | `--corp-border-strong` + sombra suave |
| Header conexão (fallback) | primary mix | `--corp-bg-deep` + neon |
| Timeline / bolhas / tags / composer | tons azuis | neon translúcido |
| Tema escuro | `#93c5fd` etc. | `--corp-accent-neon` |

## Layout preservado

Grid 3 colunas, alturas de tabs (52px), paddings, gaps, `grid-template-columns`, composer, timeline e painel direito **inalterados** (apenas tokens de cor nas mesmas regras).

## Testes

- Vitest/E2E: não executados (somente CSS).
- Smoke manual: Fila, Atendendo, Encerrados, abrir conversa, painel, timeline, composer; alternar tema claro/escuro.

## GitHub

`Sprint 240 — Ajuste de cores Chats`

## Próximo passo

Sprint 241 — concluída (`SPRINT_241_TICKETS_MODAIS_VISUAL.md`). Próximo: Sprint 242 Dashboard + Indicadores.
