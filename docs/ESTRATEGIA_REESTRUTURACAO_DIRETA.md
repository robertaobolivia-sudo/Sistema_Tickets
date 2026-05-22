# Estratégia de reestruturação direta (ambiente embrionário)

**Sprint 187** — revisão estratégica da implementação.  
**Status:** estratégia **atual** do projeto para evolução do domínio (substitui a orientação “compatibilidade longa” como caminho principal).

---

## 1. Objetivo

Atualizar o **plano de implementação** após as Sprints 184–186, incorporando a premissa de que o sistema está **embrionário**: é possível corrigir a fundação do domínio com **reestruturação direta e controlada**, em vez de manter camadas paralelas de legado por longo período.

Este documento **não** altera o modelo oficial de produto ([MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md)); altera **como chegar lá** no código.

---

## 2. Premissa atual do projeto

| Premissa | Implicação |
|----------|------------|
| Sistema em **desenvolvimento** | Mudanças estruturais profundas são aceitáveis. |
| **Sem** rigidez de produção legada | Não tratar `Carteira`, `Cliente` atual e `Ticket.conexao` como contratos imutáveis por anos. |
| **Menor** necessidade de compatibilidade longa | Evitar resolver duplo, DTOs legados paralelos e fases 0–4 do plano conservador como caminho principal. |
| **Ainda** obrigatório | Backup por sprint (`AGENTS.md`), sprints pequenas, `mvn clean install`, testes JS quando frontend mudar, app em 8080, rollback conhecido, relatório de sprint. |
| **Rastreabilidade** | Manter coleta (184), modelo oficial (185) e plano conservador (186) como histórico; decisões novas registradas aqui. |

---

## 3. Estratégia anterior (Sprint 186 e afins)

Resumo do [PLANO_COMPATIBILIDADE_CLIENTE_CONTATO.md](./PLANO_COMPATIBILIDADE_CLIENTE_CONTATO.md):

- **Alternativa C** recomendada: novas entidades (`whatsapp_matriz`, `contato_atendimento`) convivendo com legado.
- **Carteira** = contratante lógico na fase 1; **Cliente** legado intacto; **`Ticket.conexao`** preservado.
- Migração em **7 fases** com preenchimento paralelo e descontinuação tardia.
- Primeiro incremento: **WhatsApp matriz** + resolver de compatibilidade, **sem** rename.

**Limitação para o momento atual:** otimizada para **não quebrar produção**; em ambiente embrionário gera **duplicação prolongada** e atrasa o modelo mental correto no código.

---

## 4. Nova estratégia

### Princípios

1. **Reestruturação direta e controlada** — corrigir domínio principal **antes** de empilhar features novas (avaliação WhatsApp, login Cliente, indicadores completos).
2. **Uma mudança estrutural por sprint** — schema + entidades + services + APIs mínimas + tela(s) afetadas na mesma fase, não “só tabela” sem UI quando a tela já existe.
3. **Reduzir duração do legado** — legado sobrevive **até a sprint que o substitui**, não “até fase 5 genérica”.
4. **Aceitar breaking changes internos** — renomear tabelas/colunas, remover `Carteira` como conceito, remover `conexao` string, **com backup e script de migração/seed** no mesmo sprint ou no imediatamente seguinte.
5. **Modelo oficial prevalece** em conflito com código antigo.

### O que deixa de ser obrigatório

- Camada longa `ModeloCompatibilidadeService` mantendo dois mundos indefinidamente.
- Manter `TicketEtiqueta` como vínculo **principal** após Fase 2.
- Tratar integração WhatsApp sem `whatsappMatrizId` como estado final.

### O que permanece obrigatório

- Backup completo antes de cada sprint de código.
- Testes e build verdes antes de encerrar sprint.
- Documentar no relatório o que foi migrado/apagado no banco de **dev**.

---

## 5. Novo modelo-alvo prático (como o código deve caminhar)

