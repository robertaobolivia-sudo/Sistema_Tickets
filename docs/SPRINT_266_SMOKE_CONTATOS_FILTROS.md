# Sprint 266 — Smoke filtros avançados (Contatos)

## Objetivo

Validar API e massa DEV dos filtros da tela **Clientes → Contatos** (Sprint 265).

## Massa utilizada

- **Padrão:** 4 contratantes LTDA, 12 contatos (3 por cliente), tickets S253.
- **Ajuste smoke (API + flag única):**
  - Contato **63** (Rocha): etiqueta id **1**, cidade **Campinas**, UF **SP**.
  - Ticket **TK-000001** (contato 63): satisfação **nota 2** (avaliação ruim).
- Script: `tools/sprint266-seed-massa.ps1` (etiqueta/cidade).
- Flag DEV (só subida smoke): `--app.sprint266.smoke-massa=true` → `Sprint266DevContatosSmokeMassaConfig`.

## API — resultados (após JAR Sprint 265+)

| Filtro | Resultado esperado | Obtido |
|--------|-------------------|--------|
| `clienteId=90` | 3 contatos, sem mistura | OK (mistura=0) |
| `busca` impossível | 0 | OK |
| `busca` WhatsApp `5511980010111` | 1 | OK |
| `semEtiqueta=true` | 11 (63 com etiqueta) | OK |
| `comTicketsAbertos=true` | 8 (6 com ativos=1 na listagem bruta — conferir contagem API) | OK (8) |
| `comAvaliacaoRuim=true` | ≥1 | OK (1 = contato 63) |
| `etiquetaId=1` | ≥1 | OK (1) |
| `cidade=Campinas` | ≥1 | OK (1) |
| `uf=SP` | ≥1 | OK (1) |
| Combo `87+etiqueta1+abertos` | 1, mistura 0 | OK |

## Achado crítico (correção operacional)

Processo em **8080** estava com **bytecode antigo** (parâmetros avançados ignorados: todos os filtros retornavam 12).  
**Correção:** reiniciar com `target/suporte-tickets-1.0.0.jar` gerado após Sprint 265. Sem alteração de código de filtro.

## Browser

Smoke visual manual recomendado: login → Clientes → Contatos → filtros avançados → Histórico / Ver conversa após filtrar → paginação página 1.

Script API: `tools/sprint266-smoke-contatos.ps1`

## HTTP

`GET /` → **200** com app reiniciada.

## Pendências

- Reiniciar produção-like **sem** `--app.sprint266.smoke-massa=true` no dia a dia.
- Browser automation (Glass) não repetido nesta sprint (instável em execuções anteriores).
