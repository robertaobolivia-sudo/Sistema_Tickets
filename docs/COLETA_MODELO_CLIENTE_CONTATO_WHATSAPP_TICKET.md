# Coleta técnica — Modelo Cliente / Contato / WhatsApp / Ticket

**Sprint 184** — diagnóstico e documentação (somente leitura do código).  
**Data da coleta:** 2026-05-21  
**Projeto:** `suporte-tickets` (Java 21, Spring Boot 3.3, frontend estático modular em `src/main/resources/static`).

> **Histórico:** primeira versão deste relatório (Sprint 184). Sprints anteriores (178–183) já trataram UI Clientes, arte no Cliente e prioridade da arte no Chats — esta coleta consolida o estado do repositório para a reestruturação de produto.

---

## 1. Resumo executivo

### Maturidade atual
- **Operação de tickets madura:** CRUD, SLA, encerramento com grupo/subgrupo obrigatórios, Chats sobre tickets existentes, relatórios CSV/PDF, dashboard e indicadores parciais.
- **Modelo de domínio legado:** entidade **`Cliente`** ≈ empresa/pessoa do cadastro com telefones; **`Carteira`** ≈ agrupamento “conexão/revenda”; **`Ticket.conexao`** (string) duplica o nome da carteira no fluxo.
- **WhatsApp:** integração **preparatória** (`/api/integracoes/whatsapp/mensagens`), sem número matriz nem modelo explícito de “contato final WhatsApp”.
- **Etiquetas e satisfação:** implementadas no **Ticket**; satisfação **manual** (analista registra), sem disparo ao contato após RESOLVIDO.
- **Indicadores por Cliente F5:** subpágina “Clientes / Revendas” e várias abas ainda **em construção**; chamados agregam `totalClientes` pela entidade `Cliente` atual (não pelo contratante F5).

### Maior risco
Renomear/redefinir **`Cliente`** vs **`Carteira`** sem migração e sem camada de compatibilidade: impacta tickets, webhook, busca, Chats (título/arte), dashboard “por conexão”, CSV e indicadores.

### Maior reaproveitamento
- **`Ticket`** + FK **`cliente_id`** + opcional **`contato_solicitante_id`**.
- **`ContatoCliente`** (cadastro secundário no Cliente) — base para evoluir “contato”, com semântica diferente do produto novo.
- **Encerramento** com **`GrupoCategoria`** / **`SubgrupoCategoria`** já validado no backend.
- **Chats** + **`clienteArteHeaderChatsUrl`** no DTO do ticket (Sprint 181).
- **Permissões** (`ADMIN`, `SUPERVISOR`, `ANALISTA`) e padrão de sessão por headers.

---

## 2. Mapa por tela

### 2.1 Tela Clientes (`page-clientes`)

| Item | Detalhe |
|------|---------|
| **Estado atual** | Lista + busca à esquerda; formulário em seções à direita; estado vazio / novo / edição. |
| **Campos visíveis** | Nome, empresa, CNPJ; contato (e-mail, telefones); endereço (cidade, UF, endereço); status; observações; seção **Arte do header do Chats** (prévia, “Incluir arte”). |
| **Campos ocultos** | `carteiraCliente` (`type="hidden"`) — preserva `carteira` legado no payload (Sprint 182). `classificacaoCliente` (hidden, legado). |
| **Nomenclatura UI** | Apenas **Cliente/Clientes** na página; **sem** rótulos Carteira/Revenda/Conexão visíveis (validado em `clienteFormView.test.js`). |
| **Endpoints** | `GET/POST /api/clientes`, `GET/PUT /api/clientes/{id}`, `PATCH .../ativar|inativar`, `GET /api/clientes/busca?termo=`, contatos `GET/POST /api/clientes/{id}/contatos`, `PUT/PATCH /api/contatos-clientes/{id}`, `POST /api/clientes/{id}/arte-header-chats` (multipart `arte`). |
| **JS** | `js/pages/clientesPage.js`, `js/services/clienteService.js`, `js/core/clienteListView.js`, `js/core/clienteFormView.js`, `js/core/carteiraArteView.js` (validação MIME/tamanho). |
| **DTOs** | `ClienteRequestDTO` / `ClienteResponseDTO` (`carteira`, `carteiraId`, `arteHeaderChatsUrl`, etc.). |
| **Arte** | Upload no Cliente; URL pública `/uploads/clientes/header-chats/`; persistência em `clientes.arte_header_chats_url`. |
| **Representação “Cliente” hoje** | **Cliente final / empresa atendida** (telefones no cadastro), não o contratante F5 do modelo novo. |
| **Gaps modelo novo** | Falta entidade “Cliente F5”; falta “Contato WhatsApp”; telefones hoje misturam papéis. |
| **Risco alteração** | **Alto** se renomear sem migração; **baixo** para ajustes de copy e documentação. |

