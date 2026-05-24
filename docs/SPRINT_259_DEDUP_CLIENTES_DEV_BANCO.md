# Sprint 259 — Dedup Clientes DEV no banco atual

## Resultado

- Dedup executada com sucesso: **4 clientes**, **12 contatos**, **12 tickets**.
- `app.sprint256.dedup-clientes-dev=false` após execução.
- App em `http://localhost:8080/` — HTTP **200**.

## Evidência log

```
Sprint 256: removido cliente legado id=95..98 → oficiais 87–90
Sprint 256: removido ticket E2E id=179,180,181
Sprint 256: removido cliente E2E/ruido id=99,100,101
Sprint 256: dedup concluida — clientes=4, tickets=12, contatos=12
```

## Smoke API (ADMIN)

| Endpoint | Esperado | Obtido |
|----------|----------|--------|
| POST `/api/analistas/login` | 200 | OK |
| GET `/api/clientes` | 4 LTDA | 4 |
| GET `/api/contatos?gestao=true` | 12 | 12 |
| GET `/api/tickets` | 12 | 12 |

## Browser

- Automação Glass: login com senha não confiável; API e massa validados.
- Manual: Clientes → Listagem (4 linhas); Contatos → filtro por contratante.
