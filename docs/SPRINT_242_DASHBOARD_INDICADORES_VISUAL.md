# Sprint 242 — Visual corporativo Dashboard e Indicadores

## Objetivo

Repaginar Dashboard e Indicadores com tokens `--corp-*` (teal/neon), alinhados a Shell, Login, Clientes, Chats e Tickets, sem alterar cálculos, endpoints, JS ou filtros validados.

## Backup

`Sistemas_BKP\BKP_Sprint_242_Dashboard_Indicadores_Visual`

## Arquivos alterados

| Arquivo | Alteração |
|---------|-----------|
| `css/pages/dashboard.css` | Bloco `#page-dashboard` Sprint 242; correção de chave CSS em `.waiting-alert` |
| `css/pages/indicadores.css` | Bloco `#page-indicadores` Sprint 242 (filtros, cards, blocos, tabelas, empty) |
| `index.html` | Cache `dashboard.css` / `indicadores.css` → `?v=sprint242` |

**Não alterados:** backend, schema, JS, Relatórios.

## Mudanças visuais (resumo)

- **Dashboard:** header, cards KPI, `content-section`, filtros período/Cliente, métricas, tabelas críticos, botão Atualizar secundário.
- **Indicadores:** header/subtítulo, painéis de filtro (Chamados, Satisfação, Encerramento), cards métricos, blocos e `data-table`, placeholders/empty, botões primários (`button` e `btn`).

Prioridades no Dashboard mantêm tokens semânticos `--pri-*` existentes.

## Filtros preservados

- Dashboard: `dashboardEncFiltroDias` (7/30/90), `dashboardEncFiltroCliente`.
- Indicadores Encerramento: datas, Cliente, Motivo, Status pesquisa, Nota (`indicEnc*`).
- Indicadores Chamados/Satisfação: datas e filtros atuais inalterados no HTML/JS.

## Testes

- Maven/npm: não executados (somente CSS + cache).
- E2E: não obrigatório (sem mudança de seletor).

## Smoke sugerido

1. Dashboard → cards → Encerramento (7/30/90 + Cliente).
2. Indicadores → Chamados → Atualizar.
3. Indicadores → Encerramento e satisfação → filtros Cliente/Motivo/Status/Nota → Atualizar.
4. Tema claro/escuro.

## GitHub

`Sprint 242 — Visual corporativo Dashboard e Indicadores`

## Próximo passo

Sprint 243 — Relatórios (visual corporativo), conforme blueprint.
