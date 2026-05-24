# Histórico mestre — Reestruturação Cliente / Contato / WhatsApp / Ticket

> **Fonte vigente:** [README_MESTRE.md](../README_MESTRE.md). Este arquivo é **log de sprints**, não norma de implementação.
>
> **REESTRUTURAÇÃO CONCLUÍDA** (F41). Runtime: Cliente → Contato → WhatsApp Matriz → Ticket.

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

---

## Sprint 249 — Limpeza global de cores legadas no corpo

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Substituir azul legado por tokens `--corp-*` em títulos, avatares, badges, cards, notificações, SLA, focos |
| **Backup** | N/A (somente CSS/HTML cache + default cor etiqueta) |
| **Arquivos principais** | `theme.css`, `layout.css`, `components.css`, `modals.css`, `perfil.css`, `atendentes.css`, `chats.css`, `index.html` |
| **Resumo** | `--primary` → teal; `--corp-info-soft`; logo SVG #0f2f3a; Perfil/atendentes/topbar avatares corporativos |
| **Testes** | Nenhum Maven/npm (1 linha JS default cor) |
| **Smoke** | Visual claro/escuro nas telas do blueprint |
| **Pendências** | Rebuild JAR para servir estáticos empacotados; commit/push GitHub |
| **Próximo passo** | Smoke manual completo + fechamento REESTRUTURAÇÃO CONCLUIDA |

---

## Sprint 250 — Smoke final visual + rebuild + GitHub

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | JAR com CSS 249; smoke claro/escuro; tentativa push Sistema |
| **Backup** | N/A (sem código de negócio) |
| **Build** | `mvn clean package -DskipTests` OK; app 8080 HTTP 200 |
| **Smoke** | 8 links sprint249; títulos/avatar/card sem azul legado; Perfil menu oculto |
| **GitHub** | Push falhou: `Repository not found` |
| **REESTRUTURAÇÃO CONCLUIDA** | Não marcada (aguarda produto) |
| **Próximo passo** | Criar repo + push; aprovação produto para marco final |

---

## Sprint 251 — Dark Corporate Premium (Dashboard piloto)

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Corpo escuro premium no Dashboard alinhado a login/sidebar |
| **Backup** | N/A (CSS + tokens dark) |
| **Arquivos** | `theme.css`, `dashboard.css`, `index.html` (cache) |
| **Resumo** | Fundo `#0F222D`, cards `#162E3C`, tipografia clara/muted, botão Atualizar verde, ícones KPI com fundos suaves |
| **Testes** | Smoke visual dark; filtros período/cliente preservados |
| **GitHub** | Não alterado (escopo sprint) |
| **Próximo passo** | Replicar padrão em outras páginas do blueprint |

---

## Sprint 252 — Ajuste fino visual + Clientes Listagem/Novo

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Botões menos neon; submenu Clientes; listagem 10/página; formulário dedicado |
| **Arquivos** | `theme.css`, `layout.css`, `chats.css`, `clientes.css`, `index.html`, `clientesPage.js`, `router.js`, `permissions.js`, `app.js`, testes Vitest |
| **Paginação** | Frontend, `CLIENTES_LIST_PAGE_SIZE = 10` |
| **Backend** | Inalterado |
| **Testes** | `npm test` (Vitest) |
| **Próximo passo** | Dark premium nas demais páginas; rebuild JAR para smoke empacotado |

---

## Sprint 253 — Clientes: campos reais + reset DEV

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Colunas reais no Cliente; parar de gravar IE/CEP/Site/Horário em observações; massa DEV limpa (4 clientes × 3 tickets) |
| **Backup** | `C:\Users\João Falcone\Desktop\Sistemas_BKP\BKP_Sprint_253_Campos_Reais_Reset_Dev` |
| **Arquivos principais** | `Cliente.java`, DTOs, `ClienteService`, `ClienteCamposLegadoSupport`, `Sprint253DevMassaSeedConfig`, `clientesPage.js`, `application.properties` |
| **Campos** | razao_social, responsavel, whatsapp, inscricao_estadual, cep, site, horario_funcionamento (+ sync legado empresa/telefone/nome) |
| **Reset DEV** | `app.sprint253.dev-reset=true` — limpa operacional; preserva analistas/catálogos; seed Rocha Mendes, Status Automação, FastComércio, Fênix |
| **Testes** | `mvn clean install`; `npm test` |
| **Smoke** | Login ADMIN; Clientes (4); formulário; Tickets/Chats/Relatórios |
| **Pendências** | Desligar `app.sprint253.dev-reset` após massa aplicada; commit opcional |
| **Riscos** | Flag true reapaga dados operacionais a cada subida |
| **Próximo passo** | `app.sprint253.dev-reset=false`; validar cadastro manual; dark premium outras páginas |

---

## Sprint 254 — Clientes B2B, Contatos e largura total

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Submenu Cadastro/Listagem/Contatos; contatos via entidade Contato; telas em largura total |
| **Backend** | `GET /api/contatos?gestao=true` + `ContatoGestaoResponseDTO` (etiquetas, chamados) |
| **Front** | `contatoService.js`, `clienteContatosGestaoView.js`, tabelas amplas, resumo no cadastro |
| **Testes** | `mvn` (se API); Vitest + `clienteContatosGestaoView.test.js` |
| **Próximo passo** | Smoke manual Contatos filtro; edição de contato na tela Contatos (futuro) |

---

## Sprint 255 — Smoke Clientes B2B e Contatos

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar Listagem / Cadastro / Contatos (254) e vínculo Ticket+Contato |
| **Evidência** | `docs/SPRINT_255_SMOKE_CLIENTES_B2B_CONTATOS.md` |
| **HTTP 200** | OK |
| **API** | 4 LTDA × 3 contatos; 12 tickets com `contatoId`; filtro e busca OK |
| **Ressalva** | 8 clientes no banco (4 duplicados legados + 4 massa 253) |
| **Código** | Não alterado |
| **Próximo passo** | Limpeza massa dev; CRUD Contatos em sprint futura |

---

## Sprint 256 — Limpeza duplicados Clientes DEV

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Manter só 4 contratantes LTDA; remover legados 91–94 |
| **Estratégia** | Migrar tickets/contatos → oficial; excluir legado |
| **Totais** | 4 clientes, 12 contatos, 12 tickets |
| **Arquivos** | `Sprint256DevClientesDedupConfig`, `MassaOficialClientesDevConstants`, `ClienteRepository.findByCnpj`, ajuste seed 253 |
| **Próximo passo** | CRUD Contatos; smoke visual Listagem/Contatos |

---