### 2.2 Tela Chats (`page-chats`)

| Item | Detalhe |
|------|---------|
| **Estado atual** | Lista de conversas (= tickets); painel central (header WL, timeline, composer); painel direito (cliente, contato atendimento, histórico, etiquetas, anexos). |
| **Cliente** | Nome e dados espelhados do **`TicketResponseDTO`** (`cliente`, telefones, empresa…); painel “Cliente”. |
| **Contato / solicitante** | `contatoSolicitanteNome` etc. no DTO; painel “Contato do atendimento” — origem **`ContatoCliente`** vinculado ao ticket. |
| **Conexão** | Título do header: `getChatsConexaoRevendaTitle(ticket)` usa campo **`conexao`** (fallback `carteira` / cliente). |
| **Arte header** | `resolveChatsConexaoHeaderArteUrl`: (1) `clienteArteHeaderChatsUrl` / nested, (2) `ticket.arteHeaderChatsUrl`, (3) cache por `clienteId`, (4) fallback índice **Carteira** por nome, (5) gradiente. `applyChatsConexaoHeaderArte` + CSS `--chats-conexao-header-bg-image`. |
| **Etiquetas** | Vinculadas ao **ticket**: `GET/PUT /api/tickets/{numero}/etiquetas`; UI em `chatsPage.js` + `etiquetaService.js`. |
| **Histórico resumido** | `GET /api/chats/{numeroTicket}/historico-resumido` ao selecionar conversa. |
| **Mensagens** | `GET /api/tickets/{numero}/interacoes` (+ satisfação opcional no histórico). |
| **Seleção ticket** | `chatsService.listTicketsBase()` → tickets; `getTicketDetail(numero)` ao abrir conversa. |
| **Endpoints principais** | Tickets, interações, etiquetas, anexos, histórico chats, carteiras (lista para índice arte legado). |
| **JS** | `chatsPage.js`, `chatsView.js`, `chatsService.js`, `ticketService.js`, `carteiraService.js`, `clienteService.js` (hydrate arte). |
| **Gaps** | Contato final WhatsApp não modelado; tags no Contato; título ainda “Conexão/Revenda”. |
| **Risco** | **Alto** (header + arte + título); **médio** (etiquetas). |

### 2.3 Tela Abrir ticket (`page-abrir-ticket`)

| Item | Detalhe |
|------|---------|
| **Campos usuário** | Busca e seleção de **Cliente** cadastrado; exibe resumo; canal; prioridade; mensagem; opcional **contato solicitante** (dropdown de `ContatoCliente` ativos). |
| **Carteira / Conexão** | Campo readonly **`conexao`** com label **“Carteira”** — preenchido com `cliente.carteira` (nome da entidade Carteira). Payload: `conexao: clienteSelecionado.carteira \|\| conexao`. |
| **Canal** | Select/input `canal` no formulário. |
| **Criação** | `ticketService.createTicket` → `POST /api/tickets` (webhook interno) com `TicketWebhookRequestDTO`-like body. |
| **DTOs** | Payload: `cliente`, `telefone`, `mensagem`, `canal`, `conexao`, `prioridade`, `contatoSolicitanteId` opcional. |
| **Conflitos modelo novo** | “Cliente” selecionado é o cadastro atual, não contratante; `conexao` mistura carteira; sem Contato WhatsApp explícito. |
| **Risco** | **Médio/alto** (payload e labels). |

### 2.4 Tela Tickets (`page-tickets`)

