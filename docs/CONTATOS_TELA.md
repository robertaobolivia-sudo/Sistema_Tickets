# Contato WhatsApp — API (Sprint 189)

## Conceito

- **Contato** = pessoa final atendida pelo WhatsApp.
- Vinculado a um **Cliente** (contratante F5).
- Chave única: `cliente_id` + `whatsapp_normalizado` (apenas dígitos).
- **WhatsApp imutável** após criação.

## Tabela `contatos`

Campos principais: `cliente_id`, `nome`, `whatsapp`, `whatsapp_normalizado` (updatable=false), `email`, `empresa_local`, `cidade`, `uf`, `observacoes`, `ativo`, `criado_automaticamente`, `primeira_interacao_em`, `ultima_interacao_em`, `criado_em`, `atualizado_em`.

## Endpoints (sessão analista)

| Método | URL | Uso |
|--------|-----|-----|
| POST | `/api/contatos` | Criar |
| GET | `/api/contatos?clienteId=` | Listar por contratante |
| GET | `/api/contatos/{id}` | Detalhe |
| GET | `/api/contatos/busca?clienteId=&whatsapp=` | Busca por Cliente + WhatsApp |
| PUT | `/api/contatos/{id}` | Atualizar (sem mudar WhatsApp) |
| PATCH | `/api/contatos/{id}/ativar` | Ativar |
| PATCH | `/api/contatos/{id}/inativar` | Inativar |

Headers: `X-Analista-Id`, `X-Analista-Token`.

## Ticket (preparatório)

- Coluna opcional `tickets.contato_id` → `contatos.id`.
- `TicketResponseDTO`: `contatoId`, `contatoNome`, `contatoWhatsapp`.

### Sprint 190 — criação de ticket

Em `TicketService.criarTicketPorWebhook` (POST `/api/tickets`, webhook, integração WhatsApp):

1. Após definir `Cliente` e opcional `contatoSolicitante`, resolve telefone (payload → solicitante legado → telefones do Cliente).
2. Se houver telefone válido: `ContatoService.criarSeNaoExistir(clienteId, telefone, nome)` e `ticket.contato`.
3. Sem telefone: ticket segue **sem** `contato_id` (não falha).

Nome do Contato: `nomeContato` no payload → nome do solicitante legado → campo `cliente` do webhook → nome do cadastro Cliente.

`TicketWebhookRequestDTO.nomeContato` opcional; integração preenche a partir de `nomeContato`.

## Legado

- `ContatoCliente` / `contato_solicitante_id` permanecem.
- `Ticket.conexao`, `cliente_id` atuais inalterados nesta sprint.
- Sem tela UI de Contatos ainda.

## Sprint 193 — exibição no Chats

Com `contatoId` no ticket, o painel direito do Chats mostra dados do Contato (`contatoNome`, `contatoWhatsapp`, e-mail, empresa/local, cidade/UF, observações) no bloco **Contato**, separado do contratante no bloco **Cliente**.

DTO adicional no ticket: `contatoEmail`, `contatoEmpresaLocal`, `contatoCidade`, `contatoUf`, `contatoObservacoes`.

Sem `contatoId`: fallback com `contatoSolicitante*` e aviso de cadastro legado.

## Sprint 194 — Etiquetas do Contato

- Vínculo `contato_etiquetas`; APIs `GET/PUT /api/contatos/{id}/etiquetas`.
- Chats: com `contatoId` no ticket, etiquetas carregam/salvam no Contato; sem `contatoId`, fallback `TicketEtiqueta`.

## Sprint 195 — mensagem após encerramento

Nova mensagem do Contato com último ticket RESOLVIDO/CANCELADO gera pendência (`interacao_pendente_decisao`) até o analista vincular ao ticket anterior ou abrir novo ticket. Decisão no Chats (aba Fila).

## Próximo passo sugerido

- UI dedicada de Contatos por Cliente; migração histórica de etiquetas de tickets antigos.
