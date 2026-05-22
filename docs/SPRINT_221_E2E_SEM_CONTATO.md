# Sprint 221 — E2E: encerramento sem contato

## Objetivo

Regressão do encerramento quando o ticket não tem `contatoId` (sem envio de pesquisa / sem link público).

## Massa

`global-setup.ts` → `POST /api/tickets` com `cliente`, `mensagem`, `canal` — **sem** `telefone` (não vincula Contato WhatsApp). Valida `contatoId` nulo na resposta.

## Spec

`e2e/tests/encerramento-sem-contato.spec.ts`

- UI: Meus Tickets → detalhe → `detail-encerrar-ticket`
- Aviso `encerrar-aviso-sem-contato` visível
- Encerra com “Sim, enviar” na UI; backend mantém `NAO_ENVIADA` (regra Sprint 197/211)

## testid

`encerrar-aviso-sem-contato` em `index.html` (ajuste mínimo Sprint 221).

## Testes

```powershell
cd e2e
$env:E2E_SKIP_WEB_SERVER = '1'
npm test
```

Resultado: **3 passed** (~22s suite completa). Spec usa `#encerramentoSemContatoAviso` (compatível com JAR sem `data-testid` novo).

## Backup

`Sistemas_BKP/BKP_Sprint_221_E2E_Sem_Contato`