## Sprint 257 — Corrigir login sem entrar e sem erro

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Restaurar fluxo de login no browser (Entrar → app + erros visíveis) |
| **Causa** | `clientesPage.js` exportava `CLIENTES_CONTATOS_LIST_PAGE_SIZE` duas vezes → `app.js` (módulo) não carregava; submit sem listener |
| **Backup** | `Sistemas_BKP\BKP_Sprint_257_Login_Browser_Bloqueado` |
| **Arquivos** | `js/pages/clientesPage.js`, `app.js`, `index.html` (`app.js?v=sprint257`) |
| **Correção** | Remover reexport duplicado; validação e mensagem amigável no submit; limpar sessão em falha de login |
| **API** | `POST /api/analistas/login` OK (ADMIN, token) — sem mudança de regra |
| **Testes** | `npm test` 153 OK; `mvn clean install` OK |
| **HTTP 200** | OK em `http://127.0.0.1:8080/` |
| **Smoke browser** | Glass: automação não preenche senha; após fix, módulo deve carregar — validar manualmente Entrar + credenciais e-mail/senha |
| **Pendências** | Smoke manual login inválido (mensagem em `#loginAlert`); confirmar restore de sessão na carga |
| **Próximo passo** | Smoke manual ADMIN; seguir CRUD Contatos / visual Clientes |

---

## Sprint 258 — Smoke login real + massa Clientes B2B

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar login 257 e massa nas telas principais (browser real) |
| **Código** | Não alterado |
| **Login** | Válido e inválido OK (Playwright); API OK |
| **Massa API** | 12 tickets, 12 contatos; **8 clientes** (ressalva: dedup 256 não aplicado no DB) |
| **UI** | Dashboard, Cadastro, Contatos, Tickets, Chats OK; Listagem 8+ linhas |
| **HTTP 200** | OK |
| **Doc** | `docs/SPRINT_258_SMOKE_LOGIN_MASSA_CLIENTES_B2B.md` |
| **Próximo passo** | Rodar dedup 256; smoke manual Ctrl+F5; filtro Contatos por cliente |

---

## Sprint 259 — Aplicar dedup Clientes DEV no banco atual

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Executar dedup Sprint 256 no MySQL em uso; massa 4 LTDA / 12 contatos / 12 tickets |
| **Backup** | `Sistemas_BKP\BKP_Sprint_259_Dedup_Clientes_DEV` (properties) |
| **Flag** | `app.sprint256.dedup-clientes-dev=true` na 1ª subida; depois `false` |
| **Dedup** | Legados 95–98 → oficiais 87–90; remoção E2E (clientes 99–101, tickets 179–181) |
| **Ajuste código** | `Sprint256DevClientesDedupConfig` — canonical por razão, purge E2E, delete ticket seguro |
| **Totais finais** | clientes=4, contatos=12, tickets=12 (log + API) |
| **Testes** | `mvn clean install -DskipTests` OK (sem alteração JS) |
| **Smoke API** | Login ADMIN; `/api/clientes` 4; contatos gestão 12; tickets 12; HTTP 200 |
| **Smoke browser** | Glass: preenchimento de senha instável; validar manualmente Listagem 4 linhas + filtro Contatos |
| **Pendências** | Evitar E2E Playwright contra DB DEV sem isolamento; smoke UI manual pós-Ctrl+F5 |
| **Riscos** | Reativar flag dedup em produção; E2E recriar ruído se rodar contra mesmo banco |
| **Próximo passo** | Smoke manual Clientes/Contatos; CRUD Contatos; desligar seeds DEV se ainda ativos |

---

## Sprint 260 — Estabilizar massa DEV Clientes B2B

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Manter 4 LTDA na Listagem; impedir retorno a 8 clientes após reinício |
| **Causa** | `Sprint94AnalistasSeedConfig` (Order 100) recriava 4 clientes “conexão” (`FastComércio`, `Fênix`, etc.) via `findByNome`, distinto da massa S253 |
| **Correção** | Seed 94 só atualiza analista ADMIN; `DevClientesMassaSanitizer` + `Sprint260DevClientesMassaGuardConfig` (Order 255, `app.dev.clientes-massa-guard=true`) |
| **Backup** | `Sistemas_BKP\BKP_Sprint_260_Estabilizar_Massa_Clientes` |
| **Totais** | 4 clientes, 12 contatos, 12 tickets (API); estável após 2 reinícios |
| **Testes** | `mvn clean install -DskipTests` OK |
| **HTTP 200** | OK |
| **Riscos** | Guard apaga qualquer cliente fora dos 4 oficiais no DEV; E2E no mesmo MySQL |
| **Próximo passo** | Smoke manual Listagem/Contatos; CRUD Contatos |

---

## Sprint 261 — Contatos: edição básica e etiquetas

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Clientes → Contatos: Ver/Editar dados básicos e etiquetas; WhatsApp imutável |
| **API** | Reuso `PUT /api/contatos/{id}`, `GET /api/contatos/{id}`, `GET/PUT /api/contatos/{id}/etiquetas` |
| **Front** | Modal `modalContatoGestaoEdit`, `contatoGestaoEditModal.js`, `contatoService.buscarPorId/atualizar` |
| **Campos editáveis** | nome, e-mail, empresa/local, cidade, UF, observações, etiquetas |
| **Imutável** | WhatsApp (readonly + regra backend existente) |
| **Testes** | Vitest `contatoGestaoEditView.test.js`; `npm test` |
| **Próximo passo** | Smoke manual Fênix/Rocha; ativar/inativar contato na gestão (futuro) |

---

## Sprint 262 — Contatos: histórico de tickets

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Ação Histórico na listagem com chamados do contato |
| **API** | `GET /api/contatos/{id}/historico-tickets` → `ContatoTicketHistoricoItemDTO` |
| **UI** | Painel expansível abaixo da linha (um contato por vez); fecha ao trocar filtro/página |
| **Campos exibidos** | protocolo, abertura, categoria, subcategoria, motivo, status, encerramento, pesquisa/nota |
| **Testes** | Vitest; `mvn compile` |
| **Próximo passo** | Smoke manual Fênix/Rocha; link opcional para detalhe do ticket (futuro) |

---

## Sprint 263 — Contatos: Ver conversa pelo histórico

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Ação **Ver conversa** em cada linha do histórico do Contato |
| **Ativo** | `scheduleOpenChatsConversation` + aba Fila/Chats + `selectConversation` em Chats |
| **Encerrado** | Aba Histórico em Chats; composer somente leitura (`isChatsConversationReadOnly`) |
| **Front** | `contatoGestaoHistoricoView.js` (coluna + botão), `chatsView.resolveChatsTabForTicketStatus`, `chatsPage.scheduleOpenChatsConversation`, `clientesPage` + `app.js` |
| **Regras** | Sem resposta pelo histórico; sem criar ticket; sem alterar ticket ativo/WhatsApp |
| **Testes** | Vitest `chatsView` + `contatoGestaoHistoricoView`; `npm test` |
| **Próximo passo** | Smoke manual ativo vs encerrado; validar isolamento ao trocar Contato/Cliente |

---

