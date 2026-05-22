# Chats — diretrizes de evolução

## Fechamento da fase frontend (Sprint 170 — maio/2026)

**Status:** Base visual **aprovada** para evoluções futuras (backend, integração WhatsApp/API, envio em tempo real).

| Bloco | Sprints | Situação |
|--------|---------|----------|
| Lista, abas, cards, temporizador | 153–156 | OK |
| Status/canal/SLA visual (sem alterar regra) | 151–152 | OK |
| Header Conexão/Revenda + white label shell | 160–161, 168 | OK |
| Timeline eventos | 162 | OK |
| Balões in/out | 163 | OK |
| Composer visual | 164 | OK |
| Painel direito semântico + histórico estados | 157–159 | OK |
| Drawer responsivo ≤1100px | 165–166 | OK |
| Loading ao trocar conversa | 167 | OK |
| Tema claro/escuro (menu real) | 169 | OK |

**Smoke final (Sprint 170):** HTTP 200, login ADMIN, abas Atendendo/Fila/Encerrados, sem avatar na lista, Fila com “Aguardando atendimento”, troca de conversa com *Carregando conversa…*, seções do painel e composer presentes, alternância **Tema Claro/Escuro** pelo menu do usuário (`topbar.js`), drawer em viewport ≤1100px (Contexto, backdrop, fechar). Usar **Ctrl+F5** após deploy de CSS.

**Pendências futuras (fora desta fase):** integração WhatsApp/API, envio de mensagem pelo composer, WebSocket, white label com imagens reais (`--chats-conexao-header-bg-image`, logo), indicadores por etiqueta, pré-visualização de anexos.

**Não alterar sem nova sprint:** contratos de API, regras de ticket/SLA/status, schema.

## Papel da página

- **Chats** concentra atendimento conversacional (layout 3 colunas), usando tickets existentes até integração WhatsApp/API.
- **Fila**, **Chats** (em andamento) e **Histórico** (resolvido/cancelado) compartilham a mesma base de tickets.

## Status visual da conversa e canal (Sprint 151)

- O **status do ticket** no backend (`ABERTO`, `EM_ATENDIMENTO`, etc.) **não foi alterado** nesta sprint.
- No Chats, a UI exibe um **rótulo operacional** mais claro (lista lateral e cabeçalho central):
  - `ABERTO` → **Aguardando atendimento**
  - `EM_ATENDIMENTO` → **Em atendimento**
  - `AGUARDANDO_CLIENTE` → **Aguardando cliente**
  - `RESOLVIDO` → **Encerrado**
  - `CANCELADO` → **Cancelado**
- A badge genérica **“Ativo”** foi removida da listagem; prioridade do ticket continua opcional no cabeçalho quando existir.
- **Canal da conversa:** reutiliza o campo **`canal`** já existente no ticket/DTO. Normalização visual em `chatsView.js`:
  - valores com “whatsapp” → **WhatsApp**
  - “api”, “webhook”, “integracao” → **API**
  - vazio ou demais → **Sistema** (fallback seguro para tickets legados)
- Exibição discreta na **lista** (ao lado do protocolo) e no **cabeçalho** (protocolo · pill de canal).
- **WhatsApp real**, WebSocket e provedor externo **ainda não integrados** — apenas preparação visual e de leitura.

## Tempo de espera e SLA visual (Sprint 152)

- **SLA oficial** continua calculado no backend; o Chats **não recalcula** prazos nem altera metas.
- Campos reutilizados do ticket/DTO: `slaPrimeiroAtendimentoStatus`, `slaPrimeiroAtendimentoVencimento`, `slaResolucaoStatus`, `slaResolucaoVencimento`, `slaPausado`, `dataAbertura`, `dataPrimeiroAtendimento`, `dataEncerramento`.
- **Tempo de espera** (ex.: “há 5 min”, “há 2h”, “há 1d”):
  - lista: referência por `dataAbertura` (fila), `dataPrimeiroAtendimento` ou abertura (em andamento), encerramento no histórico;
  - cabeçalho: mesma regra, preferindo **última interação** carregada (`criadoEm` máximo) quando disponível após abrir a conversa.
- **Destaque visual** na lista (borda/espera): vencido, próximo do vencimento, pausado, dentro do prazo ou neutro; sem SLA calculado → “SLA —” discreto.
- **Cabeçalho:** bloco com espera, vencimentos formatados (quando existem) e badge de status SLA já persistido.
- **Timeline** permanece focada em eventos/mensagens (sem repetir SLA).

## Painel direito — histórico resumido (Sprint 158 / 159)

