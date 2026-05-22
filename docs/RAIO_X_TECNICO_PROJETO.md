# Raio-X técnico — suporte-tickets

**Data do diagnóstico:** 20/05/2026  
**Escopo:** pós-modularização do frontend e criação da área **Indicadores** (Sprints 120–124).  
**Método:** análise estática do repositório + execução de `npm test` e `mvn clean install` (sem alteração de código de produção).

---

## 1. Visão geral

| Métrica | Valor aproximado |
|--------|-------------------|
| Arquivos no projeto (excl. `target/`, `node_modules/`, `.git/`) | **~271** |
| Linhas Java (`src/main/java`) | **~9.360** |
| Linhas testes Java (`src/test/java`) | **~675** |
| Linhas JS de produção (`static/js`, sem `node_modules`) | **~6.518** |
| Linhas CSS (`static/css`) | **~2.556** |
| Linhas `index.html` | **~1.907** |
| `style.css` (fallback) | **~3** (mínimo, conforme esperado) |

### Principais pastas

```
src/main/java/com/suporte/tickets/   → controller, service, repository, dto, entity, config, exception
src/main/resources/static/          → index.html, app.js, css/, js/
src/test/java/                      → testes unitários de services/helpers
docs/                               → arquitetura, API, QA (6 arquivos)
```

### Módulos mais relevantes (negócio)

- **Tickets + SLA + escalonamento** — núcleo operacional.
- **Satisfação** — resumo, CSV, evolução temporal (`/api/tickets/satisfacao/*`).
- **Indicadores** — área gerencial nova (`/api/indicadores/chamados` + subpáginas UI).
- **Dashboard** — resumo operacional/gerencial (não confundir com Indicadores).
- **Relatórios** — busca avançada, CSV de tickets, bloco de satisfação.
- **Configurações** — horário útil, feriados, metas SLA.
- **Auditoria, notificações, clientes, analistas.**

---

## 2. Backend

| Artefato | Quantidade |
|----------|------------|
| Controllers (`*Controller.java`) | **19** |
| Services (`*Service.java`) | **34** |
| Repositories (`*Repository.java`) | **14** |
| DTOs (`*DTO.java`) | **59** |
| Entities | **24** |
| Testes Java | **17** classes |

### Endpoints principais (por prefixo)

| Prefixo | Responsabilidade |
|---------|------------------|
| `/api/tickets` | CRUD, busca, ações, CSV relatório |
| `/api/tickets/satisfacao` | resumo, evolução, CSV |
| `/api/tickets/{numero}/satisfacao` | consulta/registro por ticket |
| `/api/indicadores` | chamados agregados (Sprint 123) |
| `/api/dashboard` | resumo, gerencial, SLA |
| `/api/analistas` | login, cadastro, perfil |
| `/api/clientes`, `/api/contatos-clientes` | clientes e contatos |
| `/api/grupos-categoria`, `/api/subgrupos-categoria` | categorias |
| `/api/auditoria` | consulta/exportação |
| `/api/notificacoes` | sino / painel |
| `/api/feriados`, `/api/horarios-uteis`, `/api/sla-metas`, `/api/sla` | configuração SLA |
| `/api/webhooks` | integrações externas |

Estimativa: **~50+** operações HTTP mapeadas (GET predominante em leituras gerenciais).

### Avaliação de modularidade

**Pontos saudáveis**

- Separação clara **Controller → Service → Repository**.
- DTOs para fronteiras HTTP; regras de satisfação centralizadas em `TicketSatisfacaoConsultaService` (filtros compartilhados resumo/CSV/evolução).
- Indicadores com service dedicado (`IndicadoresChamadosService`) e autorização explícita (`exigirAdminOuSupervisor`).
- Uso de `JpaSpecificationExecutor` em tickets para buscas complexas (`TicketBuscaService`).

**Riscos / dívidas**

