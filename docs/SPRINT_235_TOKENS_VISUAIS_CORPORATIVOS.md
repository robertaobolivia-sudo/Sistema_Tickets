# Sprint 235 — Tokens visuais corporativos

## Objetivo

Definir tokens CSS globais `--corp-*` (Login 229 / Blueprint 234) sem redesenhar telas.

## Backup

`C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_235_Tokens_Visuais_Corporativos`  
(cópia de `theme.css` e `components.css` antes da alteração)

## Arquivos alterados

| Arquivo | Alteração |
|---------|-----------|
| `src/main/resources/static/css/theme.css` | Bloco documentado `--corp-*` em `:root` e overrides em `html[data-theme="dark"]` |
| `src/main/resources/static/css/components.css` | Comentário de referência para uso futuro |

**Não alterados:** `index.html`, JS, Java, `login.css` (login segue valores locais).

## Tokens (resumo)

| Grupo | Exemplos |
|-------|----------|
| Cores | `--corp-bg-deep`, `--corp-accent-neon`, `--corp-text-on-dark`, `--corp-surface-card`, `--corp-border-subtle` |
| Elevação | `--corp-shadow-card`, `--corp-shadow-soft`, `--corp-shadow-neon-glow` |
| Raio | `--corp-radius-sm` (12px), `--corp-radius-md` (16px), `--corp-radius-lg` (20px) |
| Espaço | `--corp-space-xs` … `--corp-space-xl` |
| Tipografia | `--corp-font-family`, `--corp-font-page-title`, `--corp-font-label` |
| Interação | `--corp-transition`, `--corp-focus-ring`, `--corp-focus-border` |
| Botões (futuro) | `--corp-btn-primary-*`, `--corp-btn-secondary-*` |
| Shell (futuro) | `--corp-sidebar-bg`, `--corp-nav-active-*`, `--corp-gradient-hero` |

Lista completa e comentários de uso: cabeçalho e bloco em `theme.css`.

## Impacto visual

**Nenhum consumo nos seletores existentes** — variáveis declaradas mas não referenciadas por layout/páginas/login. Telas e login permanecem como antes.

## Testes

- Maven/npm: não executados (somente CSS).
- Smoke visual (manual): Login, Dashboard, Clientes, Chats — esperado **sem diferença perceptível**.

## Próximo passo

**Sprint 236** — aplicar tokens no shell (`layout.css`: sidebar, topbar, nav ativo).
