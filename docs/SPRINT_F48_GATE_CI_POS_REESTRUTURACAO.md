# Sprint F48 — Gate local/CI pós-reestruturação

> **Regras de domínio:** [README_MESTRE.md](../README_MESTRE.md). Este doc é só o **gate CI**.

## Objetivo

Gate oficial para PR/CI/local: impede regressão no núcleo **Cliente → Contato → Ticket** após F41–F47.

## Gate (ordem)

| # | Etapa | Falha se |
|---|--------|----------|
| 1 | `mvn test` | testes Java falharem |
| 2 | Vitest `static/js` | testes JS falharem |
| 3 | `mvn package -DskipTests` | build falhar (ex.: JAR lock) |
| 4 | HTTP 200 `/` | app não responder |
| 5 | `npm run test:pos-reestruturacao` | F45 falhar / massa inválida |
| — | (implícito F45) | legados no Chats/JSON ticket |

Legados cobertos pelo E2E F45: `conexao`, `carteira`, `contatoSolicitanteId`, `contatoCliente`, Carteira no Chats, `/api/carteiras`, `/uploads/conexoes` no fluxo Chats.

## Comando oficial local (Windows)

**Dois terminais (recomendado):**

```powershell
# Terminal 1
.\scripts\start-dev-server.ps1

# Terminal 2
.\scripts\validar-pos-reestruturacao.ps1 -NoAutoStartServer
```

**Um comando (Java em background + log):**

```powershell
.\scripts\validar-pos-reestruturacao-ci.ps1
```

**Um comando (janela Java):**

```powershell
.\scripts\validar-pos-reestruturacao.ps1
```

Saída: bloco `GATE F48 (pos-reestruturacao)` com OK/FALHA por etapa; **exit code 1** se qualquer passo falhar.

## Comando oficial CI (GitHub)

Workflow: **`.github/workflows/pos-reestruturacao.yml`**

- Disparo: `pull_request`, `push` (main/master/develop), `workflow_dispatch`
- MySQL 8 service + Java 21 + Node 20
- Maven test → Vitest → package → Playwright F45 (webServer do Playwright sobe o JAR)
- Artifact `playwright-report-pos-reestruturacao` em falha

Secret recomendado: `SMOKE_ADMIN_SENHA` (fallback dev no workflow com warning — ver F51).

## Required check (F51)

Nome estável do job: **`Gate pos-reestruturacao`**. No PR, marcar como obrigatório em branch protection de `main`.  
Passo a passo: **`docs/SPRINT_F51_REQUIRED_CHECK_GATE.md`**.

## Checklist PR (obrigatório se mexer no núcleo)

- [ ] Gate local ou CI verde
- [ ] `AUDITORIA-004` ainda reflete o escopo (se mudança de domínio)
- [ ] Nenhum legado operacional reintroduzido

## Quando rodar o gate

Obrigatório em PR que altere: Cliente, Contato, Ticket, Chats, Relatórios, CSV/PDF, auth, shell, integração WhatsApp simulada.

Opcional: só docs/CSS isolado sem fluxo.

## JAR lock (Windows)

Antes de `package`: `.\scripts\stop-java-8080.ps1`

Exit **4294967295** no Java = processo morto de fora; reinicie `start-dev-server.ps1`.

## Próximo bloco

- ~~Tornar `pos-reestruturacao.yml` required check no GitHub~~ → ver **F51** (`docs/SPRINT_F51_REQUIRED_CHECK_GATE.md`); ativação na UI = pendência admin  
- Provider WhatsApp (sprint separada)
