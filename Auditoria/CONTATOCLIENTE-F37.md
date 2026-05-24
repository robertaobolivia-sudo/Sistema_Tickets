> **ATEN��O � DOCUMENTO HIST�RICO**
>
> Este arquivo descreve um estado anterior do sistema.  
> N�o usar como regra atual de implementa��o.  
> Contexto vigente: `Auditoria/AUDITORIA-004-pos-reestruturacao.md` + valida��o F45/F46.  
> Modelo atual: Cliente ? Contato ? WhatsApp Matriz ? Ticket.
>
> ---

# Sprint F37 — Ocultar UI ContatoCliente + inventário DROP

## Inventário SQL (queries)

```sql
SELECT COUNT(*) FROM contatos_clientes;

SELECT COUNT(*) FROM tickets WHERE contato_solicitante_id IS NOT NULL;

SELECT COUNT(*) FROM tickets
WHERE contato_solicitante_id IS NOT NULL AND contato_id IS NOT NULL;

SELECT COUNT(*) FROM tickets
WHERE contato_solicitante_id IS NOT NULL AND contato_id IS NULL;
```

**Execução DEV (boot 2026-05-23):**

| Métrica | Count |
|---------|------:|
| `contatos_clientes` | 0 |
| `tickets_com_solicitante` | 0 |
| `tickets_solicitante_e_contato` | 0 |
| `tickets_so_solicitante` | 0 |

**F38:** ambiente DEV **pode** backup + DROP (zero linhas legado). Prod: reler log pós-deploy.

## F37 entregue

| Item | Ação |
|------|------|
| UI Clientes `#clienteContatosSection` | **Removida** do HTML |
| JS CRUD legado | **Removido** (`clientesPage`, `clienteService`) |
| APIs `/api/clientes/{id}/contatos*` | **410 Gone** |
| APIs `/api/contatos-clientes/*` | **410 Gone** |
| Contato real (`/api/contatos`) | **Intacto** |
| `TicketResponseDTO.contatoSolicitante*` | **Mantido** (histórico) |
| Entidade/tabela | **Mantidas** |

## Decisão F38 (backup + DROP)

| Condição | Ação F38 |
|----------|----------|
| `tickets_so_solicitante` = 0 | Pode dropar FK + coluna + tabela após backup |
| `tickets_so_solicitante` > 0 | **Não** DROP até migrar ou aceitar perda de rótulo histórico |
| `contatos_clientes` > 0 | Backup `contatos_clientes_backup_f38` antes de DROP |

Consultar log do boot pós-deploy para números reais do ambiente.

## Critério F37

- UI Clientes não cadastra ContatoCliente.
- Front não chama endpoints legados.
- Endpoints legados 410.
- Inventário documentado + log automático.
