# Sprint F51 — Required check do gate pós-reestruturação

> **Domínio:** [README_MESTRE.md](../README_MESTRE.md). **Workflow:** `.github/workflows/pos-reestruturacao.yml`.

## Identificação no GitHub

| Campo | Valor |
|-------|--------|
| **Workflow file** | `.github/workflows/pos-reestruturacao.yml` |
| **Workflow name** | `Gate pos-reestruturacao` |
| **Job name (check)** | `Gate pos-reestruturacao` |
| **Nome exibido no PR** | `Gate pos-reestruturacao / Gate pos-reestruturacao` |

Repositório: https://github.com/robertaobolivia-sudo/Sistema  
**Branch padrão:** `main` (confirmado localmente).

## Eventos

- `pull_request` → `main`, `master`, `develop`
- `push` → `main`, `master`, `develop`
- `workflow_dispatch` (manual)

## O que o workflow executa

1. MySQL 8.0 (service container)
2. Java 21 + `mvn test`
3. Node 20 + Vitest (`static/js`)
4. `mvn package -DskipTests`
5. Playwright F45 (`npm run test:pos-reestruturacao`) — **webServer** sobe o JAR na 8080
6. Em falha: artifact `playwright-report-pos-reestruturacao` (7 dias)

## Secrets e variáveis

| Nome | Obrigatório? | Uso |
|------|----------------|-----|
| `SMOKE_ADMIN_SENHA` | **Recomendado** | Login E2E / global-setup |
| `SMOKE_ADMIN_EMAIL` | Não | Default no workflow: `robertaobolivia@gmail.com` |

**Não versionar senhas.** Configurar em: **Settings → Secrets and variables → Actions → New repository secret**.

Banco no CI: definido no workflow (`SPRING_DATASOURCE_*`, MySQL service). **Não** exige `DB_USER` / `DB_PASSWORD` extras se usar o YAML atual.

Variáveis já no job (não secret): `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `E2E_BASE_URL`, `CI=true`.

Se `SMOKE_ADMIN_SENHA` ausente, o workflow emite **warning** e usa fallback de dev (igual `global-setup.ts` legado) — ative o secret para ambiente sério.

## Ativar required check (GitHub UI)

1. Abra https://github.com/robertaobolivia-sudo/Sistema  
2. **Settings** → **Branches**  
3. Em **Branch protection rules**, **Add rule** ou edite a regra de **`main`**  
4. Marque **Require status checks to pass before merging**  
5. Em **Status checks that are required**, busque e marque:  
   **`Gate pos-reestruturacao / Gate pos-reestruturacao`**  
   (pode aparecer só após o workflow rodar pelo menos uma vez em um PR ou push)  
6. Opcional: **Require branches to be up to date before merging**  
7. **Save changes**

**Pendência operacional:** esta sprint **não** altera configuração do GitHub via API (exige permissão admin no repo). Quem tem admin deve aplicar os passos acima.

### Disparar o workflow antes de marcar o check

- Abra um PR para `main`, ou  
- **Actions** → **Gate pos-reestruturacao** → **Run workflow** (`workflow_dispatch`)

## Comando local antes do PR

**Um terminal:**

```powershell
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
.\scripts\validar-pos-reestruturacao.ps1
```

**Dois terminais (Windows estável):**

```powershell
# Terminal 1
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
.\scripts\start-dev-server.ps1

# Terminal 2
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
.\scripts\validar-pos-reestruturacao.ps1 -NoAutoStartServer
```

Equivalente CI local: `.\scripts\validar-pos-reestruturacao-ci.ps1`

## Checklist PR

- [ ] Gate local verde **ou** CI `Gate pos-reestruturacao` verde no PR  
- [ ] Required check ativo em `main` (após F51 operacional)  
- [ ] Secret `SMOKE_ADMIN_SENHA` configurado no repositório  

## Referências

- Gate F48: `docs/SPRINT_F48_GATE_CI_POS_REESTRUTURACAO.md`  
- E2E: `e2e/README.md`  
- README_MESTRE §30–§31 (required check)
