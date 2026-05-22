# Sprint 223 — Validação Sprint 222 (8080 + Playwright)

## Objetivo

Subir aplicação, confirmar HTTP 200 e suite E2E (3 specs) após UX “Sim desabilitado” sem contato (Sprint 222).

## Resultado (2026-05-21)

| Verificação | Resultado |
|-------------|-----------|
| HTTP `http://localhost:8080/` | **200** |
| `cd e2e && npm test` | **3 passed** (~7,4s) |
| Sem contato: `encerrar-pesquisa-sim` disabled | OK (spec 3) |

## Causa da pendência anterior

- Processo na 8080 parado após `mvn package` (JAR não estava servindo na hora do E2E).
- Tentativas com `Start-Process -WindowStyle Hidden` não aguardaram subida completa do Spring (~2 min em alguns ambientes).

## Correção

Nenhuma alteração de código. Subida em terminal com `java -jar target\suporte-tickets-1.0.0.jar` até log `Started SuporteTicketsApplication`.

## Sprint 222

**Aprovada** — UX + regressão E2E validadas nesta sprint.
