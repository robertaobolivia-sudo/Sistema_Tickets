# Sprint 290 — Smoke telefones adicionais + HTTP 200

## Causa do HTTP 500

- Processo Java anterior na porta **8080** respondia **500** em todas as rotas (`/` e `/error`), indicando aplicação em estado inválido (não erro de tela Sprint 289).
- Reinício com `java -jar target/suporte-tickets-1.0.0.jar` falhou quando o JAR não estava **reempacotado** (`mvn compile` sem `package`).
- **Correção operacional:** encerrar Java → `mvn package -DskipTests` → `java -jar target/suporte-tickets-1.0.0.jar` na raiz do projeto.

## HTTP 200

- Após reinício: `GET http://localhost:8080/` → **200**.

## Smoke API (2026-05-22)

Credenciais E2E padrão (`e2e/global-setup.ts`): `robertaobolivia@gmail.com` / senha do seed.

| Passo | Resultado |
|-------|-----------|
| Login | OK (analista id=11) |
| Contato | id **69**, cliente **89**, principal `5511980030111` |
| POST telefone adicional | `5512942833853` (origem unificação) → OK |
| GET lista | 1 telefone adicional |
| POST duplicado | **400** — já existe no cliente |
| POST igual principal | **400** — não pode ser adicional |
| GET busca por WhatsApp adicional | `contato_id` **69** (mesmo contato) |

## Smoke manual (navegador)

1. Ctrl+F5 em `http://localhost:8080/` (`app.js?v=sprint289-telefones`).
2. Clientes → Contatos → **Ver/Editar** no contato acima (ou qualquer um com cliente).
3. Conferir WhatsApp principal readonly.
4. Incluir telefone adicional; conferir lista.
5. Repetir número → mensagem de erro na seção.
6. Repetir número do principal → bloqueio.
7. Fechar e reabrir modal → telefone persiste.

## Próximo passo

Sprint Chats/histórico (exibir telefones adicionais) ou remoção de adicional, conforme produto.
