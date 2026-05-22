# Sprint 239 — Repaginação visual Clientes

## Objetivo

Alinhar a tela Clientes ao padrão corporativo (Blueprint 234 / tokens `--corp-*`), sem alterar comportamento nem backend.

## Backup

`Sistemas_BKP\BKP_Sprint_239_Clientes_Visual`

## Arquivos alterados

| Arquivo | Alteração |
|---------|-----------|
| `css/pages/clientes.css` | Repaginação completa: cards, lista, busca, formulário, arte, WhatsApps Matriz |
| `index.html` | Copy “Cliente/contratante F5”; bloco contatos legado mais discreto; `?v=sprint239` |

**Preservado:** todos os `id`, `data-testid`, estrutura do formulário e seções.

## Mudanças visuais

- Cards com borda/sombra/radius corporativos; título da página em teal.
- Lista master: item selecionado com neon; busca com foco neon.
- Blocos do formulário (dados, comunicação, endereço, status, arte, matriz) como cards internos.
- Botões primários neon na página; secundários outline teal.
- WhatsApps Matriz: itens em card, status em pill, seção com leve destaque neon.
- Arte Chats: preview com borda corp; classes `carteira-arte-*` mantidas (compatibilidade).
- Textos: “Clientes cadastrados”, “Buscar cliente”; legado de contatos internos enxuto.

## Testes

- Vitest `clienteFormView`: 5/5 OK.
- Maven/npm E2E: não obrigatório (somente CSS/HTML copy).

## Smoke manual

Clientes → buscar → selecionar → editar → arte → salvar → WhatsApps Matriz (após salvar cliente).

## GitHub

`Sprint 239 — Repaginação visual Clientes`

## Próximo passo

Sprint 240 — Chats (blueprint).
