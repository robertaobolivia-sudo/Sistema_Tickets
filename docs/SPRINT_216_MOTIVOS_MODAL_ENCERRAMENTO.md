# Sprint 216 — Motivos e repaginação do modal de encerramento

## Motivos (seed idempotente)

- `CategoriaSeedConfig` já criava **Outros** ao inserir subcategorias do seed fixo.
- **Novo:** `garantirMotivoAtivoEmSubcategoriasAtivas()` roda na subida e, para cada subcategoria **ativa** sem nenhum motivo **ativo**, cria **Outros** (se o nome ainda não existir na subcategoria).
- Não duplica **Outros** em reexecução (`existsBySubgrupoCategoriaIdAndNomeIgnoreCase`).

## Modal de encerramento (UI)

- Classes: `modal-content-encerramento`, grid 3 colunas (Categoria / Subcategoria / Motivo), bloco principal em 2 colunas (comentário + card da pesquisa).
- Modal mais **largo** (~920px), **sem** `max-height` / scroll no card de encerramento.
- Pesquisa: legenda com ⭐, opções **Não enviar** / **Sim, enviar** em cards com destaque via `:has(input:checked)`.
- Padrão continua **Não enviar** (`checked` no radio `false`).

## data-testid (preservados)

`modal-encerramento`, `encerrar-grupo`, `encerrar-subgrupo`, `encerrar-motivo`, `encerrar-comentario`, `encerrar-pesquisa-nao`, `encerrar-pesquisa-sim`, `encerrar-confirmar`.

## Arquivos

| Arquivo | Alteração |
|---------|-----------|
| `config/CategoriaSeedConfig.java` | Garantia de motivo ativo |
| `config/CategoriaSeedConfigTest.java` | Testes unitários |
| `static/index.html` | Markup do modal |
| `static/css/modals.css` | Estilos Sprint 216 |

## Resultado (2026-05-21)

| Verificação | Resultado |
|-------------|-----------|
| `mvn clean install` | OK (incl. `CategoriaSeedConfigTest`) |
| Vitest `npm test` | 137 passed |
| Playwright `e2e` | 1 passed (clique no card Sim; radios visually hidden) |
| HTTP 200 `/` | OK |

## Como validar

1. Subir app, abrir encerramento em ticket ABERTO: selects preenchíveis, sem scroll interno em desktop.
2. `mvn clean install`
3. `cd src/main/resources/static/js && npm test`
4. `cd e2e && npm test`
