# Sprint 268 — Chats sem carregar informações

## Diagnóstico

| Verificação | Resultado |
|-------------|-----------|
| `GET /api/tickets` (ADMIN) | 12 tickets (4+4+4 por status) |
| Playwright Chats | Passou (`chats-encerramento-sem-pesquisa`) |
| Backend alterado | Não |

Cenários que deixam a lista vazia sem mensagem clara:

1. Resposta não-array (envelope) → `allTickets = []` antes da correção.
2. Exceção em `buildChatsListCardHtml` em um item → lista limpa no meio do `forEach`.
3. Loader assíncrono rejeitado sem handler no router (alert só no `alertBoxChats` por 6s).

## Correções (front)

- `coerceTicketsList` em `chatsService.listTicketsBase`.
- Render da lista com try/catch por ticket.
- `syncChatsPageIdleFromTickets` após carga.
- Router: `.catch` em loaders que retornam Promise.
- Cache: `app.js?v=sprint268`.

## Smoke sugerido

1. Login ADMIN → Chats.
2. Abas Atendendo (4), Fila (4), Encerrados (4).
3. Clicar ticket ativo e encerrado; painel/timeline carrega.
4. Ctrl+F5 se ainda vazio.