- Carregado **somente ao selecionar** uma conversa: `GET /api/chats/{numeroTicket}/historico-resumido` (sessão obrigatória).
- Resposta leve: `totalTicketsCliente`, `ultimoTicketEncerrado`, até **3** `ticketsRecentes`; o ticket atual **nunca** entra nas listas.
- UI compacta: total, bloco “Último encerrado” (se houver), lista “Recentes” (protocolo, status, data, grupo quando existir).
- **Estados de UI (Sprint 159):**
  - carregando → *“Carregando histórico…”* (ao trocar conversa, bloco limpo antes do fetch);
  - vazio (sucesso sem outros chamados) → *“Sem histórico anterior localizado.”*;
  - erro de rede/sessão → *“Não foi possível carregar o histórico.”* (não confundir com vazio);
  - sucesso com dados → total / último encerrado / recentes.
- Troca rápida de conversa: sequência `historicoLoadSeq` evita exibir histórico da conversa anterior.
- Limite de consulta no backend (page size 3/1); sem carga na abertura da página Chats.

## Tema claro/escuro — smoke e ajuste fino (Sprint 169)

- Revisão pontual em `chats.css` (sem backend/JS de negócio): token `--chats-muted-readable` no escuro.
- **Lista/abas:** card ativo e hover distinguíveis no dark; abas ativas com fundo/contagem legíveis; busca com borda/surface no dark.
- **Header:** mantidos overrides Sprint 168; protocolo e pills operacionais no dark.
- **Timeline/balões/composer:** reforço de placeholders, disabled e contador no dark (composer focus já existente).
- **Painel:** blocos, subtítulos, histórico loading/empty/error (`#fca5a5` no erro), etiquetas selecionadas sem azul “claro” estourado.
- **Drawer (≤1100px):** overlay `rgba(0,0,0,0.65)`, sombra e borda do painel no dark.
- **Claro:** textos secundários (protocolo na lista, placeholders) com `color-mix` para evitar cinza fraco demais.
- Validar sempre pelo **menu do usuário → Tema Claro / Tema Escuro** (`toggleTheme` em `topbar.js`).

## Header central — alinhamento com busca lateral (Sprint 173)

- **Somente CSS** no header central (`--chats-header-align-rail: 116px`): altura fixa do card brand-only para a **borda inferior** coincidir com a base do bloco **abas + campo de busca** da coluna esquerda (sidebar **não** alterada).
- Conteúdo do header com `height: 100%` e `align-items: center` (Conexão, canal, protocolo/abertura inalterados).
- Faixa: mín. implícito **96px**, alvo **116px**, máx. **120px** (Sprint 172 evoluída pelo alinhamento).

## Header central — altura banner White Label (Sprint 172)

- Ajuste **somente CSS** na faixa do header brand-only para arte horizontal futura (ver Sprint 173 para altura final alinhada à lista).

## Header central — identidade White Label (Sprint 171)

- O card superior é **somente marca/Conexão/Revenda** (classe `chats-conexao-header--brand-only`).
- **Removido do header:** status, prioridade, SLA, espera, 1º atendimento, resolução, violação (não renderizados em `renderConversationHeader`).
- **Mantido:** título (`conexao`), logo slot, fundo/overlay WL, canal discreto na marca, protocolo e abertura em linha secundária (`getChatsHeaderMetaProtocolAberturaHtml`).
- Helpers de SLA/status no `chatsView.js` permanecem para lista, painel e outras telas.
- **Smoke Sprint 183:** validação Cliente → upload → ticket DTO → header; relatório em `docs/SPRINT_183_SMOKE_RELATORIO.md`. Após deploy, **Ctrl+F5** no Chats se a arte não atualizar.
- **Sprint 181 (origem da arte):** prioridade no helper `resolveChatsConexaoHeaderArteUrl`: (1) arte do **Cliente** (`ticket.cliente.arteHeaderChatsUrl` ou `ticket.clienteArteHeaderChatsUrl` no DTO do ticket); (2) `ticket.arteHeaderChatsUrl` direto; (3) cache opcional por `clienteId` via `GET /api/clientes/{id}` se o ticket ainda não trouxer URL; (4) fallback temporário pela **Carteira** por nome (`ticket.conexao` / `ticket.carteira`); (5) gradiente. Upload e cadastro da arte: tela **Clientes** (Sprint 180). Carteira/Conexões mantém dados legados, sem edição visual da arte na Configuração.
- **Sprint 175 (legado):** arte na Carteira por nome — usada só se o Cliente não tiver arte. URLs públicas: `/uploads/clientes/header-chats/` (principal) e `/uploads/conexoes/header-chats/` (compatibilidade). Sem arte: gradiente (`chats-conexao-header--bg-fallback`). Com arte: `--chats-conexao-header-bg-image` + `chats-conexao-header--has-custom-bg`, `background-size: cover`, `background-position: center 42%`; overlay e text-shadow inalterados.

