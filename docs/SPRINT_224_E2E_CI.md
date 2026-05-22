# Sprint 224 — E2E/CI: execução automatizada local

## Objetivo

Script operacional para regressão Playwright (3 specs) com build, app 8080 e resumo.

## Entrega

- `scripts/run-e2e-local.ps1`
- `e2e/README.md` — seção regressão completa + tabela de parâmetros

## Como executar

```powershell
cd C:\Users\João Falcone\Desktop\Sistema\suporte-tickets
.\scripts\run-e2e-local.ps1
```

Variantes:

```powershell
.\scripts\run-e2e-local.ps1 -SkipPackage -KeepServer
.\scripts\run-e2e-local.ps1 -UsePlaywrightServer
```

## E2E_SKIP_WEB_SERVER

| Cenário | Valor |
|---------|--------|
| Script `run-e2e-local.ps1` (padrão) | `1` — app já subiu no script |
| `-UsePlaywrightServer` | não definido — Playwright sobe/reutiliza JAR |
| Dev rápido (app manual) | `1` antes de `npm test` |
| CI sugerido | não definir — `webServer` no `playwright.config.ts` |

## Parar o Java

- Script sem `-KeepServer`: encerra o PID que iniciou.
- Manual: `Stop-Process -Id (Get-Content logs\e2e-app.pid)` ou fechar a janela do `java -jar`.
- Porta 8080: `Get-NetTCPConnection -LocalPort 8080` → `Stop-Process` no `OwningProcess`.

## Resultado da validação (2026-05-21)

`.\scripts\run-e2e-local.ps1 -SkipPackage -KeepServer` → **HTTP 200**, Playwright **3 passed** (~7s).
