# Sprint F23 — Smoke Chats com arte real do Cliente

**Data:** 2026-05-23  
**Tipo:** smoke / validação (pós F20/F21/F22-B)

## Massa DEV

| Campo | Valor |
|-------|--------|
| Cliente ID | **89** |
| Nome / razão | Bruno Fast / **Fast Comercio Varejo SA** |
| Ticket com arte | **TK-000339** (aba Fila) |
| Ticket sem arte (gradiente) | **TK-000170** — Cliente **88** (Ana Status / Status Automacao Industria ME) |
| Endpoint upload | `POST /api/clientes/89/arte-header-chats` (multipart `arte`) → **HTTP 200** |
| URL arte | `/uploads/clientes/header-chats/cliente-89-83e0fcac-2ab2-41a5-9bf9-68a8dc5c77ac.png` |
| Arquivo público | **HTTP 200** em `http://localhost:8080` + path acima |

## Smoke browser (2026-05-23)

**Login:** `robertaobolivia@gmail.com` (ADMIN seed) — senha não documentada.  
**Cache-bust:** `?f23=final` após login.

| Critério | Resultado |
|----------|-----------|
| Header com arte (TK-000339) | OK — `--chats-cliente-header-bg-image` → `url("/uploads/clientes/header-chats/...")`, classe `chats-cliente-header--has-custom-bg` |
| Título Cliente | OK — **Fast Comercio Varejo SA** (empresa) |
| Network `/uploads/clientes/header-chats` | OK — 1 request PNG |
| Network `/api/carteiras` (fluxo Chats) | OK — **0** |
| Network `/uploads/conexoes` | OK — **0** |
| Header gradiente (TK-000170) | OK — `chats-cliente-header--bg-fallback`, sem `--has-custom-bg` |
| Painel direito | OK (F22-B preservado) |
| Console crítico | OK — nenhum `chatsConexao*` / `carteiraService` |

## Correção mínima (F21 deploy)

**Causa:** JAR antigo servia HTML com `chatsConexaoHeader` enquanto JS F21 usava `chatsClienteHeader` → `applyChatsClienteHeaderArte` no-op.

**Fix:** `resolveChatsDomElement()` em `chatsPage.js` (fallback ids legado) + invalidar cache `clienteArteById` null após upload.

**Ops:** rebuild `mvn -DskipTests package` + restart jar para HTML F21 + JS alinhados.

## Vitest

`chatsView.test.js` — **51/51** OK (`src/main/resources/static/js`).

## Fora de escopo

Carteira global, Dashboard, Config, WhatsApp real, schema.
