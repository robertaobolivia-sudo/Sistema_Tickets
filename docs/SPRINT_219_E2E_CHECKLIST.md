# Sprint 219 — E2E: checklist operacional e estabilização Playwright

## Objetivo

Documentar e padronizar a execução do Playwright para regressão do fluxo Chats → encerramento → pesquisa → avaliação pública.

## Entregas

- `e2e/README.md` — checklist completo, pré-requisitos, variáveis, falhas comuns, smoke manual (Ctrl+F5).
- `e2e/global-setup.ts` — logs (`baseURL`, e-mail mascarado, ticket, confirmação de sobrescrita de `.massa.json`).
- `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md` — entrada Sprint 219.

## Critérios de aceitação

| Critério | Status |
|----------|--------|
| README explica execução do zero | OK |
| `SMOKE_ADMIN_*`, `E2E_SKIP_WEB_SERVER`, Chromium | OK |
| HTTP 200 e falhas comuns | OK |
| Playwright passando | 1 passed (~3,7s), `E2E_SKIP_WEB_SERVER=1`, ticket setup TK-000113 |
| Sem alteração de regra de negócio / UI | OK (só docs + logs setup) |

## Testes

```powershell
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'   # se app já em 8080
npm test
```

## Backup

`Sistemas_BKP/BKP_Sprint_219_E2E_Checklist`
