# Sprint 278 — Smoke INDEVIDO gerencial (pós-restart)

**Data:** 2026-05-22  
**Ambiente:** `http://127.0.0.1:8080/` — JAR `target/suporte-tickets-1.0.0.jar` reconstruído após `Stop-Process` + `mvn package -DskipTests`.

## Ticket usado

| Campo | Valor |
|-------|--------|
| Número | **TK-000176** |
| Antes | `ABERTO` |
| Ação | `PUT /api/tickets/TK-000176/classificar-indevido` — `motivoOperacional=PROPAGANDA`, `confirmacao=true` |
| Depois | `status=INDEVIDO`, `classificacaoOperacional=PROPAGANDA` |

## Dashboard (`GET /api/dashboard/resumo`)

| Verificação | Resultado |
|-------------|-----------|
| Card **Não atendimento** (`ticketsNaoAtendimento`) | **1** (após classificação) |
| Abertos | 3 (era 4) — indevido não conta como aberto |
| SLA resumo (`/api/dashboard/sla`) | Ticket classificado **não** aparece em críticos; contagem operacional só ativos |

## Tickets

| Verificação | Resultado |
|-------------|-----------|
| `GET /api/tickets/busca` (sem filtro) | **TK-000176 ausente** |
| `GET /api/tickets/busca?status=INDEVIDO` | **TK-000176 presente** |
| `GET /api/tickets` (lista Chats) | **TK-000176 presente** com `INDEVIDO` (esperado) |

## Indicadores (`GET /api/indicadores/chamados`)

| Campo | Valor observado |
|-------|-----------------|
| `totalChamados` | 7 (atendimento válido no período amplo) |
| `totalNaoAtendimento` | **1** |

## Relatórios / CSV (`GET /api/tickets/relatorios/csv?status=INDEVIDO`)

- Cabeçalho inclui **Classificação operacional**.
- Linha TK-000176: Status **Não atendimento (Indevido)**; classificação **Propaganda**.

## Chats (UI)

- Aba **Não atendimento** já existente (Sprint 276); smoke API confirma ticket listável em `GET /api/tickets` para consumo do front.
- Smoke browser manual recomendado: hard refresh `app.js?v=sprint277` → classificar outro ativo → conferir aba e composer somente leitura.

## Código

Nenhum ajuste nesta sprint — somente validação runtime.

## HTTP

`GET /` → **200 OK**.