## Header central — arte White Label (Sprint 175 / 176 / 177)

- **Sprint 176 (ajuste fino):** com arte, `background-position: center 42%` (banner 5:1); overlay diagonal mais leve que o fallback; `text-shadow` discreto em título, meta e canal para legibilidade sem escurecer demais a imagem. Fallback com gradiente **inalterado** quando não há `--has-custom-bg`.

### Validação real (Sprint 177 — 2026-05-21)

- **Arte de teste:** PNG gerado **1200×240** (proporção 5:1), upload em **Carteira id 10 — Fênix** (`/uploads/conexoes/header-chats/carteira-10-….png`), HTTP **200** no arquivo estático.
- **Conexão usada:** `ticket.conexao` = **Fênix** (ex.: **TK-000074**); vínculo por nome normalizado (sem alterar regra).
- **Resultado:** resolução da URL e entrega da imagem **OK**; CSS Sprint 176 mantido (**sem novo ajuste de código** após smoke técnico).
- **Fallback:** conversas cuja conexão não tem `arteHeaderChatsUrl` ou nome divergente da Carteira continuam com gradiente (`--bg-fallback`).
- **Risco de dado:** arte só aparece se `ticket.conexao` (ou `ticket.carteira`) coincidir com `Carteira.nome` (ex.: acento e grafia idênticos). Recarregar Chats (Ctrl+F5) após novo upload na Configuração.

- Dado: `GET /api/carteiras` ao abrir Chats (índice em memória; falha silenciosa → só fallback).
- URL aceita: apenas `/uploads/conexoes/header-chats/{arquivo}` (sanitização no frontend).
- Helpers: `resolveChatsConexaoHeaderArteUrl`, `applyChatsConexaoHeaderArte` em `chatsView.js`; chamada em `renderConversationHeader` / loading / limpar conversa.
- Não altera cadastro/upload, timeline, composer nem painel direito.

## Header central — altura banner White Label (Sprint 172)

- Ajuste **somente CSS** em `chats.css` no header central (`chats-conexao-header-content`), sem backend nem mudança de markup.
- Objetivo: leitura de **banner horizontal** moderado, melhor área para arte futura, sem reduzir demais a timeline.
- Modo atual (`chats-conexao-header--brand-only`): `min-height` **72px**, `max-height` **96px**, padding **11px 14px**; conteúdo genérico do header: padding **10px 14px**, `min-height` **56px**, `max-height` **88px**.
- Logo no header: slot **40×40px** (antes 36px); título, canal, protocolo e abertura permanecem com a mesma hierarquia da Sprint 171.
- Fundo/overlay WL, fallback e classes `--has-custom-bg` / `--wl-ready` inalterados em comportamento.
- Timeline, composer, painel direito, lista e abas **não** foram alterados nesta sprint.

## Header central — densidade operacional (Sprint 168, substituída no header por 171)

- Ajuste **somente visual** no header de conexão: Conexão/Revenda permanece dominante (título 16px/700, logo 36px no header).
- Protocolo e abertura na primeira linha secundária; **status**, **prioridade** e **SLA** agrupados em `.chats-header-ops` na mesma faixa (menos altura que SLA em bloco próprio).
- Pills operacionais menores (10px/500), fundos mais suaves; SLA em 9px com badge discreto — sem alterar textos, cálculos ou endpoints.
- Padding do header: 8px 12px; `max-height` 76px. Canal Sistema e pills WhatsApp/API inalterados em regra (Sprint 160).
- Tema escuro: overrides locais em `.chats-conexao-header` para contraste legível.

## Header central — Conexão/Revenda (Sprint 160 / 161 white label)

- O card superior da área central é **identidade da conexão**, não resumo do cliente (nome do cliente fica no painel direito).
- Título principal: campo **`conexao`** do ticket; fallbacks seguros: `empresa`/`carteira` (mesma regra da lista), depois *“Conexão não informada”*.
- **Canal** ao lado da marca: Sistema = rótulo discreto; WhatsApp/API = pill compacta (`chats-canal-pill--header`).
- Metadados secundários: **protocolo** e **abertura** (sem repetir nome do cliente).
- **Sprint 161 (casca white label):** camadas `chats-conexao-header-bg` + `chats-conexao-header-overlay` + `chats-conexao-header-content`; gradiente fallback premium; overlay para legibilidade com `--chats-conexao-header-bg-image` futura; classe futura `chats-conexao-header--has-custom-bg` no header.
- Logo: `chats-conexao-logo-slot--empty` (marca vazia elegante); futuro `chats-conexao-logo-slot--has-image` + `--chats-conexao-logo-image` no slot.
- Variáveis em `#page-chats` ajustadas para **tema claro e escuro** (overlay e gradientes).
- SLA/status/prioridade permanecem no header; timeline, composer e painel direito inalterados.

