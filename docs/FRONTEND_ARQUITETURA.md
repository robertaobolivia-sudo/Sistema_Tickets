# Arquitetura frontend — suporte-tickets

Documentação da estrutura modular em `src/main/resources/static/` após a extração de páginas, componentes, CSS e helpers. Use este guia para decidir **onde** implementar mudanças em sprints futuros.

## Visão geral

- **Entrada única:** `index.html` carrega folhas CSS modulares e um único módulo ES: `app.js`.
- **Padrão:** HTML estático + JavaScript em módulos (`type="module"`) + chamadas HTTP via `js/services/*`.
- **Sessão:** token e analista logado em `js/core/state.js` / `auth.js`; requisições autenticadas via `apiFetch` em `js/core/api.js`.
- **Regra de ouro:** não concentrar lógica de tela ou de API em `app.js`. O `app.js` apenas **liga** módulos (bootstrap).

```
index.html
    ├── css/theme.css … css/pages/*.css, style.css (fallback)
    └── app.js (bootstrap)
            ├── js/core/*     (infra, formatação, roteador)
            ├── js/services/* (HTTP por domínio)
            ├── js/pages/*    (uma página = um módulo)
            ├── js/components/* (modais, topbar, alertas)
            └── js/rules/*    (regras puras, testáveis)
```

---

## `app.js` — bootstrap / orquestrador

Responsabilidades **permitidas**:

- Referências a elementos globais do DOM (login, nav, `pages`, alertas compartilhados).
- `import` de módulos e chamadas `init*Page`, `init*Component`, `configureAuth`, `configureRouter`.
- Injeção de dependências nas páginas/componentes (funções de `presentation.js`, loaders, callbacks entre módulos).
- Eventos globais: submit de login, logout, tecla Escape, mudança de status do operador.
- Funções que **precisam** cruzar módulos no mesmo arquivo (ex.: `updateLoggedAnalystUi`, `setAnalystAvatarElement` com `perfilPage`, `changeTicketStatus`).

**Não colocar em `app.js`:**

- Lógica de listagem/formulário de uma página específica.
- Chamadas `fetch` diretas (usar `services`).
- Regras de exibição SLA/status/prioridade (usar `rules` + `presentation`).
- HTML de renderização de tabelas/kanban (ficar na `page` ou `presentation`).

---

## `js/core` — infraestrutura compartilhada

| Arquivo | Função |
|---------|--------|
| `api.js` | `API_BASE`, `apiFetch` (header de sessão, tratamento base). |
| `auth.js` | Login, logout, restauração de sessão, hooks configurados pelo `app.js`. |
| `state.js` | Analista logado, `localStorage`, token. |
| `messages.js` | Mensagens amigáveis, `MSG_ERRO`, helpers de erro de API/login. |
| `permissions.js` | Perfis, visibilidade de menu/páginas, auditoria, configurações. |
| `router.js` | `showPage`, `openApp`, `configureRouter`, loaders por `pageKey`. |
| `queryParams.js` | Montagem de query string (tickets, relatórios, auditoria, satisfação). |
| `presentation.js` | Formatação UI compartilhada (datas, alertas, badges, kanban mini, avatar HTML). |

Novas funções **genéricas** (sem dono de tela) → `core`. Formatação reutilizada em várias páginas → preferir `presentation.js` em vez de duplicar no `app.js`.

---

## `js/services` — chamadas de API

Um arquivo por domínio backend, usando `apiFetch` (ou `fetch` só em login público).

| Service | Domínio |
|---------|---------|
| `ticketService.js` | Tickets, status, busca, interações. |
| `clienteService.js` | Clientes e contatos. |
| `analistaService.js` | Analistas, status operador, perfil. |
| `dashboardService.js` | Resumo e métricas do dashboard. |
| `notificacaoService.js` | Notificações. |
| `auditoriaService.js` | Auditoria e retenção. |
| `configuracaoService.js` | Horário útil, feriados, metas SLA. |
| `categoriaService.js` | Grupos/subgrupos de encerramento. |
| `satisfacaoService.js` | Satisfação. |
| `httpUtil.js` | Utilitários HTTP compartilhados. |

**Nova API:** criar método no service existente ou novo `*Service.js`; **nunca** espalhar URLs em `pages` se puder evitar.

---

## `js/pages` — telas (SPA por seção)

Cada arquivo corresponde a um `#page-*` no `index.html`:

- `dashboardPage.js`, `ticketsPage.js`, `clientesPage.js`, `atendentesPage.js`, `perfilPage.js`, `abrirTicketPage.js`, `relatoriosPage.js`, `auditoriaPage.js`, `configuracoesPage.js`

Padrão típico:

- `initNomePage(deps)` — listeners e referências injetadas pelo `app.js`.
- `loadNomePage()` — carga de dados ao entrar na rota (registrado em `configureRouter.pageLoaders`).

**Nova tela:** novo `js/pages/minhaPage.js`, bloco HTML em `index.html`, item no menu, `init`/`load` no `app.js` e entrada em `pageLoaders` + `permissions.js` se houver restrição de perfil.

---

## `js/components` — UI transversal

Reutilizável entre páginas, sem ser “página inteira”:

- `ticketDetailsModal.js` — modal de detalhes, encerramento, escalonamento, satisfação.
- `topbar.js` — menu usuário, tema, avatar.
- `notificacoesPanel.js` — painel de notificações.
- `alertaTicket.js` — alertas visuais/polling de tickets novos.

