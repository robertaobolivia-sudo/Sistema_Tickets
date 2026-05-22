# Sprint 206 — Ticket ativo por Cliente + Contato

**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_206_Ticket_Ativo_Contato`

## Problema (Sprint 205)

`TicketAtivoService.buscarEntidadeAtiva` buscava corretamente por **Cliente + Contato** (`findFirstByCliente_IdAndContato_Id...`), mas se não encontrasse, **caía no fallback** `findFirstByCliente_IdAndStatusIn` — qualquer ticket ativo do contratante. Isso fazia o payload C (outro telefone/Contato) anexar ao ticket do Contato A.

## Regra corrigida

| Situação | Comportamento |
|----------|----------------|
| `clienteId` + `contatoWhatsappId` resolvidos | Só retorna ticket ativo **desse par**; se não houver, `empty` → novo ticket. **Não** busca por cliente sozinho. |
| Sem `contatoWhatsappId` | Legado: `contatoSolicitanteId`, depois `clienteId`, depois telefone → clientes. |
| Status ativos | ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE |

## Arquivos

- `TicketAtivoService.java` — retorno imediato na busca Cliente+Contato.
- Testes: `TicketAtivoServiceContatoTest`, `IntegracaoMensagemDoisContatosTest`.
- Script: `scripts/smoke-entrada-whatsapp.ps1` — payload C deve criar ticket **diferente** de A.

## Testes automatizados

- Mesmo Cliente+Contato → reutiliza.
- Outro Contato, mesmo Cliente → não chama `findFirstByCliente_Id` sozinho; vazio → cria ticket na integração.
- Sem contato WhatsApp → legado por cliente preservado.

## Smoke operacional

```powershell
$env:SMOKE_ADMIN_EMAIL = '...'
$env:SMOKE_ADMIN_SENHA = '...'
$env:SMOKE_CLIENTE_ID = '<id com matriz ativa>'
.\scripts\smoke-entrada-whatsapp.ps1
```

Esperado: A cria TK-…; B reutiliza A; **C cria TK-… diferente**.

**Execução 2026-05-21 (cliente 69, matriz 2):** A → **TK-000084** (contato 6); B → reutiliza TK-000084; C → **TK-000085** (contato 7). OK.

## Chats (manual)

Roteiro detalhado: **`docs/SPRINT_207_SMOKE_BROWSER_CHATS_DOIS_CONTATOS.md`**.

1. Após o script, abrir **Chats → Fila**.
2. Dois tickets para o mesmo Cliente, Contatos diferentes nos painéis.
3. Timeline de cada um só com mensagens do respectivo Contato.

## Riscos / pendências

- Tickets antigos sem `contato_id` continuam no legado por telefone/cliente.
- Pós-encerramento (Sprint 195) inalterado nesta sprint.
- **Sprint 211:** recheck de ativo na integração + guarda em `criarTicketPorWebhook`; ver `docs/SPRINT_211_ENCERRAMENTO_500_TICKET_ATIVO_CONTATO.md`.

## Referências

- `docs/SPRINT_205_SMOKE_OPERACIONAL_ENTRADA_WHATSAPP.md`
- `docs/MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md`
