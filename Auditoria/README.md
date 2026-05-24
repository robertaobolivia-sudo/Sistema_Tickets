# Auditoria — índice

## Fontes vigentes (ordem)

1. **[README_MESTRE.md](../README_MESTRE.md)** — documentação central do projeto
2. **`AUDITORIA-004-context.json`** — contexto compacto para IA
3. **`AUDITORIA-004-pos-reestruturacao.md`** — auditoria, riscos, matrizes, testes
4. **`e2e/README.md`** — somente execução E2E
5. **`docs/SPRINT_F46_*` / `docs/SPRINT_F48_*`** — checklists de validação e CI

### Histórico (não usar como regra de implementação)

| Artefato | Papel |
|----------|--------|
| `AUDITORIA-001-*` | Inventário pré-reestruturação |
| `AUDITORIA-002-*` | Fluxos pré-reestruturação |
| `AUDITORIA-002-addendum-whatsapp-provedor-pendente.md` | Noop / provider (pendência planejada) |
| `AUDITORIA-003-migracao-legado-cliente-contato-whatsapp.md` | Plano migração |
| `AUDITORIA-003-context.json` | Log sprints F24–F49 |
| `AUDITORIA-001-002-003-HISTORICO-ADDENDUM.md` | Aviso 001–003 = histórico |
| Auditoria F19–F40, `LEGADO-LIMPEZA-F49.md` | Migração / varredura |

**Decisões novas:** partir de **README_MESTRE** + **AUDITORIA-004**, não de 001/002/003.

---

## Documentos por tema

| Arquivo | Conteúdo |
|---------|----------|
| [README_MESTRE.md](../README_MESTRE.md) | **Entrada principal** |
| [AUDITORIA-004-pos-reestruturacao.md](./AUDITORIA-004-pos-reestruturacao.md) | Estado atual |
| [REGRA-ORIGEM-TICKET.md](./REGRA-ORIGEM-TICKET.md) | Ponteiro origens |
| [REESTRUTURACAO-F41-SMOKE.md](./REESTRUTURACAO-F41-SMOKE.md) | Marco F41 (histórico útil) |
| [LEGADO-LIMPEZA-F49.md](./LEGADO-LIMPEZA-F49.md) | Varredura F49 |

---

## Validação (F46 / F48)

```powershell
.\scripts\validar-pos-reestruturacao.ps1
```

`npm run test:pos-reestruturacao` em `e2e/`. Workflow: `.github/workflows/pos-reestruturacao.yml`.