**Novo modal/componente global:** novo arquivo em `components/`, `init` no `app.js`, estilos em `css/modals.css` ou `css/components.css`.

---

## `js/rules` — regras puras (testáveis)

Sem DOM e sem `fetch`:

- `ticketViewRules.js` — classes de status, satisfação, ticket finalizado, etc.
- `slaViewRules.js` — rótulos e HTML de badges SLA.

`presentation.js` pode importar `rules` e aplicar fallbacks (`displayValue`).

**Nova regra visual determinística:** implementar em `rules/*`, cobrir com teste em `js/tests/`, expor via `presentation` ou injetar na page/modal.

---

## `js/tests` — Vitest

Testes de funções puras (`messages`, `permissions`, `queryParams`, `ticketViewRules`, `slaViewRules`).

- Config: `js/vitest.config.js`, `js/package.json`.
- Detalhes: `js/tests/README.md`.

Não substituem smoke manual no navegador; não rodam no Maven.

---

## CSS — grupos de arquivos

Ordem de carga em `index.html` (respeitar cascata):

| Arquivo | Função |
|---------|--------|
| `css/theme.css` | Variáveis de tema claro/escuro (`data-theme`). |
| `css/base.css` | Reset, tipografia, utilitários globais. |
| `css/layout.css` | Shell: sidebar, topbar, área de conteúdo, grids de app. |
| `css/components.css` | Botões, cards, tabelas, badges, alertas, formulários genéricos. |
| `css/modals.css` | Modais compartilhados (detalhe ticket, foto, encerramento, etc.). |
| `css/pages/*.css` | Estilos específicos por página (`dashboard`, `tickets`, `clientes`, …). |
| `style.css` | **Fallback mínimo** — não adicionar estilos novos aqui; usar módulos acima. |

**Nova página:** criar `css/pages/nome-da-pagina.css` e incluir `<link>` em `index.html` (com query `?v=sprintXXX` para cache, alinhado aos demais links).

---

## Como adicionar…

### Nova tela

1. Markup: seção `#page-…` em `index.html` + botão `.nav-item` com `data-page`.
2. JS: `js/pages/…Page.js` com `init` + `load`.
3. `app.js`: import, `init…({ deps })`, `pageLoaders` em `configureRouter`.
4. `permissions.js`: `canAccessPage` / menu se necessário.
5. CSS: `css/pages/….css` + link no HTML.
6. Testes manuais + `npm test` se criou regras novas.

### Nova chamada de API

1. Método em `js/services/<dominio>Service.js` usando `apiFetch`.
2. Consumir na `page` ou `component` (não no `app.js`, salvo callback fino).
3. Mensagens de erro via `messages.js` / `showAlert` injetado.

### Novo modal / componente

1. Markup no `index.html` (ou fragmento já existente).
2. `js/components/….js` com `init(deps)` e export de ações (`open`, `close`).
3. Estilos em `css/modals.css` ou `components.css`.
4. Registrar `init` e callbacks no `app.js`.

### Nova regra visual / testável

1. Lógica em `js/rules/….js` (funções puras).
2. Teste em `js/tests/….test.js`.
3. Uso em UI via `presentation.js` ou injeção direta na page/modal.

---

## Checklist antes de concluir sprint frontend

- [ ] Código novo na pasta correta (não inchou `app.js` sem necessidade).
- [ ] Sem `null` / `undefined` / `NaN` / `Invalid Date` na UI; textos via `displayValue` / `messages`.
- [ ] Endpoints sensíveis com sessão (`apiFetch`); login/webhook público é exceção.
- [ ] Permissões de menu/página atualizadas se a feature for restrita.
- [ ] CSS na folha modular adequada (não em `style.css`).
- [ ] `cd src/main/resources/static/js && npm test` (se alterou `rules`, `queryParams`, `messages`, `permissions`).
- [ ] `mvn clean install` (parar processo na porta 8080 se o JAR estiver em uso).
- [ ] App sobe em **8080** e `http://localhost:8080/` retorna **200**.
- [ ] Smoke rápido: login, navegação, tela tocada, modal se aplicável, console sem erro crítico.

---

## Comandos de validação

```bash
cd src/main/resources/static/js
npm install   # primeira vez
npm test
```

```bash
mvn clean install
```

Subir e verificar (na raiz do projeto Maven):

```bash
java -jar target/suporte-tickets-1.0.0.jar
```

Confirmar: `http://localhost:8080/` → HTTP **200**.

---

## Dashboard, Relatórios e Indicadores

- **Dashboard** — operação rápida; não duplicar BI pesado aqui.
- **Relatórios** — listagens e exportações operacionais.
- **Indicadores** — agregações gerenciais por período (ver `docs/INDICADORES_DIRETRIZES.md`).
- Segmentação gerencial futura: **etiquetas flexíveis**; o campo legado de classificação em Clientes não expõe N1/N2 na UI.

---

## Referências

- Contratos de API: `docs/API_CONTRATOS.md`
- Indicadores e etiquetas: `docs/INDICADORES_DIRETRIZES.md`
- Testes JS: `src/main/resources/static/js/tests/README.md`
- Regras de produto e execução de sprint: `AGENTS.md` (raiz do repositório / Desktop)
