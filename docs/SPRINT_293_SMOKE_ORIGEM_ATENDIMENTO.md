# Sprint 293 — Smoke origem do atendimento por telefone

## Operação

1. Encerrado processo Java na porta 8080.
2. `mvn package -DskipTests` (JAR reempacotado).
3. `java -jar target/suporte-tickets-1.0.0.jar` — app em **8080**.
4. `GET http://localhost:8080/` → **200**.

## Correções diretas (smoke)

| Bug | Correção |
|-----|----------|
| `findFirstUltimoEncerradoPorClienteEContato` retornava 2 linhas (500) | `PageRequest.of(0, 1)` na query de último encerrado |
| `gerarNovoTicket` ignorava telefone da entrada | Campo `telefone_entrada` na pendência + uso no webhook |
| Matriz id `2` inexistente neste ambiente | Smoke usa **whatsappMatrizId = 5** (cliente 89) |

## Smoke API (script `scripts/sprint293-smoke-origem.ps1`)

Massa: contato **69**, cliente **89**, principal `5511980030111`, adicional `5512942833853`.

| Cenário | Ticket | `contato_id` | `atendimento_telefone_tipo` |
|---------|--------|--------------|-----------------------------|
| Entrada principal | **TK-000333** | 69 | **PRINCIPAL** |
| Entrada adicional | **TK-000334** | 69 | **ADICIONAL** |

Histórico `GET /api/contatos/69/historico-tickets`: ambos os protocolos com tipo correto.

Fluxo real: após encerrar ticket ativo, entrada WhatsApp gera **pendência** → `POST /api/chats/interacoes-pendentes/{id}/gerar-ticket` (com `telefone_entrada` na pendência).

## Smoke navegador (manual)

1. Hard refresh: `app.js?v=sprint292-origem-atendimento`.
2. **Chats** → abrir **TK-000333** → painel **Chamado** → linha **Origem do atendimento** → `5511980030111 (Principal)`.
3. Abrir **TK-000334** → `5512942833853 (Adicional)`.
4. **Clientes → Contatos** → histórico do contato 69 → coluna **Origem do atendimento** nos dois protocolos.

## Testes automatizados nesta sprint

- Smoke API (script PowerShell).
- `mvn package -DskipTests` após correções Java.
- `npm test` não executado (sem alteração JS).

## Riscos

- Tickets antigos sem `atendimento_*` até nova entrada.
- Ambiente com vários encerrados: depende de paginação no “último encerrado” (corrigido).

## Próximo passo

Origem por mensagem no thread; remoção de telefone adicional; E2E Playwright com `E2E_WHATSAPP_MATRIZ_ID=5`.
