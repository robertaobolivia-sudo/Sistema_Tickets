# Sprint F45 — Suite E2E única pós-reestruturação

## Objetivo

Agregar F42 + F43 + F44 em **uma** execução Playwright, massa única por timestamp, recriação automática se snapshot inválido.

## Artefatos

| Tipo | Caminho |
|------|---------|
| Spec principal | `e2e/tests/smoke-pos-reestruturacao.spec.ts` |
| Massa | `e2e/tests/helpers/massaPosReestruturacao.ts` → `.massa-pos-reestruturacao.json` |
| UI compartilhada | `e2e/tests/helpers/e2eUi.ts` |
| Export CSV/PDF | `e2e/tests/helpers/f44Massa.ts` |
| README | `e2e/README.md` |

## Massa (1 `ts`)

- **Cliente E2E Pos** `{ts}`
- **Contato manual** + **Contato receptivo** (mesmo Cliente, telefones distintos — regra ticket ativo por Contato)
- **Matriz WhatsApp** do Cliente
- **Ticket** `ATIVO_MANUAL` (`POST /api/tickets`)
- **Ticket** `RECEPTIVO_WHATSAPP` (`POST /api/integracoes/whatsapp/mensagens`)

`ensureMassaPosReestruturacao` valida snapshot e recria se inválido. **Spec F45** chama `criarMassaPosReestruturacao` a cada run (massa sempre fresca — Chats estável).

## Cobertura (12 pontos)

1. Login UI  
2. Dashboard  
3. Cliente listagem + Contatos  
4. Ticket ATIVO_MANUAL (API assert + tela Abrir Ticket)  
5. Ticket RECEPTIVO_WHATSAPP (API assert)  
6. Chats ticket manual  
7. Chats ticket receptivo  
8. Network Chats sem `/api/carteiras`  
9. Network Chats sem `/uploads/conexoes`  
10. Relatórios filtro origem (UI)  
11. CSV + PDF (API)  
12. Config Conexões/Revendas isolada  

## Execução

Ver bloco F45 em `e2e/README.md`.

## Resultado (2026-05-24)

- Playwright F45: **1 passed**
- Vitest: **219/219**
- `mvn test`: **OK**
- `mvn package -DskipTests`: **OK** (após `taskkill java`)
- HTTP `/`: **200**

## Próximo

Opcional: CI job só `test:pos-reestruturacao`; deprecar specs F42–F44 quando F45 estável N sprints.
