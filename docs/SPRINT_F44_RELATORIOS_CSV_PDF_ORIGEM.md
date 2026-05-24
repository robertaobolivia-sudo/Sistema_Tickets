# Sprint F44 — Smoke Relatórios / CSV / PDF por origem

## Objetivo

Validar saídas operacionais pós-reestruturação para tickets `ATIVO_MANUAL` e `RECEPTIVO_WHATSAPP`.

## Artefatos

| Item | Caminho |
|------|---------|
| Spec Playwright | `e2e/tests/smoke-relatorios-csv-pdf-origem.spec.ts` |
| Helper massa | `e2e/tests/helpers/f44Massa.ts` |
| Snapshot massa | `e2e/.massa-f44.json` |
| PDF sem Conexão (unit) | `TicketPdfServiceF26Test` |

## Massa

- Reutiliza `.massa-f42.json` / `.massa-f43.json` quando IDs/tickets existem no banco.
- Recria Cliente/Contato/ticket manual ou receptivo se snapshot estiver stale.
- Ticket manual: `POST /api/tickets` com `contatoWhatsappId` → `ATIVO_MANUAL`.
- Ticket receptivo: helper F43 (`POST /api/integracoes/whatsapp/mensagens`).

## Validações

1. `GET /api/tickets/busca?origemTicket=…&textoLivre=…` — ambas origens, sem campos legado no JSON.
2. `GET /api/tickets/relatorios/csv` — header `Origem ticket`, valor correto, sem coluna Conexão/Carteira operacional.
3. `GET /api/tickets/{numero}/pdf` — HTTP 200, `%PDF-`, bytes; Cliente/Contato/Origem via API do ticket (PDF FlateDecode).
4. UI Relatórios — filtro origem + cliente, Gerar, tabela com ticket e sem rótulos legado.

## Execução

```powershell
# app em :8080
cd e2e
$env:E2E_SKIP_WEB_SERVER='1'
npx playwright test smoke-relatorios-csv-pdf-origem.spec.ts
```

```powershell
cd src\main\resources\static\js
npm test
mvn test   # raiz do projeto
```

## Resultado (2026-05-24)

- Playwright F44: **1 passed**
- Vitest: **219/219**
- `mvn test`: **OK**
- HTTP `/`: **200**
- `mvn package -DskipTests`: **FAIL** (jar em uso pelo processo Java da instância local)

## Próximo

Smoke encerramento + exportação em lote; ou Playwright regressão única F42+F43+F44.
