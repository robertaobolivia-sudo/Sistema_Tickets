# Sprint 226 — Validar workflow E2E no GitHub Actions

## Objetivo

Confirmar `.github/workflows/e2e.yml` no runner GitHub e endurecer pontos frágeis de CI.

## Execução no GitHub (obrigatório para fechar 226)

Este ambiente de desenvolvimento **não** possui `gh` CLI nem repositório git com `remote` configurado — a execução no runner deve ser disparada por você:

1. Commit/push do projeto (incluindo `.github/workflows/e2e.yml`) para o GitHub.
2. **Actions** → workflow **E2E Playwright** → **Run workflow** (`workflow_dispatch`).
3. Ou abrir PR para `main` / `master` / `develop`.

Anote o link da run: `https://github.com/<org>/<repo>/actions/workflows/e2e.yml`.

| Campo | Preencher após run |
|-------|-------------------|
| Run ID / URL | _(usuário)_ |
| Resultado job `e2e` | _(success / failure)_ |
| Falha (se houver) | _(logs MySQL / Maven / Playwright)_ |

### Secrets

| Secret | Obrigatório |
|--------|-------------|
| `SMOKE_ADMIN_SENHA` | Não — workflow usa fallback do seed (`@Hipcom123789`) sem expor no YAML |

## Ajustes Sprint 226 (CI)

| Alteração | Motivo |
|-----------|--------|
| `e2e.yml` — fallback senha no step `npm test` | Secret vazio no GitHub |
| `playwright.config.ts` — `webServer.timeout` 300s se `CI` | 1ª subida + DDL no MySQL service |
| `global-setup.ts` — retry login (15×, 2s) | App ainda subindo seeds |

## Simulação local do modo CI

Sem `E2E_SKIP_WEB_SERVER`, `CI=true`, Spring apontando para MySQL local:

- **3 passed** (~15s) — Playwright subiu o JAR e rodou global-setup + specs.

Regressão script local: `.\scripts\run-e2e-local.ps1 -SkipPackage -KeepServer` — manter após push.

## Critérios

| Critério | Status |
|----------|--------|
| Workflow no repositório | OK |
| Simulação CI local | OK (3 passed) |
| Run verde no GitHub | Pendente — disparo manual |
| Senha real no YAML | Não |

## Próximo passo

Após run verde no Actions, marcar Sprint 225/226 fechadas no histórico. Se falhar, colar log do step que quebrou e aplicar correção pontual na sprint seguinte.
