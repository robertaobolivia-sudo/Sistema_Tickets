# Sprint 225 — CI: GitHub Actions E2E

## Workflow

`.github/workflows/e2e.yml` — job `e2e` em `ubuntu-latest`.

## Pipeline

1. Service **MySQL 8** (`suporte_tickets`, root / `e2e_ci_root`).
2. **Java 21** (Temurin) + `mvn package -DskipTests`.
3. **Node 20** + `npm ci` em `e2e/`.
4. `npx playwright install --with-deps chromium`.
5. `npm test` — **sem** `E2E_SKIP_WEB_SERVER` (Playwright sobe o JAR via `webServer`).

## Variáveis de ambiente (job)

| Variável | Origem | Uso |
|----------|--------|-----|
| `SPRING_DATASOURCE_URL` | workflow | MySQL no service (localhost:3306) |
| `SPRING_DATASOURCE_USERNAME` | workflow | `root` |
| `SPRING_DATASOURCE_PASSWORD` | workflow | `e2e_ci_root` (não é secret de produção) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | workflow | `update` |
| `SMOKE_ADMIN_EMAIL` | workflow | E-mail do analista seed (`robertaobolivia@gmail.com`) |
| `SMOKE_ADMIN_SENHA` | secret opcional | Senha; se vazio, `global-setup` usa padrão do seed |
| `CI` | workflow | `true` — Playwright não reutiliza servidor antigo |
| `E2E_BASE_URL` | workflow | `http://localhost:8080` |

## Secrets / variáveis GitHub

| Nome | Obrigatório | Descrição |
|------|-------------|-----------|
| `SMOKE_ADMIN_SENHA` | Não | Override da senha do analista E2E. **Não** commitar senha real no YAML. Sem secret, usa-se o valor do seed oficial (dev/CI). |

Opcional futuro: `E2E_WHATSAPP_MATRIZ_ID` se a massa exigir matriz específica.

## Artefatos

Em falha: upload `e2e/playwright-report/` (7 dias).

## E2E setup (CI-friendly)

`global-setup.ts` resolve grupo/subgrupo/motivo via API e tenta integração WhatsApp com `whatsappMatrizId` 2, com fallback sem matriz.

## Local

Continua válido: `.\scripts\run-e2e-local.ps1` ou `cd e2e && npm test` (com ou sem `E2E_SKIP_WEB_SERVER`).

Validação local (2026-05-21): `run-e2e-local.ps1 -SkipPackage -KeepServer` → HTTP 200, **3 passed**; setup resolve IDs via API (ex.: grupo 3 / sub 9 / motivo 2 neste banco).