## Carregamento global da conversa (Sprint 167)

- Ao selecionar ou trocar conversa, a área central (`.chats-main`) recebe `is-loading-conversa` e `aria-busy="true"`.
- Timeline é limpa imediatamente; placeholder `CHATS_CONVERSA_LOADING_MSG` (*Carregando conversa…*) no `#chatsTimelineEmpty`.
- Header temporário: título `CHATS_CONVERSA_LOADING_TITLE`, protocolo via `getChatsHeaderLoadingProtocolText`, SLA/prioridade/canal neutros.
- Anexos da conversa anterior não permanecem visíveis (`ticketAnexos` zerado até o fetch).
- Após o `Promise.all` do detalhe, estado de loading é removido e header/timeline/painel renderizam com dados finais.
- Troca rápida: mesmo `historicoLoadSeq` invalida renders atrasados (central e histórico resumido).
- Erro no fetch: remove loading, mensagem *Não foi possível carregar a conversa.* na timeline; histórico do painel em estado `error` (Sprint 159 inalterada).
- Helpers em `chatsView.js`; lógica em `chatsPage.js`.

## Responsividade — painel direito (Sprint 165 / smoke Sprint 166)

- **≥ 1101px:** layout de **3 colunas** inalterado (lista | central | painel fixo na grade).
- **≤ 1100px:** o painel **não** usa `display: none`; sai da grade e vira **drawer** fixo à direita (`transform` + classe `is-panel-open` em `#page-chats`).
- Acesso: botão **Contexto** no cabeçalho central (`#chatsContextoBtn`); fechar por **×** no topo do painel, clique no **backdrop** ou tecla **Escape**.
- Scroll interno do painel preservado (`overflow-y: auto`); lista e conversa central permanecem em 2 colunas (`220px 1fr`).
- Overlay e sombras usam `color-mix` com tokens do tema (**claro/escuro**).
- Regra pura `isChatsPanelDrawerViewport` em `chatsView.js` (testável); lógica de abrir/fechar em `chatsPage.js` (sem `app.js`).

**Smoke Sprint 166 (2026-05-21):** app via `java -jar target/suporte-tickets-1.0.0.jar` (MySQL 3306 ativo), HTTP 200, login ADMIN, Chats Fila. Em 1000px: grade 2 colunas, botão Contexto visível, drawer abre/fecha (×, backdrop, Escape), `body` sem lock após fechar, seções do painel e composer presentes, `overflow-y: auto` no painel. Em 1280px: 3 colunas, drawer fechado, Contexto oculto. Tema escuro: surface do painel legível. Falha operacional anterior no Cursor: `mvn spring-boot:run` com `ClassNotFoundException` — preferir JAR após `mvn package`.

## Composer de atendimento (Sprint 164)

- Refino **somente visual** do rodapé central: campo, contador `0 / 4000`, anexo e Enviar.
- Estrutura: `chats-composer-main` + `chats-composer-field` (textarea + `chats-composer-count` no canto inferior direito).
- Foco com borda primary suave; estado `is-disabled` no composer quando sem ticket ou histórico somente leitura.
- **Envio e validação** inalterados (botão continua desabilitado / alerta de integração futura).
- Contador atualizado apenas no `input` (`formatChatsComposerCharCount`); `maxlength="4000"` no HTML.

## Balões de mensagem (Sprint 163)

- Refino **somente visual** de `chats-msg-in` (cliente, esquerda) e `chats-msg-out` (agente, direita): mesma autoria (`isInteracaoFromCliente`), ordem e textos.
- Corpo da mensagem primeiro; metadados abaixo (`chats-msg-time` + `chats-msg-type` discreto).
- `width: fit-content` + `max-width` ~26rem; `overflow-wrap` para textos longos; cantos assimétricos leves (cauda visual).
- Agente mantém tom azul/info; cliente em surface neutra; tema escuro com overrides locais.

## Timeline central — eventos (Sprint 162)

