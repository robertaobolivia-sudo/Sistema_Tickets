# Sprint 237 — Login e Avaliação pública no padrão corporativo

## Objetivo

Avaliação pública alinhada ao Login/shell corporativo; Login migrado para tokens `--corp-*` sem mudar o visual aprovado (Sprint 229).

## Backup

`Sistemas_BKP\BKP_Sprint_237_Login_Avaliacao_Publica`

## Arquivos alterados

| Arquivo | Alteração |
|---------|-----------|
| `css/pages/avaliacao-publica.css` | Repaginação teal/neon, card, notas, CTA, variantes de estado |
| `css/pages/login.css` | Valores via `--corp-*` (gradiente do botão Entrar preservado) |
| `js/core/avaliacaoPublicaView.js` | `estadoVariant` para estilos (pendente/respondida/expirada/invalida) |
| `js/pages/avaliacaoPublicaPage.js` | Classes CSS de estado no `#avaliacaoPublicaEstado` |
| `js/tests/avaliacaoPublicaView.test.js` | Cobertura de `estadoVariant` |
| `index.html` | Bump `?v=sprint237` em login e avaliação pública |

**Inalterados:** backend, APIs, token, autenticação, `data-testid`, fluxo `?page=avaliacao&token=`.

## Mudanças visuais

- **Avaliação pública:** fundo hero teal como login; card com borda/sombra corp; notas com seleção neon; botão enviar neon; estados com cores distintas (verde âmbar vermelho).
- **Login:** aparência mantida; implementação referencia tokens globais.

## Testes

| Suite | Resultado |
|-------|-----------|
| Vitest `avaliacaoPublicaView` | 4 passed |
| Playwright `e2e` | 2 passed (inclui fluxo avaliação pública); 1 failed `encerramento-sem-contato` (ticket TK-000151 não na lista — massa/ambiente, não CSS) |

## Smoke manual

- `?page=avaliacao&token=` válido → formulário PENDENTE.
- Token respondido/expirado/inválido → mensagem + estilo de estado.
- Login válido/inválido → fluxo e testids iguais.

## GitHub

Registrar commit/PR com mensagem: **Sprint 237 — Login e Avaliação pública no padrão corporativo** (repositório local sem `.git` neste ambiente — aplicar no remoto pelo operador).

## Próximo passo

Sprint 238 — repaginação visual Clientes (`clientes.css`).