| Conceito | Direção no código |
|----------|-------------------|
| **Cliente** | Entidade/tabela **`clientes`** passa a ser **contratante F5** (hoje ≈ `Carteira` + metadados). Cadastro “Clientes” na UI = contratantes. |
| **Contato** | Nova entidade **`contatos`** (ou `contato_atendimento`): pessoa WhatsApp; UK `(cliente_id, whatsapp_e164)`; WhatsApp **imutável**. |
| **WhatsApp matriz** | Entidade **`whatsapp_matriz`**: FK **Cliente**; entrada da API identifica contratante. |
| **Ticket** | FK **`cliente_id`** (contratante) + FK **`contato_id`**; opcional **`whatsapp_matriz_id`**; **sem** `conexao` string como fonte de verdade. |
| **Tags** | **`contato_tag`**; remover uso principal de **`ticket_etiquetas`**. |
| **Categoria / Subcategoria / Motivo** | Renomear labels para produto; entidade **`motivo`**; FKs no ticket no encerramento. |
| **Avaliação** | Evoluir/replace **`ticket_satisfacao`** → fluxo WhatsApp, status, expiração, visibilidade por perfil. |
| **Indicadores / relatórios / dashboard** | Agregação por **`cliente_id`** (contratante); filtros por Contato, tag, motivo. |

**Mapeamento físico sugerido (dev, uma linha de direção):**

- Dados de **`carteiras`** → migrar para **`clientes`** (contratante).
- Dados de **`clientes`** atuais (pessoa/empresa atendida) → migrar para **`contatos`**.
- **`tickets.cliente_id`** hoje → após migração aponta para **contratante**; novo **`contato_id`** preenchido por regra telefone/nome.
- **`contatos_clientes`** → **descontinuar** após Contato WhatsApp e Abrir ticket ajustados (sprint própria).

---

## 6. Legados removíveis ou substituíveis (por fase)

| Legado | Ação futura | Fase alvo |
|--------|-------------|-----------|
| Entidade **`Carteira`** / tabela `carteiras` | Absorvida em **Cliente** contratante; tabela removida | Fase 1 + 7 |
| **`Ticket.conexao`** (string) | Removida; substituída por FK Cliente (+ matriz) | Fase 1 + 7 |
| **`Cliente` como “cliente final”** | Dados em **Contato**; semântica de `clientes` só contratante | Fase 1 |
| **`TicketEtiqueta`** | Substituída por **`contato_tag`** | Fase 2 + 7 |
| **`ContatoCliente`** | Removida se Abrir ticket/Chats usarem **Contato** | Fase 2 ou 7 |
| **`arteHeaderChatsUrl` no cadastro errado** | Apenas em **Cliente** contratante | Fase 2 |
| UI **Revenda / Conexão / Carteira** | Remover rótulos; Cliente + Contato | Fase 2–5 |
| **`classificacao_cliente`** em cadastro atendido | Reavaliar por Contato ou remover | Fase 7 |
| Endpoint arte **`/api/carteiras/.../arte-header-chats`** | Remover após migração arte | Fase 7 |
| Integração só com **`clienteId` legado** | Exigir **`whatsappMatrizId`** + resolver Contato | Fase 1–2 |

---

## 7. Legados que ainda devem sobreviver (até sprint específica)

Mesmo em dev, **não** apagar no mesmo dia sem substituto:

| Item | Até quando |
|------|------------|
| **Analista**, **PerfilAcesso**, login, sessão | Permanente (evoluir depois com CLIENTE) |
| **Ticket** (número, status, SLA, interações, anexos) | Permanente (FKs mudam) |
| **GrupoCategoria / SubgrupoCategoria** | Até Fase 3 (renomear labels + Motivo) |
| **SLA**, feriados, horário útil | Permanente |
| **Configurações** mínimas para subir app | Até cada tela migrada |
| **`TicketSatisfacao`** | Até Fase 4 |
| **Webhook `/api/webhooks/tickets`** | Até Fase 1–2 alinhado a Contato + matriz |
| Coleta e docs 184–186 | Permanente (histórico) |

---

## 8. Nova sequência de implementação (agressiva e controlada)

### Fase 1 — Fundamento de domínio (crítica)

- **Cliente** = contratante (migrar `carteiras` → `clientes`; redefinir entidade `Cliente`).
- **Contato** = pessoa atendida (migrar `clientes` antigos → `contatos`).
- **WhatsAppMatriz** vinculado ao Cliente.
- **Ticket**: `cliente_id` (contratante) + `contato_id`; remover dependência funcional de `conexao`; integração/webhook atualizados.
- **Sprints típicas:** 2–4 (ver estimativa §10).

### Fase 2 — Chats

- Header e arte por **Cliente** contratante.
- Painel direito: **Contato**; **tags** no Contato.
- Fila e fluxo **pós-encerramento** (decisão analista).
- Remover fallback Carteira no header quando arte Cliente existir.