| Risco | Nível | Observação |
|-------|--------|------------|
| `DashboardService` com agregações em memória (`findAll`) | **MÉDIO** | Escala mal com volume alto de tickets |
| `IndicadoresChamadosService` carrega tickets do período em memória | **MÉDIO** | Aceitável na v1; monitorar períodos amplos |
| Cobertura de testes Java concentrada em SLA/satisfação/auditoria | **MÉDIO** | Poucos testes de controller/integration |
| Dois controllers sob `/api/clientes` (`ClienteController`, `ClienteContatoController`) | **BAIXO** | Organização aceitável, exige atenção na documentação |

---

## 3. Frontend JavaScript

### `app.js`

| Métrica | Valor |
|---------|--------|
| Linhas | **~466** |
| Imports | **~22** módulos |
| `addEventListener` / `apiFetch` / `innerHTML` no arquivo | **Baixo** (~8 ocorrências de listeners globais) |

**Conclusão:** `app.js` atua majoritariamente como **orquestrador/bootstrap** (router, auth, `init*Page`, `pageLoaders`, UI global do analista), alinhado a `docs/FRONTEND_ARQUITETURA.md`. Ainda concentra callbacks transversais (`changeTicketStatus`, avatares, refresh pós-status) — aceitável, mas é o ponto a vigiar para não reabsorver lógica de página.

### Maiores arquivos JS (produção)

| Linhas | Arquivo |
|-------|---------|
| 625 | `components/ticketDetailsModal.js` |
| 495 | `pages/atendentesPage.js` |
| 488 | `pages/perfilPage.js` |
| 460 | `pages/dashboardPage.js` |
| 421 | `pages/relatoriosPage.js` |
| 408 | `pages/configuracoesPage.js` |
| 388 | `components/alertaTicket.js` |
| 382 | `pages/clientesPage.js` |
| 170 | `pages/indicadoresPage.js` |

### Estrutura atual

| Pasta | Arquivos | Papel |
|-------|----------|--------|
| `js/core/` | 9 | api, auth, router, permissions, presentation, queryParams, messages, state, indicadoresSubpages |
| `js/services/` | 11 | HTTP por domínio (ticket, satisfacao, indicadores, dashboard, …) |
| `js/pages/` | 10 | Uma página SPA por módulo |
| `js/components/` | 4 | topbar, modais, alertas, notificações |
| `js/rules/` | 2 | `slaViewRules`, `ticketViewRules` (funções puras) |
| `js/tests/` | 6 | Vitest (47 testes) |

### Duplicações / imports suspeitos

| Item | Severidade | Detalhe |
|------|------------|---------|
| `ticketDetailsModal.js` | **ALTO** | Maior módulo; mistura DOM, fluxo de ticket, satisfação, escalonamento |
| `relatoriosPage.js` | **MÉDIO** | Cresce com satisfação + relatório de tickets no mesmo arquivo |
| Estilos kanban prioridade em `components.css` e `atendentes.css` | **MÉDIO** | Regras duplicadas; **erro de sintaxe em `atendentes.css`** (chave `{` não fechada em `.analyst-card-header .perfil-acesso-badge` ~L544) pode afetar parse de regras seguintes |
| `presentation.js` (~224 linhas) | **BAIXO** | Hub de formatação — coerente, mas tende a crescer |

---

## 4. Frontend CSS

| Arquivo | Linhas |
|---------|--------|
| `components.css` | ~598 |
| `layout.css` | ~467 |
| `pages/atendentes.css` | ~487 |
| `modals.css` | ~238 |
| `pages/dashboard.css` | ~254 |
| `theme.css` | ~130 |
| `pages/indicadores.css` | ~67 |
| `pages/relatorios.css` | ~26 |
| Demais `pages/*.css` | pequenos |

**`style.css`:** fallback mínimo (comentário Sprint 111) — **OK**.

**Tokens de prioridade (Sprint 120.1):** concentrados em `theme.css` + uso em `components.css` — boa direção.

**Problemas**

- Duplicação kanban/prioridade (`components.css` vs `atendentes.css`).
- `atendentes.css`: possível **CSS inválido** por bloco mal fechado (ver seção de riscos).

---

## 5. HTML (`index.html`)

| Métrica | Valor |
|---------|--------|
| Linhas | **~1.907** |

### Páginas (`section.page`)

