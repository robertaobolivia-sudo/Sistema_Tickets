# Sprint 232 — Relatórios: filtro Cliente (clienteId)

## Objetivo

Alinhar Relatórios com Indicadores/Dashboard: seleção de **Cliente** por ID (`clienteId`) na busca de tickets, CSV de tickets e bloco de satisfação (resumo, evolução, CSV).

## Alterações

- UI: `relatorioFiltroCliente` passou de texto para `<select>` (“Todos os clientes” + ativos).
- Frontend: `buildRelatorioBuscaParams` / `buildSatisfacaoResumoParams` enviam `clienteId`; vazio não envia parâmetro.
- Backend tickets: `TicketFiltroDTO.clienteId` + `TicketBuscaService` (igual por id; `cliente` string legado).
- Backend satisfação: `clienteId` em repository, consulta, resumo, evolução, CSV e controller.
- Seção satisfação: removido input duplicado; usa o filtro Cliente do formulário principal.

## Critérios de aceitação

| Item | Status |
|------|--------|
| Filtrar por Cliente nos Relatórios | OK |
| Busca com `clienteId` | OK |
| CSV tickets com `clienteId` | OK |
| Motivo / Status pesquisa / Nota | Mantidos |
| “Todos os clientes” sem `clienteId` | OK |

## Smoke manual

1. Relatórios → selecionar Cliente → Gerar relatório (rede: `clienteId=`).
2. Exportar CSV tickets (mesma query).
3. Limpar → sem `clienteId`.
4. Atualizar/exportar satisfação com mesmo Cliente.

**Validação visual:** Sprint 233 — `docs/SPRINT_233_VALIDACAO_RELATORIOS_CLIENTE.md`

## Backup

`Sistemas_BKP/BKP_Sprint_232_Relatorios_Cliente`