- Refino **somente visual** dos eventos de sistema (“Chamado aberto”, “Atendimento iniciado”, encerramento, etc.): mesma ordem, mesmos textos e mesma origem (`buildChatsTimelineEntries`).
- Estrutura: `chats-timeline-event` + marcador na linha (`chats-timeline-event-marker`) + corpo (`chats-timeline-event-body`) com título, descrição opcional e horário.
- Linha vertical mais fina e discreta; espaçamento uniforme; balões de mensagem inalterados.
- Separadores de data com pill leve alinhado ao fundo da timeline.
- Tokens de surface/border em tema claro e escuro.

## Checklist — smoke integrado fase Chats (Sprints 154–159)

Validar com **ADMIN** no navegador (login real, Ctrl+F5 se cache antigo):

1. **Abas:** Atendendo, Fila, Encerrados — rótulo + contador abaixo; aba ativa com linha inferior; filtros e contagens coerentes.
2. **Lista:** cards sem avatar; nome + protocolo; temporizador compacto à direita; Fila com “Aguardando atendimento”; Atendendo só tempo; Encerrados tempo estático.
3. **Central:** cabeçalho alinhado; protocolo; canal Sistema sem pill; WhatsApp/API com pill; SLA legível; timeline com scroll; composer discreto.
4. **Painel:** Cliente sem duplicidade (Nome/Empresa/Carteira/telefone); Contato sem analista interno; Chamado atual; Histórico nos 4 estados; etiquetas; observações; anexos; scroll do painel.
5. **Histórico:** trocar conversa → loading → dados/vazio/erro; ticket atual fora da lista; rede `historico-resumido` 200 com sessão.
6. **Tema:** claro/escuro pelo menu do sistema; contraste nas 3 colunas (ou 2 colunas + drawer do painel ≤1100px).
7. **Responsivo (≤1100px):** botão Contexto abre/fecha painel; Cliente, Chamado, Histórico, etiquetas, observações e anexos acessíveis.
8. **Console:** sem erro crítico; app `HTTP 200` na raiz.

## Painel direito — deduplicação semântica (Sprint 157)

- **Cliente:** oculta Empresa se igual ao Nome; Carteira se igual a Nome ou Empresa; Tel. contato se igual ao Telefone (comparação por dígitos).
- **Contato do atendimento:** apenas solicitante/telefone/e-mail do contato; sem analista interno; oculta linhas iguais aos dados do cliente.
- **Chamado atual:** protocolo, canal, abertura, prioridade; encerramento só na aba Encerrados; sem status/SLA duplicando o cabeçalho.

## Painel direito — contexto do cliente (Sprint 156)

- Seções: **Cliente**, **Contato do atendimento**, **Chamado atual**, **Histórico resumido**, **Atendimento** (etiquetas, observações, anexos).
- Dados do `TicketResponseDTO` já carregado no detalhe do chat; linhas vazias (`—`, `null`, etc.) não são exibidas.
- Chamado atual: texto discreto (status/canal sem pills pesadas); SLA/espera em uma linha resumida, sem recálculo.
- Nome do cliente não repete no topo do painel (fica na seção Cliente e no cabeçalho central).

## Acabamento da tela (Sprint 155)

- Três colunas com mesma base de superfície (`--surface`); timeline e blocos do painel em `--surface-alt`.
- Cabeçalho da conversa: título mais leve (15px/600), meta em coluna (protocolo → badges → SLA), SLA com tipografia menor; canal **Sistema** não exibe pill no protocolo (só protocolo).
- Painel direito: blocos com borda leve e fundo alternado; intro alinhada à hierarquia da lista.
- Rolagem interna: lista, timeline e painel (scrollbars finas já definidas).

## Abas da lista (Sprint 156 — padronização)

- Três abas com a mesma estrutura: **rótulo** (`chats-tab-label`) centralizado e **contador** (`chats-tab-count`) logo abaixo, centralizado.
- Aba ativa: cor primária + linha inferior (`box-shadow` inset); contador na mesma hierarquia, sem badge lateral.
- Filtros e contagens (`data-chats-tab`, `countTicketsByTab`) inalterados.

## Lista esquerda — cards minimalistas (Sprint 153 / 154)

- Abas: **Atendendo** (em andamento), **Fila**, **Encerrados** (lógica de filtro inalterada).
- **Sprint 154:** sem avatar/foto na lista; card com **nome** + **nº do ticket** à esquerda; bloco temporal à direita (largura estável).
- Tempo na lista: formato compacto (`9h 12min`, `12min 35s`), sem “há” e sem TME; atualização a cada 1s em Fila e Atendendo.
- **Fila:** rótulo “Aguardando atendimento” + contador progressivo.
- **Atendendo:** apenas contador (sem rótulo de status).
- **Encerrados:** tempo estático discreto ou vazio; sem contador live.
- SLA/canal/conexão/pills/prévia/data **não** aparecem no card da lista (cabeçalho e painel mantêm SLA).

