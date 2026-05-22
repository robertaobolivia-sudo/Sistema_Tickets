# Sprint 244 — Visual corporativo Relatórios

## Objetivo

Alinhar a página Relatórios ao padrão teal/neon (`--corp-*`), sem alterar filtros, busca, exportação CSV nem regras de negócio.

## Backup

`Sistemas_BKP\BKP_Sprint_244_Relatorios_Visual`

## Arquivos

- `css/pages/relatorios.css` — bloco `#page-relatorios` Sprint 244
- `index.html` — `relatorios.css?v=sprint244`

**Inalterados:** backend, schema, JS (`relatoriosPage.js`), paginação Sprint 243.

## Visual

Header, `content-section`, filtros do relatório e bloco Satisfação, botões (Gerar, Exportar CSV, Limpar, Atualizar), cards de resumo, tabelas (`data-table`), empty states e foco de inputs.

## Filtros / CSV preservados

- Período, Cliente, Motivo, Status pesquisa, Nota, demais filtros do formulário e seção Satisfação — mesmos IDs e handlers.
- `relatorioExportarCsvBtn`, `satisfacaoExportarCsvBtn` — sem mudança funcional.

## Testes

Maven/npm não executados (somente CSS).

## Smoke sugerido

Relatórios → filtros combinados → Gerar relatório → Exportar CSV (tickets e satisfação) → tema claro/escuro.

## GitHub

`Sprint 244 — Visual corporativo Relatórios`

## Próximo passo

Sprint 245 — Configurações e demais páginas (blueprint).
