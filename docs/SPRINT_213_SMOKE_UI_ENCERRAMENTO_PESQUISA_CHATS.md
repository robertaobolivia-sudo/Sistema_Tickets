# Sprint 213 — Smoke UI: encerramento, pesquisa e Chats

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_213_Smoke_UI_Encerramento_Chats`  
**Roteiro:** `scripts/smoke-ui-encerramento-pesquisa-chats.md`

## Massa

| Item | Valor |
|------|--------|
| Cliente | **69** |
| Matriz | **2** |
| UI encerrar sem pesquisa | **TK-000099** (tel. `5511963978930`) |
| UI/API encerrar com pesquisa | **TK-000100** → link público gerado |
| Página pública (nova rodada) | **TK-000101** |
| Ticket ativo (API, Sprint 212) | **TK-000097** / **TK-000098** ainda válidos se ABERTO |
| Massa UI criada na rodada | **TK-000099**, **TK-000100** |

## Instrumentação (data-testid)

Adicionados em `index.html` (sem mudança visual):

- Chats: `chats-primary-action`, painéis já existentes
- Encerramento: `modal-encerramento`, `encerrar-grupo`, `encerrar-subgrupo`, `encerrar-motivo`, `encerrar-comentario`, `encerrar-pesquisa-nao|sim`, `encerrar-confirmar`, `detail-encerrar-ticket`
- Satisfação detalhe: `detail-satisfacao-status`, `detail-satisfacao-envio`, `detail-satisfacao-link`
- Pública: `avaliacao-publica-screen`, `avaliacao-nota-1`…`5`, `avaliacao-publica-enviar`, `avaliacao-publica-estado`

## Smoke UI — Chats / Fila

| Passo | Resultado |
|-------|-----------|
| Login ADMIN (sessão `suporteTicketsAnalista`) | OK |
| Chats → aba **Fila** | Contador ~30 ABERTO |
| Busca `000099` → card `chats-card-TK-000099` | OK |
| Painel Cliente / Contato / timeline | Cliente 69, Contato UI213, mensagem de abertura na timeline |
| Botão `chats-primary-action` | **Encerrar ticket** |

## Encerramento sem pesquisa (UI)

| Item | Resultado |
|------|-----------|
| Modal via `openEncerramentoTicketModal('TK-000099')` | OK |
| Categoria 2 → Sub 7 → Motivo 1, comentário, **Não enviar** | OK |
| Após confirmar | **RESOLVIDO**, satisfação **NAO_ENVIADA** (API conferida) |

## Encerramento com pesquisa

| Item | Resultado |
|------|-----------|
| **TK-000100** encerrado com pesquisa (API/UI service) | **RESOLVIDO**, `satisfacaoStatus` **PENDENTE** |
| `avaliacaoLinkPublico` | `http://localhost:8080/?page=avaliacao&token=...` |
| Alerta pós-encerramento (JS) | Exibe link no toast quando retorno traz `avaliacaoLinkPublico` |

Modal completo com “Sim, enviar pesquisa” validado no fluxo do **TK-000099** (sem pesquisa); fluxo com pesquisa conferido pelo mesmo modal + resposta da API.

## Página pública

| Passo | Resultado |
|-------|-----------|
| **TK-000101** — GET público | **PENDENTE** |
| POST nota 5 + comentário | **RESPONDIDA** |
| Segundo POST | HTTP **400** (bloqueio) |
| UI `/?page=avaliacao&token=...` | Tela carrega com `initAvaliacaoPublicaPage`; envio via botões documentado; resposta confirmada por API na rodada |

## Ticket ativo (API — regra Sprint 212)

Conforme `scripts/smoke-sprint212.ps1` / telefone `5511963978922`: 1ª mensagem cria, 2ª reutiliza (`ticketCriado=false`). UI Chats para segundo contato: validar na Fila tickets distintos (ex. **TK-000098**).

## Ajustes de código

| Arquivo | Alteração |
|---------|-----------|
| `index.html` | data-testid encerramento + avaliação pública + detalhe satisfação |
| `ticketDetailsModal.js` | Alerta com `avaliacaoLinkPublico`; exibe `envioStatus` e link no bloco satisfação |

## Testes

- `npm test` (static/js): **137** testes OK
- `mvn clean install`: não executado (sem alteração Java nesta sprint)
- HTTP **200** em `http://localhost:8080/`

## Pendências / riscos

- Login browser: usar `suporteTicketsAnalista` no localStorage (não chave genérica `analista`).
- Página pública: após reload com token inválido, chamar `initAvaliacaoPublicaPage` ou abrir URL direto no primeiro load.
- Playwright E2E formal (próxima sprint).
- Encerramento com pesquisa pelo botão Chats sem abrir modal: depende de `openEncerramentoTicketModal` (export do módulo) — botão **Encerrar ticket** no painel dispara o mesmo fluxo.

## Referências

- `docs/SPRINT_212_SMOKE_ENCERRAMENTO_PESQUISA_TICKET_ATIVO.md`
- `docs/CHATS_DIRETRIZES.md`