### Fase 3 — Encerramento

- **Motivo** (entidade + ADMIN + obrigatoriedade).
- Labels Categoria/Subcategoria alinhadas ao modelo oficial.

### Fase 4 — Avaliação

- Opt-in analista; envio WhatsApp; expiração sexta 18h; visibilidade ANALISTA vs ADMIN/SUPERVISOR vs CLIENTE.

### Fase 5 — Indicadores / Relatórios / Dashboard

- Consolidação por **Cliente**; filtros Contato, tag, motivo, avaliação.
- Substituir “por conexão” por “por Cliente”; micro visão + reset 08–18h.

### Fase 6 — Login Cliente consultivo

- Perfil **CLIENTE**; escopo por contratante; sem dados internos de analista.

### Fase 7 — Limpeza final

- DROP tabelas/colunas legado; remover fallbacks; revisar nomenclatura e docs de tela.

---

## 9. Critérios de segurança (inalterados em espírito)

- Backup `Sistemas_BKP\BKP_Sprint_XX_*` antes de código.
- **Uma** mudança estrutural dominante por sprint.
- `mvn clean install`; `npm test` se `static/js` alterado.
- HTTP 200 em `http://localhost:8080/`.
- Script de migração **idempotente** ou documentado para reexecução em dev.
- Rollback = restaurar backup + branch anterior (não “compatibilidade eterna”).
- Relatório curto de sprint (AGENTS.md).

---

## 10. Nova estimativa de sprints

| Marco | Sprints (ordem de grandeza) | Conteúdo |
|-------|-----------------------------|----------|
| **MVP domínio + Chats utilizável** | **5–7** | Fases 1–2 |
| **Encerramento + avaliação básica** | **+3–4** | Fases 3–4 |
| **Gestão completa (indicadores/relatórios/dashboard)** | **+4–6** | Fase 5 |
| **Portal Cliente** | **+2–3** | Fase 6 |
| **Limpeza e hardening** | **+1–2** | Fase 7 |
| **Total até versão completa com login Cliente** | **~15–22** sprints de código | Depende de tamanho de cada sprint |

**Blocos mais arriscados:** Fase 1 (migração de dados + FK ticket); Fase 2 (Chats + tags); Fase 4 (WhatsApp avaliação); Fase 5 (reagregação indicadores).

**Versão funcional interna (analistas):** após Fases **1–3** (~8–11 sprints).

---

## Sprint 188 — Cliente como contratante F5 (Fase 1a, concluída em documentação/código)

| Item | Situação |
|------|----------|
| UI Clientes | Textos e seções alinhados a **contratante**; sem Carteira/Revenda/Conexão; seção **Comunicação** (não “Contato”). |
| Backend | `Cliente` documentado como contratante; `ClienteService` não cria Carteira por nome; `carteiraId` só se explícito. |
| Legado | Tabela `carteiras`, FK `carteira_id`, `Ticket.conexao`, `ContatoCliente`, webhook que ainda associa carteira em `TicketService`. |
| Próximo passo | **Sprint 189:** entidade **Contato** WhatsApp + vínculo ticket (Fase 1b). |

## Sprint 189 — Contato WhatsApp (Fase 1b)

| Item | Situação |
|------|----------|
| Entidade | `Contato` / tabela `contatos`, UK `(cliente_id, whatsapp_normalizado)`. |
| API | `/api/contatos` CRUD + busca + ativar/inativar; ver `docs/CONTATOS_TELA.md`. |
| Ticket | FK opcional `contato_id`; DTO `contatoId`, `contatoNome`, `contatoWhatsapp`. |
| UI | Nenhuma tela nova; Chats/Abrir ticket inalterados. |
| Próximo passo | **Sprint 190:** preencher `contato_id` no fluxo ticket/WhatsApp ou UI listagem. |

## Sprint 190 — Ticket preenche Contato na criação

| Item | Situação |
|------|----------|
| Fluxos | `criarTicketPorWebhook`: Abrir ticket, `POST /api/tickets`, webhook, integração WhatsApp. |
| Regra | Telefone → `criarSeNaoExistir`; sem telefone → `contato_id` vazio. |
| DTO | `TicketWebhookRequestDTO.nomeContato` opcional. |
| Próximo passo | **Sprint 191:** WhatsAppMatriz ou Chats consumindo `contatoId`. |

## Sprint 191 — WhatsApp Matriz

