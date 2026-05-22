# Sprint 229 — UX: Login corporativo moderno

## Objetivo

Repaginar visualmente a tela de login (corporativa, moderna), sem alterar autenticação, sessão ou endpoints.

## Alterações

| Arquivo | Mudança |
|---------|---------|
| `index.html` | Estrutura visual: logo F neon, título, ícones nos campos, links decorativos, rodapé |
| `css/pages/login.css` | Novo — fundo `#0F2F3A`, card, neon `#00FFAA`, responsivo |
| `css/layout.css` | Removidos estilos legados do login (movidos para `login.css`) |

## Preservado (fluxo / testes)

- `id`: `loginScreen`, `loginForm`, `loginUser`, `loginPass`, `loginAlert`
- `data-testid`: `login-email`, `login-password`, `login-submit`
- Classes do botão: `button button-primary button-large`
- `form-group`, `login-wrapper`, `login-box`

## Backup

`Sistemas_BKP/BKP_Sprint_229_Login_Corporativo`

## Links decorativos

“Esqueceu a senha?”, “Cadastre-se” e “Mudar para Inglês” são apenas visuais (`onclick="return false"` / `type="button"`).
