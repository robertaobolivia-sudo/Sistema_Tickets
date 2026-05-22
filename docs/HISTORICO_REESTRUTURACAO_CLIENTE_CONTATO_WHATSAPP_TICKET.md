# Histórico mestre — Reestruturação Cliente / Contato / WhatsApp / Ticket

Registro das sprints da fase de reestruturação. Sprints anteriores à 234 estão resumidas em `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md` e nos `docs/SPRINT_*` individuais.

---

## Sprint 234 — Blueprint telas + padrão visual corporativo

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Inventariar telas/menus, propor menu ideal e padrão visual teal/neon (Login 229), plano de repaginação 235–244 |
| **Backup** | N/A (sprint documental, sem código) |
| **Arquivos principais** | `docs/BLUEPRINT_TELAS_NOVO_MODELO.md`, `docs/SPRINT_234_BLUEPRINT_TELAS_PADRAO_VISUAL.md`, `docs/ESTRATEGIA_REESTRUTURACAO_DIRETA.md` |
| **Resumo** | Blueprint com inventário de 12 rotas autenticadas + login + avaliação pública; menu atual vs ideal; classificação manter/renomear/mover; tokens visuais documentados; legados Carteira/Conexão/Revenda mapeados |
| **Decisão produto/arquitetura** | Contato WhatsApp no Chats; contatos internos em Clientes permanecem legado até sprint de domínio; Categoria/Subcategoria sem CRUD UI (só Motivo + selects encerramento); repaginação só CSS/HTML classes em sprints futuras |
| **Testes executados** | Nenhum (documental) |
| **Smoke** | Não aplicável |
| **Pendências** | Implementar tokens (235), shell (236), páginas na ordem do blueprint; CRUD Categoria/Subcategoria em sprint de produto separada |
| **Riscos** | Split visual login vs app; confusão Contato interno vs Contato Chats; testes Vitest de view ao mudar classes |
| **Próximo passo** | Sprint 235 — design tokens em `theme.css` |

---

## Sprint 235 — Tokens visuais corporativos

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Tokens `--corp-*` globais para repaginação futura, sem redesign |
| **Backup** | `BKP_Sprint_235_Tokens_Visuais_Corporativos` |
| **Arquivos principais** | `css/theme.css`, `css/components.css` (comentário), `docs/SPRINT_235_TOKENS_VISUAIS_CORPORATIVOS.md` |
| **Resumo** | Paleta teal/neon, radius, sombras, foco, botões e shell preparados; `--primary` legado intacto |
| **Decisão** | Login mantém `login.css` isolado; tokens não consumidos nesta sprint |
| **Testes** | Nenhum build; smoke visual manual esperado neutro |
| **Pendências** | Consumir tokens em layout (236) e páginas (237+) |
| **Riscos** | Cache `?v=` em index não bumpado (HTML intocado); ao aplicar tokens validar contraste claro/escuro |
| **Próximo passo** | Sprint 236 — shell sidebar/topbar |

---

## Sprint 236 — Shell corporativo

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Sidebar/topbar/nav com tokens `--corp-*`; páginas internas inalteradas |
| **Backup** | `BKP_Sprint_236_Shell_Corporativo` |
| **Arquivos principais** | `css/layout.css`, `css/theme.css` |
| **Resumo** | Sidebar teal; item ativo/hover neon; topbar e menu usuário coerentes; tema escuro unificado no shell |
| **Decisão** | Logo SVG ST no HTML mantido (azul legado); repaginação de logo em sprint futura |
| **Testes** | Smoke visual manual; sem Maven/npm |
| **Pendências** | Logo sidebar; mobile sidebar oculta (&lt;720px) sem drawer ainda |
| **Riscos** | Contraste neon em item ativo; cache CSS sem bump em index |
| **Próximo passo** | Sprint 237 — login + avaliação pública |

---

## Sprint 237 — Login e Avaliação pública corporativa

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Avaliação pública no padrão login/shell; login com tokens sem mudar visual |
| **Backup** | `BKP_Sprint_237_Login_Avaliacao_Publica` |
| **Arquivos principais** | `avaliacao-publica.css`, `login.css`, `avaliacaoPublicaView.js`, `avaliacaoPublicaPage.js`, testes Vitest |
| **Resumo** | Tela pública teal/card/neon; estados respondida/expirada/inválida com classes; login referencia `--corp-*` |
| **Decisão** | Botão Entrar mantém gradiente 229; `estadoVariant` só para CSS |
| **Testes** | Vitest 4/4; Playwright 2/3 (falha sem-contato = massa TK-000151) |
| **Smoke** | Fluxo público E2E no spec pesquisa OK |
| **Pendências** | Commit GitHub no remoto (sem `.git` local) |
| **Riscos** | Contraste estados em card claro; cache CSS |
| **Próximo passo** | Sprint 238 — Clientes |

