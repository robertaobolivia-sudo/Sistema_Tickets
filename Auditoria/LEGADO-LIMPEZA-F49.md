# Sprint F49 — Varredura de legado e limpeza de ruído

**Data:** 2026-05-24  
**Escopo:** documentação, comentários, índices — **sem** alteração de regra de negócio, schema ou runtime funcional.

## Resumo

| Ação | Quantidade (resumo) |
|------|---------------------|
| Banner histórico | 9 arquivos Auditoria + `PLANO_COMPATIBILIDADE` |
| Docs normativos atualizados | `CONTRATOS_E_QA.md`, `API_CONTRATOS.md`, `README.md` |
| Índices novos | `DOCUMENTACAO_VIGENTE.md`, `historico-sprints/README.md`, `HISTORICO_LEGADO_REMOVIDO.md` |
| Código runtime removido | Nenhum (já limpo F24–F41) |
| Patches/backfill mantidos | F29, F32, F34, F38, F40 (nomes legados intencionais) |

## Grep `src/main` (Java + JS operacional)

| Termo | Achado | Classificação |
|-------|--------|---------------|
| `ticket_etiquetas` | Patches F32/F34, backfill | **BACKUP/AUDITORIA** — manter |
| `ContatoCliente` | `ContatoClienteDropF38Patch` | **BACKUP** — manter |
| `contatoSolicitanteId` | Testes assert **undefined** | **VIGENTE** — garante ausência |
| `getConexao` / `setConexao` | 0 em features operacionais | **Removido** |
| `chatsConexao` / `carteiraService` em chats | 0 | **Removido** |
| `carteira_id` em Cliente entity | 0 (DROP F40) | **Removido** |

## Grep `docs/` + `Auditoria/`

| Tipo | Tratamento |
|------|------------|
| Sprints F42–F48 | **VIGENTE** (validação) |
| Sprints 183–297, visuais | **HISTÓRICO** — índice em `historico-sprints/README.md` |
| Auditoria F19–F40 | **HISTÓRICO** — banner |
| AUDITORIA-004, REGRA-ORIGEM, F41 | **VIGENTE** |
| PLANO_COMPATIBILIDADE | **HISTÓRICO** — banner |

## Endpoints legados (documentação)

| Endpoint | Status documentado |
|----------|-------------------|
| `/api/contatos-clientes/*` | Removido F37 |
| `/api/clientes/{id}/contatos*` | Legado → `/api/contatos` |
| `/api/tickets/{n}/etiquetas` | Removido F34 |
| `/api/carteiras` | **Config global** — permitido |
| `/api/contatos/{id}/etiquetas` | **Vigente** |

## Achados permitidos (não remover)

- Config Carteira / Conexões / Revendas (UI + API)
- `NoopWhatsAppMessageSender`
- `uploads/conexoes` (Config)
- Classes `*Drop*Patch`, `*Backfill*` com nomes de tabela legada
- Testes que negam payload legado (`contatoSolicitanteId` undefined)

## Achados removidos / corrigidos (F49)

- Texto normativo em `CONTRATOS_E_QA` apontando `contatos-clientes` como atual
- Seções API_CONTRATOS sem marcação de removido
- README sem ponteiro para AUDITORIA-004 / gate F48
- README schema SQL sem aviso de legado

## Grep final (runtime operacional — esperado)

```text
src/main/java ... ticket_etiquetas  → só patches/backfill
src/main/java ... ContatoCliente    → só F38 patch
src/main/resources/static/js/features/chats → sem carteiraService / chatsConexao
```

## Validação pós-F49

Rodar gate F46/F48 quando alterar código; esta sprint foi **só docs**.

## Próximo

- Opcional: mover `docs/SPRINT_2*.md` físicos para `docs/historico-sprints/` (não feito — só índice)
- Required check GitHub + provider WhatsApp
