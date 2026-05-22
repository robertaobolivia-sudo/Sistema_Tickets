# Sprint 210 — Smoke: etiqueta em outro ticket do mesmo Contato + fallback legado

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_210_Etiquetas_Segundo_Ticket_Fallback`  
**Base:** Sprint 209 (`Smoke Contato` no Contato 8), Sprint 208 (TK-000086 / TK-000087)

## Massa

| Item | Valor |
|------|--------|
| Cliente | id **69** |
| Contato 8 | etiqueta **Smoke Contato** (id **1**) em `contato_etiquetas` |
| Ticket original Contato 8 | **TK-000086** (ABERTO) |
| Outro ticket Contato 8 | **TK-000088** (ABERTO) — criado via `POST /api/integracoes/whatsapp/mensagens` (mesmo telefone `5511963978963`) |
| Ticket legado | **TK-000020** (`contatoId` vazio, `EM_ATENDIMENTO`) |
| Contato 9 | **TK-000087** — controle de isolamento |

**Observação:** no ambiente ficaram **dois** tickets ABERTO do Contato 8 (086 e 088). A regra de ticket ativo costuma impedir duplicidade; o encerramento do 086 via API retornou HTTP 500 (motivo cadastrado), mas a integração ainda gerou o 088. O smoke de etiquetas por Contato **não depende** de um único ABERTO — a etiqueta é do Contato, não do ticket.

## Cenário A — outro ticket do mesmo Contato

### Preparação

- Mensagem de integração com `whatsappMatrizId=2`, telefone do Contato 8 → **TK-000088** (`ticketCriado=true`, `contatoId=8`).
- `GET /api/contatos/8/etiquetas` → **Smoke Contato**.

### UI (Chats, CDP assistido)

| Passo | Resultado |
|-------|-----------|
| Abrir **TK-000088** (Fila) | Chip **Smoke Contato** `selected` / `aria-selected=true` |
| Rede ao carregar | **GET `/api/contatos/8/etiquetas`** |
| Abrir **TK-000086** (mesmo Contato) | Mesma etiqueta **selecionada** |
| Abrir **TK-000087** (Contato 9) | Chip **não** selecionado |

**Conclusão A:** a etiqueta do Contato 8 aparece em **outro ticket** (088) e no 086; não vaza para o Contato 9.

## Cenário B — fallback legado (sem `contatoId`)

### Massa

- **TK-000020:** `contatoId` ausente no detalhe; painel Contato legado.

### UI

| Passo | Resultado |
|-------|-----------|
| Aba **Atendendo**, busca `000020`, abrir ticket | OK |
| Hint `[data-testid="chats-etiquetas-legacy-hint"]` | **Visível** — texto: *Etiquetas vinculadas ao chamado (cadastro legado).* |
| Salvar (tentativa UI) | Rede registrou **PUT `/api/tickets/TK-000020/etiquetas`** (sessão do browser expirou no feedback; endpoint correto) |

### API (confirmação)

- `PUT /api/tickets/TK-000020/etiquetas` com `etiquetaIds: [1]` → vínculo no **ticket**.
- `GET /api/contatos/8/etiquetas` → **Smoke Contato** mantido (isolamento Contato vs ticket legado).

## Endpoints observados

| Contexto | GET | PUT |
|----------|-----|-----|
| TK-000086 / 088 (contatoId 8) | `/api/contatos/8/etiquetas` | `/api/contatos/8/etiquetas` |
| TK-000087 (contatoId 9) | `/api/contatos/9/etiquetas` | — |
| TK-000020 (sem contatoId) | `/api/tickets/TK-000020/etiquetas` | `/api/tickets/TK-000020/etiquetas` |

## Ajustes de código

Nenhum (apenas smoke + documentação).

## Testes

- Smoke UI/API manual; **HTTP 200** em `http://localhost:8080/`.
- Cobertura automatizada prévia: `chatsView.test.js` (fallback sem `contatoId`).

## Pendências / riscos

- Encerrar TK-000086 / TK-000088 duplicados: ver correção Sprint 211 (`docs/SPRINT_211_ENCERRAMENTO_500_TICKET_ATIVO_CONTATO.md`) — 500 por `nota` null em `ticket_satisfacao`; integração reforçada contra segundo ABERTO.
- Reexecutar salvar etiqueta no **TK-000020** com sessão válida no browser (opcional).