| Item | Detalhe |
|------|---------|
| **Filtros** | Texto livre, status, **Conexão** (`conexao`), **Cliente**, analista ID, datas, prioridade. |
| **Listagem** | Via busca API `tickets/busca` com query params montados em `ticketsPage.js` / `queryParams.js`. |
| **Colunas / labels** | Cliente, status, prioridade, canal, conexão/carteira (conforme renderização em tabela). |
| **Modal detalhe** | `ticketDetailsModal.js`: dados ticket, cliente, **Conexão** + **Carteira** (`detailConexao`, `detailCarteira`), encerramento, satisfação, etiquetas, ações. |
| **Ações** | Status, encerrar, reabrir, escalonar, PDF, satisfação, observação atendimento, etc. |
| **Endpoints** | `ticketService` (listagem, get, patch status, encerrar, etiquetas, satisfação, PDF…). |
| **Risco** | **Médio** (filtros e modal). |

### 2.5 Encerramento de ticket

| Item | Detalhe |
|------|---------|
| **Onde** | Modal `#closeTicketModal` / `encerrarTicketForm` em `index.html`; lógica em `ticketDetailsModal.js`; ação “Encerrar” também no Chats (`chatsView` botão principal). |
| **Obrigatório** | **Grupo** + **Subgrupo** + **Comentário de encerramento** (`EncerrarTicketRequestDTO`). |
| **Motivo** | **Não existe** catálogo/entidade Motivo — só texto em `comentarioEncerramento`. |
| **Observações** | `observacaoAtendimento` é fluxo separado (`PUT .../observacao-atendimento`), não o encerramento. |
| **Backend** | `TicketService.encerrarTicket` — valida grupo/subgrupo ativos e coerência; status `RESOLVIDO`; SLA pausa finalizada. |
| **Endpoint** | `PUT /api/tickets/{numeroTicket}/encerrar`. |
| **Risco** | **Médio** ao acrescentar Motivo obrigatório (schema + UI + validação). |

### 2.6 Etiquetas

| Item | Detalhe |
|------|---------|
| **Entidade** | `Etiqueta`; vínculo `TicketEtiqueta` (ticket_id + etiqueta_id). |
| **Cadastro** | Configurações → seção Etiquetas (`etiquetasConfigSection.js`, `etiquetaService.js`, `EtiquetaController`). |
| **Aplicação** | **No ticket** — Chats (`PUT /api/tickets/{numero}/etiquetas`); possível no fluxo de detalhe do ticket. |
| **Relatórios** | Indireto via dados do ticket; sem painel dedicado “por etiqueta” em indicadores ativos. |
| **Impacto mover para Contato** | Novo vínculo Contato↔Etiqueta; Chats e histórico precisariam carregar tags do contato da conversa; tickets antigos com tags no ticket. |
| **Risco** | **Alto** para mudança de dono; **baixo** para manter no ticket. |

### 2.7 Categoria / Subcategoria / Motivo

| Item | Detalhe |
|------|---------|
| **Entidades** | `GrupoCategoria`, `SubgrupoCategoria` (FK grupo no subgrupo). |
| **Cadastro** | Configurações (controllers `GrupoCategoriaController`, `SubgrupoCategoriaController`; `categoriaService.js`). |
| **Encerramento** | Obrigatório grupo + subgrupo (IDs no DTO). |
| **Motivo** | **Inexistente** como entidade ou campo catalogado. |
| **Risco Motivo obrigatório** | **Médio** (nova entidade ou enum + migration opcional + UI modal encerrar). |

### 2.8 Satisfação / Avaliação

| Item | Detalhe |
|------|---------|
| **Entidade** | `TicketSatisfacao` (1:1 com ticket): `nota`, `comentario`, `criadoEm`. |
| **Endpoints** | `GET/POST /api/tickets/{numeroTicket}/satisfacao`; resumos em `TicketSatisfacaoResumoController` / indicadores / relatórios. |
| **UI** | Modal detalhe ticket; Chats (timeline entrada satisfação); Relatórios/Indicadores (filtros e gráficos). |
| **Quem vê** | Perfis com acesso a tickets/relatórios/indicadores (ADMIN/SUPERVISOR conforme página). |
| **Fluxo** | **Manual** — analista registra nota/comentário; **não** há envio automático WhatsApp nem flag “enviar avaliação”. |
| **Vínculo** | Sempre ao **ticket**. |
| **Regra futura** | Exige workflow pós-RESOLVIDO + opt-in analista + canal WhatsApp + possível novo DTO/status de envio. |
| **Risco** | **Médio/alto** (produto + integração). |

