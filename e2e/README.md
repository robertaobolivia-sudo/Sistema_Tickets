# E2E Playwright — suporte-tickets

> **Domínio e regras:** [README_MESTRE.md](../README_MESTRE.md).  
> **Auditoria:** [Auditoria/AUDITORIA-004-pos-reestruturacao.md](../Auditoria/AUDITORIA-004-pos-reestruturacao.md).

## Validação oficial (F46)

Esteira mínima pós-reestruturação: **Maven → Vitest → package → HTTP 200 → Playwright F45**.

```powershell
# Raiz do projeto (recomendado)
.\scripts\validar-pos-reestruturacao.ps1

# Dois terminais (mais estável no Windows)
.\scripts\start-dev-server.ps1          # terminal 1
.\scripts\validar-pos-reestruturacao.ps1 -NoAutoStartServer   # terminal 2
```

| Script | Uso |
|--------|-----|
| `scripts/stop-java-8080.ps1` | Antes de `mvn package` / `clean` |
| `scripts/start-dev-server.ps1` | `package` + JAR em janela dedicada |
| `scripts/validar-pos-reestruturacao.ps1` | Esteira completa |

Checklist de aprovação de sprint: `docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md`.

**Gate PR/CI (F51):** workflow `Gate pos-reestruturacao` — required check em `main` (ver [docs/SPRINT_F51_REQUIRED_CHECK_GATE.md](../docs/SPRINT_F51_REQUIRED_CHECK_GATE.md)).

**Lock JAR:** pare o Java na 8080 antes do `package`. Exit **4294967295** = processo Java encerrado de fora (reinicie o servidor).

---

## Suite principal pós-reestruturação (F45)

**Comando E2E** (app já em :8080):

```powershell
.\scripts\stop-java-8080.ps1    # só se for rebuild antes
.\scripts\start-dev-server.ps1  # ou java -jar em terminal dedicado
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm run test:pos-reestruturacao
```

| Item | Detalhe |
|------|---------|
| Spec | `tests/smoke-pos-reestruturacao.spec.ts` |
| Massa | `e2e/.massa-pos-reestruturacao.json` (1 Cliente, 2 Contatos, Matriz, ticket manual + receptivo) |
| Helpers | `helpers/massaPosReestruturacao.ts`, `helpers/e2eUi.ts`, `helpers/f44Massa.ts` (CSV/PDF API) |
| Doc sprint | `docs/SPRINT_F45_SUITE_E2E_POS_REESTRUTURACAO.md` |

Specs legados (ainda válidos isolados): F42 `smoke-reestruturacao-final.spec.ts`, F43 `smoke-receptivo-whatsapp-final.spec.ts`, F44 `smoke-relatorios-csv-pdf-origem.spec.ts`.

---

Regressão do fluxo principal:

**Chats/Fila → encerramento com pesquisa (Sim/Não) → link público → avaliação → bloqueio da 2ª resposta → detalhe do ticket** (Sprints 214–215, estabilizado na 219).

**F42 — smoke pós reestruturação:** `tests/smoke-reestruturacao-final.spec.ts` (Login → Cliente/Contato → Ticket ATIVO_MANUAL → Chats → Relatórios → Config). Ver `docs/SPRINT_F42_PLAYWRIGHT_SMOKE_FINAL.md`.

**F43 — receptivo WhatsApp simulado:** `tests/smoke-receptivo-whatsapp-final.spec.ts` (`POST /api/integracoes/whatsapp/mensagens` → `RECEPTIVO_WHATSAPP` → Chats). Ver `docs/SPRINT_F43_E2E_RECEPTIVO_WHATSAPP.md`.

---

## Checklist rápido (antes de rodar)

- [ ] MySQL acessível com o mesmo `application.properties` usado no dev local.
- [ ] JAR gerado: `mvn clean install` na raiz do projeto (`target/suporte-tickets-1.0.0.jar`).
- [ ] HTTP 200 em `http://localhost:8080/` (app rodando ou deixar o Playwright subir o JAR).
- [ ] Node.js 18+.
- [ ] `cd e2e && npm install` (primeira vez).
- [ ] Chromium do Playwright: `npx playwright install chromium` (primeira vez).
- [ ] Credenciais de analista admin válidas (env ou padrão de dev).
- [ ] Para regressão diária com app já no ar: `$env:E2E_SKIP_WEB_SERVER = '1'`.

---

## Pré-requisitos (detalhe)

### MySQL

O backend precisa conectar ao banco configurado em `src/main/resources/application.properties` (ou override local). Sem MySQL, o JAR não sobe ou o `global-setup` falha no login/integração.