## Sprint 264 — Microajuste sidebar: centralizar ícones

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Centralizar ícones na sidebar compacta (70px) |
| **CSS** | `layout.css` — `:not(:hover)` em itens principais/parent: `justify-content: center`, `gap: 0`, padding horizontal simétrico |
| **Preservado** | Largura 70px/250px hover; `box-shadow: inset 3px 0 0` no `.nav-item.active` (sem mover indicador) |
| **Testes** | Smoke visual; sem JS/Java |
| **Próximo passo** | Ctrl+F5; validar Clientes/Indicadores expandidos no hover |

---

## Sprint 265 — Contatos: filtros inteligentes e avançados

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Filtros principais visíveis + painel recolhível de filtros avançados |
| **API** | `GET /api/contatos?gestao=true` + `etiquetaId`, `cidade`, `uf`, `comTicketsAbertos`, `comAvaliacaoRuim`, `semEtiqueta`; `busca` restrita a nome/WhatsApp |
| **Front** | `contatoGestaoFiltrosView.js`, `clientesPage.js`, `index.html`, `clientes.css` |
| **Regras** | `clienteId` isola contratante; paginação página 1 ao filtrar; Histórico/Ver conversa preservados |
| **Avaliação ruim** | Ticket com `nota <= 2` respondida |
| **Testes** | Vitest; `mvn` se API alterada |
| **Próximo passo** | Smoke combinações de filtros; reiniciar app se JAR antigo |

---

## Sprint 266 — Smoke filtros avançados Contatos

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar filtros Sprint 265 (API + massa mínima) |
| **Doc** | `docs/SPRINT_266_SMOKE_CONTATOS_FILTROS.md` |
| **Massa** | Contato 63: etiqueta 1, Campinas/SP, satisfação nota 2 (flag `app.sprint266.smoke-massa` ou seed API) |
| **Achado** | JAR antigo em 8080 ignorava filtros avançados; resolvido com restart do fat JAR |
| **Bugs código** | Nenhum no filtro após restart |
| **Próximo passo** | Smoke browser manual; desligar flag smoke-massa na subida habitual |

---

## Sprint 267 — Contatos: ativar, inativar e marcações operacionais

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Ativar/inativar na gestão sem excluir; visual claro; base etiquetas operacionais |
| **Campo** | `Contato.ativo` (já existente); API `PATCH /api/contatos/{id}/ativar` e `/inativar` |
| **UI** | Coluna Status, botões Ativar/Inativar, linha `is-inativo`, hint etiquetas Indevido/Pessoal/Propaganda |
| **Preservado** | Histórico, Ver conversa, filtros, tickets, etiquetas; sem delete físico |
| **Testes** | Vitest status + colspan histórico |
| **Próximo passo** | Smoke inativar/reativar + histórico/chats |

---

## Sprint 268 — Chats: corrigir tela sem carregar informações

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Diagnosticar e corrigir Chats vazio ao abrir pelo menu |
| **Backup** | `BKP_Sprint_268_Chats_Sem_Carregar` |
| **Arquivos principais** | `chatsService.js`, `chatsPage.js`, `router.js`, `index.html` (`app.js?v=sprint268`), `chatsService.test.js` |
| **Diagnóstico** | API OK (12 tickets: 4 ABERTO, 4 EM_ATENDIMENTO, 4 RESOLVIDO); Playwright Chats OK no JAR atual; causa provável: sessão/cache ou lista não-array / erro em um card derrubando render |
| **Resumo** | `coerceTicketsList`; render por item com try/catch; `syncChatsPageIdleFromTickets`; router trata Promise rejeitada dos loaders |
| **Endpoints** | `GET /api/tickets`, `GET /api/etiquetas/ativas`, `GET /api/chats/interacoes-pendentes` |
| **Testes** | Vitest 169; Playwright `chats-encerramento-sem-pesquisa`; `tools/sprint268-chats-api.ps1` |
| **Smoke** | Fila / Atendendo / Encerrados com contagem e cards (massa 12 tickets) |
| **HTTP 200** | `http://localhost:8080/` |
| **Pendências** | Hard refresh se cache antigo; reiniciar JAR após alteração Java |
| **Riscos** | Item de ticket corrompido omitido da lista (log silencioso) |
| **Próximo passo** | Smoke manual pós-deploy; validar “Ver conversa” → Chats com massa real |

---

## Sprint 269 — Páginas abrindo abaixo / scroll global

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Páginas abrem no topo visível; sem espaço vazio acima do conteúdo |
| **Backup** | `BKP_Sprint_269_Scroll_Topo_Paginas` |
| **Causa** | `scrollTop` do `.main-content` mantido entre navegações; Chats forçava `height: 100vh` na coluna ao lado da sidebar |
| **Arquivos** | `router.js`, `layout.css`, `chats.css`, `index.html` (`app.js?v=sprint269`), `routerScroll.test.js` |
| **Correção** | `resetMainContentScroll()` em `showPage`; shell `#appScreen` com altura viewport e overflow controlado; `main-content` com `min-height: 0`; removido `100vh` fixo do Chats no main |
| **Testes** | Vitest 170; smoke visual menu |
| **HTTP 200** | `http://localhost:8080/` |
| **Próximo passo** | Smoke mobile/tablet se houver queixa de scroll |

---

## Sprint 270 — Smoke visual pós-correção de scroll

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar rotas principais/submenus no topo após Sprint 269; corrigir só bug visual direto |
| **Backup** | `BKP_Sprint_270_Smoke_Scroll_Topo` |
| **Causa raiz** | `</div>` extra no fim de `#page-clientes` quebrava o DOM: páginas de Abrir Ticket em diante ficavam fora de `.main-content` → `display: none` em `.page` sem override de filho direto |
| **Arquivos** | `index.html` (remove `</div>` órfão; `app.js?v=sprint270`), `layout.css` (regras `.main-content > .page` ativa/inativa sprint 270), `e2e/tests/sprint270-scroll-topo.spec.ts` |
| **Resumo** | Todas as seções `.page` voltam como filhas de `main`; scroll reset da 269 efetivo; smoke E2E de rotas + submenus Clientes/Indicadores + tema + Chats com lista |
| **Testes** | Playwright `sprint270-scroll-topo.spec.ts`; sem alteração Java; npm test JS não exigido (HTML/CSS/E2E) |
| **Smoke** | Ciclo Dashboard → Tickets → Chats → Clientes → Relatórios → Atendentes → Abrir Ticket → Auditoria → Configurações; submenu Contatos; Indicadores Chamados; Perfil; tema claro/escuro |
| **HTTP 200** | `http://localhost:8080/` |
| **Pendências** | Smoke manual completo no Glass se desejado; validar mobile |
| **Riscos** | Cache `index.html` antigo no browser — hard refresh após deploy |
| **Próximo passo** | Retomar backlog funcional; monitorar Chats após correção estrutural do shell |

---

