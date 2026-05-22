# Sprint 218 — Smoke visual modal + E2E

## Massa

- Ticket criado para smoke: **TK-000111** (`POST /api/integracoes/whatsapp/mensagens`, telefone `5511963978208`).
- Login browser: analista admin (ambiente dev).

## Smoke visual (browser, desktop)

| Item | Resultado |
|------|-----------|
| Modal abre (`modalEncerramento.ativo`) | OK |
| Largura ~640px (Sprint 217) | OK após sync `index.html` + `modals.css` em `target/classes/static` e cache-bust |
| Scroll interno no card | **Não** (`overflow: visible`, `scrollHeight === clientHeight`) |
| Fluxo vertical (`encerramento-stack`) | OK |
| Pergunta com ⭐ | OK |
| Botões centralizados (`encerramento-actions--center`) | OK |
| Categorias carregam (grupos > 1 opção) | OK (5 opções) |
| Aviso sem contato | Oculto para TK-000111 com contato (esperado) |

**Nota:** Com JAR antigo em execução, o modal ainda aparecia com layout 920px (Sprint 216). Após copiar estáticos para `target/classes/static`, validar com **Ctrl+F5** ou `?v=sprint217` / `?v=sprint218`.

## Playwright

- `cd e2e && npm test` → **1 passed**

## HTTP

- `http://localhost:8080/` → **200**

## Ajustes de código nesta sprint

- Nenhum (validação + doc + sync estático no ambiente local).

## Próximo passo

- Reiniciar app após `mvn package` em deploys para garantir Sprint 217 no JAR empacotado.
