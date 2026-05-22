# Sprint 227 — Indicadores: filtro Cliente (Encerramento e satisfação)

## Objetivo

Expor na UI o parâmetro `clienteId` já suportado por `GET /api/indicadores/encerramento-avaliacao` (Sprint 202).

## Alterações

- `index.html` — `<select id="indicEncFiltroCliente">` (opção vazia = visão geral).
- `indicadoresPage.js` — carrega clientes via `clienteService.listOrSearch('', true)`; envia `clienteId` no request.
- `encerramentoAvaliacaoView.js` — `filterClientesAtivosIndicadores()`; `buildIndicadoresEncerramentoAvaliacaoParams` já incluía `clienteId`.

## Testes

- `encerramentoAvaliacaoView.test.js` — query com/sem `clienteId`; filtro de ativos.

## Backup

`Sistemas_BKP/BKP_Sprint_227_Indicadores_Filtro_Cliente`

## Validação (Sprint 228)

- Aprovada em 2026-05-21 — ver `docs/SPRINT_228_VALIDACAO_SPRINT_227.md`
