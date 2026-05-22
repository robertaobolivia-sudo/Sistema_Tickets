# WhatsApp Matriz (Sprint 191)

## Conceito

Número WhatsApp do **Cliente contratante** conectado à API. A mensagem recebida na matriz identifica o Cliente; o telefone do remetente identifica/cria o **Contato**.

## Tabela `whatsapp_matriz`

| Campo | Observação |
|-------|------------|
| cliente_id | FK obrigatória |
| numero / numero_normalizado | Único global (`uk_whatsapp_matriz_numero_norm`) |
| ativo | Inativar um número não afeta outros do mesmo Cliente |
| provedor, identificador_externo | Opcionais |

## Endpoints

Base: `/api/whatsapp-matrizes` (sessão analista).

- `POST` criar
- `GET ?clienteId=` listar por contratante
- `GET /{id}`
- `PUT /{id}`
- `PATCH /{id}/ativar` | `/inativar`

## Integração (`POST /api/integracoes/whatsapp/mensagens`)

Campos opcionais no payload:

- `whatsappMatrizId` — resolve Cliente pela matriz cadastrada
- `numeroMatriz` — resolve pela matriz ativa com mesmo número normalizado

Se **nenhum** vier: comportamento legado (`clienteId`, `buscarOuCriarCliente`).

Com matriz:

1. Cliente = contratante da matriz  
2. Ticket ativo buscado com `clienteId` do contratante  
3. Novo ticket: `clienteContratanteId`, `whatsappMatrizId`, `contato_id` via telefone do remetente  
4. `Ticket.conexao` preenchido com nome do contratante se vazio  

Matriz inexistente/inativa → erro controlado (não cria ticket no Cliente errado).

## Ticket

- FK opcional `tickets.whatsapp_matriz_id`
- DTO: `whatsappMatrizId`, `whatsappMatrizNumero`

## UI (Sprint 192)

- Tela **Clientes** → seção **WhatsApps Matriz** no formulário do contratante salvo.
- Service: `js/services/whatsappMatrizService.js`.

## Sprint 193 — Chats

Bloco **Entrada do atendimento** no painel direito: `whatsappMatrizNumero` e `whatsappMatrizNome` (quando distinto do número). Oculto se não houver matriz no ticket.

## Sprint 204 — Smoke entrada

- Registro completo: `docs/SPRINT_204_SMOKE_ENTRADA_WHATSAPP.md`
- Testes automatizados: `IntegracaoMensagemEntradaSmokeTest`, `IntegracaoMensagemMatrizTest`

## Sprint 205 — Smoke operacional (script)

- Roteiro: `docs/SPRINT_205_SMOKE_OPERACIONAL_ENTRADA_WHATSAPP.md`
- Script: `scripts/smoke-entrada-whatsapp.ps1` (login ADMIN + payloads A/B/C + GET ticket)

## Próximas sprints

- Inativar Cliente → inativar todas as matrizes  
- Inativar Cliente → inativar/desconectar todas as matrizes  
- Provedor WhatsApp real  
- Fluxo pós-encerramento e avaliação no Chats  