## Sprint 271 — Contatos: etiquetas operacionais e indevidos

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Preparar uso operacional de Indevido, Contato Pessoal e Propaganda na gestão de Contatos (sem automação de tickets) |
| **Backup** | `BKP_Sprint_271_Etiquetas_Operacionais_Contatos` |
| **Backend** | `EtiquetaOperacionalCatalog`, `Sprint271EtiquetasOperacionaisSeedConfig` (seed idempotente), `ContatoGestaoResponseDTO.temEtiquetaOperacional`, `ContatoService` |
| **Front** | `clienteContatosGestaoView.js`, `contatoGestaoEditView.js`, `contatoGestaoEditModal.js`, `clientesPage.js`, `clientes.css`, `index.html` (`app.js?v=sprint271`) |
| **UI** | Linha destacada + chips operacionais na lista; aviso no modal Ver/Editar; hint se faltar etiqueta no catálogo; filtro avançado com “(operacional)”; orientação em Configurações → Etiquetas |
| **Regras** | Sem invalidar ticket; sem excluir contato; WhatsApp e ticket ativo inalterados; Histórico e Ver conversa preservados |
| **Testes** | `EtiquetaOperacionalCatalogTest`; Vitest `clienteContatosGestaoView.test.js`; `mvn clean install` OK |
| **Smoke** | Marcar etiqueta operacional → lista + modal + filtro por etiqueta; Histórico/Ver conversa (manual ou fluxo existente) |
| **HTTP 200** | Subir JAR após build |
| **Pendências** | Regra automática de invalidação de ticket em sprint futura |
| **Próximo passo** | Smoke browser na view Contatos; validar seed em ambiente sem as três etiquetas |

---

## Sprint 272 — Smoke etiquetas operacionais em Contatos

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar uso real das etiquetas operacionais (catálogo, marcação, destaque, aviso, filtro, Histórico, Ver conversa) sem regra automática em tickets |
| **Backup** | `BKP_Sprint_272_Smoke_Etiquetas_Operacionais` (recomendado; smoke + correções pontuais) |
| **Arquivos** | `e2e/tests/sprint272-etiquetas-operacionais-contatos.spec.ts`, `contatoGestaoHistoricoView.js`, `clientesPage.js`, `docs/SPRINT_272_SMOKE_ETIQUETAS_OPERACIONAIS_CONTATOS.md` |
| **Resumo** | Playwright cobre catálogo, Propaganda + Indevido em dois contatos, lista destacada, modal com aviso, filtro UI/API, Histórico + Ver conversa, tickets inalterados via API |
| **Correções** | Colspan histórico 10 colunas; painel Histórico não some ao `loadClientesContatosPage`; E2E limpa filtro “avaliação ruim” que zerava a tabela |
| **Testes** | Playwright sprint272 **passed**; app `java -jar target/suporte-tickets-1.0.0.jar` |
| **Smoke** | Automatizado E2E; manual opcional em `docs/SPRINT_272_...md` |
| **HTTP 200** | `http://127.0.0.1:8080/` |
| **Pendências** | UX: checkbox “Com avaliação ruim” pode permanecer marcado entre sessões — analista deve usar **Limpar avançados** |
| **Riscos** | Massa E2E sobrescreve `.massa.json`; muitos contatos E2E na lista |
| **Próximo passo** | Sprint de **regra automática** para tickets indevidos (somente após validação de produto) |

---

## Sprint 273 — Requisito: regra de tickets indevidos

| Campo | Conteúdo |
|-------|----------|
| **Tipo** | Sprint **documental** (sem alteração de código funcional) |
| **Objetivo** | Definir regra de produto para tickets indevidos a partir de etiquetas operacionais do Contato |
| **Entrega** | `docs/REGRA_TICKETS_INDEVIDOS_ETIQUETAS_OPERACIONAIS.md`; link no §9 do modelo oficial |
| **Decisões** | Sem automação imediata ao aplicar etiqueta; **confirmação do analista** para classificar ticket; fora de Fila/Atendendo; **não** conta atendimento/SLA/avaliação; relatórios excluem por padrão; motivo de encerramento Indevido; Propaganda impede ticket ativo automático, não bloqueia cadastro; regra global F5 fase 1 |
| **Status alvo** | `INDEVIDO` terminal (ou `CANCELADO` + flag operacional) + grupo visual Chats “Não atendimento” |
| **Testes** | Nenhum Maven/npm (aplicação não alterada) |
| **Smoke** | N/A documental |
| **HTTP 200** | Não exigido para entrega doc; manter app em 8080 se já rodando |
| **Próximo passo** | Sprint **274** backend (status/flag + motivos + API confirmação) |

---

## Sprint 274 — Backend: classificação de ticket indevido

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Base backend para classificar ticket como indevido com confirmação do analista |
| **Estratégia** | Status terminal **`INDEVIDO`** (fora de `STATUS_ATIVOS`) + campos `classificacaoOperacional`, `classificadoOperacionalEm`, `classificadoOperacionalPorAnalistaId`, `comentarioClassificacaoOperacional` |
| **API** | `PUT /api/tickets/{numeroTicket}/classificar-indevido` — body `confirmacao`, `motivoOperacional` (INDEVIDO, CONTATO_PESSOAL, PROPAGANDA), `comentario` opcional; grupo/sub/motivo encerramento opcionais |
| **Regras** | Só ticket **ativo**; `confirmacao=true` obrigatório; sessão analista; **não** altera etiquetas do Contato; PUT `/status` com INDEVIDO bloqueado |
| **Auditoria / timeline** | `TICKET_CLASSIFICAR_INDEVIDO` + interação tipo ENCERRAMENTO na timeline |
| **SLA / avaliação** | Finaliza pausa SLA; avaliação pendente → `NAO_ENVIADA`; não chama `registrarDecisaoPosEncerramento` |
| **Arquivos** | `TicketStatus`, `Ticket`, `TicketIndevidoService`, `TicketController`, `TicketAtivoService`, `TicketSatisfacaoService`, testes `TicketIndevidoClassificacaoTest`, etc. |
| **Testes** | `mvn test` OK (151 testes) |
| **HTTP 200** | Subir JAR após `mvn package` |
| **Próximo passo** | Sprint **275** — Chats/Contatos: confirmação UX e fila “Não atendimento” |

---

## Sprint 276 — Front: classificar ticket indevido

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Ação controlada no Chats para `PUT /api/tickets/{numero}/classificar-indevido` (Sprint 274) |
| **Local da ação** | Painel direito Chats → bloco Chamado: botão **Classificar indevido** (ticket ativo); modal com motivo + checkbox de confirmação |
| **Aba** | **Não atendimento** (`data-chats-tab=indevidos`) — tickets `INDEVIDO` fora de Fila/Atendendo |
| **Arquivos** | `classificarIndevidoModal.js`, `classificarIndevidoView.js`, `ticketService.classificarTicketIndevido`, `chatsView.js`, `chatsPage.js`, `index.html`, `chats.css`, `ticketViewRules.js` |
| **Comportamento** | Após sucesso: lista atualizada, aba Não atendimento, composer somente leitura; etiquetas do contato não disparam classificação |
| **Testes** | Vitest (`classificarIndevidoView`, `chatsView`, `ticketViewRules`); `npm test` |
| **HTTP 200** | Sem alteração Java |
| **Próximo passo** | Indicadores/relatórios excluir INDEVIDO por padrão; opcional atalho em Tickets |
| **Riscos** | Front ainda lista tickets INDEVIDO em `GET /api/tickets` até filtro na 275; Chats sem UI de classificação nesta sprint |

