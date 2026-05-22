# Sprint 245 — Visual corporativo Configurações

## Objetivo

Página Configurações alinhada ao padrão `--corp-*` (teal/neon), sem alterar cadastros, permissões, formulários nem integrações.

## Backup

`Sistemas_BKP\BKP_Sprint_245_Configuracoes_Visual`

## Arquivos

- `css/pages/configuracoes.css` — bloco `#page-configuracoes` Sprint 245
- `index.html` — `configuracoes.css?v=sprint245`

**Inalterados:** backend, schema, JS (`configuracoesPage.js`, seções filhas), IDs, `data-testid`.

## Seções (fluxos preservados)

- Horário útil · Feriados · Metas SLA · Conexões/Revendas (Carteira) · Motivos · Etiquetas  
  (navegação por blocos na mesma página; permissões `config-admin-only` / etiquetas intactas)

## Visual

Header, `content-section`, hints, formulários, checkbox groups, botões, tabelas, empty states, inputs SLA, etiquetas/carteira preview.

## Testes

Maven/npm não executados (somente CSS).

## Smoke sugerido

Configurações → percorrer seções → recarregar listas → salvar onde seguro (horário/feriado) → tema claro/escuro.

## GitHub

`Sprint 245 — Visual corporativo Configurações`

## Próximo passo

Sprint 246 — Atendentes, Auditoria, Abrir ticket, Perfil (blueprint).
