# Sprint 183 — Smoke integrado Cliente → Arte → Chats

Data: 2026-05-21  
Ambiente: `http://localhost:8080/` (HTTP 200)

## Automatizado (API + Vitest)

| Verificação | Resultado |
|-------------|-----------|
| Login ADMIN (`/api/analistas/login`) | OK |
| Cliente com arte (`GET /api/clientes/59`) | `arteHeaderChatsUrl` = `/uploads/clientes/header-chats/cliente-59-….png` |
| Ticket vinculado (`GET /api/tickets/TK-000078`) | `clienteId=59`, `clienteArteHeaderChatsUrl` igual à do cliente |
| Imagem pública (`HEAD` na URL da arte) | HTTP 200 |
| Prioridade Cliente > Carteira (`chatsView.test.js`) | OK |
| Página Clientes sem rótulo Carteira (`clienteFormView.test.js`) | OK |
| `npm test` | 116 testes OK |

**Cliente usado no smoke API:** id **59** (nome no banco: ex. Fênix / Mercado Exemplo conforme cadastro).  
**Ticket usado:** **TK-000078** (clienteId 59).

## Manual no navegador (checklist)

1. Login ADMIN (credenciais de QA em `docs/QA_SEGURANCA_FLUXOS.md`).
2. **Clientes:** busca/lista; selecionar cliente **59** (ou outro com arte); confirmar ausência de rótulos Carteira/Revenda/Conexão; seção **Arte do header do Chats** + **Incluir arte**; prévia após upload; reabrir cadastro e conferir persistência.
3. **Chats:** **Ctrl+F5**; abrir conversa do mesmo cliente (ex. TK-000078); confirmar arte no header WL (classe `chats-conexao-header--has-custom-bg`, variável `--chats-conexao-header-bg-image`).
4. Conversa de cliente **sem** arte: gradiente ou fallback Carteira por nome (legado).
5. Tema claro/escuro; console sem erros.

## Cache

Se a arte não aparecer após deploy, usar **Ctrl+F5** no Chats.

## Pendências fora do escopo

- Migração automática arte Carteira → Cliente: não feita.
- Nomenclatura Carteira/Conexão em outras telas (Abrir Ticket, Relatórios, Configurações): documentado, não alterado nesta sprint.
