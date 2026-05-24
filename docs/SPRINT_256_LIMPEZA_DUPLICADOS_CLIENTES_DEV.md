# Sprint 256 — Limpeza duplicados Clientes B2B (DEV)

## Estratégia

1. Identificar **oficiais** pela razão social da massa Sprint 253 (4 LTDA).
2. Marcar **legados** (nomes curtos: Rocha Mendes, Status Automação, FastComércio, Fênix) e mapear para o oficial correspondente.
3. **Tickets:** `cliente_id` → oficial; `contato_id` → contato do oficial com mesmo `whatsapp_normalizado`.
4. **Realinhamento extra:** tickets `S253-{razão}%` apontando para o `cliente_id` correto.
5. Remover contatos, WhatsApp matriz e registros em `contatos_clientes` dos legados; **excluir** clientes legados (ids 91–94 na base antes da limpeza).

## Execução

- Propriedade (somente local): `app.sprint256.dedup-clientes-dev=true` no boot (já revertida para `false` após rodar).
- Classe: `Sprint256DevClientesDedupConfig` + `MassaOficialClientesDevConstants`.
- Seed 253: idempotência por **CNPJ** (`findByCnpj`) para não recriar duplicados em novo reset.

## Totais após smoke API

| Recurso | Esperado | Obtido |
|---------|----------|--------|
| Clientes | 4 | 4 |
| Contatos | 12 | 12 |
| Tickets | 12 | 12 (3 por cliente 87–90) |

## Oficiais mantidos

- Rocha Mendes Comercio LTDA  
- Status Automacao Industria ME  
- Fast Comercio Varejo SA  
- Fenix Servicos Digitais LTDA  

## Preservado

- Login ADMIN, catálogos, SLA, motivos, permissões.
