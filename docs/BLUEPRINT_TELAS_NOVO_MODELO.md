# Blueprint — Telas e padrão visual corporativo

**Sprint 234** — documento de referência. **Não implementa** CSS nem altera comportamento funcional.

**Referência visual:** Login Corporativo (Sprint 229) — `css/pages/login.css`, escopo `#loginScreen`.

**Modelo de domínio na UI:** **Cliente** → **Contato** (interno) + **WhatsApp Matriz** → **Ticket** (Chats / lista / detalhe), com **Conexão** (entidade Carteira/Revenda) e **classificação de encerramento** (Categoria → Subcategoria → Motivo).

---

## 1. Inventário — telas e escopos

| Área | ID / rota lógica | Onde vive | Módulo JS principal | CSS dedicado |
|------|------------------|-----------|---------------------|--------------|
| Avaliação pública | `#avaliacaoPublicaScreen` | Fora do app autenticado | `avaliacaoPublicaPage.js` | `avaliacao-publica.css` |
| Login corporativo | `#loginScreen` | Pré-auth | `auth` / login em `app.js` | `login.css` (**padrão-mãe**) |
| Dashboard | `page-dashboard` | Menu | `dashboardPage.js` | `dashboard.css` |
| Clientes | `page-clientes` | Menu (ADMIN) | `clientesPage.js` | `clientes.css` |
| Atendentes | `page-atendentes` | Menu | `atendentesPage.js` | `atendentes.css` |
| Perfil | `page-perfil` | Menu + topbar | `perfilPage.js` | `perfil.css` |
| Abrir ticket | `page-abrir-ticket` | Menu | `abrirTicketPage.js` | `abrir-ticket.css` |
| Tickets (lista) | `page-tickets` | Menu — título UI **“Meus Tickets”** | `ticketsPage.js` + tabela em `app.js` | `tickets.css` |
| Detalhe ticket | `#modalDetalhes` | Modal global | `ticketDetailsModal.js` | `modals.css` |
| Chats | `page-chats` | Menu | `chatsPage.js` | `chats.css` |
| Relatórios | `page-relatorios` | Menu | `relatoriosPage.js` | `relatorios.css` |
| Indicadores | `page-indicadores` | Menu + 7 subpainéis | `indicadoresPage.js` | `indicadores.css` |
| Auditoria | `page-auditoria` | Menu (ADMIN) | `auditoriaPage.js` | `auditoria.css` |
| Configurações | `page-configuracoes` | Menu | `configuracoesPage.js` + blocos | `configuracoes.css` |
| Shell | sidebar + topbar | `appScreen` | `router.js`, `topbar.js` | `layout.css`, `theme.css`, `components.css` |

**Modais globais adicionais:** encerrar ticket, escalonar, fila analista, foto perfil (fora do escopo de menu, mas fazem parte da UX Ticket/Chats).

---

## 2. Menu atual (sidebar)

Ordem literal em `index.html`:

1. Dashboard — `dashboard` (todos os perfis)
2. Clientes — `clientes` (ADMIN)
3. Atendentes — `atendentes` (ADMIN, SUPERVISOR)
4. Perfil — `perfil` (todos) — **duplicado** com atalho na topbar
5. Abrir Ticket — `abrir-ticket` (ADMIN, ANALISTA)
6. Tickets — `tickets` (todos) — rótulo da página: “Meus Tickets”
7. Chats — `chats` (todos)
8. Relatórios — `relatorios` (ADMIN, SUPERVISOR)
9. **Indicadores** (grupo expansível) — `indicadores` (ADMIN, SUPERVISOR)
   - Visão geral *(placeholder)*
   - Chamados
   - Satisfação
   - Encerramento e satisfação
   - Clientes
   - Atendentes
   - SLA
10. Auditoria — `auditoria` (ADMIN)
11. Configurações — `configuracoes` (ADMIN, SUPERVISOR; edição sensível só ADMIN)
12. Sair — `logoutBtn`

**Fora do menu:** Login, Avaliação pública.

---

## 3. Menu ideal proposto

Agrupar por jornada e reduzir ruído, mantendo `data-page` e permissões atuais até sprints de implementação.

### 3.1 Estrutura sugerida

| Grupo | Itens | Observação |
|-------|--------|------------|
| **Visão** | Dashboard | Entrada gerencial |
| **Operação** | Chats → Tickets → Abrir ticket | Ordem operacional (conversa → fila → criação) |
| **Cadastros** | Clientes | Contato + WhatsApp Matriz **dentro** da página |
| **Análise** | Indicadores (submenu atual) → Relatórios | Gerencial |
| **Administração** | Atendentes *(rótulo futuro: Usuários e perfis)* → Configurações *(submenu interno)* → Auditoria | ADMIN/SUPERVISOR conforme regra atual |
| **Conta** | *(remover Perfil do menu)* | Apenas topbar: avatar, tema, alertas, perfil |
| **Rodapé** | Sair | Mantém |

