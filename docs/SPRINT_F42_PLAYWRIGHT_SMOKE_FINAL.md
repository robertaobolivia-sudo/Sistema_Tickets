# Sprint F42 — Playwright smoke final pós reestruturação

## 1. Objetivo

Validar no navegador o fluxo **Login → Dashboard → Clientes → Contato → Abrir Ticket (ATIVO_MANUAL) → Chats → Relatórios → Config**, após marco F41 **REESTRUTURAÇÃO CONCLUÍDA**.

## 2. Massa usada

Criada via API no `beforeAll` do spec (timestamp único). Exemplo da última execução bem-sucedida: ver `e2e/.massa-f42.json` (gerado em runtime).

- Cliente: `Cliente F42 Smoke {ts}` / LTDA
- Contato: `Contato F42 Smoke {ts}` + WhatsApp `5511999{ts7}`
- Ticket: abertura manual UI → `origemTicket=ATIVO_MANUAL`, `clienteId`, `contatoId`

## 3. Credencial

`robertaobolivia@gmail.com` (ADMIN DEV, seed). **Senha não documentada** — env `SMOKE_ADMIN_SENHA` ou `.massa.json` local.

## 4. Fluxos testados

Login, Dashboard, Clientes listagem, Contatos gestão, Abrir Ticket, Chats (Fila + painel), Relatórios filtro Origem + gerar, Config Conexões/Revendas isolada, ausência de `/api/carteiras` no trecho Chats.

## 5. Fluxos não testados

- Receptivo WhatsApp E2E (simulação integração — fora escopo F42).
- CSV/PDF export (não exigido no spec F42).
- Etiquetas Chats (opcional sprint).
- Entrada do atendimento **visível** em ticket manual sem matriz (bloco pode ficar `--no-data`; título validado).

## 6. Resultado Playwright

`npm test -- smoke-reestruturacao-final.spec.ts` com `E2E_SKIP_WEB_SERVER=1` → **1 passed** (~5s + global-setup).

## 7–9. Maven / Vitest / package

- `mvn test` — OK  
- Vitest — **218/218** (39 files)  
- `mvn package -DskipTests` — OK  

## 10. HTTP 200

`http://localhost:8080/` — 200 (jar DEV).

## 11. Bugs encontrados

Nenhum de runtime legado. Ajustes só no spec: submenu Clientes/Relatórios (`aria-expanded`), painel Entrada manual, clique Relatórios via `evaluate`, filtro console 403 benigno.

## 12. Riscos residuais

- Jar corrupto se `mvn package` concorrente com JVM (Windows) — rebuild `target` antes de E2E.
- `global-setup` legado (tickets 214/220) ainda roda antes do F42.
- 403 console em algum asset — ignorado no smoke; investigar se persistir.

## 13. Próximo bloco

Playwright receptivo com `E2E_WHATSAPP_MATRIZ_ID`; ou limpeza doc/README legado; repaginação blueprint 235+.

## Comando

```powershell
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm test -- smoke-reestruturacao-final.spec.ts
```
