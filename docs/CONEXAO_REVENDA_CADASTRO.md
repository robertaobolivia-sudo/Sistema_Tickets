# Conexão / Revenda — cadastro e arte do header Chats

## Nome técnico

- Entidade JPA: **`Carteira`** (tabela `carteiras`).
- No produto e nos tickets, o nome da carteira aparece como **conexão** (`ticket.conexao`) e/ou **carteira** do cliente.
- Não há entidade separada “Revenda”; o cadastro desta sprint é em **Configurações → Conexões / Revendas**.

## Arte do header do Chats (Sprint 174)

- Campo opcional: `arteHeaderChatsUrl` (URL pública relativa, ex.: `/uploads/conexoes/header-chats/carteira-1-….webp`).
- Armazenamento em disco: `uploads/conexoes/header-chats/` (servido via `/uploads/**`).
- Formatos: PNG, JPG/JPEG, WEBP. Limite: **5 MB** (multipart global do projeto até 10 MB).
- Dimensão recomendada na UI: **1200 × 240 px**, proporção **5:1** (orientação; upload não é bloqueado por dimensão).
- A página **Chats** ainda **não** consome esta URL (sprint futura).

## API

| Método | Caminho | Permissão |
|--------|---------|-----------|
| GET | `/api/carteiras` | Sessão |
| GET | `/api/carteiras/{id}` | Sessão |
| POST | `/api/carteiras` | ADMIN ou SUPERVISOR |
| PUT | `/api/carteiras/{id}` | ADMIN ou SUPERVISOR |
| POST multipart `arte` | `/api/carteiras/{id}/arte-header-chats` | ADMIN ou SUPERVISOR |
