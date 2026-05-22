# Sprint 231 — Validação visual Dashboard (período + Cliente)

## Objetivo

Fechar smoke visual da Sprint 230 no navegador.

## Ambiente

- HTTP `GET /` → **200**
- Login: `robertaobolivia@gmail.com` (ADMIN)
- App: `http://localhost:8080/`

## Smoke (Dashboard → Encerramento e satisfação)

| Critério | Resultado |
|----------|-----------|
| Dashboard abre após login | OK |
| Select **Período** (7 / 30 / 90) | OK (3 opções) |
| Select **Cliente** | OK (`dashboardEncFiltroCliente`) |
| Request `dias=7` | OK |
| Request `dias=90` | OK |
| Request `dias=30` | OK |
| Cliente **Audit Test** (`clienteId=66`) | OK |
| “Todos os clientes” sem `clienteId` | OK |
| Hint de período atualiza | OK (ex.: 20/02/2026 a 21/05/2026 em 90 dias) |
| Cards renderizam | OK |
| Console erro crítico | Nenhum no fluxo automatizado |

## Sprint 230

**Validada visualmente** (Sprint 231).

## Ajustes de código

Nenhum.

## Próximo passo

Retomar linha principal da reestruturação ou próximo item do backlog.
