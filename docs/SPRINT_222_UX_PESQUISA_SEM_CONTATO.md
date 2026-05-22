# Sprint 222 — UX: desabilitar “Sim, enviar” sem contato

## Objetivo

No modal de encerramento, quando o ticket não tem `contatoId`, a opção **Sim, enviar** fica desabilitada visualmente; **Não enviar** permanece padrão; aviso de ausência de contato segue visível.

## Alterações

- `encerramentoView.js` — `deveDesabilitarPesquisaSimEncerramento(contatoId)`
- `ticketDetailsModal.js` — `disabled` no radio Sim + classe `is-disabled` no label
- `modals.css` — estilo desabilitado (opacidade, `pointer-events: none`)
- `encerramentoView.test.js` — teste unitário
- `e2e/tests/encerramento-sem-contato.spec.ts` — assert `toBeDisabled()` em vez de clicar Sim

## Critérios

| Critério | Status |
|----------|--------|
| Com contato: Sim/Não disponíveis | OK (sem alterar fluxo) |
| Sem contato: Sim desabilitado | OK |
| Sem contato: Não selecionado | OK |
| Aviso visível | OK |
| E2E 3 passed | OK — Sprint 223 (`npm test`, 2026-05-21) |

## Backup

`Sistemas_BKP/BKP_Sprint_222_UX_Sem_Contato`