### 3.2 Submenu Configurações (futuro)

Unificar blocos longos da página única em âncoras ou abas (somente visual/UX):

- Horário útil e feriados (SLA calendário)
- Metas SLA por prioridade
- **Conexões** (hoje “Conexões / Revendas” — entidade Carteira)
- **Classificação:** Motivos de encerramento *(Categoria/Subcategoria: cadastro backend; UI futura opcional)*
- Etiquetas (Chats)

### 3.3 Nomenclatura alvo (UI)

| Atual | Alvo | Motivo |
|-------|------|--------|
| Contratantes (lista Clientes) | Clientes | Alinhado ao domínio e filtros Dashboard/Relatórios |
| Meus Tickets (H1) | Tickets ou Fila de tickets | Menu já diz “Tickets”; evitar “meus” para SUPERVISOR/ADMIN |
| Conexões / Revendas | Conexões | Carteira é implementação; usuário vê “conexão” no ticket |
| Carteira (label Abrir ticket) | Conexão | Consistência |
| Atendentes | Usuários (opcional) | Inclui perfil ADMIN/SUPERVISOR/ANALISTA |

---

## 4. Função de cada página (esqueleto ideal)

### 4.1 Cliente (cadastro mestre)

- **Função:** contratante F5; origem de `clienteId` em filtros (Dashboard, Indicadores, Relatórios).
- **Blocos:** dados, comunicação, endereço, status, arte header Chats, **WhatsApps Matriz**, ações salvar/novo.
- **Filhos na mesma tela:** `#clienteContatosSection` (contatos internos legado), não menu separado.

### 4.2 Contato

- **Função:** pessoas vinculadas ao cliente (cadastro interno); API `clientes/{id}/contatos`.
- **Estado:** marcado como legado em relação ao Contato WhatsApp do Chats; **manter** até migração de produto.
- **Ideal:** aba ou subseção “Contatos” dentro de Clientes, visual igual a WhatsApp Matriz (lista + formulário).

### 4.3 WhatsApp Matriz

- **Função:** números de entrada API por cliente; habilita atendimento Chats.
- **Regra UX:** visível após salvar cliente; mensagens “salve o cliente antes”.

### 4.4 Ticket

- **Função:** unidade de trabalho; status, prioridade, SLA, encerramento, satisfação.
- **Superfícies:** Chats (operacional), lista Tickets (busca avançada), modal Detalhes (leitura/ações), Abrir ticket (criação UI), Relatórios (export).
- **Vínculos exibidos:** Cliente, Conexão/Carteira, contato solicitante, motivo/subgrupo/grupo no encerramento.

### 4.5 Conexão (Carteira / Revenda)

- **Função:** revenda/carteira técnica; label “conexão” em tickets e Chats.
- **Onde:** Configurações → Conexões; **não** página de menu própria.

### 4.6 Classificação (Categoria / Subcategoria / Motivo)

- **Motivo:** CRUD em Configurações (terceiro nível).
- **Categoria / Subcategoria:** selects no encerramento e filtros Relatórios; **sem tela admin dedicada hoje** — gap documentado para sprint futura de cadastro ou importação.

---

## 5. Classificação: manter, remover, renomear, mover

| Item | Decisão | Ação nas sprints visuais |
|------|---------|---------------------------|
| Todas as `page-*` atuais | **Manter** | Repaginar, não remover rotas |
| Avaliação pública | **Manter** | Aplicar tokens corporativos (paleta login) |
| Login | **Manter** | Já referência; alinhar logo ST sidebar depois |
| Perfil no sidebar | **Mover** | Só topbar |
| Indicadores → Visão geral | **Manter oculto ou renomear** | Até ter conteúdo; ou remover do submenu quando produto definir |
| Indicadores → placeholders “em construção” | **Manter** | Empty state corporativo |
| Contatos internos (legado) | **Manter** | Renomear copy; não remover sem sprint de domínio |
| `classificacaoCliente` hidden | **Manter** | Campo legado; não expor na repaginação |
| Grupo/Subcategoria sem CRUD UI | **Manter gap** | Sprint futura “Classificação” em Config ou import |
| Duplicata terminologia Carteira/Conexão/Revenda | **Renomear** | UI só “Conexão”; hint técnico opcional |
| Logo sidebar azul `#1e40af` | **Renomear visual** | Alinhar neon/teal do login |
| Cadastre-se / Esqueceu senha no login | **Manter** | Visual apenas |

