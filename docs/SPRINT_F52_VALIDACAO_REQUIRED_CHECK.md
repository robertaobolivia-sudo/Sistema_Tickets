# Sprint F52 — Validação GitHub Actions e required check

> **Domínio:** [README_MESTRE.md](../README_MESTRE.md).

## 1. Objetivo

Executar o workflow **Gate pos-reestruturacao** no GitHub, obter execução verde e ativar o required check na branch **`main`**.

## 2. Branch

- **Trabalho local:** `chore/f52-gate-pos-reestruturacao` (criada nesta sprint)
- **Principal esperada:** `main` — `github.com/robertaobolivia-sudo/Sistema`

## 3. Commit / PR

| Item | Estado |
|------|--------|
| Commit local | Ver hash registrado abaixo após `git log -1` |
| Push | **Pendente** se `git push` falhar (auth / repo privado / `Repository not found`) |
| PR | Abrir manualmente: `chore/f52-gate-pos-reestruturacao` → `main` |

Mensagem sugerida do commit:

```text
docs(ci): centraliza README_MESTRE e gate pos-reestruturacao
```

## 4. Resultado local (referência)

| Etapa | Resultado |
|-------|-----------|
| `mvn test` | OK (sessão F52) |
| Vitest | 219/219 OK |
| `mvn package -DskipTests` | Rodar após `stop-java-8080.ps1` |
| F45 local | Opcional com app em 8080 ou `E2E_SKIP_WEB_SERVER=1` |
| HTTP 200 | Com `start-dev-server.ps1` |

## 5. Resultado GitHub Actions

| Campo | Valor |
|-------|--------|
| Workflow | `Gate pos-reestruturacao` |
| Arquivo | `.github/workflows/pos-reestruturacao.yml` |
| Check name | `Gate pos-reestruturacao / Gate pos-reestruturacao` |
| Execução | **Preencher após push** — link: `https://github.com/robertaobolivia-sudo/Sistema/actions` |

Se vermelho: anotar etapa (Maven / Vitest / package / Playwright) e log resumido; corrigir só CI/scripts.

## 6. Secrets

| Secret | Status |
|--------|--------|
| `SMOKE_ADMIN_SENHA` | Configurar em Settings → Secrets → Actions (**recomendado**) |
| `SMOKE_ADMIN_EMAIL` | Opcional (workflow usa default) |

Sem secret: workflow emite warning e usa fallback dev (risco documentado em F51).

## 7. Required check

Após **uma** execução verde:

1. Settings → Branches → regra **`main`**
2. **Require status checks to pass before merging**
3. Marcar **`Gate pos-reestruturacao / Gate pos-reestruturacao`**
4. Save

| Ativado? | Registrar |
|----------|-----------|
| sim / não | Admin do repositório |

**Pendência:** sem permissão de admin → required check permanece manual.

## 8. Ajustes de CI nesta sprint

- (Nenhum / listar se houver)

## 9. Próximo bloco

1. Confirmar Actions verde no PR  
2. Ativar required check em `main`  
3. Configurar `SMOKE_ADMIN_SENHA` no GitHub  
4. Provider WhatsApp real (sprint separada)

---

*Atualizar a tabela §5 e §7 após o primeiro push com sucesso.*