---

## Sprint 238 — Playwright regressão 237

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | E2E 3/3; corrigir busca tickets e flaky UI |
| **Causa** | HTTP 500 texto livre + CLOB; corrida load vs busca |
| **Arquivos** | `TicketBuscaService.java`, `ticketsPage.js`, `e2e/global-setup.ts`, `e2e/tests/helpers/ticketsUi.ts`, specs E2E |
| **Testes** | Playwright 3/3; build package |
| **Próximo passo** | Sprint 239 — Clientes visual |

---

## Sprint 239 — Repaginação visual Clientes

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | UI Clientes alinhada ao padrão teal/neon; hierarquia lista/formulário/matriz/arte |
| **Backup** | `BKP_Sprint_239_Clientes_Visual` |
| **Arquivos** | `clientes.css`, `index.html` (copy + cache) |
| **Testes** | Vitest clienteFormView 5/5 |
| **Próximo passo** | Sprint 240 — Chats visual |

---

## Sprint 240 — Chats cores corporativas

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Cores/contrastes Chats via `--corp-*`; layout intacto |
| **Backup** | `BKP_Sprint_240_Chats_Cores` |
| **Arquivos** | `chats.css` |
| **Próximo passo** | Sprint 241 — Tickets/modais visual |

---

## Sprint 241 — Repaginação visual Tickets e modais

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Tickets/Meus Tickets, detalhe e modais (encerramento) no padrão teal/neon; fluxos e E2E intactos |
| **Backup** | `BKP_Sprint_241_Tickets_Modais_Visual` (`tickets.css`, `modals.css`) |
| **Arquivos principais** | `css/pages/tickets.css`, `css/modals.css`, `index.html` (`?v=sprint241`) |
| **Resumo** | Header, filtros, tabela, empty state e botões em `#page-tickets`; bloco Sprint 241 em modais para `#modalDetalhes` e `.modal-encerramento` (header, selects, choices, aviso sem contato); IDs e `data-testid` preservados |
| **Decisão** | Somente CSS + cache bump; sem backend/JS funcional |
| **Testes** | `cd e2e && npm test` — **3/3** |
| **Smoke** | E2E cobre encerramento com/sem pesquisa e sem contato; smoke manual Tickets (busca/detalhe) recomendado no navegador |
| **Build** | Não executado (apenas CSS/HTML cache) |
| **HTTP 200** | Validado na subida JAR 8080 ao fechar sprint |
| **Pendências** | Commit GitHub no remoto (sem `.git` local): mensagem `Sprint 241 — Repaginação visual Tickets e modais` |
| **Riscos** | Cache CSS antigo sem hard refresh; contraste tabela no tema escuro |
| **Próximo passo** | Sprint 242 — Dashboard + Indicadores visual (blueprint) |

---

## Sprint 242 — Visual corporativo Dashboard e Indicadores

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Dashboard e Indicadores no padrão `--corp-*`; filtros e requests intactos |
| **Backup** | `BKP_Sprint_242_Dashboard_Indicadores_Visual` |
| **Arquivos principais** | `dashboard.css`, `indicadores.css`, `index.html` (`?v=sprint242`) |
| **Resumo** | Cards, sections, filtros, tabelas e estados vazios escopados por `#page-dashboard` / `#page-indicadores`; fix CSS `.waiting-alert` no dashboard |
| **Decisão** | Somente CSS; Relatórios fora do escopo |
| **Testes** | Nenhum build (CSS) |
| **Smoke** | Manual Dashboard + Indicadores (período, Cliente, encerramento/sat) |
| **Pendências** | Commit GitHub (sem `.git` local) |
| **Riscos** | JAR/cache; contraste cards no escuro |
| **Próximo passo** | Sprint 243 — Relatórios visual |

---

## Sprint 243 — Paginação 15 itens (Clientes e Tickets)

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Paginação frontend 15/página; Primeira/Anterior/Próxima/Última; filtro reinicia pág. 1 |
| **Backup** | `BKP_Sprint_243_Paginacao_15_Listas` |
| **Arquivos principais** | `listPagination.js`, `listPaginationBar.js`, `clientesPage.js`, `ticketsPage.js`, `index.html`, `components.css`, Vitest |
| **Resumo** | Padrão reutilizável; cache local; sem alteração de API/backend |
| **Testes** | Vitest 149/149 (incl. `listPagination.test.js`) |
| **Smoke** | Clientes e Tickets: busca, navegação páginas, seleção/detalhe |
| **Pendências** | Commit GitHub |
| **Riscos** | Seleção em item fora da página atual sem scroll automático |
| **Próximo passo** | Sprint 244 — Relatórios visual |

