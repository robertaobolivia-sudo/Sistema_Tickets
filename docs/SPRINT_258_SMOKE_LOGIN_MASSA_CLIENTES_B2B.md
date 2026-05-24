# Sprint 258 — Smoke login real + massa Clientes B2B

## Tipo

Sprint **smoke** (validação; sem alteração de código funcional).

## Objetivo

Confirmar no browser (Chromium/Playwright, equivalente Chrome/Edge) o login corrigido na Sprint 257 e a massa operacional nas telas principais.

## Ambiente

- App: `http://127.0.0.1:8080/` — **HTTP 200 OK**
- Cache: `app.js?v=sprint257` no HTML servido
- Credenciais: `e2e/.massa.json` (não versionado)

## Resultados — Login

| Cenário | Resultado | Evidência |
|---------|-----------|-----------|
| Login válido ADMIN | **OK** | `#appScreen.screen-active` após submit; Dashboard `#page-dashboard.active` |
| Login inválido | **OK** | `#loginAlert`: "E-mail ou senha inválidos."; permanece em login |
| Console erro crítico no boot/login | **OK** | `pageErrors: []` no fluxo Playwright |
| API login | **OK** | `POST /api/analistas/login` 200 + `authToken` |

## Resultados — Massa API (pós-login)

| Entidade | Esperado (Sprint 256) | Obtido | Status |
|----------|----------------------|--------|--------|
| Clientes | 4 LTDA | **8** (4 oficiais 87–90 + 4 legado/ruído 95–98) | **Ressalva** |
| Contatos gestão | 12 | **12** | OK |
| Tickets | 12 | **12** | OK |

Dedup Sprint 256 não está refletido neste banco (`app.sprint256.dedup-clientes-dev=false`). Recomenda-se rodar dedup DEV ou repetir Sprint 256 antes de exigir “só 4” na listagem.

## Resultados — UI (Playwright)

| Tela | Resultado | Detalhe |
|------|-----------|---------|
| Dashboard | OK | Visível após login |
| Clientes → Listagem | Ressalva | **10 linhas** na tabela (`#clientesListaTableBody`) — alinhado aos 8 clientes da API |
| Clientes → Cadastro | OK | `#clienteFormTitle` visível ao abrir submenu Cadastro |
| Clientes → Contatos | OK | **12 linhas**; filtro `#contatosGestaoFiltroCliente` com opções (12); filtro por índice 1 deixou 0 linhas — validar manualmente por contratante |
| Tickets | OK | **15 linhas** em `#ticketsBody2` (lista carregada; API 12 — possível paginação/linhas extras na UI) |
| Chats | OK | **4** itens em `#chatsLista`; lista não vazia |

## Ajustes de código

Nenhum (smoke apenas).

## Testes automatizados

- `npm` / `mvn`: não executados (sem mudança de código).
- Playwright: login UI validado; spec `encerramento-sem-contato` passou `loginUi` e falhou depois em seletor duplicado de ticket E2E (fora do escopo desta sprint).

## Pendências

1. Aplicar massa oficial 4 clientes (Sprint 256 dedup) no ambiente local.
2. Smoke manual Chrome/Edge com **Ctrl+F5** pelo usuário (recomendado).
3. Validar filtro de Contatos por contratante específico na UI.

## Próximo passo

Sprint operacional: reexecutar dedup DEV (256) ou CRUD Contatos; em seguida smoke visual Listagem com exatamente 4 contratantes LTDA.