**Não remover nesta fase:** Auditoria, Relatórios, Atendentes, nenhuma subpágina de Indicadores (permissões e loaders já amarrados).

---

## 6. Blocos por página (auditoria resumida)

### Dashboard

- Encerramento e satisfação (período + **Cliente** — Sprint 230)
- Prioridades, categorias recorrentes, top grupos/subgrupos
- Tickets críticos/altos, SLA 1º atendimento e resolução, escalonados
- Indicadores operacionais, painel por conexão

### Chats

- Lista conversas + busca avançada embutida
- Painel conversa + contexto Cliente/Contato/Ticket
- Etiquetas, observações, mídias — **não repaginar regra de negócio**

### Tickets

- Busca avançada (texto, status, conexão, cliente texto, analista, datas, prioridade)
- Tabela + refresh — detalhe via modal

### Relatórios

- Filtros avançados + **select Cliente (`clienteId`)** — Sprint 232
- Resumo tickets + tabela + CSV
- Bloco Satisfação + evolução (mesmo filtro Cliente)

### Indicadores

- 7 subpainéis (`data-indicadores-subpanel`); Chamados e Encerramento com filtros Cliente alinhados
- Vários blocos “Indicador em construção”

### Configurações

- Horário útil, Feriados, Metas SLA (ADMIN)
- Conexões/Revendas (carteira), Motivos, Etiquetas
- Supervisor: leitura parcial; ADMIN: edição

### Atendentes

- Cadastro analista + lista + painel online (duplicata parcial com Indicadores → Atendentes)
- **Usuários/perfis (ADMIN):** bloco `#adminPerfisSection` na mesma página (gestão de perfil de acesso), não item de menu separado

---

## 7. Padrão visual global (a documentar → implementar depois)

**Princípio:** estender o vocabulário do Login para `theme.css` + `layout.css` + `components.css`, sem quebrar tema claro/escuro existente (`html[data-theme]`).

### 7.1 Paleta

| Token | Valor | Uso |
|-------|--------|-----|
| `--corp-bg-deep` | `#0F2F3A` | Sidebar, fundos hero, header app opcional |
| `--corp-accent-neon` | `#00FFAA` | CTA primário, foco, ícones ativos, logo |
| `--corp-text-on-dark` | `#FFFFFF` | Títulos em fundo teal |
| `--corp-text-muted-dark` | `#5A7A86` / `#94A3B8` | Subtítulos em fundo escuro |
| `--corp-surface-card` | `rgba(255,255,255,0.97)` (claro) / `rgba(30,41,59,0.92)` (escuro) | Cards |
| `--corp-border-subtle` | `rgba(0,255,170,0.22)` | Bordas de card/input |
| `--corp-shadow-card` | `0 4px 24px rgba(0,0,0,0.25), 0 0 48px rgba(0,255,170,0.08)` | Elevação |

Manter `--primary` atual para **compatibilidade** até migração completa; mapear gradualmente para teal/neon.

### 7.2 Tipografia

- Família: `system-ui`, `Segoe UI`, sans-serif (igual login).
- Títulos de página: 1.5–1.75rem, peso 700, letter-spacing leve negativo.
- Labels de formulário: 0.8125rem, peso 600.
- Corpo: 1rem; muted com `--text-muted` / teal acinzentado.

### 7.3 Componentes

| Componente | Diretriz |
|------------|----------|
| **Card** (`content-section`) | `border-radius: 16–20px`, borda sutil neon, sombra difusa, padding generoso (24–32px) |
| **Botão primário** | Fundo neon, texto `#0F2F3A` ou branco conforme contraste; hover: brilho/glow leve |
| **Botão secundário** | Outline teal ou fundo `rgba(15,47,58,0.08)`; sem borda pesada |
| **Input / select** | `border-radius: 12px`; focus: borda `#00FFAA` + `box-shadow: 0 0 0 3px rgba(0,255,170,0.22)` |
| **Tabela** | Cabeçalho discreto; linhas zebra suaves; hover row; sem grade pesada |
| **Empty state** | Ícone ou texto central, muted, uma ação clara |
| **Nav sidebar** | Fundo teal; item ativo com barra/indicador neon; ícones monocromáticos com acento no ativo |
| **Topbar** | Superfície clara/escura consistente; sino, tema, avatar |

### 7.4 Estados e acessibilidade

