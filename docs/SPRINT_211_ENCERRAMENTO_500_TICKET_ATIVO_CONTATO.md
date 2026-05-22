# Sprint 211 — Encerramento HTTP 500 e ticket ativo duplicado (Cliente + Contato)

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_211_Encerramento_Ticket_Ativo`  
**Base:** Sprint 210 (massa Contato 8, TK-000086 / TK-000088), Sprint 206 (ticket ativo por Cliente + Contato)

## Objetivo

Corrigir erro **500** ao encerrar ticket sem pesquisa de satisfação e reforçar a regra de **um ticket ativo** por **Cliente + Contato** no fluxo automático (integração WhatsApp preparatória e criação por webhook).

## Massa observada (smoke Sprint 210)

| Item | Valor |
|------|--------|
| Cliente | id **69** |
| Contato | id **8** (`5511963978963`) |
| Tickets ABERTO mesmo Contato | **TK-000086**, **TK-000088** |
| Encerramento TK-000086 | HTTP **500** com motivo válido e `enviarPesquisaSatisfacao=false` |

## Causa do erro 500 (encerramento)

Após `PUT /api/tickets/{numero}/encerrar` com pesquisa **não** enviada, `TicketSatisfacaoService.registrarDecisaoPosEncerramento` persiste registro com status **NAO_ENVIADA** e **`nota` null**.

O MySQL rejeitou o insert:

```text
Column 'nota' cannot be null
```

A entidade JPA não marcava `nota` como nullable de forma alinhada ao uso (PENDENTE / NAO_ENVIADA sem nota até resposta).

### Correção

- `TicketSatisfacao.java`: `@Column(nullable = true)` em `nota` (comentário Sprint 211).
- `@Column(nullable = true)` na entidade.
- Na subida: `TicketSatisfacaoNotaNullablePatch` executa `ALTER TABLE ticket_satisfacao MODIFY COLUMN nota INT NULL` (MySQL).
- Manual, se necessário: `ALTER TABLE ticket_satisfacao MODIFY nota INT NULL;`

Regras Sprint 196 (motivo obrigatório) e 197–200 (avaliação pós-RESOLVIDO) **inalteradas**. Erros de validação continuam como `IllegalArgumentException` → **400** (`GlobalExceptionHandler`).

## Causa da duplicidade (dois ABERTO, Contato 8)

| Fator | Conclusão |
|-------|-----------|
| Massa / smoke Sprint 210 | Integração criou **TK-000088** enquanto **TK-000086** permaneceu ABERTO (encerramento do 086 falhou com 500). |
| Regra automática | Possível contribuição: em `IntegracaoMensagemEntradaService`, `resolverContatoWhatsapp` **engolia** `RuntimeException` e retornava `null`, fazendo a busca de ativo cair no caminho legado (sem par Cliente+Contato) e permitir novo ticket. |

Não foi feita limpeza em massa no banco (fora de escopo). Para smoke futuro: encerrar ou resolver tickets duplicados antes de nova mensagem de integração no mesmo telefone.

## Correções de ticket ativo (fluxo automático)

1. **`IntegracaoMensagemEntradaService`**
   - `resolverContatoWhatsapp`: sem `try/catch` que zera contato em falha.
   - **Recheck** `buscarEntidadeAtiva(clienteId, contatoWhatsapp.getId(), …)` imediatamente antes de `criarTicketPorWebhook`.

2. **`TicketService` (`criarTicketPorWebhook`)**
   - Após vincular contato, consulta `TicketRepository` com os mesmos status ativos de `TicketAtivoService` (evita ciclo `TicketService` ↔ `TicketAtivoService`).
   - Se já existir ativo para o mesmo **cliente + contato** → `IllegalArgumentException` (400).

`TicketAtivoService` (Sprint 206): com `contatoWhatsappId` resolvido, **não** faz fallback por cliente sozinho.

## Regra final validada

| Regra | Comportamento |
|-------|----------------|
| Ticket ativo | Um protocolo ativo (ABERTO / EM_ATENDIMENTO / AGUARDANDO_CLIENTE) por **Cliente + Contato** quando contato WhatsApp está resolvido. |
| Integração | Mensagem nova no mesmo telefone/contato → reutiliza ativo (`ticketCriado=false`). |
| Criação webhook | Não cria segundo ABERTO para o mesmo par se já houver ativo. |
| Encerramento | Categoria + Subcategoria + Motivo + Comentário; sem pesquisa → RESOLVIDO + satisfação NAO_ENVIADA **sem** 500. |
| Etiquetas por Contato | Não alteradas nesta sprint (Sprint 210). |

## Arquivos alterados

| Arquivo | Alteração |
|---------|-----------|
| `entity/TicketSatisfacao.java` | `nota` nullable |
| `config/TicketSatisfacaoNotaNullablePatch.java` | ALTER MySQL na subida |
| `service/IntegracaoMensagemEntradaService.java` | contato + recheck ativo |
| `service/TicketService.java` | guarda ativo em `criarTicketPorWebhook` |
| `test/.../IntegracaoMensagemRecheckAtivoTest.java` | novo |
| `test/.../TicketServiceCriacaoContatoTest.java` | mock `TicketAtivoService` |

## Testes executados

- `mvn clean install` — suite Java (incl. encerramento, satisfação, integração recheck, criação contato).
- `npm test` — não executado (sem alteração em `static/js/`).

## Smoke Sprint 211 (operacional)

| Cenário | Esperado | Resultado local 2026-05-21 |
|---------|----------|---------------------------|
| A — Encerrar ABERTO sem pesquisa | HTTP 200, **RESOLVIDO** | TK-000086 e TK-000088 → RESOLVIDO (após patch `nota`) |
| B — Encerrar com pesquisa (com contato) | PENDENTE / envio 197–200 | Não reexecutado nesta rodada |
| C — Mensagem com ativo ABERTO | `ticketCriado=false` | Após abrir TK-000090, segunda mensagem reutiliza **TK-000090** |
| D — Sem ativo (todos RESOLVIDO) | Novo ticket ou pendência | Nova mensagem criou **TK-000090**; não há segundo ABERTO simultâneo |

Credenciais e payloads: ambiente local; não versionar senhas neste doc.

## Pendências / riscos

- Massa legada: **TK-000086** e **TK-000088** podem continuar ambos ABERTO até encerramento manual pós-correção.
- Confirmar coluna `nota` nullable no MySQL de produção se `ddl-auto` não rodar.
- Playwright / automação: aguardar smoke estável de encerramento + ticket ativo (próxima sprint sugerida).

## Referências

- `docs/SPRINT_206_SMOKE_TICKET_ATIVO_CONTATO.md`
- `docs/SPRINT_210_SMOKE_ETIQUETAS_CONTATO_FALLBACK.md`
- `docs/CHATS_DIRETRIZES.md` (fluxo integração)
- `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md`
