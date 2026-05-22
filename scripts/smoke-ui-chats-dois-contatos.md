# Smoke UI — Chats com dois contatos (Sprint 208)

## Pré-requisitos

- App em `http://localhost:8080/` (HTTP 200).
- Massa Sprint 207 ou script `scripts/smoke-entrada-whatsapp.ps1` com `SMOKE_CLIENTE_ID=69`.
- Credenciais ADMIN (variáveis `SMOKE_ADMIN_EMAIL` / `SMOKE_ADMIN_SENHA`).

## Seletores (`data-testid`)

| Passo | Seletor |
|-------|---------|
| E-mail login | `[data-testid="login-email"]` |
| Senha login | `[data-testid="login-password"]` |
| Entrar | `[data-testid="login-submit"]` |
| Menu Chats | `[data-testid="nav-chats"]` |
| Aba Fila | `[data-testid="chats-tab-fila"]` |
| Lista | `[data-testid="chats-list"]` |
| Card ticket | `[data-testid="chats-card-TK-000086"]` (substituir número) |
| Painel Cliente | `[data-testid="chats-panel-cliente"]` |
| Painel Contato | `[data-testid="chats-panel-contato"]` |
| Painel Entrada | `[data-testid="chats-panel-entrada"]` |
| Painel Chamado | `[data-testid="chats-panel-chamado"]` |
| Timeline | `[data-testid="chats-timeline"]` |

## Passos manuais / assistidos

1. Login ADMIN.
2. Clicar **Chats** → aba **Fila**.
3. Confirmar cards `chats-card-TK-000086` e `chats-card-TK-000087` (ou tickets gerados pelo script).
4. Abrir **TK-000086**: painel Contato = Contato 8; Entrada = matriz `551198877665544`; timeline com mensagens A/B; sem texto do payload C.
5. Abrir **TK-000087**: Contato 9; mesma matriz; timeline só payload C; sem A/B.
6. Abrir **Contexto** se o painel estiver recolhido no layout atual.
7. Console (F12): sem erro crítico.

## Automação (exemplo CDP / Playwright)

```javascript
await page.fill('[data-testid="login-email"]', email);
await page.fill('[data-testid="login-password"]', senha);
await page.click('[data-testid="login-submit"]');
await page.click('[data-testid="nav-chats"]');
await page.click('[data-testid="chats-tab-fila"]');
await page.click('[data-testid="chats-card-TK-000086"]');
// assert text in [data-testid="chats-panel-contato"] e [data-testid="chats-timeline"]
```

## Etiquetas por Contato (Sprint 209)

1. Com TK-000086 aberto, abrir painel **Contexto** se necessário.
2. Em `[data-testid="chats-etiquetas-list"]`, clicar chip **Smoke Contato**.
3. Clicar `[data-testid="chats-etiquetas-save"]` — esperar “Etiquetas salvas com sucesso.”
4. Confirmar na rede: **PUT** `/api/contatos/8/etiquetas` (não `/api/tickets/.../etiquetas`).
5. Abrir TK-000087 — chip **Smoke Contato** **não** deve estar `selected`; hint `[data-testid="chats-etiquetas-legacy-hint"]` oculto.
6. Voltar ao TK-000086 — chip ainda selecionado.

Detalhe: `docs/SPRINT_209_SMOKE_UI_ETIQUETAS_CONTATO.md`.

## Etiquetas — outro ticket e legado (Sprint 210)

- Mesmo Contato: abrir **TK-000088** e **TK-000086** — ambos devem mostrar **Smoke Contato** selecionada; rede **GET `/api/contatos/8/etiquetas`**.
- Legado: aba **Atendendo**, ticket **TK-000020** — hint legado visível; **PUT `/api/tickets/TK-000020/etiquetas`**.

Ver `docs/SPRINT_210_SMOKE_ETIQUETAS_CONTATO_FALLBACK.md`.