## Observação interna de atendimento (Sprint 135+ / 147)

- Campo **`observacaoAtendimento`** no ticket (texto opcional, até **2000** caracteres).
- Persistência: `PUT /api/tickets/{numero}/observacao-atendimento` com corpo `{ "observacao": "texto" }` (sessão obrigatória).
- **Texto vazio** após trim **limpa** a observação (`null` no banco).
- A observação é **interna ao atendimento** — aparece no painel direito do Chats e no detalhe do ticket quando exposto no DTO.
- **Não** é mensagem do cliente, **não** cria interação na timeline e **não** substitui o **comentário de encerramento**.
- **Não** altera status, SLA nem regras de encerramento/reabertura.
- Na UI Chats: textarea + **Salvar observação**; ao trocar de conversa e voltar, o valor vem do `GET` de detalhe do ticket.
- Aba **Histórico** (ticket resolvido/cancelado): campo **somente leitura** (exibe texto salvo, se houver).
- Auditoria: `TICKET_OBSERVACAO_ATENDIMENTO` (falha de auditoria não interrompe o save).

## Etiquetas (Sprint 136+ / gestão Sprint 148)

- Etiquetas são **flexíveis** e cadastradas em **Configurações → Etiquetas** (ADMIN e SUPERVISOR).
- No Chats, apenas etiquetas **ativas** aparecem para seleção em ticket em andamento; inativas não entram como opção nova (vínculos legados podem permanecer visíveis como somente leitura).
- Vínculo ticket ↔ etiqueta: `GET/PUT /api/tickets/{numero}/etiquetas` (inalterado nesta sprint).
- Indicadores por etiqueta: sprint futura.

## Anexos e mídias (Sprint 138+)

- Metadados em **`TicketAnexo`** (`GET/POST /api/tickets/{numero}/anexos`, `GET .../anexos/{id}/download`).
- **Armazenamento físico:** pasta local `uploads/tickets/{numero-sanitizado}/` (relativa ao diretório de execução da aplicação), mesmo padrão de `uploads/analistas/`.
- O JSON **não expõe** caminho absoluto nem `identificadorArquivo` interno; apenas nome, tipo, tamanho, origem e flag `downloadDisponivel`.
- **Origens:** `MANUAL` (UI), `WHATSAPP` e `SISTEMA` (reservadas para integrações futuras).
- Limite atual de upload: **10 MB** por arquivo (multipart).

## Ticket ativo por cliente/telefone (Sprint 139+)

- **Ativos:** `ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE`.
- **Encerrados (não reutilizados):** `RESOLVIDO`, `CANCELADO`.
- Consulta técnica: `GET /api/tickets/ativo?telefone=&clienteId=&contatoSolicitanteId=` (sessão obrigatória).
  - **200** com o ticket ativo mais recente; **204** se não houver; **400** sem parâmetros.
- Prioridade da busca: `contatoSolicitanteId` → `clienteId` → `telefone` (normalizado só dígitos, alinhado ao cadastro de clientes).
- **Webhook atual não foi alterado** nesta sprint.

### Fluxo WhatsApp/API (preparatório — Sprint 140)

Endpoint **protegido por sessão** (não é contrato público final do provedor):

`POST /api/integracoes/whatsapp/mensagens`

Payload exemplo: `telefone`, `nomeContato`, `mensagem`, `canal`, `clienteId`, `contatoSolicitanteId`, `origemExternaId` (opcional).

Fluxo:

1. Normaliza telefone (somente dígitos).
2. Resolve **Contato** WhatsApp do cliente (sem engolir exceção silenciosa).
3. `TicketAtivoService` busca ticket **ativo** por **Cliente + Contato** quando o contato está resolvido (`ABERTO`, `EM_ATENDIMENTO`, `AGUARDANDO_CLIENTE`).
4. **Se houver ativo:** registra interação `MENSAGEM_CLIENTE` no ticket existente — **não** cria novo protocolo.
5. **Recheck** ativo pelo par Cliente+Contato antes de criar ticket (Sprint 211).
6. **Se não houver** (inexistente ou último `RESOLVIDO`/`CANCELADO`): cria via `criarTicketPorWebhook`, que **rejeita** segundo ativo para o mesmo par (400).

Resposta: `ticketCriado`, `numeroTicket`, `status`, `mensagemRegistrada`.

