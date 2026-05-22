# Sprint 238 — Fechar regressão Playwright (Sprint 237)

## Causa raiz

1. **HTTP 500 em `GET /api/tickets/busca?textoLivre=...`** — `TicketBuscaService` aplicava `lower()` em `mensagemInicial` (`@Lob`/CLOB no MySQL), gerando erro Hibernate: *Parameter 1 of function 'lower()' has type 'STRING', but argument is of type 'CLOB'*.
2. **Corrida no front** — ao abrir a página Tickets, `loadTicketTableDefault()` podia concluir depois da busca avançada e substituir a tabela (flaky na suíte completa).

Não era falha de massa do ticket sem contato nem regressão visual da Sprint 237.

## Correções

| Área | Alteração |
|------|-----------|
| `TicketBuscaService.java` | Texto livre deixa de incluir `mensagemInicial` no `OR` (evita 500); busca por número/cliente/conexão/categorias mantida |
| `ticketsPage.js` | Sequência `ticketTableLoadSeq` ignora respostas antigas da listagem padrão |
| `e2e/global-setup.ts` | Valida cada ticket criado via API de busca antes dos testes |
| `e2e/tests/helpers/ticketsUi.ts` | Helper: limpar filtros, buscar, aguardar resposta 200 |
| Specs E2E | Usam o helper compartilhado |

## Testes

```
cd e2e && npm test
→ 3 passed (pesquisa, sem pesquisa, sem contato)
```

`mvn package -DskipTests` após parar processo na porta 8080.

## GitHub

Mensagem sugerida: **Sprint 238 — Fechar regressão Playwright Sprint 237**

## Pendência produto

Reintroduzir busca por conteúdo da mensagem inicial com `cast` CLOB→VARCHAR compatível MySQL (sprint futura).

## Próximo passo

Sprint 239 (blueprint) — repaginação visual **Clientes**.
