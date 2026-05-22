# Sprint 243 — Paginação 15 itens nas listas principais

## Objetivo

Paginação frontend reutilizável (15 itens/página) em Clientes e Tickets, com Primeira, Anterior, Próxima e Última; busca/filtro volta à página 1.

## Backup

`Sistemas_BKP\BKP_Sprint_243_Paginacao_15_Listas`

## Arquivos

| Arquivo | Papel |
|---------|--------|
| `js/core/listPagination.js` | Constantes e funções puras (slice, clamp, label) |
| `js/components/listPaginationBar.js` | Barra de navegação reutilizável |
| `js/pages/clientesPage.js` | Cache da lista + paginação |
| `js/pages/ticketsPage.js` | Cache da tabela + paginação |
| `js/tests/listPagination.test.js` | Vitest |
| `index.html` | `#clientesListaPagination`, `#ticketsListaPagination` |
| `css/components.css` | Estilos `.list-pagination-*` |

**Backend:** inalterado.

## Comportamento

- Máximo **15** itens visíveis por página.
- Barra oculta se total ≤ 15.
- `loadClientesPage` / busca tickets / `loadTicketTable` / Limpar filtros / Buscar → **página 1**.
- Troca de página re-renderiza do cache (sem novo request).
- Estado vazio e seleção de cliente preservados (item selecionado pode ficar fora da página atual, mas permanece selecionado no estado).

## Testes

`cd src/main/resources/static/js && npm test`

## GitHub

`Sprint 243 — Paginação 15 itens nas listas principais`

## Próximo passo

Sprint 244 — Relatórios visual corporativo (blueprint).