### 2.9 WhatsApp / API

| Item | Detalhe |
|------|---------|
| **Endpoints** | `POST /api/integracoes/whatsapp/mensagens` (sessão analista); `POST /api/webhooks/tickets` (público webhook criação); `GET /api/tickets/ativo?clienteId&contatoSolicitanteId&telefone`. |
| **DTO entrada** | `IntegracaoWhatsappMensagemRequestDTO`: `telefone`, `nomeContato`, `mensagem`, `canal`, `clienteId`, `contatoSolicitanteId`, `origemExternaId`. |
| **Service** | `IntegracaoMensagemEntradaService` — busca ticket ativo ou cria via `criarTicketPorWebhook`. |
| **Número matriz** | **Não existe** campo dedicado. |
| **Número contato** | Um campo `telefone` na mensagem; matching em `Cliente.telefone` / `ContatoCliente` / ticket ativo. |
| **Canal** | String (`WHATSAPP` default na integração). |
| **Gap** | Mapear matriz → Cliente F5; contato → pessoa WhatsApp; separar de `ContatoCliente` interno. |
| **Risco** | **Alto** quando ligar provedor real. |

### 2.10 Dashboard (`page-dashboard`)

| Item | Detalhe |
|------|---------|
| **Cards** | Resumo operacional, gerencial (prioridades, categorias), SLA, métricas, **Painel por Conexão**, fila analistas. |
| **Agrupamento** | **Por conexão** (`ticket.conexao` normalizado em `DashboardService`); tickets críticos mostram nome do **Cliente** (entidade). |
| **Endpoints** | `/api/dashboard/resumo`, `/gerencial`, `/sla`, fila analistas, pendências conexão. |
| **DTOs** | `DashboardResumoDTO`, `DashboardGerencialDTO`, `DashboardSlaDTO`, `ConexaoPendenciasDTO`, etc. |
| **Dependência** | Tickets + campo `conexao` + entidade Cliente para contagens. |
| **Risco** | **Médio** ao trocar “conexão” por Cliente F5. |

### 2.11 Indicadores (`page-indicadores`)

| Item | Detalhe |
|------|---------|
| **Subpáginas ativas** | **Chamados** (`/api/indicadores/chamados`), **Satisfação** (resumo/evolução via services de satisfação). |
| **Placeholder** | Visão geral, **Clientes / Revendas**, Atendentes, SLA — UI “Indicador em construção”. |
| **Filtros chamados** | Data início/fim; parâmetro legado `classificacaoCliente` **ignorado** no service. |
| **Agrupamentos** | Total chamados, **totalClientes distintos** (IDs entidade `Cliente`), por atendente, top grupos/subgrupos, prioridade, status. |
| **Nomenclatura** | Menu “Clientes / Revendas” em `indicadoresSubpages.js`. |
| **Lacunas** | Indicadores por **Cliente F5** (ex-Carteira); por **Contato**; por tags. |
| **Risco** | **Médio** (novos endpoints/agregações). |

### 2.12 Relatórios (`page-relatorios`)

| Item | Detalhe |
|------|---------|
| **Filtros** | Datas, status, **cliente**, analista, grupo, subgrupo, prioridade, SLA 1º atend./resolução, escalonado; satisfação (nota, status ticket, cliente). |
| **Export** | CSV tickets (`TicketRelatorioCsvService`); PDF ticket; CSV satisfação. |
| **Campos CSV** | Inclui **Cliente**, **Conexão/Carteira** (concatena `conexao` + `carteira` do DTO), grupo, subgrupo, SLA, escalonamento… |
| **Lacunas** | Contato WhatsApp, tags, motivo, flag envio avaliação. |
| **Risco** | **Médio**. |

