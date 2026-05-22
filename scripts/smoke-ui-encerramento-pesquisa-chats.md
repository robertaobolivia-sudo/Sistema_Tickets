# Smoke UI — encerramento, pesquisa e Chats (Sprint 213)

Pré-requisitos: app em `http://localhost:8080/`, credenciais ADMIN (`SMOKE_ADMIN_EMAIL` / `SMOKE_ADMIN_SENHA`).

## Massa API (antes do browser)

```powershell
cd Sistema\suporte-tickets
# Dois tickets ABERTO para encerrar pela UI:
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/analistas/login' -Method Post -ContentType 'application/json' -Body '{"email":"...","senha":"..."}'
$h = @{ 'X-Analista-Id'="$($login.id)"; 'X-Analista-Token'=$login.authToken }
# UI-A sem pesquisa: telefone 5511963978930
# UI-B com pesquisa: telefone 5511963978931
# Ativo D: 5511963978922 (TK-000097) + segunda mensagem reutiliza
```

## Roteiro browser

1. Login (`data-testid=login-*`).
2. **Chats** → aba **Fila** (`chats-tab-fila`).
3. Buscar ticket (ex. `000097` ou card `chats-card-TK-...`).
4. Abrir conversa; conferir painéis `chats-panel-cliente|contato|entrada|chamado` e `chats-timeline`.
5. Botão `chats-primary-action` → **Encerrar ticket** (se ativo).
6. Modal `modal-encerramento`: `encerrar-grupo`, `encerrar-subgrupo`, `encerrar-motivo`, `encerrar-comentario`, `encerrar-pesquisa-nao|sim`, `encerrar-confirmar`.
7. Após encerrar: detalhes (se abrir) → `detail-satisfacao-status` (NAO_ENVIADA ou PENDENTE).
8. Com pesquisa: alerta com link ou `detail-satisfacao-link`; abrir `/?page=avaliacao&token=...`.
9. Página pública: `avaliacao-nota-5`, comentário, `avaliacao-publica-enviar`, estado `avaliacao-publica-estado`.
10. Segunda tentativa de envio → bloqueio/respondida.

## Seletores principais

| Área | data-testid |
|------|-------------|
| Fila | `chats-tab-fila`, `chats-list`, `chats-card-{numero}` |
| Painel | `chats-panel-cliente`, `chats-panel-contato`, `chats-panel-entrada`, `chats-panel-chamado` |
| Ação | `chats-primary-action` |
| Encerrar | `modal-encerramento`, `encerrar-*` |
| Satisfação detalhe | `detail-satisfacao-status`, `detail-satisfacao-envio`, `detail-satisfacao-link` |
| Pública | `avaliacao-publica-*`, `avaliacao-nota-1`…`5` |
