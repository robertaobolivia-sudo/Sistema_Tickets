# Sprint 215 — Playwright E2E: detalhe do ticket e link de satisfação

## Objetivo

Ampliar o spec da Sprint 214 para validar na UI autenticada os campos de satisfação no modal de detalhes após encerramento com pesquisa e após resposta pública.

## Alterações

| Item | Detalhe |
|------|---------|
| Spec | `e2e/tests/chats-encerramento-pesquisa.spec.ts` |
| Massa | Reuso `e2e/global-setup.ts` / `.massa.json` |

## Validações adicionadas

1. Após `PUT .../encerrar` com pesquisa: abrir detalhe via `chats-primary-action` (**Detalhes**).
2. `detail-satisfacao-status` → **Pendente**.
3. `detail-satisfacao-envio` → **SIMULADO** (linha visível).
4. `detail-satisfacao-link` → mesmo path/query do `avaliacaoLinkPublico` da resposta do encerramento.
5. Fluxo público (nota 5 + comentário + 400 na segunda resposta) — mantido da 214.
6. Aba Chats **Histórico** → card do ticket → detalhe → status **Respondida**, nota **5 / 5**.

## Execução

```powershell
# App em http://localhost:8080 (JAR ou dev)
$env:E2E_SKIP_WEB_SERVER = '1'
cd e2e
npm test
```

## Resultado (2026-05-21)

| Verificação | Resultado |
|-------------|-----------|
| Backup | `BKP_Sprint_215_E2E_Detalhe_Satisfacao` |
| Playwright | **1 passed** (~3,5s) |
| `mvn` / Vitest app | N/A — só E2E + docs |
| HTTP 200 `/` | OK |

## Critérios de aceitação

- [x] E2E cobre detalhe satisfação na UI autenticada
- [x] Link da avaliação pública validado na resposta do encerramento; no detalhe, comparação do `href` quando a linha do link está visível
- [x] Status Pendente → Respondida coerente
- [x] Playwright passou no ambiente local
- [x] HTTP 200 `/` confirmado

## Observação (link no detalhe)

Após encerramento com pesquisa, o token já está persistido (hash). O `GET /api/tickets/{numero}/satisfacao` pode não devolver `linkAvaliacaoPublico`, e a linha `detail-satisfacao-link` permanece oculta. O E2E valida o link operacional via `avaliacaoLinkPublico` do `PUT .../encerrar` e a página pública; se a API passar a expor o link no GET, o spec também valida o `href` na UI.