### 2.13 Permissões / usuários

| Item | Detalhe |
|------|---------|
| **Perfis** | `PerfilAcesso`: **ADMIN**, **SUPERVISOR**, **ANALISTA** (`entity/PerfilAcesso.java`). |
| **Login** | `POST /api/analistas/login` — apenas **Analista**; sessão `X-Analista-Id` + `X-Analista-Token`. |
| **Perfil CLIENTE** | **Não existe**. |
| **Páginas** | `permissions.js`: ex. Clientes só **ADMIN**; Chats/Tickets ANALISTA+; Indicadores/Relatórios ADMIN/SUPERVISOR; Config ADMIN/SUPERVISOR (etiquetas: ADMIN+SUPERVISOR). |
| **Futuro login Cliente** | Novo perfil, rotas, JWT, escopo de dados por contratante. |
| **Risco** | **Alto** para portal Cliente; **baixo** para ajustar permissões internas. |

---

## 3. Mapa backend

### 3.1 Entidades principais (29 em `entity/`)

| Entidade | Função atual | Campos/relações chave | Conflito modelo novo | Futuro sugerido |
|----------|--------------|------------------------|----------------------|-----------------|
| **Cliente** | Cadastro “cliente” do suporte | `nome`, telefones, email, endereco, `carteira_id` → Carteira, `arteHeaderChatsUrl`, `classificacaoCliente`, `ativo` | Nome “Cliente” ≠ contratante F5 | **Adaptar** ou renomear com migração |
| **Carteira** | Agrupamento conexão/revenda | `nome`, `arteHeaderChatsUrl` (legado) | Equivale ao **Cliente F5** do produto | **Preservar** dados; **substituir** papel aos poucos |
| **ContatoCliente** | Contato da empresa (cargo, tel) | FK `cliente_id` | ≠ contato WhatsApp final | **Adaptar** ou nova entidade Contato |
| **Ticket** | Atendimento | FK `cliente_id`, `contato_solicitante_id`, `conexao` string, `canal`, SLA, grupo/subgrupo, encerramento | `cliente_id` hoje ≠ Contato | **Preservar** linhas; evoluir FKs |
| **TicketInteracao** | Timeline | FK ticket | OK | **Preservar** |
| **TicketEtiqueta** | Tags no ticket | N:N ticket-etiqueta | Tags no Contato no produto novo | **Adaptar** se mudar dono |
| **Etiqueta** | Catálogo de tags | nome, cor, ativo | OK | **Preservar** |
| **GrupoCategoria** / **SubgrupoCategoria** | Encerramento | hierarquia grupo→subgrupo | OK no ticket | **Preservar** |
| **TicketSatisfacao** | Avaliação | 1:1 ticket | Envio WhatsApp futuro | **Estender** |
| **Analista** | Usuário interno | login, perfil | Sem portal cliente | **Preservar** |
| **Demais** | SLA, feriados, anexos, auditoria, notificações… | — | Fora do núcleo desta reestruturação | **Preservar** |

### 3.2 DTOs (amostra relevante ~71 arquivos)
- **Cliente:** `ClienteRequestDTO`, `ClienteResponseDTO` (+ `arteHeaderChatsUrl`, `carteira`/`carteiraId`).
- **Carteira:** `CarteiraRequestDTO`, `CarteiraResponseDTO`.
- **Ticket:** `TicketResponseDTO` (+ `clienteId`, `clienteArteHeaderChatsUrl`, `carteira`, `conexao`, dados espelhados do cliente).
- **Contato:** `ContatoClienteRequestDTO`, `ContatoClienteResponseDTO`.
- **Encerramento:** `EncerrarTicketRequestDTO` (grupoId, subgrupoId, comentario).
- **Webhook/WhatsApp:** `TicketWebhookRequestDTO`, `IntegracaoWhatsappMensagemRequestDTO`, `IntegracaoMensagemEntradaResponseDTO`.
- **Busca:** `TicketFiltroDTO` (cliente, conexao, …).
- **Indicadores:** `IndicadoresChamadosDTO`, `IndicadorContagemDTO`.
- **Dashboard:** `DashboardResumoDTO`, `DashboardGerencialDTO`, `DashboardSlaDTO`, …
- **Satisfação:** `TicketSatisfacaoRequestDTO`, `TicketSatisfacaoResponseDTO`, `TicketSatisfacaoResumoDTO`, filtros.