| Item | Situação |
|------|----------|
| Entidade/API | `whatsapp_matriz`, `/api/whatsapp-matrizes` |
| Integração | `whatsappMatrizId` / `numeroMatriz` opcionais; legado `clienteId` preservado |
| Ticket | `whatsapp_matriz_id` + DTO |
| Próximo passo | **Sprint 192:** Chats/painel Contato ou UI matriz no Cliente |

## Sprint 192 — UI WhatsApps Matriz em Clientes

| Item | Situação |
|------|----------|
| UI | Seção listar/cadastrar/editar/ativar/inativar em `page-clientes` |
| Próximo passo | **Sprint 193:** Chats usando Contato + matriz |

## Sprint 193 — Chats painel Contato/Matriz

| Item | Situação |
|------|----------|
| Painel direito | Cliente / Contato / Entrada do atendimento / Chamado atual |
| DTO | `contatoEmail`, `contatoEmpresaLocal`, `contatoCidade`, `contatoUf`, `contatoObservacoes`, `whatsappMatrizNome` |
| Fallback | Tickets sem `contatoId` mantêm cadastro legado no painel |
| Próximo passo | Etiquetas no Contato; Chats pós-encerramento; renomear helper do header |

## Sprint 194 — Etiquetas do Contato no Chats

| Item | Situação |
|------|----------|
| Dados | `contato_etiquetas` + service + endpoints |
| Chats | Contato quando `contatoId`; fallback ticket |
| Legado | `ticket_etiquetas` preservado |
| Próximo passo | Migração histórica; indicadores por etiqueta/contato |

## Sprint 195 — Decisão pós-encerramento

| Item | Situação |
|------|----------|
| Dados | `interacao_pendente_decisao` |
| Integração | Pendência se último ticket encerrado (Cliente+Contato) |
| Chats | Fila + banner de decisão |
| Próximo passo | Notificação push; envio WhatsApp real |

## Sprint 196 — Motivo no encerramento

| Item | Situação |
|------|----------|
| Catálogo | `motivos` → Subcategoria |
| API | `/api/motivos` |
| Encerramento | `motivoId` obrigatório no PUT encerrar |
| Próximo passo | Indicadores/relatórios por motivo |

## Sprint 207 — Smoke browser Chats (2 contatos)

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_207_SMOKE_BROWSER_CHATS_DOIS_CONTATOS.md` |
| API | TK-086 / TK-087 separados (contato e interações) |
| UI | Checklist manual Fila + painel |

## Sprint 206 — Ticket ativo Cliente + Contato

| Item | Situação |
|------|----------|
| Correção | `TicketAtivoService` sem fallback por cliente quando `contato_id` resolvido |
| Doc | `docs/SPRINT_206_SMOKE_TICKET_ATIVO_CONTATO.md` |

## Sprint 211 — Encerramento 500 + duplicidade ativo

| Item | Situação |
|------|----------|
| Correção | `ticket_satisfacao.nota` nullable; integração recheck + guarda webhook |
| Doc | `docs/SPRINT_211_ENCERRAMENTO_500_TICKET_ATIVO_CONTATO.md` |

## Sprint 212 — Smoke encerramento + pesquisa + ativo

| Item | Situação |
|------|----------|
| Smoke | `scripts/smoke-sprint212.ps1`; API A–D OK |
| Doc | `docs/SPRINT_212_SMOKE_ENCERRAMENTO_PESQUISA_TICKET_ATIVO.md` |
| Ajuste | `avaliacaoLinkPublico` no encerramento com pesquisa; resumo satisfação no detalhe do ticket |

## Sprint 213 — Smoke UI Chats + encerramento

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_213_SMOKE_UI_ENCERRAMENTO_PESQUISA_CHATS.md` |
| UI | Fila Chats, modal encerramento, link público; data-testid |

## Sprint 214 — Playwright E2E Chats → encerramento → avaliação pública

| Item | Situação |
|------|----------|
| E2E | `e2e/` — Playwright; spec `tests/chats-encerramento-pesquisa.spec.ts` |
| Massa | `e2e/global-setup.ts` (API integração + login) |
| Doc | `docs/SPRINT_214_PLAYWRIGHT_E2E_CHATS_ENCERRAMENTO.md`, `e2e/README.md` |
| Smoke | `cd e2e && npm test` — 1 passed (2026-05-21) |

## Sprint 215 — E2E detalhe satisfação (link/envio/status)