| ID | Área |
|----|------|
| `page-dashboard` | Dashboard |
| `page-atendentes` | Kanban / filas |
| `page-perfil` | Perfil do analista |
| `page-clientes` | Cadastro clientes |
| `page-abrir-ticket` | Abertura |
| `page-tickets` | Listagem operacional |
| `page-relatorios` | Relatórios + satisfação |
| `page-indicadores` | Indicadores (subpainéis internos) |
| `page-auditoria` | Auditoria |
| `page-configuracoes` | Configurações |

### Modais principais

- `modalDetalhes`, `modalEscalonamento`, `modalEncerramento`, `modalAnalistaFila`, `modalFotoVisualizar`

### Avaliação

| Aspecto | Classificação |
|---------|----------------|
| Tamanho único monolítico | **Risco MÉDIO–ALTO** para manutenção e conflitos de merge |
| Acoplamento por `getElementById` | **ALTO** — centenas de IDs; mudança de HTML exige busca ampla |
| Subpainéis em Indicadores via `data-indicadores-subpanel` | **Boa** — evita multiplicar `section.page` |

**IDs críticos (amostra):** `page-*`, `navIndicadoresGroup`, `navIndicadoresToggle`, `relatorioFiltro*`, `satisfacao*`, `indicadoresFiltro*`, `ticketsBody*`, `modalDetalhes`, `alertBox*`.

---

## 6. Indicadores (Sprint 123–124)

### Estrutura implementada

| Peça | Status arquitetural |
|------|---------------------|
| Menu expansível + submenu | Alinhado (`layout.css`, `initIndicadoresSidebarNav`) |
| Subpáginas declaradas em `indicadoresSubpages.js` | Alinhado (metadados e chaves estáveis) |
| `indicadoresPage.js` | Alinhado — troca de painel + carga lazy só em **Chamados** |
| `indicadoresService.js` | Alinhado — fino, só HTTP |
| `indicadores.css` | Alinhado — escopo `#page-indicadores` |
| Permissões `PAGE_ACCESS.indicadores` → ADMIN/SUPERVISOR | Alinhado |
| Router tratando `data-indicadores-sub` | Alinhado |

### Prontidão para crescer

**Sim, com ressalvas:**

1. Manter **uma subpágina = um bloco** em `index.html` ou evoluir para templates lazy-load por subpasta `js/pages/indicadores/*`.
2. Novos indicadores devem seguir: **service** → endpoint `/api/indicadores/...` → **subpage** em `indicadoresSubpages.js` → render em `indicadoresPage.js` ou módulo filho.
3. Evitar duplicar satisfação em Relatórios e Indicadores sem plano de migração.

### Próximo indicador mais seguro para implementar

**Indicadores → Satisfação** (placeholder já existe):

- Backend já expõe `/api/tickets/satisfacao/resumo`, `/evolucao`, `/csv` com filtros maduros.
- Migração **somente de visualização gerencial** (cards + tabela evolução) para Indicadores; manter em Relatórios export CSV e contexto de “relatório filtrado” até sprint dedicada de desacoplamento.
- Reaproveitar `satisfacaoService.js` e `buildSatisfacaoResumoParams` — não duplicar query building.

---

## 7. Testes

| Tipo | Quantidade | Resultado (20/05/2026) |
|------|------------|-------------------------|
| Testes JS (Vitest) | **47** em 6 arquivos | **PASS** |
| Testes Java (JUnit) | **17** classes | **PASS** (`mvn clean install`) |
| Build Maven | — | **PASS** |

### Lacunas importantes

| Lacuna | Impacto |
|--------|---------|
| Sem testes e2e / integração HTTP | Regressões de contrato API |
| Sem testes de `indicadoresPage` (só `indicadoresSubpages`) | Regressões de navegação interna |
| Sem testes de `TicketBuscaService` / `IndicadoresChamadosService` integrados ao JPA | Filtros e queries |
| `ticketDetailsModal.js` sem testes | Fluxos críticos de ticket |

### HTTP 200

- Build gera `target/suporte-tickets-1.0.0.jar` com sucesso.
- Após subir o JAR: `GET http://localhost:8080/` → **200**.

