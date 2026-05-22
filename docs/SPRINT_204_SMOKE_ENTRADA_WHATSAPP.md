# Sprint 204 — Smoke técnico: entrada WhatsApp (simulada)

**Data:** 2026-05-21  
**Backup:** `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_204_Smoke_Entrada_WhatsApp`  
**Endpoint:** `POST /api/integracoes/whatsapp/mensagens` (sessão analista: headers `X-Analista-Id`, `X-Analista-Token`)

## Objetivo

Validar o caminho **WhatsApp Matriz → Cliente contratante → Contato → Ticket → Chats**, sem provedor real.

## Massa de teste (referência)

| Entidade | Uso |
|----------|-----|
| Cliente contratante | Ex.: **Fênix** (id conforme banco local) |
| WhatsApp Matriz | Ativo, `cliente_id` do contratante, número único (ex. `5511888000000`) |
| Contato cenário A | Telefone novo: `5511999002041` (ajustar se já existir) |
| Contato cenário B | Mesmo telefone do A após ticket ativo criado |
| Contato cenário C | Telefone `5511999002043` com `numeroMatriz` |

> Não apagar dados em massa. Registrar ids reais obtidos no ambiente após cada execução manual.

## Payload base

```json
{
  "whatsappMatrizId": 3,
  "telefone": "5511999002041",
  "nomeContato": "Smoke Contato Novo",
  "mensagem": "Mensagem smoke 204 cenário A",
  "canal": "WHATSAPP",
  "origemExternaId": "smoke-204-a-1"
}
```

Alternativa matriz por número:

```json
{
  "numeroMatriz": "5511888000000",
  "telefone": "5511999002043",
  "nomeContato": "Smoke Numero Matriz",
  "mensagem": "Cenário C numeroMatriz"
}
```

## Cenário A — Contato novo (`whatsappMatrizId`)

| Verificação | Esperado | Resultado automatizado (JUnit) | Resultado API/manual |
|-------------|----------|-------------------------------|----------------------|
| Cliente resolvido pela matriz | `clienteContratanteId` = id do contratante | OK (`IntegracaoMensagemEntradaSmokeTest`) | Preencher após smoke manual |
| Contato criado/reutilizado | `criarSeNaoExistir` no cliente correto | OK | Preencher |
| Ticket criado | `ticketCriado=true`, status ABERTO | OK | Preencher |
| `cliente_id` / `contato_id` / `whatsapp_matriz_id` | Preenchidos no webhook → ticket | OK (captura webhook) | `GET /api/tickets/{numero}` |
| Primeira mensagem | `registrarAberturaAutomatica` + `mensagemInicial` | OK (fluxo `TicketService`) | Timeline Chats |
| Aparece na Fila Chats | Status ABERTO/EM_ATENDIMENTO na lista | — | Abrir Chats → Fila |

**IDs usados no teste mock:** matriz `3`, cliente `50`, contato `701`, ticket `TK-S204-A`.

## Cenário B — Contato existente, ticket ativo

Segundo payload: mesma matriz, **mesmo telefone** do A, nova `mensagem`.

| Verificação | Esperado | Resultado automatizado | Resultado API/manual |
|-------------|----------|------------------------|----------------------|
| Mesmo contato | Uma chamada `criarSeNaoExistir` (reuso) | OK | Preencher |
| Sem ticket novo | `ticketCriado=false` | OK | Preencher |
| Mensagem no ticket ativo | `registrarMensagemEntradaExterna` | OK | Timeline |

**IDs mock:** contato `702`, ticket ativo `TK-S204-B`.

## Cenário C — `numeroMatriz`

| Verificação | Esperado | Resultado automatizado | Resultado API/manual |
|-------------|----------|------------------------|----------------------|
| Matriz por número | `resolverMatrizAtivaPorNumero` | OK | Preencher |
| Cliente correto | Contratante da matriz | OK | Preencher |
| Ticket criado | `ticketCriado=true` | OK | Preencher |

**IDs mock:** ticket `TK-S204-C`.

## Smoke Chats (manual)

1. Login ADMIN.
2. **Chats → Fila:** ticket do cenário A visível.
3. Abrir ticket: painel direito com **Cliente**, **Contato**, **Entrada do atendimento** (matriz), **Chamado atual**.
4. Timeline com mensagem do payload.
5. Console sem erro crítico.

Se ticket não aparecer: conferir status (fila = ABERTO / EM_ATENDIMENTO / AGUARDANDO_CLIENTE conforme `chatsView.js`) e filtros da aba — documentar causa; refactor grande → Sprint 205.

## Bugs encontrados

| # | Descrição | Severidade | Ajuste |
|---|-----------|------------|--------|
| — | Nenhum bloqueante no smoke automatizado | — | Ajuste menor: nome do contato na integração usa `nomeContato` explicitamente em `resolverContatoWhatsapp` |

## Ajustes realizados (código)

- `IntegracaoMensagemEntradaService.resolverContatoWhatsapp`: prioriza `nomeContato` no cadastro do Contato.
- Testes: `IntegracaoMensagemEntradaSmokeTest` (cenários A, B, C).

## Testes executados

- `mvn clean install` (inclui smoke JUnit acima).
- `npm test` — somente se houver alteração JS (esta sprint: sem alteração de frontend).

## Pendências

- Smoke **API/manual** com ids reais do banco local (requer credenciais ADMIN e matriz cadastrada).
- Validar em ambiente com MySQL CLI ou UI se telefones de teste já existem (evitar colisão).
- Pendência pós-encerramento (Sprint 195): mensagem após RESOLVIDO pode abrir `InteracaoPendenteDecisao` em vez de ticket novo — fora do escopo deste smoke de entrada limpa.

## Próxima sprint sugerida

- **205:** smoke E2E com dados seed + script curl/PowerShell documentado; ou automação browser no Chats após entrada.