### 3.3 Controllers (24)
`ClienteController`, `ClienteContatoController`, `ContatoClienteController`, `CarteiraController`, `TicketController`, `TicketInteracaoController`, `TicketAnexoController`, `ChatsController`, `IntegracaoWhatsappController`, `WebhookController`, `EtiquetaController`, `GrupoCategoriaController`, `SubgrupoCategoriaController`, `TicketSatisfacaoController`, `TicketSatisfacaoResumoController`, `DashboardController`, `IndicadoresController`, `AnalistaController`, `AuditoriaController`, `SlaController`, `SlaMetaController`, `HorarioUtilController`, `FeriadoController`, `NotificacaoController`.

### 3.4 Services (44)
Núcleo negócio: `TicketService`, `ClienteService`, `CarteiraService`, `ContatoClienteService`, `TicketBuscaService`, `TicketEtiquetaService`, `TicketAtivoService`, `IntegracaoMensagemEntradaService`, `IndicadoresChamadosService`, `DashboardService`, `DashboardSlaService`, `ChatsHistoricoResumoService`, `TicketSatisfacaoService`, `TicketSatisfacaoResumoService`, `TicketSatisfacaoEvolucaoService`, + SLA/anexos/auditoria.

### 3.5 Repositories (17)
Um repositório JPA por agregado principal (`ClienteRepository`, `CarteiraRepository`, `TicketRepository`, `TicketEtiquetaRepository`, …).

### 3.6 Testes
- **Backend:** 28 testes em `src/test/java` (SLA, satisfação, integração WhatsApp, etiquetas, indicadores, carteira/arte, ticket ativo, …).
- **Frontend:** 14 suites Vitest em `static/js/tests` (chatsView, clienteFormView, clienteListView, permissions, satisfacaoView, …).

---

## 4. Relacionamentos atuais

```
Carteira (nome) ←── optional ── Cliente.carteira_id
       ↑
       └── espelhado em Ticket.conexao (string) e TicketResponseDTO.carteira / .conexao

Ticket ──required──► Cliente (FK cliente_id)
Ticket ──optional──► ContatoCliente (contato_solicitante_id)
Ticket ──N:N──► Etiqueta (via TicketEtiqueta)
Ticket ──0..1──► TicketSatisfacao
Ticket ──optional──► GrupoCategoria, SubgrupoCategoria (encerramento)
```

| Relação | Situação hoje |
|---------|----------------|
| **Ticket → Cliente** | FK obrigatória; DTO denormaliza telefones/email. |
| **Cliente → Carteira** | FK opcional `carteira_id`. |
| **Ticket → Conexão** | Campo string `conexao` (não FK); frequentemente = nome da Carteira. |
| **Ticket → Etiqueta** | Vínculo ativo no ticket. |
| **Ticket → Satisfação** | Uma avaliação por ticket (opcional). |
| **Ticket → Categoria** | Grupo/subgrupo preenchidos no encerramento. |
| **WhatsApp** | Telefone na mensagem; canal; criação/busca ticket ativo; sem matriz. |

---

## 5. Modelo alvo resumido (produto)

| Conceito alvo | Significado |
|---------------|-------------|
| **Cliente** | Contratante / conexão / carteira da F5 |
| **WhatsApp matriz** | Número do Cliente na API |
| **Contato** | Cliente final atendido no WhatsApp |
| **WhatsApp do Contato** | Número da pessoa |
| **Ticket** | Pertence a Cliente + Contato |
| **Tags** | Pertencem ao Contato |
| **Categoria / Subcategoria / Motivo** | Encerramento do ticket (Motivo obrigatório) |
| **Avaliação** | Enviada ao Contato após RESOLVIDO se analista optar |
| **Interação indevida** | Fora do escopo técnico desta coleta (regra de negócio) |
| **Login Cliente** | Acesso consultivo do contratante |

---

## 6. Gaps de implementação