---

## Sprint 277 — INDEVIDOS: filtro gerencial (Dashboard, Indicadores, Relatórios, Tickets)

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Separar tickets `INDEVIDO` (Não atendimento) das métricas operacionais válidas |
| **Backend** | `TicketAtivoService` helpers; `DashboardService` card `ticketsNaoAtendimento`, sem analista só ativos, médias/gerencial/SLA sem indevido; `IndicadoresChamadosService` `totalNaoAtendimento`; encerramento/avaliação excluem INDEVIDO nas queries; `TicketBuscaService` exclui INDEVIDO por padrão; CSV coluna classificação operacional + status legível |
| **Front** | Card Dashboard e Indicadores “Não atendimento”; filtros Tickets/Relatórios `INDEVIDO`; lista Tickets default sem indevido; `indicadoresGerencialView.js` |
| **Testes** | `mvn test`; `npm test` (+ `indicadoresGerencialView.test.js`) |
| **Smoke** | Classificar ticket → Dashboard/Indicadores/Relatório CSV/Tickets |
| **Próximo passo** | Sprint 277 mensagem+etiqueta ou satisfação resumo global |
| **Riscos** | `GET /api/tickets` ainda traz INDEVIDO (Chats precisa); KPI legado pode contar cancelado+indevido em algum gráfico |

---

## Sprint 278 — Smoke INDEVIDO gerencial (pós-restart)

| Campo | Conteúdo |
|-------|----------|
| **Tipo** | Smoke / documental de evidência |
| **Ticket** | TK-000176 → `INDEVIDO` / `PROPAGANDA` via API |
| **Evidência** | `docs/SPRINT_278_SMOKE_INDEVIDO_GERENCIAL.md` |
| **Resultado** | Dashboard `ticketsNaoAtendimento=1`; busca default sem indevido; filtro INDEVIDO OK; CSV com classificação; Indicadores `totalNaoAtendimento=1`; app reiniciada com JAR novo |
| **Código** | Não alterado |
| **HTTP 200** | OK |

---

## Sprint 279 — Dashboard: blueprint operacional em tempo real

| Campo | Conteúdo |
|-------|----------|
| **Tipo** | Sprint documental |
| **Entrega** | `docs/DASHBOARD_OPERACIONAL_BLUEPRINT.md` |
| **Regra** | Dashboard ≠ consulta histórica; Indicadores/Relatórios concentram período e filtros analíticos |
| **Layout alvo** | Início **Operação Agora** (Fila + Atendimento); removido hero Devidos/Indevidos; 6 blocos + submenu Visão geral / Acompanhamento (read-only) |
| **Roadmap** | Sprints 280–285 planejadas no blueprint |
| **Código** | Não alterado |
| **HTTP 200** | App em 8080 OK |

---

## Sprint 280 — Dashboard: bloco Operação Agora

| Campo | Conteúdo |
|-------|----------|
| **API** | `GET /api/dashboard/operacao-agora` — `DashboardOperacaoAgoraService` |
| **Regra** | Em Atendimento = `EM_ATENDIMENTO` + TMA médio (desde 1º atendimento ou abertura); Em Fila = `ABERTO` + TME médio (desde abertura), alinhado aba Fila Chats |
| **UI** | Hero **Operação Agora**; cards legados ocultos (`dashboard-legacy-cards`); dark premium `dashboard.css` sprint280 |
| **Arquivos** | `DashboardOperacaoAgoraService`, DTOs, `DashboardController`, `dashboardPage.js`, `dashboardOperacaoAgoraView.js`, `index.html` |
| **Testes** | `DashboardOperacaoAgoraServiceTest`, Vitest `dashboardOperacaoAgoraView.test.js`; `mvn test`; `npm test` |
| **Próximo passo** | Sprint 281 — Analistas Online |

---

## Sprint 281 — Dashboard: Analistas Online

| Campo | Conteúdo |
|-------|----------|
| **API** | `GET /api/dashboard/analistas-online` — `DashboardAnalistasOnlineService` |
| **Presença** | AUSENTE manual; OCUPADO = sessão válida ou ONLINE + ticket `EM_ATENDIMENTO`; ONLINE = sessão (`authToken` não expirado) ou status ONLINE; senão OFFLINE |
| **UI** | Kanban 4 colunas abaixo Operação Agora; avatar, nome, cargo, quadrado de cor por status |
| **Testes** | `DashboardAnalistasOnlineServiceTest`, Vitest `dashboardAnalistasOnlineView.test.js` |
| **Próximo passo** | Sprint 282 — Operação por Cliente B2B |

---

## Sprint 282 — Dashboard: Operação por Cliente B2B

| Campo | Conteúdo |
|-------|----------|
| **API** | `GET /api/dashboard/operacao-cliente-b2b` |
| **Chamados** | `STATUS_ATIVOS` (sem INDEVIDO) + pendências `PENDENCIA_DECISAO` por cliente ativo |
| **TME/TMA** | TME = agora − abertura (ou − `criadaEm` pendência); TMA = agora − 1º atendimento só em `EM_ATENDIMENTO` |
| **UI** | Card por cliente ativo; tabela protocolo/contato/status/analista/TME/TMA; botão Acompanhar (prep Sprint 283) |
| **Próximo passo** | Sprint 283 — subpágina Acompanhamento read-only |

---

## Sprint 283 — Dashboard: Acompanhamento somente leitura

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Subpágina Dashboard → Acompanhamento para supervisão de ticket/conversa sem ações operacionais |
| **Backup** | `BKP_Sprint_283_Dashboard_Acompanhamento` (recomendado antes do deploy) |
| **Arquivos principais** | `index.html`, `app.js`, `router.js`, `permissions.js`, `dashboard.css`, `dashboardPage.js`, `dashboardAcompanhamentoPage.js`, `dashboardAcompanhamentoView.js`, Vitest |
| **UI** | Submenu Dashboard: Visão geral + Acompanhamento; resumo protocolo/cliente/contato/status/analista/TME/TMA; timeline read-only (mensagens + eventos) |
| **Navegação** | B2B **Acompanhar** → `openDashboardAcompanhamento(TK-…)`; pendência → toast orientando Chats; submenu Acompanhamento sem ticket → empty state |
| **Dados** | Reuso `getTicketByNumero`, `listInteracoes`, satisfação; `buildChatsTimelineEntries` |
| **Escopo** | Sem alteração Chats, backend ou regra de ticket ativo |
| **Testes** | `npm test` (Vitest `dashboardAcompanhamentoView.test.js`); smoke: Acompanhar ABERTO/EM_ATENDIMENTO; submenu vazio; sem composer |
| **Próximo passo** | Sprint 284 — SLA + encerramentos do dia na visão geral (blueprint) |