Pós-encerramento com pesquisa (Sprint 212): resposta do `PUT .../encerrar` pode incluir `avaliacaoLinkPublico` (`?page=avaliacao&token=...`); envio WhatsApp simulado (`envioStatus=SIMULADO`). Mensagem em contato com último ticket encerrado pode gerar **pendência de decisão** em vez de novo ABERTO — tratar no Chats.

**Ainda fora de escopo:** provedor WhatsApp real, WebSocket, envio de mensagem pelo sistema, anexos automáticos da API, endpoint público sem autenticação.

O Chats visualiza mensagens ao recarregar ticket/interações (sem alteração de layout nesta sprint).

## Integrações futuras

- WhatsApp/API poderá registrar anexos com origem `WHATSAPP` sem alterar o contrato de listagem no painel.
- Pré-visualização inline (imagem/áudio/vídeo) e sincronização em tempo real ficam para sprints posteriores.

## Smoke da fase Chats (Sprint 141)

Checklist automatizado: `mvn clean install`, `npm test` (77 testes JS + testes Java incl. fluxo WhatsApp preparatório).

Checklist manual sugerido: abas Fila/Chats/Histórico, etiquetas, **observação (salvar/trocar conversa/limpar)**, anexos, Encerrar ticket / Detalhes, `GET /api/tickets/ativo`, `POST /api/integracoes/whatsapp/mensagens`, Tickets e Indicadores sem erro no console.

## O que não fazer agora

- Não alterar regras de abertura, status ou encerramento de ticket por causa de anexos.
- Não expor diretório físico do servidor na API ou no frontend.

## Sprint 193 — Chats com Contato e contexto Cliente/Matriz

### Painel direito (contexto operacional)

Quatro blocos, nesta ordem:

1. **Cliente** — contratante F5 (`ticket.cliente`, e-mail/empresa/CNPJ/cidade do cadastro do cliente no ticket).
2. **Contato** — pessoa atendida quando `contatoId` está preenchido (`contatoNome`, `contatoWhatsapp`, e-mail, empresa/local, cidade/UF, observações).
3. **Entrada do atendimento** — WhatsApp Matriz (`whatsappMatrizNumero`, `whatsappMatrizNome` quando distinto).
4. **Chamado atual** — protocolo, canal, abertura, prioridade (inalterado).

Nomenclatura visível: **Cliente**, **Contato**, **Entrada do atendimento**, **Chamado atual**. Não usar Revenda, Conexão, Carteira ou Subcliente no painel.

**Sprint 204:** smoke técnico da entrada simulada documentado em `docs/SPRINT_204_SMOKE_ENTRADA_WHATSAPP.md` (validar ticket na Fila e painel direito após `POST /api/integracoes/whatsapp/mensagens`).

**Sprint 205:** script reproduzível `scripts/smoke-entrada-whatsapp.ps1` + roteiro `docs/SPRINT_205_SMOKE_OPERACIONAL_ENTRADA_WHATSAPP.md` — após o script, validar Fila e painel manualmente.

**Sprint 207:** smoke browser dois Contatos — `docs/SPRINT_207_SMOKE_BROWSER_CHATS_DOIS_CONTATOS.md` (validação API dos painéis/timeline + checklist Fila).

**Sprint 208:** smoke UI reproduzível — `data-testid` + `docs/SPRINT_208_SMOKE_UI_CHATS_DOIS_CONTATOS.md` + `scripts/smoke-ui-chats-dois-contatos.md`.

**Sprint 209:** etiquetas por Contato no Chats — `docs/SPRINT_209_SMOKE_UI_ETIQUETAS_CONTATO.md` (PUT `/api/contatos/{id}/etiquetas`; isolamento entre contatos).

**Sprint 210:** mesmo Contato em outro ticket (**TK-000088**) + fallback legado **TK-000020** — `docs/SPRINT_210_SMOKE_ETIQUETAS_CONTATO_FALLBACK.md`.

### Fallback (tickets sem `contatoId`)

- Bloco **Cliente** mantém telefones/e-mail do cadastro legado denormalizado no ticket.
- Bloco **Contato** usa `contatoSolicitante*` quando existir, com aviso discreto de cadastro antigo.
- Bloco **Entrada** oculto se não houver matriz no ticket.

### Dados no `TicketResponseDTO`

Campos usados no painel: `cliente`, `clienteId`, `clienteArteHeaderChatsUrl`, `contatoId`, `contatoNome`, `contatoWhatsapp`, `contatoEmail`, `contatoEmpresaLocal`, `contatoCidade`, `contatoUf`, `contatoObservacoes`, `whatsappMatrizId`, `whatsappMatrizNumero`, `whatsappMatrizNome`, legado `contatoSolicitante*`, `telefone`, `empresa`, `cidade`, `uf`, protocolo/abertura.

