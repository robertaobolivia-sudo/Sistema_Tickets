# Sprint 241 — Repaginação visual Tickets e modais

## Objetivo

Alinhar Tickets (lista/Meus Tickets), detalhe e modais relacionados ao padrão corporativo teal/neon (Login 229, Shell 236, Clientes 239, Chats 240), sem alterar regras, busca, encerramento, satisfação ou contratos E2E.

## Escopo entregue

- `src/main/resources/static/css/pages/tickets.css` — superfície `#page-tickets`: cabeçalho, `content-section`, grid de filtros (layout Sprint 110), inputs/foco, botões primário/secundário, `data-table`, estados vazios e hierarquia visual.
- `src/main/resources/static/css/modals.css` — bloco final Sprint 241: `#modalDetalhes`, `.modal-encerramento` (header, sombras/bordas, botões, selects, grupos de encerramento, aviso ticket sem contato).
- `index.html` — cache `tickets.css?v=sprint241`, `modals.css?v=sprint241`.

## Preservado

- IDs e `data-testid` usados por Playwright (`detail-encerrar-ticket`, `encerrar-grupo`, `modal-encerramento`, etc.).
- Backend, schema e JS funcional (sem mudança de lógica de busca/paginação/encerramento).

## Backup

`C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_241_Tickets_Modais_Visual`

## Testes

| Teste | Resultado |
|-------|-----------|
| `cd e2e && npm test` | 3 passed |
| `mvn clean install` | Não executado (somente CSS) |
| Vitest JS | Não executado (sem alteração em `static/js`) |

## Smoke

- Automatizado: specs de encerramento/pesquisa e sem contato (Chats + modais compartilhados).
- Manual sugerido: Tickets → busca texto → filtros → abrir detalhe → encerrar (com e sem pesquisa).

## GitHub

Repositório local sem `.git` neste workspace. Registrar no remoto:

**`Sprint 241 — Repaginação visual Tickets e modais`**

Arquivos: `tickets.css`, `modals.css`, `index.html`, `docs/SPRINT_241_TICKETS_MODAIS_VISUAL.md`, `docs/HISTORICO_REESTRUTURACAO_CLIENTE_CONTATO_WHATSAPP_TICKET.md`.

## Próximo passo

Sprint 242 — Dashboard + Indicadores (visual corporativo), conforme `docs/BLUEPRINT_TELAS_NOVO_MODELO.md`.