| Item | Situação |
|------|----------|
| E2E | Mesmo spec — detalhe `detail-satisfacao-*` antes/depois da resposta pública |
| Doc | `docs/SPRINT_215_E2E_DETALHE_SATISFACAO.md` |
| Smoke | `cd e2e && npm test` — 1 passed (2026-05-21) |

## Sprint 216 — Motivos + modal encerramento

| Item | Situação |
|------|----------|
| Seed | `garantirMotivoAtivoEmSubcategoriasAtivas` em `CategoriaSeedConfig` |
| UI | Modal encerramento largo/baixo; pesquisa com ⭐ e cards Sim/Não |
| Doc | `docs/SPRINT_216_MOTIVOS_MODAL_ENCERRAMENTO.md` |
| Smoke | `mvn` + Vitest 137 + `e2e` 1 passed (2026-05-21) |

## Sprint 217 — Modal encerramento layout vertical

| Item | Situação |
|------|----------|
| UI | Coluna única + pesquisa inline + rodapé centralizado |
| Doc | `docs/SPRINT_217_MODAL_ENCERRAMENTO_VERTICAL.md` |

## Sprint 218 — Smoke visual modal + E2E

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_218_SMOKE_MODAL_E2E.md` |
| E2E | `cd e2e && npm test` — 1 passed |
| Visual | Modal sem scroll interno; layout 217 após Ctrl+F5 / sync estático |

## Sprint 219 — E2E checklist operacional

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_219_E2E_CHECKLIST.md`, `e2e/README.md` |
| Setup | Logs em `global-setup.ts`; `.massa.json` sobrescrito a cada run |
| Regressão | `cd e2e && npm test` |

## Sprint 220 — E2E sem pesquisa

| Item | Situação |
|------|----------|
| Spec | `e2e/tests/chats-encerramento-sem-pesquisa.spec.ts` |
| Massa | `numeroTicket` + `numeroTicketSemPesquisa` no global-setup |
| Doc | `docs/SPRINT_220_E2E_SEM_PESQUISA.md` |

## Sprint 221 — E2E sem contato

| Item | Situação |
|------|----------|
| Spec | `e2e/tests/encerramento-sem-contato.spec.ts` |
| Massa | `POST /api/tickets` sem telefone → `numeroTicketSemContato` |
| UI | `data-testid="encerrar-aviso-sem-contato"` |
| Doc | `docs/SPRINT_221_E2E_SEM_CONTATO.md` |

## Sprint 222 — UX pesquisa sem contato

| Item | Situação |
|------|----------|
| UI | `encerrarPesquisaSim` desabilitado + label `is-disabled` |
| Doc | `docs/SPRINT_222_UX_PESQUISA_SEM_CONTATO.md` |
| Testes | Vitest 138 + E2E 3 passed (validação Sprint 223) |
| Status | Aprovada |

## Sprint 223 — Validação 222 (8080 + Playwright)

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_223_VALIDACAO_222.md` |
| HTTP 200 + E2E | 3 passed; app via `java -jar` |

## Sprint 224 — E2E/CI script local

| Item | Situação |
|------|----------|
| Script | `scripts/run-e2e-local.ps1` |
| Doc | `docs/SPRINT_224_E2E_CI.md`, `e2e/README.md` |

## Sprint 225 — GitHub Actions E2E

| Item | Situação |
|------|----------|
| Workflow | `.github/workflows/e2e.yml` |
| Doc | `docs/SPRINT_225_CI_GITHUB_ACTIONS.md` |
| Setup | `global-setup` resolve IDs de encerramento via API |

## Sprint 227 — Indicadores filtro Cliente

| Item | Situação |
|------|----------|
| UI | `indicEncFiltroCliente` em Encerramento e satisfação |
| Doc | `docs/SPRINT_227_INDICADORES_FILTRO_CLIENTE.md` |
| Validação | **Aprovada** (Sprint 228) |

## Sprint 228 — Validar Sprint 227

| Item | Situação |
|------|----------|
| HTTP 200 | OK (`http://localhost:8080/`) |
| Smoke filtro Cliente | OK (`clienteId` com/sem parâmetro) |
| Doc | `docs/SPRINT_228_VALIDACAO_SPRINT_227.md` |
| Deploy | `mvn package -DskipTests` + JAR 8080 |

## Sprint 229 — Login corporativo (UX)

