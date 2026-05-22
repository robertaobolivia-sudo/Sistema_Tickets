# Sprint 220 — E2E: encerramento sem pesquisa

## Objetivo

Regressão automatizada do encerramento com opt-out da pesquisa (evitar regressão do erro 500 / nota null — Sprint 211).

## Entregas

- `e2e/tests/chats-encerramento-sem-pesquisa.spec.ts`
- `e2e/global-setup.ts` — segundo ticket (`numeroTicketSemPesquisa`)
- `e2e/README.md` — spec documentado

## Validações

- UI: Chats/Fila → modal → “Não enviar pesquisa” (`label.encerramento-choice--nao`)
- PUT encerrar: HTTP 200, `status=RESOLVIDO`, `satisfacaoStatus=NAO_ENVIADA`, sem `avaliacaoLinkPublico`
- Detalhe: status “Não enviada”, linha de link público oculta

## Testes

```powershell
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm test
```

Resultado (2026-05-21): **2 passed** (~5,1s) — TK-000114 (com pesquisa), TK-000115 (sem pesquisa).

## Backup

`Sistemas_BKP/BKP_Sprint_220_E2E_Sem_Pesquisa`