---

## Sprint 284 — Dashboard: SLA vivo e encerramentos do dia

| Campo | Conteúdo |
|-------|----------|
| **API** | `GET /api/dashboard/sla` (campo `vivo`); `GET /api/dashboard/encerramentos-dia` |
| **SLA vivo** | Por ticket ativo: dentro / próximo / vencido (pior entre 1º atend. e resolução); mais crítico = 1º da lista crítica; exclui `INDEVIDO` |
| **Encerramentos dia** | Calendário `America/Sao_Paulo`: finalizados (`RESOLVIDO`), não resolvidos (`CANCELADO`), escalonados (flag no encerramento), abandonados (encerrado hoje sem 1º atendimento); top 5 motivos |
| **UI** | Blocos abaixo B2B; cards legados/analíticos em `dashboard-legacy-analitico` ocultos |
| **Testes** | `mvn test` (unit encerramentos); `npm test` (`dashboardOperacionalView.test.js`) |
| **Próximo passo** | Sprint 285 — avaliação em tempo real + limpeza cards legados |

---

## Sprint 285 — Dashboard: avaliação em tempo real e limpeza da Visão Geral

| Campo | Conteúdo |
|-------|----------|
| **API** | `GET /api/dashboard/avaliacao-tempo-real` |
| **Regra** | Pesquisas de tickets com status ≠ `INDEVIDO`; média = notas 1–5 (RESPONDIDA / REGISTRADA_MANUALMENTE); ruins = nota ≤ 2; contagens por status PENDENTE / RESPONDIDA+manual / EXPIRADA |
| **UI** | Bloco após Encerramentos do dia; hint para Indicadores; markup analítico legado removido do `index.html` |
| **Visão geral final** | Operação Agora → Analistas → B2B → SLA → Encerramentos → Avaliação |
| **Testes** | `mvn test`; `npm test` |
| **Próximo passo** | Smoke contínuo; evoluções fora do blueprint operacional → Indicadores |

---

## Sprint 286 — Dashboard: Status Operador + topbar

| Campo | Conteúdo |
|-------|----------|
| **UI** | Bloco **Status Operador** — grid único (máx. 2 fileiras), cards com dot à esquerda do status |
| **Topbar** | Menu avatar → Online / Ocupado / Ausente / Offline → `PUT /api/analistas/{id}/status` |
| **Backend** | `StatusOperador.OCUPADO`; exibição manual (sem derivar por ticket/sessão); DTO `operadores` ordenado |
| **Encoding** | Correção em massa `index.html` (mojibake) + ícones ⚙️/🔴 no Dashboard |
| **Testes** | `mvn test`; `npm test` |

### Sprint 286b — Menu Status no avatar (visual)

| Campo | Conteúdo |
|-------|----------|
| **UI** | Item **Status** com submenu; quadrados coloridos; ✓ no status atual; ícone 🔔 corrigido na topbar |
| **Arquivos** | `topbar.js`, `topbarStatusMenuView.js`, `layout.css`, `index.html` |

---

## Sprint 275 — Topbar: botão personalizado chat.png

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Atalho na topbar para Chats com ícone `chat.png` |
| **Arquivo** | `src/main/resources/static/assets/icons/chat.png` (cópia do ícone em `assets/ico/`) |
| **UI** | Botão 40×40px à esquerda do sino; imagem 24×24 `object-fit: contain`; `index.html`, `layout.css`, `topbar.js` |
| **Navegação** | Clique → `showPage('chats')`; sidebar marca Chats ativo (router existente); fecha menu perfil e painel notificações |
| **Escopo** | Sem alteração backend nem layout interno de Chats |
| **Testes** | `npm test` 172 OK (sem novo spec — topbar usa DOM no load); smoke manual login → botão → Chats → Dashboard → Chats; tema claro/escuro |
| **HTTP 200** | App em 8080; hard refresh `app.js?v=sprint275` se cache antigo |
| **Próximo passo** | Sprint 275 UX Chats indevidos ou indicador ativo no botão quando houver mensagens |
| **Riscos** | Tickets ativos com etiqueta sem encerramento; alinhar enum vs flag na 274 |

---

## Sprint 287 — Avatar persistente do operador

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Foto do operador estável entre navegação, tema, refresh e mesma origem em topbar/Perfil/Dashboard |
| **Backup** | `BKP_Sprint_287_Avatar_Persistente` |
| **Arquivos principais** | `js/core/analystAvatar.js`, `js/core/state.js`, `app.js`, `presentation.js`, `topbar.js`, `perfilPage.js`, `dashboardAnalistasOnlineView.js`, `js/tests/analystAvatar.test.js`, `index.html` |
| **Resumo** | Módulo único `getAnalystPhotoUrl` (`fotoUrl` / `fotoPerfilUrl`); merge de sessão preserva foto quando API omite campo; `applyAnalystAvatarToElement` evita re-render que troca foto por iniciais; cache-bust na URL; troca de status usa `setLoggedAnalyst` |
| **Decisão** | Backend continua `fotoUrl`; alias `fotoPerfilUrl` só no front; sem mudança de layout |
| **Testes** | `npm test` (incl. `analystAvatar.test.js`) |
| **Smoke** | Login → topbar → Dashboard/Chats/Clientes/Perfil → tema → Ctrl+F5 |
| **Pendências** | Nenhuma crítica |
| **Riscos** | Cache agressivo de CDN/proxy em `/uploads/` (mitigado com `?v=`) |
| **Próximo passo** | Smoke operacional ou sprint UX seguinte da reestruturação |

---

## Sprint 288 — Backend: múltiplos telefones por Contato

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Base backend para telefones adicionais no mesmo Contato (histórico unificado por `contato_id`) |
| **Entidade** | `ContatoTelefone` → `contato_telefones` |
| **Unicidade** | `(cliente_id, telefone_normalizado)` e `(contato_id, telefone_normalizado)` |
| **Resolução** | `buscarEntidadePorClienteETelefone` + `criarSeNaoExistir` / integração WhatsApp |
| **API** | `GET/POST /api/contatos/{id}/telefones-adicionais` |
| **Testes** | `ContatoServiceTest`, `ContatoTelefoneServiceTest`, `IntegracaoMensagemTelefoneAdicionalTest` |
| **Próximo passo** | UI cadastro de telefones adicionais no Contato |

---

