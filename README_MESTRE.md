# README MESTRE — suporte-tickets

**Fonte central vigente** (Sprint F50). Qualquer IA ou programador deve começar **aqui**.

| Complemento | Quando usar |
|-------------|-------------|
| [Auditoria/AUDITORIA-004-pos-reestruturacao.md](Auditoria/AUDITORIA-004-pos-reestruturacao.md) | Matrizes de risco, antes/depois, referências F41–F48 |
| [Auditoria/AUDITORIA-004-context.json](Auditoria/AUDITORIA-004-context.json) | Contexto JSON compacto para IA |
| [e2e/README.md](e2e/README.md) | Comandos Playwright, massa, troubleshooting E2E |
| [docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md](docs/SPRINT_F46_VALIDACAO_OFICIAL_POS_REESTRUTURACAO.md) | Checklist esteira local |
| [docs/SPRINT_F48_GATE_CI_POS_REESTRUTURACAO.md](docs/SPRINT_F48_GATE_CI_POS_REESTRUTURACAO.md) | Gate GitHub Actions |

**Histórico:** AUDITORIA-001/002/003 (se existirem) + docs com banner — **não** são regra atual.

**Marco:** `REESTRUTURACAO_CONCLUIDA` (F41), validado F45/F46.

---

## 1. O que o sistema faz

Gestão de **tickets de suporte técnico remoto** com atendimento conversacional (Chats), cadastro de **Clientes** (contratantes) e **Contatos** (pessoas atendidas), entrada **receptiva** via WhatsApp (simulada ou integração), abertura **ativa** manual, relatórios/CSV/PDF, dashboard operacional, indicadores gerenciais, configurações globais e auditoria de ações.

Stack: **Spring Boot 3.3** (Java 21) + SPA estática em `src/main/resources/static` + **MySQL 8**.

---

## 2. Quem usa o sistema

| Perfil | Uso |
|--------|-----|
| **Analista / operador** | Chats, tickets, clientes, contatos, encerramento, avaliação |
| **ADMIN** | Configurações, catálogos, usuários, carteiras globais (legado isolado) |
| **Gestor** | Dashboard, indicadores, relatórios |
| **Contato (WhatsApp)** | Mensagens; não acessa o painel |
| **Portal Cliente** | Consulta futura/planejada — não é núcleo operacional atual |

---

## 3. Modelo de negócio atual

```
Cliente contratante (F5)
  → Contato atendido
  → WhatsApp Matriz (número do Cliente na API)
  → Ticket
```

- **Cliente** = contratante; dono de SLA, arte Chats, WhatsApps matriz.
- **Contato** = pessoa atendida; etiquetas no Contato.
- **Ticket** = atendimento; sempre ligado a Cliente + Contato no fluxo atual.
- **Ticket ativo:** um ativo por **Cliente + Contato**; não reutilizar ativo de outro Contato.

**Termos proibidos em UI nova:** Subcliente, Revenda/Conexão como domínio de atendimento, Carteira no Ticket/Chats/Relatórios operacionais.

---

## 4. Fluxos principais

| Fluxo | Entrada | Origem ticket |
|-------|---------|---------------|
| Abertura manual | UI ou `POST /api/tickets` | `ATIVO_MANUAL` |
| Entrada WhatsApp | `POST /api/integracoes/whatsapp/mensagens` | `RECEPTIVO_WHATSAPP` |
| Chats | Lista de tickets / conversas | — |
| Encerramento | UI Chats / API | Exige categoria, subcategoria, motivo, comentário |
| Avaliação | Após resolvido + confirmação | `TicketSatisfacao` |
| Relatórios / CSV / PDF | Filtros + export | Sem conexão/carteira operacional |

---

## 5. Regras de negócio (resumo)