| Item | Situação |
|------|----------|
| Visual | Fundo teal `#0F2F3A`, card, logo F neon, campos com ícones |
| Auth | Inalterada (mesmos ids / data-testid) |
| Doc | `docs/SPRINT_229_LOGIN_CORPORATIVO.md` |

## Sprint 230 — Dashboard encerramento período/cliente

| Item | Situação |
|------|----------|
| Período | 7 / 30 / 90 dias (padrão 30) |
| Cliente | Filtro opcional no bloco |
| Doc | `docs/SPRINT_230_DASHBOARD_ENCERRAMENTO_PERIODO.md` |
| Validação visual | **OK** (Sprint 231) |

## Sprint 231 — Smoke visual Dashboard encerramento

| Item | Situação |
|------|----------|
| Smoke browser | Período + Cliente + Network OK |
| Doc | `docs/SPRINT_231_VALIDACAO_DASHBOARD_ENC.md` |

## Sprint 232 — Relatórios filtro Cliente (clienteId)

| Item | Situação |
|------|----------|
| UI Relatórios | Select Cliente + satisfação usa mesmo filtro |
| API/CSV | `clienteId` busca tickets e satisfação |
| Doc | `docs/SPRINT_232_RELATORIOS_FILTRO_CLIENTE.md` |
| Validação visual | **OK** (Sprint 233) |

## Sprint 233 — Smoke visual Relatórios

| Item | Situação |
|------|----------|
| Smoke browser | Cliente + Motivo + Pesquisa + Nota + CSV + Network |
| Doc | `docs/SPRINT_233_VALIDACAO_RELATORIOS_CLIENTE.md` |

## Sprint 234 — Blueprint telas + padrão visual corporativo

| Item | Situação |
|------|----------|
| Escopo | Somente documentação (sem CSS/JS/Java funcional) |
| Doc | `docs/BLUEPRINT_TELAS_NOVO_MODELO.md` |
| Próximo | Sprint 235 — tokens em `theme.css` |

## Sprint 235 — Tokens visuais corporativos

| Item | Situação |
|------|----------|
| Escopo | `--corp-*` em `theme.css`; sem HTML/JS/Java |
| Doc | `docs/SPRINT_235_TOKENS_VISUAIS_CORPORATIVOS.md` |
| Impacto UI | Neutro (tokens não consumidos) |
| Próximo | Sprint 236 — shell `layout.css` |

## Sprint 236 — Shell corporativo

| Item | Situação |
|------|----------|
| Escopo | `layout.css` + overrides shell em `theme.css` |
| Doc | `docs/SPRINT_236_SHELL_CORPORATIVO.md` |
| Login | Inalterado |
| Próximo | Sprint 237 — login + avaliação pública |

## Sprint 237 — Login e Avaliação pública

| Item | Situação |
|------|----------|
| Avaliação pública | Visual corporativo + `estadoVariant` |
| Login | Tokens `--corp-*`, visual 229 preservado |
| E2E avaliação | OK em `chats-encerramento-pesquisa.spec.ts` |
| Doc | `docs/SPRINT_237_LOGIN_AVALIACAO_PUBLICA.md` |
| Próximo | Sprint 238 — Clientes |