## Sprint 289 — Front: telefones adicionais do Contato

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Listar e incluir telefones adicionais em Clientes → Contatos (modal Ver/Editar) |
| **Arquivos** | `contatoTelefoneService.js`, `contatoGestaoTelefonesView.js`, `contatoGestaoEditModal.js`, `index.html`, `clientes.css`, `contatoGestaoTelefonesView.test.js` |
| **UI** | Seção “Telefones do contato”: WhatsApp principal readonly; lista GET; formulário POST com origem opcional |
| **Validação** | Erros do backend exibidos no alerta da seção (duplicidade, igual ao principal) |
| **Testes** | `npm test` (+ `contatoGestaoTelefonesView.test.js`); backend inalterado |
| **Próximo passo** | Exibir telefones adicionais no Chats/histórico (leitura) ou remoção de adicional (sprint futura) |

---

## Sprint 290 — Smoke telefones + HTTP 200

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Restabelecer HTTP 200 e validar fluxo 288+289 |
| **Causa 500** | Instância Java antiga inválida; reinício exige JAR após `mvn package` |
| **Correção código** | Nenhuma (sprint operacional/smoke) |
| **Evidência** | `docs/SPRINT_290_SMOKE_TELEFONES_HTTP200.md` |
| **Smoke API** | Contato 69: adicional `5512942833853`; duplicidade/principal 400; busca mesmo `contato_id` |
| **HTTP 200** | Confirmado após reinício |
| **Próximo passo** | UI Chats/histórico ou remoção de telefone adicional |

---

## Sprint 291 — Exibir telefones adicionais em Chats e histórico

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Telefones adicionais visíveis em leitura (Chats + histórico Contatos) |
| **Arquivos** | `contatoGestaoTelefonesView.js`, `chatsPage.js`, `chatsView.js`, `clientesPage.js`, `contatoGestaoHistoricoView.js`, `chats.css`, `clientes.css` |
| **Chats** | Painel Contato: WhatsApp (principal) + linhas adicionais; badge na lista após carregar conversa |
| **Histórico** | Bloco readonly acima da tabela de chamados (mesmo `contato_id`) |
| **Testes** | `npm test` 199 OK |
| **Próximo passo** | Remoção de adicional ou origem da mensagem no thread |

---

## Sprint 292 — Origem do atendimento por número

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Registrar e exibir qual telefone originou o ticket (Principal vs Adicional) |
| **Backup** | `BKP_Sprint_292_Origem_Atendimento_Telefone` (recomendado antes do deploy) |
| **Campos Ticket** | `atendimento_telefone`, `atendimento_telefone_normalizado`, `atendimento_telefone_tipo` (`PRINCIPAL` / `ADICIONAL`) |
| **Backend** | `ContatoAtendimentoOrigemService`; preenchimento em `TicketService.criarTicketPorWebhook`; DTOs ticket, histórico contato, resumo Chats |
| **Front** | Chats painel Chamado + meta aba Histórico; tabela histórico Contatos coluna “Origem do atendimento” |
| **Testes** | `ContatoAtendimentoOrigemServiceTest`; `npm test` (chats + historico) |
| **Regras preservadas** | Ticket ativo; telefone origem readonly; mesmo `contato_id` na entrada |
| **Próximo passo** | Origem por mensagem no thread; backfill opcional tickets legados; remoção telefone adicional |

---

## Sprint 293 — Smoke origem atendimento (runtime)

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar Sprint 292 com JAR novo em 8080 |
| **Evidência** | `docs/SPRINT_293_SMOKE_ORIGEM_ATENDIMENTO.md` |
| **Smoke API** | TK-000333 PRINCIPAL / TK-000334 ADICIONAL, contato 69 |
| **Correções** | Último encerrado com Pageable; `telefone_entrada` na pendência |
| **HTTP 200** | Confirmado após reinício |
| **Próximo passo** | Smoke browser nos protocolos acima ou E2E com matriz 5 |

---

## Sprint 294 — Origem do telefone por interação/mensagem

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Rastrear principal/adicional em cada mensagem WhatsApp no timeline |
| **Campos** | `ticket_interacoes`: `telefone_origem`, `telefone_origem_normalizado`, `telefone_origem_tipo` |
| **Backend** | `ContatoAtendimentoOrigemService.aplicarOrigemNaInteracao`; entrada WhatsApp com `telefoneNorm`; abertura copia origem do Ticket |
| **Front** | Badge discreto `Principal`/`Adicional` no meta da bolha do cliente (`chats-msg-origem`) |
| **Compat** | Interações antigas sem campos; origem do Ticket (292) preservada |
| **Testes** | `TicketInteracaoTelefoneOrigemTest`; `npm test` chatsView |
| **Próximo passo** | Reiniciar JAR; smoke alternância principal/adicional no mesmo ticket ativo |

---

## Sprint 295 — Máquina de status e transições do Ticket

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Centralizar regras de transição de status |
| **Serviço** | `TicketStatusTransicaoService` + `MotivoTransicao` (MANUAL, ENCERRAMENTO, REABERTURA, CLASSIFICACAO_INDEVIDO) |
| **Integração** | `TicketService` (status, encerrar, reabrir); `TicketIndevidoService` |
| **Bloqueios** | PUT não resolve nem classifica indevido; terminal não volta ao ativo exceto reabertura → ABERTO; INDEVIDO terminal |
| **Testes** | `TicketStatusTransicaoServiceTest` |
| **Próximo passo** | Reiniciar JAR; smoke Chats transições + tentativas inválidas |

---

## Sprint 296 — Smoke máquina de status

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar Sprint 295 em runtime |
| **Evidência** | `docs/SPRINT_296_SMOKE_STATUS_TICKET.md` |
| **Tickets** | TK-000335 (fluxo), TK-000336 (INDEVIDO) |
| **HTTP 200** | Após reinício do JAR |
| **Smoke API** | Transições permitidas + 6 bloqueios com mensagem 400 |
| **Próximo passo** | Smoke visual Chats; CANCELADO → reabertura |

---

## Sprint 297 — Smoke Chats + status (UI operacional)

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Smoke navegador: Fila/Atendendo, encerramento, INDEVIDO, bloqueios UI |
| **Backup** | `BKP_Sprint_297_Chats_Status_Smoke` |
| **Correção** | Painel Chats **Status do chamado** (`chatsStatusOperacionalView.js`, `chatsPage.js`) — gap encontrado no smoke |
| **Massa** | TK-000337 (fluxo → RESOLVIDO), TK-000338 (INDEVIDO) |
| **Evidência** | `docs/SPRINT_297_SMOKE_CHATS_STATUS.md` |
| **Testes** | `npm test` 205 OK; `mvn package -DskipTests`; browser status flow OK |
| **HTTP 200** | Confirmado |
| **Pendências** | Encerramento modal: validação manual/E2E; refresh lista após indevido |
| **Próximo passo** | Playwright status flow ou smoke CANCELADO/reabertura |

---