- Hover: transição 0.2s em borda/sombra/cor.
- Focus visível em todos os controles (não remover outline sem substituto neon).
- Disabled: opacidade ~0.5, sem neon.
- Responsivo: breakpoints alvo **&lt;768px** (sidebar colapsável / drawer), **768–1024** (grids 1 coluna), **&gt;1024** (layout atual master-detail Clientes e Chats).

### 7.5 O que não fazer na repaginação visual

- Não alterar ordem de campos obrigatórios nem IDs usados por JS/tests.
- Não mudar contratos de API nem textos de erro técnicos expostos ao usuário.
- Não unificar Chats e Tickets em uma página sem sprint de produto.

---

## 8. Itens em desuso ou desalinhados (legado)

1. **Perfil** no menu lateral enquanto topbar já abre perfil.
2. **Termo “Contratante”** na tela Clientes vs **“Cliente”** no restante do produto.
3. **Contatos internos** com aviso de legado — risco de confusão com contato do Chats.
4. **`classificacaoCliente`** oculto no form — dívida de modelo.
5. **Indicadores → Visão geral** sem conteúdo.
6. **Placeholders** “Indicador em construção” em Indicadores e subblocos de etiquetas.
7. **Identidade visual split:** login teal/neon vs sidebar azul `#1e40af` vs `theme.css` azul corporativo antigo.
8. **Categoria/Subcategoria** sem manutenção na UI (apenas Motivo).
9. **Filtro “Cliente” texto** em Tickets (busca avançada) vs **select `clienteId`** em Relatórios/Dashboard — alinhar em sprint futura de UX, não neste blueprint de código.

---

## 9. Ordem sugerida das próximas sprints (repaginação visual)

| Sprint | Escopo visual | Arquivos-alvo principais |
|--------|----------------|---------------------------|
| **235** | Design tokens corporativos em `theme.css` (**concluído** — ver `SPRINT_235`) | `theme.css` |
| **236** | Shell: sidebar, topbar, `layout.css` (**concluído** — ver `SPRINT_236`) | `layout.css`, `theme.css` |
| **237** | Login polish + Avaliação pública (**concluído** — `SPRINT_237`) | `login.css`, `avaliacao-publica.css` |
| **239** | Clientes (**concluído** — `SPRINT_239`) | `clientes.css`, `index.html` Clientes |
| **240** | Chats cores corporativas (**concluído** — `SPRINT_240`) | `chats.css` |
| **241** | Tickets lista + modal Detalhes + modais encerrar (**concluído** — `SPRINT_241`) | `tickets.css`, `modals.css` |
| **242** | Dashboard + Indicadores (**concluído** — `SPRINT_242`) | `dashboard.css`, `indicadores.css` |
| **243** | Paginação 15 itens Clientes/Tickets (**concluído** — `SPRINT_243`) | `listPagination.js`, pages |
| **244** | Relatórios visual (**concluído** — `SPRINT_244`) | `relatorios.css` |
| **245** | Configurações (**concluído** — `SPRINT_245`) | `configuracoes.css` |
| **246** | Atendentes, Auditoria, Abrir ticket, Perfil; Perfil só topbar (**concluído** — `SPRINT_246`) | CSS páginas + `layout.css` |

Cada sprint visual: backup AGENTS.md, `npm test` se tocar em classes referenciadas em testes de view (se houver), smoke browser, **sem** alterar services/backend.

---

## 10. Riscos

| Risco | Mitigação |
|-------|-----------|
| Regressão em testes Vitest de view/DOM | Repaginar por classes BEM novas; manter ids `data-testid` |
| Tema escuro inconsistente | Definir tokens duplos claro/escuro na 235 |
| Chats layout complexo | Sprint dedicada 239; não acoplar com Clientes |
| Permissões de menu | Alterar ordem/ocultar itens só com `permissions.js` em sprint explícita |
| Escopo creep (CRUD Categoria) | Fora da repaginação; sprint de produto separada |

---

## 11. Critérios Sprint 234 (checklist)

- [x] Documento blueprint criado
- [x] Menu atual mapeado
- [x] Menu ideal proposto
- [x] Legados identificados
- [x] Plano manter/remover/renomear/mover
- [x] Padrão visual global documentado
- [x] Ordem das próximas sprints sugerida
- [x] Nenhum código funcional alterado nesta sprint

---

## 12. Referências no repositório

- Permissões: `js/core/permissions.js` — `PAGE_ACCESS_BY_PERFIL`
- Roteamento: `js/core/router.js`, `app.js` — `pageLoaders`
- Login visual: `css/pages/login.css`
- Estratégia: `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md`
