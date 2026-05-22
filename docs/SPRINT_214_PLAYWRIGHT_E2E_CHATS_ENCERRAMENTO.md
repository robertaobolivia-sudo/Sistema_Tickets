# Sprint 214 — Playwright E2E: Chats → encerramento → pesquisa → avaliação pública

## Objetivo

Automatizar o roteiro validado na Sprint 213 (testids + smoke manual) com **@playwright/test**.

## Entregáveis

| Item | Caminho |
|------|---------|
| Config Playwright | `e2e/playwright.config.ts` |
| Massa API (global setup) | `e2e/global-setup.ts` → `e2e/.massa.json` |
| Spec principal | `e2e/tests/chats-encerramento-pesquisa.spec.ts` |
| Como rodar | `e2e/README.md` |

## Fluxo automatizado

1. Login UI (`login-email`, `login-password`, `login-submit`) — sessão `suporteTicketsAnalista`.
2. `nav-chats` → `chats-tab-fila` → busca por número do ticket → `chats-card-{numero}`.
3. Painéis `chats-panel-cliente`, `chats-panel-contato`, `chats-timeline`.
4. `chats-primary-action` → **Encerrar ticket** → `modal-encerramento`.
5. Motivo (grupo 2 / subgrupo 7 / motivo 1), comentário, `encerrar-pesquisa-sim`, `encerrar-confirmar`.
6. Intercepta `PUT /api/tickets/{numero}/encerrar` → valida `avaliacaoLinkPublico`.
7. Navega para link público → `avaliacao-nota-5` + comentário → `avaliacao-publica-enviar`.
8. `avaliacao-publica-estado` com mensagem de sucesso; formulário oculto.
9. `POST /api/public/avaliacoes/{token}/responder` duplicado → **HTTP 400**.

## Massa

- Criada em runtime: `POST /api/integracoes/whatsapp/mensagens` (matriz 2, telefone `5511963978{últimos 3 dígitos do timestamp}`).
- Credenciais: `SMOKE_ADMIN_EMAIL` / `SMOKE_ADMIN_SENHA` (mesmo padrão `scripts/smoke-sprint212.ps1`).

## Como executar

```powershell
# Raiz do projeto (build se necessário)
mvn clean install

cd e2e
npm install
npx playwright install chromium
npm test
```

App já rodando na 8080:

```powershell
$env:E2E_SKIP_WEB_SERVER = '1'
cd e2e
npm test
```

## Resultado da execução (2026-05-21)

| Verificação | Resultado |
|-------------|-----------|
| Backup | `BKP_Sprint_214_Playwright_E2E_Chats_Encerramento` |
| `mvn clean install -DskipTests` | OK |
| `npm test` (static/js) | N/A (sem alteração em `static/js/`) |
| Playwright E2E | **1 passed** (~3,8s) — `e2e/tests/chats-encerramento-pesquisa.spec.ts` |
| HTTP 200 `/` | OK (`http://localhost:8080/`) |

## Critérios de aceitação

- [x] Teste E2E / roteiro Playwright documentado
- [x] Fluxo Chats → resposta pública executado com sucesso no ambiente local
- [x] Segunda resposta bloqueada (400 API no spec)
- [x] Console sem erro crítico no spec
- [x] Histórico mestre atualizado (`docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md`)

## Riscos

- Integração WhatsApp simulada: telefone legado com pendência aberta pode falhar — E2E usa telefone novo por execução.
- `webServer` do Playwright exige JAR em `target/`; sem build, usar `E2E_SKIP_WEB_SERVER=1` com app manual.