- Encerramento: **Categoria, Subcategoria, Motivo** (motivo da subcategoria, ativo), **Comentário** obrigatórios.
- Motivo inativo não entra em novo encerramento.
- Avaliação: com Contato e envio confirmado → **PENDENTE**; sem Contato ou não enviada → **NAO_ENVIADA**; token/hash não vaza na UI pública.
- SLA: `America/Sao_Paulo`, seg–sex 08:00–18:00 (regras de SLA nos services).
- Erro de negócio → HTTP 4xx adequado, não 500 genérico.
- Falha de auditoria/log/notificação não derruba fluxo principal salvo regra explícita.

---

## 6. Origem dos tickets

| Valor | Significado |
|-------|-------------|
| `ATIVO_MANUAL` | Analista abre com Contato escolhido/cadastrado |
| `RECEPTIVO_WHATSAPP` | Mensagem na matriz + Contato identificado/criado |

Detalhe histórico: `Auditoria/REGRA-ORIGEM-TICKET.md` (ponteiro).

---

## 7. Cliente

- Contratante F5; **sem** `carteira_id` operacional (DROP F40).
- CRUD: `/api/clientes`; arte header Chats por Cliente.
- Não usar Carteira como regra de vínculo de Ticket.

---

## 8. Contato

- Pessoa atendida; `clienteId` obrigatório; WhatsApp **imutável** após criação.
- API: `/api/contatos` (substitui legado `contatos-clientes`).
- Unicidade: `cliente_id` + `whatsapp_normalizado`.

---

## 9. WhatsApp Matriz

- Número do **Cliente** na integração; identifica Cliente na entrada receptiva.
- API: `/api/whatsapp-matrizes`.
- Integração: `POST /api/integracoes/whatsapp/mensagens` com `whatsappMatrizId` ou `numeroMatriz`.

---

## 10. Ticket

- Campos operacionais: `clienteId`, `contatoId`, `origemTicket`, `whatsappMatrizId` (receptivo), status, analista, etc.
- **Sem** `conexao`, **sem** `contato_solicitante_id`, **sem** etiquetas no ticket (F34).

---

## 11. Chats

- Layout: lista + conversa + painel **Cliente / Contato / Entrada / Chamado**.
- Header: arte do **Cliente** ou gradiente; **sem** Carteira/Conexão/Revenda no painel operacional.
- Módulo **não** chama `/api/carteiras` para regra de atendimento.
- Etiquetas: **ContatoEtiqueta** (`/api/contatos/{id}/etiquetas`).

---

## 12. Etiquetas

- **ContatoEtiqueta** — vinculadas ao Contato.
- ~~TicketEtiqueta~~ / ~~`/api/tickets/{n}/etiquetas`~~ — **removidos** (F34).

---

## 13. Satisfação

- Entidade **TicketSatisfacao**; pesquisa ao Contato após encerramento quando aplicável.
- Estados: PENDENTE, RESPONDIDA, NAO_ENVIADA, EXPIRADA (conforme implementação).

---

## 14. Relatórios

- Busca/export com **origem**, cliente, contato, status, datas.
- **Sem** filtro/coluna Conexão ou Carteira operacional.

---

## 15. Dashboard

- Visão operacional resumida; pendências por **Cliente** (não por Conexão operacional).

---

## 16. Indicadores

- Análise gerencial detalhada; dimensões alinhadas ao modelo Cliente/Contato/origem.

---

## 17. Configurações

- Catálogos, usuários, grupos de categoria, etc.
- **Carteira / Conexões / Revendas:** existem em **Config global** (`/api/carteiras`, UI Config) — **isolados**; não comandam Ticket nem Chats operacional.

---

## 18. Auditoria

- Log de ações sensíveis (`AuditoriaService`).
- Pacote vigente: **AUDITORIA-004** + este README.
- 001/002/003 = histórico pré-reestruturação.

---

## 19. Portal Cliente

- Planejado/consultivo futuro; não substitui painel analista atual.

---

## 20. Backend

- Pacote: `com.suporte.tickets`
- Camadas: `controller` → `service` → `repository` / JPA
- Patches DEV: `config/*Patch.java` (DROP/backfill F29–F40) — migração, não regra de produto
- Testes: `mvn test` (JUnit/MockMvc)

