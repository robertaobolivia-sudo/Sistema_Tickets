# Sprint 260 — Estabilizar massa DEV Clientes B2B

## Causa do retorno para 8 clientes

`Sprint94AnalistasSeedConfig` executava em **toda subida** (`@Order(100)`):

- `criarClientesConexoes()` criava 4 registros por nome curto (FastComércio, Fênix, Rocha Mendes, Status Automação).
- `findByNome` não encontrava os oficiais S253 (nome = responsável, razão LTDA).
- Resultado: ids novos (ex. 106–109) somados aos 4 oficiais (87–90).

## Correção

1. Seed 94: apenas `salvarOuAtualizarAnalista(dutyBreaker())`.
2. `DevClientesMassaSanitizer` — lógica única de dedup/saneamento.
3. `Sprint260DevClientesMassaGuardConfig` — `@Order(255)`, `app.dev.clientes-massa-guard=true`, roda a cada boot.
4. `app.sprint256.dedup-clientes-dev=false` (guard substitui dedup manual).

## Evidência API

| Reinício | clientes | contatos | tickets | HTTP |
|----------|----------|----------|---------|------|
| 1 | 4 | 12 | 12 | 200 |
| 2 | 4 | — | — | — |

## Browser

Validar manualmente: login → Clientes Listagem (4 linhas) → Contatos com filtro por contratante.