### Baixo risco
- Documentação e glossário; mensagens UI; testes de contrato DTO adicionais.
- Expor campos novos como opcionais sem remover legado.
- Completar subpáginas de Indicadores placeholder (sem mudar modelo).

### Médio risco
- Renomear labels Conexão/Carteira → Cliente F5 em Abrir ticket, Tickets, Relatórios, Dashboard.
- Chats: título do header e remoção gradual do fallback Carteira.
- Encerramento: catálogo **Motivo** + validação.
- Satisfação: flag “enviar avaliação” + estado de envio (sem provedor).
- Relatórios/CSV: novas colunas (Contato, Motivo).

### Alto risco
- Redefinição semântica **Cliente** vs **Contato** com migração de dados históricos.
- Mover **etiquetas** de Ticket para Contato.
- WhatsApp produção (matriz, webhooks, identidade do contato).
- Portal **login Cliente** + autorização por contratante.
- Remover `conexao` string / deprecar `Carteira` antes de estabilizar novo modelo.

---

## 7. Sprints sugeridas (títulos e função — sem prompts)

| # | Título | Função |
|---|--------|--------|
| 1 | Glossário e decisões de migração | Alinhar termos, equivalência Carteira↔Cliente F5, política de dados legados |
| 2 | Modelo contratante (backend) | Entidade/API do Cliente F5 sem quebrar Carteira |
| 3 | Modelo Contato WhatsApp (backend) | Entidade, telefone, vínculo ticket |
| 4 | WhatsApp matriz + integração | Campos no Cliente F5; evolução DTO integração |
| 5 | Migração de dados fase 1 | Scripts/consolidação; telefones; arte |
| 6 | UI Configurações e Abrir ticket | Nomenclatura e fluxos alinhados ao contratante |
| 7 | UI Clientes e Contatos | Separar cadastros; arte no contratante |
| 8 | Chats — vínculo e header | Cliente F5 + Contato + arte |
| 9 | Encerramento — Motivo | Catálogo + obrigatoriedade |
| 10 | Etiquetas no Contato | Novo vínculo + Chats + histórico |
| 11 | Avaliação pós-RESOLVIDO | Opt-in analista + envio WhatsApp |
| 12 | Dashboard e Indicadores por Cliente F5 | Agregações e telas |
| 13 | Relatórios e exportações | Colunas e filtros novos |
| 14 | Login Cliente (portal) | Perfil, escopo, APIs read-only |
| 15 | Depreciação legado | `conexao`, arte Carteira, fallback Chats |

---

## 8. Perguntas pendentes (decisão humana)

1. **Migração:** a entidade atual `Cliente` passa a ser **Contato** e `Carteira` vira **Cliente F5**, ou mantém-se `Cliente` com novo significado e cria-se só Contato novo?
2. **ContatoCliente existente:** vira contato interno, é absorvido no Contato WhatsApp, ou coexistem dois tipos?
3. **Ticket.conexao:** mantém-se para sempre como cache do nome do contratante ou remove-se após migração?
4. **Etiquetas históricas** em tickets: migram para o contato da conversa, duplicam, ou permanecem no ticket?
5. **Arte:** só no Cliente F5 (cadastro atual Clientes) ou também exibir por contratante renomeado?
6. **Avaliação:** nota coletada só via WhatsApp do contato ou também manual no sistema?
7. **WhatsApp matriz:** um número por Cliente F5 ou múltiplas instâncias/canais?
8. **Indicadores “Clientes / Revendas”:** renomear para “Clientes (contratantes)” e qual métrica mínima na primeira entrega?
9. **Login Cliente:** quais telas exatamente (só indicadores/relatórios ou também tickets)?
10. **Interação indevida:** critérios exatos e impacto em SLA/ticket — fora do código atual?

---

## 9. Confirmação da sprint

- **Nenhum arquivo de aplicação** (Java, HTML, CSS, JS) foi alterado nesta Sprint 184.
- **Nenhum dado** foi alterado.
- **Apenas** o arquivo `docs/COLETA_MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md` foi criado.
- Build/testes da aplicação **não foram exigidos** para esta sprint documental.

---

*Fim da coleta Sprint 184.*