---

## Sprint 244 — Visual corporativo Relatórios

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Relatórios no padrão `--corp-*`; filtros e CSV intactos |
| **Backup** | `BKP_Sprint_244_Relatorios_Visual` |
| **Arquivos principais** | `relatorios.css`, `index.html` (`?v=sprint244`) |
| **Resumo** | Header, sections, filtros, cards resumo/satisfação, tabelas, botões, empty |
| **Decisão** | Somente CSS; paginação 243 não alterada |
| **Testes** | Não executados (CSS) |
| **Smoke** | Filtros combinados + CSV tickets/satisfação |
| **Pendências** | Commit GitHub |
| **Riscos** | Cache CSS / JAR desatualizado |
| **Próximo passo** | Sprint 245 — Configurações e páginas restantes |

---

## Sprint 245 — Visual corporativo Configurações

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Configurações no padrão `--corp-*`; cadastros e fluxos intactos |
| **Backup** | `BKP_Sprint_245_Configuracoes_Visual` |
| **Arquivos principais** | `configuracoes.css`, `index.html` (`?v=sprint245`) |
| **Resumo** | Escopo `#page-configuracoes`: sections, forms, tables, botões, hints, empty |
| **Decisão** | Somente CSS; sem CRUD novo |
| **Testes** | Não executados (CSS) |
| **Smoke** | Seções Horário, Feriados, SLA, Conexões, Motivos, Etiquetas |
| **Pendências** | Commit GitHub |
| **Riscos** | Cache/JAR |
| **Próximo passo** | Sprint 246 — páginas restantes + menu |

---

## Sprint 246 — Telas complementares e menu

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Visual `--corp-*` em Atendentes, Auditoria, Abrir Ticket, Perfil; Perfil fora do menu lateral |
| **Backup** | `BKP_Sprint_246_Telas_Complementares_Menu` |
| **Arquivos principais** | `atendentes.css`, `auditoria.css`, `perfil.css`, `abrir-ticket.css`, `layout.css`, `index.html` |
| **Resumo** | Headers, sections, forms, tables, cards, botões; sidebar Perfil oculta; topbar mantém Meu Perfil |
| **Decisão** | Somente CSS + cache; sem feature nova |
| **Testes** | Não executados (CSS) |
| **Smoke** | Telas complementares + Perfil pela topbar |
| **Pendências** | Commit GitHub; JAR/cache para ver CSS |
| **Riscos** | Usuário acostumado ao atalho lateral Perfil |
| **Próximo passo** | Smoke geral / próximo item de produto fora do blueprint visual |

---

## Sprint 247 — Smoke geral visual corporativo

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar repaginação 235–246 no browser (consistência, tema, topbar, rotas) |
| **Tipo** | Documental + smoke (sem backend) |
| **Arquivos** | `docs/SPRINT_247_SMOKE_VISUAL_CORPORATIVO.md`; ajuste cache `index.html` (chats 240, components 243) |
| **Smoke** | Login, Dashboard, Clientes, Perfil via topbar; HTTP 200; tokens `--corp-*` no source |
| **Achado** | JAR em 8080 sem layout Sprint 246 (Perfil ainda na sidebar) até novo package |
| **Console** | Sem crítico na sessão |
| **Pendências** | `mvn package` + smoke manual claro/escuro em todas as telas |
| **Próximo passo** | Rebuild estáticos e fechamento visual com produto |

---

## Sprint 248 — Publicar projeto completo no GitHub Sistema

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Git remoto `Sistema`, projeto versionado, `.gitignore`, README clone/run, workflow E2E |
| **Backup** | N/A (git + docs; sem alteração de regra de negócio) |
| **Arquivos principais** | `.gitignore`, `README.md`, `docs/SPRINT_248_PUBLICAR_GITHUB_SISTEMA.md`, `e2e/README.md` (sem senha no doc) |
| **Resumo** | Preparação para clone em outra máquina; exclusão de `target/`, `node_modules/`, `.massa.json`, uploads |
| **Decisão** | Repositório canônico GitHub `robertaobolivia-sudo/Sistema`; credenciais só via env/local |
| **Testes** | `git status`, `git remote -v` |
| **Smoke** | HTTP 200 em 8080 se ambiente local com app ativa |
| **Pendências** | Push depende de autenticação Git na máquina do operador |
| **Riscos** | `application.properties` de dev com placeholder — trocar em cada ambiente |
| **Próximo passo** | `mvn package` + smoke visual manual; marcar REESTRUTURAÇÃO CONCLUIDA após produto |