---

## 8. Riscos técnicos (classificação)

| Risco | Nível | Descrição |
|-------|--------|-----------|
| `index.html` monolítico (~1,9k linhas) | **ALTO** | Manutenção, IDs frágeis, merges |
| `ticketDetailsModal.js` (~625 linhas) | **ALTO** | Acoplamento de fluxos; difícil testar |
| `app.js` voltar a crescer com lógica de negócio | **MÉDIO** | Tendência histórica; hoje sob controle |
| CSS inválido/duplicado em `atendentes.css` | **MÉDIO** | Pode quebrar estilos kanban/prioridade |
| Indicadores crescer sem padrão (HTML inline gigante) | **MÉDIO** | Mitigado por subpainéis + `indicadoresSubpages.js` |
| Relatórios com satisfação + tickets (dois domínios) | **MÉDIO** | Candidato a split conceitual Dashboard vs Indicadores vs Relatórios |
| Agregações em memória (Dashboard/Indicadores) | **MÉDIO** | Performance futura |
| Endpoints de relatório/CSV sem testes de contrato | **BAIXO–MÉDIO** | Regressão silenciosa |
| `relatoriosPage.js` tamanho | **BAIXO–MÉDIO** | Refatorar quando migrar satisfação |

---

## 9. Recomendações

### Próximos 5 sprints sugeridos (ordem)

1. **Indicadores → Satisfação (UI)** — reutilizar APIs existentes; placeholder → painel real.  
2. **Correção CSS `atendentes.css` + remoção duplicação kanban** — quick win de qualidade visual.  
3. **Decomposição `ticketDetailsModal.js`** — extrair satisfação, escalonamento, encerramento em submódulos/components.  
4. **Indicadores → SLA (leitura)** — consumir `/api/dashboard/sla` ou extrair endpoints dedicados em `/api/indicadores/sla`.  
5. **Testes de contrato** — smoke REST para `/api/indicadores/chamados`, `/api/tickets/satisfacao/*`, busca relatório.

### O que evitar

- Novos gráficos/painéis executivos no **Dashboard** (manter operacional).
- Nova lógica pesada em **`app.js`** ou **`index.html`** sem extrair módulo.
- Duplicar filtros de satisfação (manter `TicketSatisfacaoConsultaService` como fonte única no backend).
- Endpoints de indicadores sem autorização de perfil.

### O que está saudável

- Modularização JS (core/services/pages/components/rules).
- CSS modular + `style.css` mínimo.
- Área Indicadores com submenu e subpáginas (Sprint 124).
- Serviços de satisfação com filtros unificados.
- Vitest em helpers puros (permissions, queryParams, rules).
- Documentação em `docs/FRONTEND_ARQUITETURA.md`.

### Monitoramento contínuo

- Tamanho de `app.js`, `relatoriosPage.js`, `ticketDetailsModal.js`.
- Tempo de resposta de `GET /api/indicadores/chamados` e buscas de relatório com período longo.
- Cobertura de testes Java vs novos services (satisfação/indicadores).

---

## 10. Candidatos a migração futura (sem ação neste raio-x)

| Bloco atual | Onde está | Destino sugerido |
|-------------|-----------|------------------|
| Resumo + evolução + export satisfação | `page-relatorios` / `relatoriosPage.js` | **Indicadores → Satisfação** (gerencial) |
| CSV satisfação | Relatórios | Pode permanecer em Relatórios (export) ou duplicar atalho em Indicadores |
| Cards gerenciais de prioridade | Dashboard (`/api/dashboard/gerencial`) | Manter no Dashboard (operacional ao vivo) vs cópia em Indicadores apenas se necessário |

---

## 11. Execução dos testes (evidência)

```text
cd src/main/resources/static/js && npm test
→ 6 files, 47 tests passed

mvn clean install
→ BUILD SUCCESS (após encerrar JAR em execução que bloqueava clean)

GET http://localhost:8080/
→ 200 (após java -jar target/suporte-tickets-1.0.0.jar)
```

---

*Documento gerado no âmbito da Sprint 125. Nenhum código de produção foi alterado.*