### Preservado

- Header White Label (arte do Cliente + fallback Carteira).
- Lista esquerda, timeline, composer, drawer, histórico resumido, etiquetas e observação interna.

## Sprint 194 — Etiquetas do Contato no Chats

### Vínculo

- Tabela `contato_etiquetas` (entidade `ContatoEtiqueta`), UK `(contato_id, etiqueta_id)`.
- `TicketEtiqueta` e endpoints `/api/tickets/{numero}/etiquetas` **permanecem** (legado).

### Endpoints

- `GET /api/contatos/{id}/etiquetas`
- `PUT /api/contatos/{id}/etiquetas` — body `{ "etiquetaIds": [1,2] }`

### Chats (painel Atendimento → Etiquetas)

| Condição | Carregar/salvar |
|----------|-----------------|
| `ticket.contatoId` preenchido | `/api/contatos/{id}/etiquetas` |
| Sem `contatoId` | `/api/tickets/{numero}/etiquetas` + hint de vínculo legado no chamado |

Etiquetas do Contato valem para todos os tickets daquele Contato. Não há migração automática de `ticket_etiquetas` → `contato_etiquetas` nesta sprint.

## Sprint 195 — Decisão pós-encerramento no Chats

### Entidade

`interacao_pendente_decisao` — status: `PENDENTE`, `VINCULADA_ANTERIOR`, `NOVO_TICKET`, `CANCELADA`.

### Detecção (integração WhatsApp preparatória)

1. Ticket **ativo** (ABERTO / EM_ATENDIMENTO / AGUARDANDO_CLIENTE) do mesmo Cliente + Contato → mensagem na interação do ticket.
2. Sem ativo, com **último encerrado** (RESOLVIDO/CANCELADO) e `contato_id` → cria pendência (não grava mensagem ainda).
3. Sem histórico encerrado com Contato → cria ticket novo (fluxo legado).

Requer `clienteId` resolvido (matriz ou payload) e Contato WhatsApp (`criarSeNaoExistir`). Sem Contato, mantém criação automática de ticket.

### Endpoints

- `GET /api/chats/interacoes-pendentes`
- `POST /api/chats/interacoes-pendentes/{id}/vincular-anterior` — interação no ticket encerrado, **sem reabrir**
- `POST /api/chats/interacoes-pendentes/{id}/gerar-ticket` — novo ticket + primeira mensagem

### Chats

- Pendências aparecem na aba **Fila** (prefixo de lista `PEND-{id}`).
- Ao selecionar: banner com botões **Manter no ticket anterior** / **Gerar novo ticket**.

### Pendências

- Migração histórica opcional ticket → contato.
- Uma pendência aberta por Contato por vez.

## Sprint 196 — Motivo obrigatório no encerramento

Encerramento exige **Categoria** (grupo), **Subcategoria**, **Motivo** e comentário. Cadastro de motivos em **Configurações → Motivos de encerramento** (ADMIN). Tickets antigos sem `motivo_id` exibem “—” no detalhe.

## Sprint 199 — Link público de avaliação

Contato responde sem login em `/?page=avaliacao&token=...` (API `/api/public/avaliacoes/...`). Endpoint interno da Sprint 198 permanece para testes com sessão.

## Sprint 198 — Resposta e expiração

Pesquisa `PENDENTE` pode ser respondida via API interna `POST /api/tickets/{numero}/satisfacao/responder` (autenticada). Após `expira_em`, job ou tentativa de resposta marca `EXPIRADA`. Detalhe do ticket: nota exibida em `RESPONDIDA`; `PENDENTE` mostra “Aguardando resposta”. Sem página pública do Contato nesta sprint.

## Sprint 197 — Pesquisa pós-RESOLVIDO (preparação)

No modal **Encerrar ticket** (Tickets ou Chats): após motivo/comentário, pergunta **Enviar pesquisa de satisfação ao contato?** (padrão: não). Payload `enviarPesquisaSatisfacao`. Sem `contatoId` no ticket: aviso na UI; backend grava `NAO_ENVIADA` se opt-in sem contato. Detalhe do ticket exibe **status** da pesquisa quando houver registro. Envio WhatsApp real e resposta do Contato: sprint futura.
- Título central do header ainda usa helper legado `getChatsConexaoRevendaTitle` (campo `conexao`); renomear função em sprint futura.
- Tickets antigos podem misturar semântica “cliente atendido” vs contratante até migração de dados.
