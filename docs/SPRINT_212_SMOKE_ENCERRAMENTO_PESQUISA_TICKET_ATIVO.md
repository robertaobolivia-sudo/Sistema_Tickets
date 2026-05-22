# Sprint 212 — Smoke: encerramento com/sem pesquisa e ticket ativo

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_212_Smoke_Encerramento_Pesquisa_Ativo`  
**Base:** Sprint 211 (encerramento 500, ticket ativo, patch `nota`)

## Massa (dev)

| Campo | Valor |
|--------|--------|
| Cliente | id **69** |
| WhatsApp Matriz | id **2** |
| Grupo / Subgrupo / Motivo | **2** / **7** / **1** (Smoke Motivo 210) |
| Telefones smoke (dedicados) | A `5511963978920`, B `5511963978921`, D `5511963978922`, outro D `5511888777666` |
| Contatos criados na rodada | A → contato **13** (TK-000095); B → contato novo (TK-000096) |

**Observação:** telefone **5511963978963** (Contato 8 legado) pode retornar HTTP 500 com *"Ja existe pendencia de decisao aberta para este contato"* após encerramentos anteriores — usar telefones dedicados no script (`scripts/smoke-sprint212.ps1`).

## Cenário A — encerramento sem pesquisa

| Item | Resultado |
|------|-----------|
| Ticket | **TK-000095** (criado via integração) |
| Request | `enviarPesquisaSatisfacao=false`, comentário obrigatório |
| HTTP | 200 |
| Status ticket | **RESOLVIDO** |
| Satisfação | **NAO_ENVIADA**, `nota` null (sem 500) |
| Detalhe | `motivoNome` = Smoke Motivo 210; `satisfacaoStatus` preenchido no GET ticket |

## Cenário B — encerramento com pesquisa

| Item | Resultado |
|------|-----------|
| Ticket | **TK-000096** |
| Request | `enviarPesquisaSatisfacao=true` |
| Status ticket | **RESOLVIDO** |
| Satisfação | **PENDENTE** |
| `envioStatus` (GET `/satisfacao`) | **SIMULADO** |
| Link | `avaliacaoLinkPublico` na resposta do encerramento: `http://localhost:8080/?page=avaliacao&token=...` |
| Token hash | persistido (`token_resposta_hash`); preview interno só na resposta do encerramento (token não reexposto em GET posterior) |

## Cenário C — resposta pública

| Passo | Resultado |
|-------|-----------|
| GET `/api/public/avaliacoes/{token}` | OK, mensagem de avaliação |
| POST `/api/public/avaliacoes/{token}/responder` nota 5 + comentário | **RESPONDIDA** |
| Segundo POST | HTTP **400** (bloqueio) |

## Cenário D — ticket ativo

| Passo | Resultado |
|-------|-----------|
| 1ª mensagem (`5511963978922`) | `ticketCriado=true` → **TK-000097** ABERTO |
| 2ª mensagem mesmo telefone | `ticketCriado=false`, `numeroTicket=TK-000097` (reutiliza) |
| Outro telefone (`5511888777666`) | `ticketCriado=true` → **TK-000098** (Contato distinto) |

## Chats — validação rápida

| Item | Resultado |
|------|-----------|
| HTTP raiz | **200** em `http://localhost:8080/` |
| UI manual | Abrir Chats → Fila: tickets **TK-000097** / **TK-000098** devem aparecer após integração; painel Cliente/Contato coerente; timeline com mensagens de abertura/reuso; sem erro crítico no console (checklist manual pós-smoke API). |

## Ajustes de código (Sprint 212)

| Arquivo | Alteração |
|---------|-----------|
| `TicketSatisfacaoService` | `registrarDecisaoPosEncerramento` retorna token opaco; `preencherResumoNoTicketResponse`; DTO satisfação com `envioStatus` e `linkAvaliacaoPublico` quando há preview |
| `TicketService` | Encerramento preenche resumo satisfação + `avaliacaoLinkPublico`; detalhe do ticket inclui campos satisfação |
| `TicketResponseDTO` | Campo `avaliacaoLinkPublico` |
| `TicketSatisfacaoResponseDTO` | `envioStatus`, `linkAvaliacaoPublico` |
| `scripts/smoke-sprint212.ps1` | Smoke API A–D |
| Testes | Mocks `PesquisaSatisfacaoEnvioService` / `TicketSatisfacaoService` em testes de `TicketService` |

## Testes executados

- `mvn clean install` — OK (137 testes Java)
- `npm test` — não executado (sem alteração JS)
- `.\scripts\smoke-sprint212.ps1` — **SMOKE_212_OK**

## Pendências / riscos

- Contato 8 / telefone legado: pendências de decisão podem bloquear integração até tratar no Chats.
- Link público só na resposta imediata do encerramento com pesquisa (por segurança do token opaco).
- Playwright E2E Chats + encerramento: próxima sprint sugerida.

## Referências

- `docs/SPRINT_211_ENCERRAMENTO_500_TICKET_ATIVO_CONTATO.md`
- `docs/CHATS_DIRETRIZES.md`
- `docs/MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md`