### Aplicação na porta 8080

- **Modo automático (padrão):** o `playwright.config.ts` executa `java -jar target/suporte-tickets-1.0.0.jar` e espera `http://localhost:8080/` (até 180s). Se já houver processo na 8080, **reutiliza** (`reuseExistingServer: true`).
- **Modo manual:** suba o JAR você mesmo e defina `E2E_SKIP_WEB_SERVER=1` para não tentar outro processo.

Confirme HTTP 200:

```powershell
Invoke-WebRequest -Uri http://localhost:8080/ -UseBasicParsing | Select-Object StatusCode
```

### Credenciais (analista admin)

| Variável | Uso | Padrão (dev local) |
|----------|-----|---------------------|
| `SMOKE_ADMIN_EMAIL` | Login API no `global-setup` | `robertaobolivia@gmail.com` |
| `SMOKE_ADMIN_SENHA` | Senha do analista | *(obrigatório — defina no ambiente; não versionar)* |

**Recomendação:** em máquinas compartilhadas ou CI, use sempre variáveis de ambiente; não commite senhas.

### URL base

| Variável | Padrão |
|----------|--------|
| `E2E_BASE_URL` | `http://localhost:8080` |

### Pular o webServer do Playwright

| Variável | Valor | Efeito |
|----------|--------|--------|
| `E2E_SKIP_WEB_SERVER` | `1` | Não inicia o JAR; exige app já em 8080 com HTTP 200 |

### Chromium / Playwright

Os testes usam o projeto **chromium** (`Desktop Chrome`). Na primeira instalação:

```powershell
cd e2e
npm install
npx playwright install chromium
```

Atualizar browsers após upgrade do `@playwright/test`:

```powershell
npx playwright install chromium
```

---

## Primeira execução (do zero)

Na **raiz** do projeto:

```powershell
mvn clean install
```

Na pasta **e2e**:

```powershell
cd e2e
npm install
npx playwright install chromium
npm test
```

O Playwright sobe o JAR (se 8080 estiver livre ou reutilizar instância), roda `global-setup.ts` e em seguida o spec.

---

## Regressão completa (recomendado — Sprint 224)

Na **raiz** do projeto:

```powershell
.\scripts\run-e2e-local.ps1
```

O script verifica MySQL (TCP 3306), roda `mvn package -DskipTests`, sobe o JAR (janela visível), aguarda HTTP 200, executa `npm test` com `E2E_SKIP_WEB_SERVER=1` e encerra o Java ao final (use `-KeepServer` para deixar a app no ar).

| Parâmetro | Uso |
|-----------|-----|
| `-SkipPackage` | Pula Maven quando o JAR já está atualizado |
| `-UsePlaywrightServer` | Não inicia Java; Playwright usa `webServer` do `playwright.config.ts` |
| `-KeepServer` | Não mata o processo Java iniciado pelo script |

**Gate pos-reestruturacao (F48/F54):** `.github/workflows/pos-reestruturacao.yml` — package, `nohup java -jar`, wait HTTP 200, Playwright com **`E2E_SKIP_WEB_SERVER=1`** (sem `webServer` do Playwright).

**GitHub Actions (Sprint 225):** workflow `.github/workflows/e2e.yml` — MySQL service, Java 21, Maven package, Playwright com `webServer` (**não** define `E2E_SKIP_WEB_SERVER`). Ver `docs/SPRINT_225_CI_GITHUB_ACTIONS.md`.

---

## Execuções futuras (regressão manual)

Com build já feito e app **já rodando** (mais rápido no dia a dia):

```powershell
$env:E2E_SKIP_WEB_SERVER = '1'
cd e2e
npm test
```

Relatório HTML (última falha):

```powershell
npx playwright show-report
```

Modo visual (debug):

```powershell
npm run test:headed
npm run test:ui
```

---

## Massa de dados (`.massa.json`)

O `global-setup.ts`:

1. Faz login em `POST /api/analistas/login`.
2. Cria ticket **ABERTO** via `POST /api/integracoes/whatsapp/mensagens` (telefone único por execução).
3. Grava **`e2e/.massa.json`** (gitignored).

**Importante:** a cada `npm test`, o arquivo é **sobrescrito** (`writeFileSync`). Não reutilize manualmente um `.massa.json` antigo — o spec sempre lê o ticket da última execução.

Campos típicos: `numeroTicket`, `numeroTicketSemPesquisa`, `numeroTicketSemContato`, `telefone`, `telefoneSemPesquisa`, `email`, `grupoId`, `subgrupoId`, `motivoId`.

