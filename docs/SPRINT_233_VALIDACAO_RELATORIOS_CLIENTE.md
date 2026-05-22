# Sprint 233 — Smoke visual Relatórios (Cliente + filtros + CSV)

## Ambiente

- HTTP **200** em `http://localhost:8080/`
- Login ADMIN (sessão já ativa no navegador de teste)
- App: `suporte-tickets-1.0.0.jar` na porta 8080

## Cliente usado

- **ID 61** — Status Automação (37+ tickets na API; listagem com só `clienteId=61` → resumo **148** tickets no relatório)

## Validações

| Item | Resultado |
|------|-----------|
| Página Relatórios abre | OK (`data-page=relatorios`) |
| Select Cliente (`relatorioFiltroCliente`) | OK — **21** opções (Todos + ativos) |
| `clienteId` na busca | OK — `/api/tickets/busca?clienteId=61` |
| Filtros combinados | OK — `clienteId=61&motivoId=1&statusPesquisa=NAO_ENVIADA&notaAvaliacao=5` (listagem 0 com esse combo restritivo) |
| CSV tickets | OK — `/api/tickets/relatorios/csv?...clienteId=61...` |
| Satisfação resumo/evolução | OK — requests com `clienteId` quando Cliente selecionado |
| Limpar Cliente + nova busca | OK — `/api/tickets/busca` **sem** `clienteId` |
| Console | OK — sem erros críticos capturados no smoke |

## Sprint 232

**Validada visualmente no navegador** (Sprint 233).

## Observação

Combinação Cliente + Motivo + Status pesquisa + Nota pode retornar **0** tickets se não houver registros com esse conjunto; isso é comportamento esperado dos filtros, não falha do `clienteId`.