## Sprint 226 — Validar workflow no runner GHA

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_226_VALIDACAO_WORKFLOW_GHA.md` |
| Hardening | timeout CI, retry login, senha fallback no step |
| Simulação local CI | 3 passed |
| Run GitHub | Pendente disparo manual (sem gh/git remote aqui) |

## Sprint 205 — Smoke operacional WhatsApp

| Item | Situação |
|------|----------|
| Script | `scripts/smoke-entrada-whatsapp.ps1` |
| Doc | `docs/SPRINT_205_SMOKE_OPERACIONAL_ENTRADA_WHATSAPP.md` |
| Próximo | Chats E2E browser ou preencher resultados locais no doc |

## Sprint 204 — Smoke entrada WhatsApp

| Item | Situação |
|------|----------|
| Doc | `docs/SPRINT_204_SMOKE_ENTRADA_WHATSAPP.md` |
| API | `POST /api/integracoes/whatsapp/mensagens` |
| Testes | `IntegracaoMensagemEntradaSmokeTest` |

## Sprint 203 — Dashboard Encerramento + Satisfação

| Item | Situação |
|------|----------|
| API | `GET /api/dashboard/encerramento-satisfacao` |
| UI | Bloco de cards no Dashboard |
| Período | Últimos 30 dias (fixo) |
| Pendência | Filtro de período no Dashboard |

## Sprint 202 — Indicadores Motivo + Pesquisa

| Item | Situação |
|------|----------|
| API | `GET /api/indicadores/encerramento-avaliacao` |
| UI | Subpágina Encerramento e satisfação |
| Pendência | Filtro Cliente na UI |

## Sprint 201 — Relatórios Motivo + Avaliação

| Item | Situação |
|------|----------|
| Relatórios | Filtros e colunas CSV |
| Próximo passo | Dashboard (reuso endpoint 202) |

## Sprint 200 — Envio WhatsApp (preparação)

| Item | Situação |
|------|----------|
| Config | `app.public-base-url` |
| Envio | Simulado (`NoopWhatsAppMessageSender`) |
| Próximo passo | Provedor WhatsApp real |

## Sprint 199 — Token público de avaliação

| Item | Situação |
|------|----------|
| Token | Hash SHA-256 + opaco na criação PENDENTE |
| API | `/api/public/avaliacoes/{token}` |
| UI | `?page=avaliacao&token=` |
| Próximo passo | WhatsApp com link |

## Sprint 198 — Resposta e expiração da avaliação

| Item | Situação |
|------|----------|
| API | `POST .../satisfacao/responder` (sessão) |
| Job | Scheduler 30 min → `EXPIRADA` |
| Próximo passo | Token/link público + WhatsApp real |

## Sprint 197 — Avaliação pós-RESOLVIDO preparada

| Item | Situação |
|------|----------|
| Modelo | `TicketSatisfacao` + status de envio |
| Encerramento | `enviarPesquisaSatisfacao` no PUT encerrar |
| Expiração | `expira_em` = próxima sexta 18h (fuso SLA) |
| Próximo passo | Provedor WhatsApp, link resposta Contato, job `EXPIRADA` |

---

## 11. Primeiro sprint de código recomendado (após Sprint 187)

**Não** iniciar só com `WhatsAppMatriz` isolado: matriz depende de **Cliente contratante** estável.

**Recomendação (Sprint 188):**  
**“Fase 1a — Cliente contratante + Contato + Ticket (FKs e migração dev)”**

Escopo sugerido (título, não prompt completo):

1. Backup + script/migration **dev**: absorver **`carteiras`** em **`clientes`** (contratante); mover cadastros atendidos para **`contatos`**; ajustar **`tickets`** (`cliente_id` contratante, `contato_id`).
2. Remover uso **funcional** de **`Carteira`** nos services principais (pode manter tabela vazia uma sprint se facilitar rollback).
3. Deprecar escrita em **`Ticket.conexao`** (leitura temporária ok uma sprint).
4. APIs mínimas: CRUD Cliente contratante, CRUD Contato, criação ticket com Cliente+Contato.
5. Seed mínimo para login e um fluxo Abrir ticket ou webhook de teste.
6. Testes unitários de migração e criação de ticket.

**Sprint 189 provável:** `WhatsAppMatriz` + integração entrada + Chats leitura básica.

**Avaliação da sugestão do produto** (“só Cliente contratante primeiro”): **válida como 188a** se dividir Contato/Ticket em **189**; **preferível 188 única** com Contato+Ticket no mesmo backup para não deixar `cliente_id` em estado ambíguo.

---

## 12. Regras para Cursor / IA

1. **Não** tratar legado como produção rígida; **não** construir compatibilidade longa sem necessidade documentada.
2. **Ainda não** apagar tabelas/dados sem backup e sprint de limpeza explícita.
3. **Não** misturar Fase 1 + Fase 4 + Fase 5 na mesma sprint.
4. Em conflito código vs [MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md), **priorizar o modelo oficial**.
5. Plano conservador (186) é **referência histórica**; estratégia **ativa** é este documento (187).
6. Atualizar coleta (184) após Fase 1 para refletir novo estado.

---

## Referências cruzadas

| Documento | Papel |
|-----------|--------|
| [COLETA_MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./COLETA_MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md) | Snapshot “antes” (atualizar pós-Fase 1) |
| [MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md) | Regras de produto (inalteradas) |
| [PLANO_COMPATIBILIDADE_CLIENTE_CONTATO.md](./PLANO_COMPATIBILIDADE_CLIENTE_CONTATO.md) | Plano conservador (histórico) |

---

*Sprint 187 — somente documentação; nenhuma alteração de aplicação.*