---

## 21. Frontend

- `src/main/resources/static/js/`
- `js/services/` — API (sem DOM)
- `js/pages/` — telas
- `js/components/` — componentes
- `js/core/` — compartilhado
- `app.js` — bootstrap
- Testes: Vitest em `static/js`, `npm test`

---

## 22. Estrutura de pastas (essencial)

```text
suporte-tickets/
  README_MESTRE.md          ← você está aqui
  README.md                 ← entrada rápida
  src/main/java/...         ← backend
  src/main/resources/static/js/  ← frontend
  e2e/                      ← Playwright
  scripts/                  ← validação e servidor dev
  Auditoria/                ← AUDITORIA-004 + histórico
  docs/                     ← stubs + sprints F42–F48
```

---

## 23. Entidades principais

Cliente, Contato, WhatsAppMatriz, Ticket, TicketMensagem, ContatoEtiqueta, Etiqueta, Categoria/Subcategoria/Motivo, TicketSatisfacao, Usuario, Carteira (Config apenas).

---

## 24. APIs principais

| Área | Base |
|------|------|
| Auth | `/api/auth/login`, logout |
| Clientes | `/api/clientes` |
| Contatos | `/api/contatos`, `/{id}/etiquetas` |
| WhatsApp matriz | `/api/whatsapp-matrizes` |
| Tickets | `/api/tickets`, busca, relatórios, PDF |
| Integração WA | `/api/integracoes/whatsapp/mensagens` |
| Dashboard | `/api/dashboard` |
| Config carteira | `/api/carteiras` (**isolado**) |

**Removidos (410/ ausente):**

- `/api/contatos-clientes/*`
- `/api/tickets/{numero}/etiquetas`
- Contatos só via `/api/clientes/{id}/contatos` legado — usar `/api/contatos`

---

## 25. Testes automáticos

| Camada | Comando |
|--------|---------|
| Backend | `mvn test` |
| Frontend | `cd src/main/resources/static/js && npm test` |
| Build | `mvn package -DskipTests` (após `scripts/stop-java-8080.ps1` se lock) |

---

## 26. E2E

- Oficial: `e2e/tests/smoke-pos-reestruturacao.spec.ts`
- `cd e2e && npm run test:pos-reestruturacao`
- Detalhes: **e2e/README.md**

---

## 27. Scripts oficiais

| Script | Função |
|--------|--------|
| `scripts/stop-java-8080.ps1` | Liberar porta/JAR |
| `scripts/start-dev-server.ps1` | Subir app |
| `scripts/validar-pos-reestruturacao.ps1` | Gate F46 local |
| `scripts/validar-pos-reestruturacao-ci.ps1` | CI local |

---

## 28. Como rodar

1. MySQL 8 + schema/migrations (perfil dev do projeto).
2. `mvn spring-boot:run` ou `start-dev-server.ps1`.
3. `http://localhost:8080/` — SPA + API mesma origem.
4. Login ADMIN conforme ambiente (não commitar senhas).

Requisitos: Java 21, Maven 3.6+, Node 18+ para testes front/E2E.

---

## 29. Como validar sprint

```powershell
.\scripts\validar-pos-reestruturacao.ps1
```

Ou dois terminais: `start-dev-server.ps1` + `validar-pos-reestruturacao.ps1 -NoAutoStartServer`.

Inclui: Maven test, Vitest, package, HTTP 200, Playwright F45.

---

## 30. CI / Gate

- **Workflow:** `.github/workflows/pos-reestruturacao.yml`  
- **Nome do check (required):** `Gate pos-reestruturacao / Gate pos-reestruturacao`  
- **Doc gate:** `docs/SPRINT_F48_GATE_CI_POS_REESTRUTURACAO.md`  
- **Required check (F51):** `docs/SPRINT_F51_REQUIRED_CHECK_GATE.md`  
- **Validação push/Actions (F52):** `docs/SPRINT_F52_VALIDACAO_REQUIRED_CHECK.md`

### Required check no GitHub (`main`)