## Sprint F41 NITRO — Smoke final e marco

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Validar fluxo pós F24–F40; registrar **REESTRUTURAÇÃO CONCLUÍDA** |
| **Documento** | `Auditoria/REESTRUTURACAO-F41-SMOKE.md` |
| **Grep runtime** | Sem `conexao`/Carteira/ContatoCliente/TicketEtiqueta em Ticket/Cliente/Chats |
| **Testes** | `mvn test`; Vitest 217/217; `mvn package -DskipTests`; HTTP 200 |
| **Marco** | **REESTRUTURAÇÃO CONCLUÍDA** |
| **Mantido** | Config global Carteira/Conexões/Revendas; `/api/carteiras` |
| **Fora** | WhatsApp real; DROP tabela `carteiras` |
| **Próximo bloco** | Produto pós-marco: repaginação blueprint 235+, Playwright regressão, ou limpeza doc/README legado |

---

## Sprint F42 NITRO — Playwright smoke final

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | E2E navegador pós F41 — fluxo Cliente → Contato → Ticket → Chats → Relatórios |
| **Spec** | `e2e/tests/smoke-reestruturacao-final.spec.ts` |
| **Doc** | `docs/SPRINT_F42_PLAYWRIGHT_SMOKE_FINAL.md` |
| **Playwright** | 1 passed (`E2E_SKIP_WEB_SERVER=1`) |
| **Testes** | `mvn test`; Vitest 218/218; package OK |
| **Próximo** | E2E receptivo WhatsApp; CSV/PDF smoke opcional |

---

## Sprint F43 NITRO — E2E RECEPTIVO_WHATSAPP + Matriz

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Simular entrada WhatsApp → Contato + Ticket `RECEPTIVO_WHATSAPP` → Chats |
| **Endpoint** | `POST /api/integracoes/whatsapp/mensagens` |
| **Spec** | `e2e/tests/smoke-receptivo-whatsapp-final.spec.ts`, helper `f43Massa.ts` |
| **Doc** | `docs/SPRINT_F43_E2E_RECEPTIVO_WHATSAPP.md` |
| **Playwright** | 1 passed |
| **Provider real** | Não implementado |
| **Próximo** | Relatório origem RECEPTIVO; limpeza doc legado |

---

## Sprint F44 NITRO — Relatórios / CSV / PDF por origem

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Smoke consulta/exportação ATIVO_MANUAL + RECEPTIVO_WHATSAPP |
| **Spec** | `e2e/tests/smoke-relatorios-csv-pdf-origem.spec.ts`, helper `f44Massa.ts` |
| **Doc** | `docs/SPRINT_F44_RELATORIOS_CSV_PDF_ORIGEM.md` |
| **Playwright** | 1 passed |
| **Vitest** | 219/219 |
| **Package** | Falhou local (jar locked — app :8080) |
| **Próximo** | Regressão E2E única ou encerramento em lote |

---

## Sprint F45 NITRO — Suite E2E única pós-reestruturação

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | F42+F43+F44 → `smoke-pos-reestruturacao.spec.ts` |
| **Massa** | `.massa-pos-reestruturacao.json` (Cliente + 2 Contatos + Matriz + 2 tickets) |
| **Comando** | `npm run test:pos-reestruturacao` com `E2E_SKIP_WEB_SERVER=1` |
| **Playwright** | 1 passed (~1.4m, massa fresca/run) |
| **Doc** | `docs/SPRINT_F45_SUITE_E2E_POS_REESTRUTURACAO.md`, `e2e/README.md` |
| **Próximo** | CI job `test:pos-reestruturacao`; deprecar F42–F44 isolados quando estável |

---

## Sprint F46 NITRO — Validação oficial pós-reestruturação

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Esteira Maven + Vitest + package + HTTP + F45 documentada e automatizada |
| **Scripts** | `stop-java-8080.ps1`, `start-dev-server.ps1`, `validar-pos-reestruturacao.ps1` |
| **Doc** | `docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md` |
| **Comando único** | `.\scripts\validar-pos-reestruturacao.ps1` |
| **Próximo** | CI / gate em PR para fluxo central |

---

## Sprint F47 NITRO — Pacote Auditoria 004

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | AUDITORIA-004 como verdade vigente; 001–003 histórico |
| **Arquivos** | `AUDITORIA-004-pos-reestruturacao.md`, `AUDITORIA-004-context.json`, addendum histórico, `Auditoria/README.md` |
| **Próximo** | Provider WhatsApp; CI F46 |

---

## Sprint F48 NITRO — Gate CI/local pos-reestruturacao

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Gate PR: Maven + Vitest + package + F45 |
| **Workflow** | `.github/workflows/pos-reestruturacao.yml` |
| **Scripts** | `validar-pos-reestruturacao-ci.ps1`, ajustes `validar-pos-reestruturacao.ps1` |
| **Doc** | `docs/SPRINT_F48_GATE_CI_POS_REESTRUTURACAO.md` |
| **Próximo** | Required check no GitHub |

---

## Sprint F49 NITRO — Varredura legado e ruído

| Campo | Conteúdo |
|-------|----------|
| **Objetivo** | Banners histórico + índices + contratos API atualizados |
| **Doc** | `Auditoria/LEGADO-LIMPEZA-F49.md`, `docs/DOCUMENTACAO_VIGENTE.md` |
| **Runtime** | Sem alteração funcional |
| **Próximo** | Provider WhatsApp; opcional mover SPRINT_* para `historico-sprints/` |

---

## Sprint F51 — Required check gate

| Campo | Conteúdo |
|-------|----------|
| **Check** | `Gate pos-reestruturacao / Gate pos-reestruturacao` |
| **Doc** | `docs/SPRINT_F51_REQUIRED_CHECK_GATE.md` |
| **Workflow** | `.github/workflows/pos-reestruturacao.yml` (job name estável) |
| **Ativação GitHub** | Pendência admin — branch `main` |
| **Runtime** | Sem alteração funcional |

---

## Sprint F52 — Validação Actions + required check

| Campo | Conteúdo |
|-------|----------|
| **Branch** | `chore/f52-gate-pos-reestruturacao` |
| **Doc** | `docs/SPRINT_F52_VALIDACAO_REQUIRED_CHECK.md` |
| **Push/Actions** | Pendente credencial GitHub (`Repository not found` no ambiente agente) |
| **Required check** | Admin após 1ª run verde |

---

## Sprint F56 — Profile CI boot Gate

| Campo | Conteúdo |
|-------|----------|
| **Profile** | `spring.profiles.active=ci` |
| **Doc** | `docs/SPRINT_F56_CI_PROFILE_BOOT_GATE.md` |
| **Causa** | Boot CI com seeds DEV + SQL verbose → Tomcat não 200 em 240s |
| **Fix** | `application-ci.properties`; seeds DEV off; `CiMinimalAdminSeedConfig`; workflow profile ci |
| **Actions** | Validar após push |
