# Tela Clientes (Sprint 178–188)

## Semântica (Sprint 188 — Fase 1a)

- **Clientes** = **contratantes F5** (conexão/carteira da operação), donos da arte White Label do Chats e futuros WhatsApps matriz.
- A pessoa final atendida pelo WhatsApp será a entidade **Contato** (próxima sprint); não confundir com a seção **Pessoas de contato (cadastro interno)** (`ContatoCliente` legado).

## Layout

- Título: **Clientes**.
- Coluna esquerda: busca + lista de contratantes.
- Coluna direita: formulário em seções + pessoas de contato internas (legado).

## Formulário

Seções visíveis:

- **Dados do cliente** — nome do cliente, razão social/empresa, CNPJ.
- **Comunicação** — e-mail, telefones (não usar rótulo “Contato” nesta tela).
- **Endereço e localização**
- **Status do cadastro**
- **Arte do header do Chats**

### Nomenclatura

- Na UI **não** aparecem: Carteira, Revenda, Conexão, Subcliente.
- O vínculo `carteira_id` no banco pode permanecer em registros antigos; a tela **não envia** `carteira` no payload (Sprint 188).
- Backend **não cria** Carteira por nome no `ClienteService`; só aplica FK se `carteiraId` explícito (API legada).

### WhatsApps Matriz (Sprint 192)

- Seção no formulário do contratante (após arte do Chats).
- Lista, cadastro, edição, ativar/inativar via `/api/whatsapp-matrizes`.
- Cliente **novo** sem ID: mensagem para salvar o cliente antes; após primeiro salvamento, permanece em edição para permitir cadastrar matrizes.
- Ver `docs/WHATSAPP_MATRIZ.md`.

### Arte do header do Chats (Sprint 180+)

- Upload e prévia na seção dedicada; URL em `clientes.arte_header_chats_url`.
- Chats prioriza esta arte (Sprint 181); fallback Carteira fora desta tela até limpeza.

## Chats / Tickets

- Tickets e Chats **não** foram reestruturados nesta sprint; ainda usam `Ticket.cliente_id` → cadastro atual e `Ticket.conexao` quando aplicável.
- Dados legados (cadastro que era “empresa atendida”) podem coexistir até migração Contato (Sprint 189+).

## Fora do escopo desta tela

- Configurações → **Conexões / Revendas** (legado temporário; ver `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md`).
- Entidade Contato WhatsApp, WhatsAppMatriz, migração de tickets.

## Smoke

- Ver `docs/SPRINT_183_SMOKE_RELATORIO.md` (arte Cliente → Chats); repetir após alterações de domínio quando necessário.