O setup cria **três** tickets ABERTO por execução (integração WhatsApp ×2 + `POST /api/tickets` sem telefone), para os testes não competirem pelo mesmo número.

---

## Spec coberto

| Arquivo | O que valida |
|---------|----------------|
| `tests/chats-encerramento-pesquisa.spec.ts` | Chats → encerramento **com** pesquisa → avaliação pública → detalhe (`detail-satisfacao-*`) — ticket `numeroTicket` |
| `tests/chats-encerramento-sem-pesquisa.spec.ts` | Chats → encerramento **sem** pesquisa → `NAO_ENVIADA`, sem link público, sem 5xx — ticket `numeroTicketSemPesquisa` (Sprint 220) |
| `tests/encerramento-sem-contato.spec.ts` | Tickets → detalhe → encerramento **sem contato** → aviso + `NAO_ENVIADA` mesmo com “Sim” — `numeroTicketSemContato` (Sprint 221) |

**testids** usados no modal (Sprint 213+): `modal-encerramento`, `encerrar-grupo`, `encerrar-subgrupo`, `encerrar-motivo`, `encerrar-comentario`, `encerrar-pesquisa-sim`, `encerrar-pesquisa-nao`, `encerrar-confirmar`.

**Dica UI:** o spec clica no `label.encerramento-choice--sim` porque os radios podem estar ocultos por CSS.

---

## Smoke manual no navegador (UI)

Quando validar modal ou Chats **fora** do Playwright:

1. App em 8080 com HTTP 200.
2. **Ctrl+F5** (ou abrir com query de cache, ex. `modals.css?v=sprint217`) para não ver CSS/HTML antigo embutido em JAR desatualizado.
3. Login como analista; em Chats, abrir ticket da **Fila** (ABERTO).
4. Conferir modal: ordem Categoria → Subcategoria → Motivo → Comentário → Pesquisa → Ações; sem scroll interno no card em desktop.

---

## Falhas comuns e correções

| Sintoma | Causa provável | Correção |
|---------|----------------|----------|
| `Login API falhou: 401/403` | Credenciais erradas ou analista inativo | Ajustar `SMOKE_ADMIN_EMAIL` / `SMOKE_ADMIN_SENHA`; conferir usuário no banco |
| `fetch failed` / `ECONNREFUSED` | App não está em 8080 | Subir JAR ou remover `E2E_SKIP_WEB_SERVER` e rodar `npm test` |
| `Integração falhou: 4xx/5xx` | MySQL down, seed ou integração WhatsApp | Ver logs do Spring; conferir MySQL e `whatsappMatrizId` no setup |
| `Esperado ticket novo` | API não criou ticket | Ver corpo da resposta nos logs do setup; conferir regras de integração |
| Timeout no `webServer` | JAR ausente ou porta ocupada por outro serviço | `mvn clean install`; liberar 8080 ou usar `E2E_SKIP_WEB_SERVER=1` com app correto |
| `Executable doesn't exist` (Chromium) | Browser não instalado | `npx playwright install chromium` |
| Elemento `encerrar-pesquisa-sim` não visível | Radio oculto | Spec já usa label `.encerramento-choice--sim`; não mudar para `.check()` no input hidden |
| Ticket não aparece em Chats “Encerrados” | Lista pode não refletir imediato | Spec abre detalhe via **Tickets** + busca (comportamento esperado) |
| UI modal largo (920px) / layout antigo | Cache ou JAR com estáticos velhos | Ctrl+F5; rebuild `mvn package` e reiniciar JAR |
| `Sessão expirada` no browser manual | Token/localStorage expirado | Login de novo; E2E não depende da sessão do browser manual |

---

## Logs do global-setup

Com `DEBUG` implícito via console, cada run imprime base URL, e-mail (mascarado), ticket criado e caminho do `.massa.json`. Em falha, a mensagem de erro inclui status HTTP e corpo quando disponível.

---

## Referência de sprints

- 214 — E2E inicial encerramento + pesquisa + público  
- 215 — Detalhe satisfação no ticket  
- 218 — Smoke visual modal + E2E  
- 219 — Este README e checklist operacional  
- 220 — E2E encerramento sem pesquisa (`NAO_ENVIADA`)  
- 221 — E2E ticket sem `contatoId` (aviso + `NAO_ENVIADA`)  
- 222 — UX “Sim, enviar” desabilitado sem contato  
- 224 — Script `scripts/run-e2e-local.ps1`  
- 225 — Workflow `.github/workflows/e2e.yml`  

Documentos: `docs/SPRINT_219_E2E_CHECKLIST.md` … `docs/SPRINT_225_CI_GITHUB_ACTIONS.md`.
