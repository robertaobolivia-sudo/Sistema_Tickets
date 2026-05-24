> **ATEN«√O ó DOCUMENTO HIST”RICO**
>
> Este arquivo descreve um estado anterior do sistema.  
> N„o usar como regra atual de implementaÁ„o.  
> Contexto vigente: `Auditoria/AUDITORIA-004-pos-reestruturacao.md` + validaÁ„o F45/F46.  
> Modelo atual: Cliente ? Contato ? WhatsApp Matriz ? Ticket.
>
> ---

# Sprint F38 ‚Äî DROP ContatoCliente + FK solicitante

## Patch

`ContatoClienteDropF38Patch` (@Order 4), idempotente:

1. Log pr√©-drop (contagens).
2. `CREATE TABLE contatos_clientes_backup_f38 AS SELECT * FROM contatos_clientes` (se tabela existir).
3. `CREATE TABLE tickets_contato_solicitante_backup_f38 AS SELECT id, numero, contato_solicitante_id, contato_id, cliente_id, origem_ticket, data_abertura FROM tickets WHERE contato_solicitante_id IS NOT NULL`.
4. DROP FK em `tickets.contato_solicitante_id` (via `information_schema`).
5. `ALTER TABLE tickets DROP COLUMN contato_solicitante_id`.
6. `DROP TABLE contatos_clientes`.

N√£o altera `contatos` nem `contato_etiquetas`.

## Runtime removido

- Entidade, repository, service, controllers, DTOs ContatoCliente.
- Campo JPA/DTO `contatoSolicitante*`.
- Endpoints `/api/clientes/{id}/contatos*` e `/api/contatos-clientes/*`.
- `ContatoClienteInventarioF37Patch` (substitu√≠do pelo drop F38).

## Contato real

Intacto: `/api/contatos`, entidade `Contato`, Chats `contatoId`, abertura manual `contatoWhatsappId`.

## Hist√≥rico

F36‚ÄìF37 desligaram opera√ß√£o; F38 remove schema + c√≥digo.
