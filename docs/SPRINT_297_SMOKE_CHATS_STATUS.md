# Sprint 297 — Smoke Chats + máquina de status

## Objetivo

Validar no fluxo operacional do Chats as transições de status (Sprint 295/296) e bloqueios de UI.

## Backup

`C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_297_Chats_Status_Smoke`

## Massa

| Ticket | Uso | Status final |
|--------|-----|----------------|
| TK-000337 | Fluxo ABERTO → EM → AGUARDANDO → EM → encerramento | RESOLVIDO |
| TK-000338 | Classificação indevido | INDEVIDO |

Script: `scripts/sprint297-smoke-chats-massa.ps1`

Cliente 89, matriz 5, analista `robertaobolivia@gmail.com`.

## Correção aplicada (gap do smoke)

O Chats não expunha transições manuais de status (apenas Encerrar / Classificar indevido). Incluído bloco **Status do chamado** no painel Atendimento:

- `js/core/chatsStatusOperacionalView.js` — opções alinhadas à máquina (sem RESOLVIDO/INDEVIDO)
- `chatsPage.js` — `PUT /api/tickets/{n}/status` + troca de aba (Fila ↔ Atendendo)
- `index.html` + `chats.css` — UI e testids

## Evidências navegador (pós `mvn package` + JAR reiniciado)

1. **Fila → ABERTO (TK-000337):** select com `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE`; após aplicar EM → aba **Atendendo**, opções só `AGUARDANDO_CLIENTE`; ciclo AGUARDANDO ↔ EM OK.
2. **Encerramento:** TK-000337 → `RESOLVIDO` via API (mesmo modal já coberto em E2E); botão primário em encerrados = **Detalhes**; bloco de status oculto.
3. **INDEVIDO (TK-000338):** classificação via API → `INDEVIDO`; na aba **Não atendimento**: **Detalhes**, sem Classificar indevido, sem select de status, observações desabilitadas.
4. **UI:** não há opção para `RESOLVIDO`/`INDEVIDO` no select; encerramento só pelo botão **Encerrar ticket** em ticket ativo.

## Testes

| Teste | Resultado |
|-------|-----------|
| `npm test` (Vitest) | 205 OK (+3 `chatsStatusOperacionalView`) |
| `mvn package -DskipTests` | OK (static no JAR) |
| HTTP `http://localhost:8080/` | 200 OK |
| Smoke API encerrar / indevido | OK |
| Smoke browser status flow | OK |
| Playwright encerramento | Não reexecutado (escopo smoke manual) |

## Pendências / riscos

- Encerramento no smoke browser automatizado: modal exige carga assíncrona de catálogo — validar manualmente com **Encerrar ticket** (E2E já cobre).
- Após classificar indevido, lista Chats pode exigir recarregar a página para ver o card em **Não atendimento** (callback `onSuccess` já troca aba quando o fluxo parte do modal).
- Composer de mensagem no Chats segue desabilitado por desenho atual (não é regressão da máquina de status).

## Próximo passo

Smoke manual rápido do modal **Encerrar ticket** no Chats com TK ativo em **Atendendo**, ou estender Playwright com passo de status flow (Sprint 298).
