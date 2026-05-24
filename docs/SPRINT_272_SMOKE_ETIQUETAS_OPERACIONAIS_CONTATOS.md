# Sprint 272 — Smoke etiquetas operacionais em Contatos

## Objetivo

Validar fluxo analista: catálogo, marcação, destaque, aviso no modal, filtro, Histórico, Ver conversa — sem invalidação automática de tickets.

## Evidência E2E (2026-05-22)

| Item | Resultado |
|------|-----------|
| Playwright `sprint272-etiquetas-operacionais-contatos.spec.ts` | **1 passed** (~7s) |
| HTTP `http://127.0.0.1:8080/` | **200** |
| Contatos usados | Primeiros dois de `GET /api/contatos?gestao=true` (ex.: id **69** + segundo da lista no run) |
| Etiquetas aplicadas | Contato A: **Propaganda**; Contato B: **Indevido** |
| Filtro | UI (Propaganda) + API `gestao=true&etiquetaId=` contém id A |
| Histórico / Ver conversa | Painel `.contato-historico-panel`; botão **Ver conversa** abre Chats quando disponível |
| Tickets | Snapshot `historico-tickets` antes/depois **inalterado** (sem regra automática) |

## Correções diretas na sprint

1. **`contatoGestaoHistoricoView.js`**: `COLUNAS_TABELA` = 10 (alinha tabela Contatos).
2. **`clientesPage.js`**: painel Histórico preservado ao recarregar lista (`reabrirHistoricoId` / `montarPainelHistoricoParaContato`).
3. **E2E**: limpar filtros avançados (evita **Com avaliação ruim** marcado zerar lista); contatos via API + busca por WhatsApp.

## Roteiro manual (complementar)

1. Clientes → Contatos → **Limpar avançados** se a lista vier vazia com filtros antigos.
2. Configurações → Etiquetas: Indevido, Contato Pessoal, Propaganda ativas.
3. Ver/Editar → marcar etiquetas operacionais → Salvar.
4. Lista, modal aviso, filtro por etiqueta, Histórico, Ver conversa.
5. Conferir tickets do contato (status inalterados).

## Automação

- `e2e/tests/sprint272-etiquetas-operacionais-contatos.spec.ts`
- Massa: `e2e/.massa.json`