1. Settings → Branches → regra em **`main`**.  
2. **Require status checks to pass before merging**.  
3. Selecionar **`Gate pos-reestruturacao / Gate pos-reestruturacao`** (após 1ª execução do workflow).  
4. Secret recomendado: **`SMOKE_ADMIN_SENHA`** (Actions secrets).

Repositório: `https://github.com/robertaobolivia-sudo/Sistema`

### Antes de abrir PR (local)

```powershell
cd "C:\Users\João Falcone\Desktop\Sistema\suporte-tickets"
.\scripts\validar-pos-reestruturacao.ps1
```

Dois terminais: `start-dev-server.ps1` + `validar-pos-reestruturacao.ps1 -NoAutoStartServer`.

---

## 31. Legados removidos (runtime)

- `ticket.conexao` / coluna `tickets.conexao`
- `Cliente.carteira_id` / `clientes.carteira_id`
- `ContatoCliente` / `contatos_clientes`
- `tickets.contato_solicitante_id`
- `TicketEtiqueta` / `ticket_etiquetas`
- Fallback Carteira/Conexão no Chats
- Fallback ContatoCliente na abertura manual
- Fallback TicketEtiqueta no Chats
- Dashboard por Conexão operacional
- CSV/PDF com Conexão operacional
- `/api/contatos-clientes`
- `/api/tickets/{numero}/etiquetas`

---

## 32. Legados isolados (ainda existem)

- Entidade/API **Carteira**, `/api/carteiras`
- Config → Conexões/Revendas
- `uploads/conexoes`
- Backups/patches F29, F34, F38, F40
- Documentos com banner histórico

**Não** comandam Cliente/Ticket/Chats/Relatórios operacionais.

---

## 33. WhatsApp real (pendente)

- **`NoopWhatsAppMessageSender`** = adapter temporário **planejado**, **não é bug**.
- Provider real **não implementado**.
- Entrada receptiva **simulada/autenticada** validada (F43/F45).
- Envio real = bloco futuro dedicado.

---

## 34. Riscos residuais

| ID | Estado |
|----|--------|
| C01 | Mitigado — revisar auth webhook em deploy |
| C02 | Mitigado — ticket ativo Cliente+Contato |
| C03 | Mitigado — ContatoEtiqueta |
| C04 | Aberto — race Chats (refator futuro) |
| M01–M05 | Ver AUDITORIA-004 §9 |

---

## 35. Próximos blocos recomendados

1. **Ativar required check** em `main`: `Gate pos-reestruturacao / Gate pos-reestruturacao` (F51 — UI GitHub)
2. Provider WhatsApp real
3. C04 se refator Chats
4. Deprecar specs E2E F42–F44 isolados quando estável

---

## 36. Guia para nova IA / programador

1. Ler **este README_MESTRE** por completo.
2. Ler `Auditoria/AUDITORIA-004-context.json` + `AUDITORIA-004-pos-reestruturacao.md`.
3. Implementar só com modelo **Cliente → Contato → Matriz → Ticket**.
4. **Não** reintroduzir legados da §31 no fluxo operacional.
5. Validar com `validar-pos-reestruturacao.ps1` antes de fechar sprint de código.
6. E2E: ver `e2e/README.md`.
7. Ignorar AUDITORIA-001/002/003 como verdade de runtime.

---

## 37. Glossário

| Termo | Definição |
|-------|-----------|
| **Cliente** | Contratante F5 |
| **Contato** | Pessoa atendida no WhatsApp |
| **WhatsApp Matriz** | Número do Cliente na API |
| **Ticket** | Registro de atendimento |
| **Origem** | `ATIVO_MANUAL` ou `RECEPTIVO_WHATSAPP` |
| **Etiqueta** | Classificação no **Contato** (ContatoEtiqueta) |
| **Tag** | Sinônimo de etiqueta no produto |
| **Carteira/Conexão** | Apenas Config global legada — não domínio Ticket |

---

*Atualizado: Sprint F50 — centralização documental.*
