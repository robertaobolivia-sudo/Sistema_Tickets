# Plano técnico de compatibilidade — Carteira, Cliente e Contato

**Sprint 186** — planejamento de migração gradual (somente documentação).  
**Objetivo:** definir como o projeto convive com estruturas legadas e o modelo oficial, **antes** da primeira alteração de backend.

> **Atualização Sprint 187:** este plano **conservador** continua documentado para referência. A estratégia **ativa** do projeto, em ambiente **embrionário**, é a [reestruturação direta](./ESTRATEGIA_REESTRUTURACAO_DIRETA.md) — menor compatibilidade longa, migração e remoção de legado por fases curtas com backup.

---

## 1. Objetivo

Estabelecer estratégia, fases, campos preservados, estruturas futuras e critérios de entrada para implementação, de forma que:

- tickets, Chats, relatórios e integrações **continuem funcionando** durante a transição;
- **nenhum** rename destrutivo de tabela/coluna ocorra no primeiro incremento;
- o modelo oficial ([MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md)) seja alcançado por **camadas paralelas** e **preenchimento gradual**.

---

## 2. Referências

| Documento | Conteúdo |
|-----------|----------|
| [COLETA_MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./COLETA_MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md) | Estado atual do código (Sprint 184) |
| [MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md) | Regras e modelo-alvo (Sprint 185) |
| [ESTRATEGIA_REESTRUTURACAO_DIRETA.md](./ESTRATEGIA_REESTRUTURACAO_DIRETA.md) | Estratégia de implementação ativa (Sprint 187) |
| `AGENTS.md` | Processo de backup, build e testes nas sprints de código |

**Nomenclatura neste plano (evitar ambiguidade):**

| Termo no plano | Significado |
|----------------|-------------|
| **Cliente legado** | Entidade JPA `Cliente` / tabela `clientes` (cadastro atendido hoje) |
| **Carteira legado** | Entidade `Carteira` / tabela `carteiras` (agrupamento conexão/revenda) |
| **Cliente contratante (alvo)** | Contratante F5 do modelo oficial |
| **Contato WhatsApp (alvo)** | Pessoa final atendida pelo WhatsApp |

---

## 3. Estado atual resumido

### Cliente legado (`clientes`)
- Cadastro da **empresa/pessoa atendida** (nome, telefone, e-mail, empresa, CNPJ, endereço, status, observações).
- FK opcional **`carteira_id`** → `Carteira`.
- **`arte_header_chats_url`** (arte WL no cadastro errado semanticamente, mas já usada no Chats).
- **`classificacao_cliente`** (enum legado).
- API: `/api/clientes`, busca, contatos aninhados, upload arte.

### Carteira legado (`carteiras`)
- Agrupamento **conexão/revenda** (nome, ativo, **`arte_header_chats_url`** legado).
- Configurações ainda expõem cadastro; arte principal migrada para Cliente legado na UI, endpoint legado de arte em carteira mantido.
- Equivale semanticamente ao **Cliente contratante (alvo)**.

### `Ticket.conexao`
- **String** (até 100 chars), **sem FK**.
- Preenchida com nome da Carteira / fluxo Abrir ticket / webhook.
- Usada em dashboard “por conexão”, filtros de tickets, CSV “Conexão/Carteira”.

### `ContatoCliente` (`contatos_clientes`)
- Contato **interno** da empresa (cargo, telefones, e-mail) vinculado ao **Cliente legado**.
- Opcional em ticket: **`contato_solicitante_id`**.
- **Não** é o Contato WhatsApp do modelo oficial.

### `TicketEtiqueta` / `Etiqueta`
- Tags no **ticket** (N:N `ticket_etiquetas`).
- Chats e APIs `GET/PUT /api/tickets/{numero}/etiquetas`.

### `TicketSatisfacao`
- 1:1 com ticket; registro **manual** pelo analista; sem envio WhatsApp nem expiração.

### `GrupoCategoria` / `SubgrupoCategoria`
- Encerramento obrigatório (grupo + subgrupo + comentário).
- **Motivo** inexistente.

### `IntegracaoWhatsappMensagemRequestDTO`
- Campos: `telefone`, `nomeContato`, `mensagem`, `canal`, **`clienteId`** (legado), **`contatoSolicitanteId`** (ContatoCliente), `origemExternaId`.
- **Sem** identificador de WhatsApp matriz.
- Fluxo: busca ticket ativo ou cria ticket via webhook interno.

### Ticket (FK atual)
- **`cliente_id`** obrigatório → Cliente legado.
- Denormalização no `TicketResponseDTO` (nome cliente, telefones, `carteira`, `conexao`, `clienteId`, `clienteArteHeaderChatsUrl`).

---

## 4. Modelo alvo resumido

| Conceito alvo | Papel |
|---------------|--------|
| **Cliente contratante** | Pai da operação; indicadores; arte WL; inativação desconecta matrizes |
| **WhatsApp matriz** | Número API do contratante; resolve Cliente na entrada |
| **Contato WhatsApp** | Pessoa atendida; WhatsApp imutável; tags; UK `(cliente_contratante_id, whatsapp)` |
| **Ticket** | `cliente_contratante_id` + `contato_id` (+ matriz origem) |
| **Tags** | No Contato (`contato_tag`) |
| **Categoria / Subcategoria / Motivo** | Encerramento do ticket |
| **Avaliação** | WhatsApp pós-RESOLVIDO, opt-in, expiração, visibilidade por perfil |

Detalhes normativos: documento oficial Sprint 185.

---

## 5. Alternativas avaliadas

### Alternativa A — Promover Carteira → Cliente contratante; Cliente legado → Contato (futuro)

| Dimensão | Avaliação |
|----------|-----------|
| **Ideia** | Renomear/reaproveitar tabela `carteiras` como contratante; migrar dados de `clientes` para nova semântica de Contato na mesma tabela ou swap de nomes. |
| **Vantagens** | Menos tabelas novas; `carteira_id` e `conexao` alinhados naturalmente ao contratante; dashboard “conexão” aproxima-se do alvo. |
| **Riscos** | Rename de `clientes` quebra FK em massa (`tickets.cliente_id`); JPA `Cliente` usado em dezenas de pontos; rollback difícil. |
| **Banco** | Migrations rename pesadas; janela de indisponibilidade conceitual alta. |
| **Backend** | Refactor amplo de entidades, repositories, DTOs, testes. |
| **Frontend** | Tela Clientes hoje = legado; teria de virar Contato ou duplicar durante transição confusa. |
| **Relatórios** | Coluna “Cliente” no CSV hoje = legado; rename exige compatibilidade em export. |
| **Migração** | **Alto** risco se feito cedo. |
| **Recomendação** | **Não** como primeira onda; possível **fase final** de limpeza se nomes físicos forem unificados. |

### Alternativa B — Manter Cliente legado; criar `ClienteContratante` separado

| Dimensão | Avaliação |
|----------|-----------|
| **Ideia** | Nova entidade contratante; Cliente legado permanece; Ticket ganha FK opcional ao contratante; Carteira vira alias ou é absorvida depois. |
| **Vantagens** | Sem rename de `clientes`; tickets antigos intactos; leitura explícita no código (`ClienteContratante` vs `Cliente`). |
| **Riscos** | Dois tipos “cliente” no código por longo período; duplicação Carteira vs ClienteContratante se não houver 1:1 rígido. |
| **Banco** | Nova tabela + FKs opcionais em ticket; baixo impacto em linhas antigas. |
| **Backend** | Médio: novos services + resolver de compatibilidade Carteira↔Contratante. |
| **Frontend** | Pode adiar mudança de tela Clientes; Chats usa resolver para header/contratante. |
| **Relatórios** | Adicionar colunas sem remover legado. |
| **Migração** | **Médio**; preenchimento paralelo Carteira→Contratante. |
| **Recomendação** | Viável; boa clareza semântica no Java. |

### Alternativa C — Novas entidades alvo + legado intacto até descontinuação

| Dimensão | Avaliação |
|----------|-----------|
| **Ideia** | Criar `cliente_contratante` (ou mapear 1:1 inicial a `carteira_id`), `whatsapp_matriz`, `contato_atendimento`; manter `Cliente`, `Carteira`, `Ticket.conexao`, `TicketEtiqueta` até fase de limpeza. |
| **Vantagens** | **Menor risco** de quebra; migração gradual; testes por feature; rollback por feature flag. |
| **Riscos** | Mais tabelas e lógica duplicada temporária; disciplina para não esquecer caminho legado. |
| **Banco** | Apenas **ADD** (colunas/tabelas novas), sem DROP/rename inicial. |
| **Backend** | Camada **CompatibilidadeResolver** central; DTOs com campos legados + novos opcionais. |
| **Frontend** | Mudanças incrementais; Chats pode priorizar novos IDs quando presentes. |
| **Relatórios** | Período longo com colunas legado + novas. |
| **Migração** | **Baixo** risco por etapa. |
| **Recomendação** | **Escolhida** (ver seção 6). |

---

## 6. Estratégia recomendada

**Adotar Alternativa C** com mapeamento inicial **Carteira legado ≡ Cliente contratante** (sem renomear tabela):

1. **Identificador de contratante na fase 1:** usar `carteira.id` como `clienteContratanteId` lógico (ou tabela espelho 1:1 `cliente_contratante` com PK = `carteira_id` apenas se produto exigir campos extras antes de evoluir Carteira).
2. **Cliente legado** permanece referenciado por `tickets.cliente_id` até migração ticket a ticket para `contato_atendimento_id`.
3. **`Ticket.conexao`** continua sendo **escrita e lida**; serviços novos **preenchem** `conexao` a partir do nome da Carteira/contratante para compatibilidade com dashboard/CSV.
4. **ContatoCliente** permanece para Abrir ticket manual; **Contato atendimento** nasce para WhatsApp; vínculo opcional `contato_atendimento.legacy_cliente_id` para backfill.
5. **Arte WL:** fase de compatibilidade lê **Cliente legado** e **Carteira** como hoje; depois move fonte canônica para contratante.
6. **Tags:** manter `TicketEtiqueta` em produção; introduzir `contato_tag` em paralelo; Chats passa a ler tags do contato quando `contato_atendimento_id` existir, senão fallback ticket.

Princípios (alinhados ao modelo oficial §20):

- Não renomear `Cliente`/`Carteira` no schema até fase de limpeza aprovada.
- Não remover `conexao`, `carteira_id`, payloads nem filtros atuais.
- Toda API pública existente mantém contrato; campos novos **opcionais** nos DTOs.

---

## 7. Primeiro incremento recomendado (implementação real pós-plano)

**Título sugerido:** *Compatibilidade — WhatsApp matriz + resolver de contratante (Carteira)*

**Escopo mínimo do incremento (não é prompt de sprint):**

1. **Nova tabela `whatsapp_matriz`** com FK **`carteira_id`** (contratante legado), `numero_e164`, `ativo`, metadados de integração (sem desconectar produção legado ainda).
2. **Service `ModeloCompatibilidadeService` (ou pacote `compat`):**
   - `resolverContratantePorCarteiraId(id)` / `resolverContratantePorConexao(String conexao)`;
   - `resolverContratantePorWhatsappMatrizId(id)`;
   - normalização de telefone compartilhada para uso futuro em Contato.
3. **DTOs aditivos** (ex.: `TicketResponseDTO`): `clienteContratanteId` (nullable, = carteira_id lógico), `whatsappMatrizId` (nullable) — **sem remover** campos atuais.
4. **Estender `IntegracaoWhatsappMensagemRequestDTO`** com campo opcional `whatsappMatrizId` (ou número matriz); se ausente, comportamento **idêntico ao atual**.
5. **Testes unitários** do resolver e da matriz; **zero** alteração obrigatória em telas Clientes/Tickets/Chats na mesma entrega (opcional: log/feature flag).

**Por que este incremento primeiro:** habilita o eixo de entrada (matriz → contratante) sem tocar em `tickets.cliente_id`, sem mover etiquetas e sem renomear entidades — atende menor risco e não quebra Chats/relatórios.

**Segundo incremento provável (apenas referência):** `contato_atendimento` + preenchimento na integração WhatsApp + FK opcional no ticket.

---

## 8. Campos legados a preservar (não remover até fase limpeza)

| Artefato | Motivo |
|----------|--------|
| Tabela **`carteiras`** e entidade `Carteira` | Contratante lógico fase 1; config e índice arte legado Chats |
| **`clientes.carteira_id`** | Vínculo legado cadastro → carteira |
| Tabela **`clientes`** e entidade `Cliente` | FK `tickets.cliente_id`; tela Clientes; busca webhook |
| **`tickets.conexao`** | Dashboard, filtros, CSV, Abrir ticket, webhook |
| **`clientes.arte_header_chats_url`** | Chats (prioridade Sprint 181) |
| **`carteiras.arte_header_chats_url`** | Fallback Chats |
| **`ticket_etiquetas`** / `TicketEtiqueta` | Chats e APIs atuais |
| **`tickets.contato_solicitante_id`** / `ContatoCliente` | Abrir ticket e tickets com contato interno |
| **Payloads** `TicketWebhookRequestDTO` / criação ticket: `cliente`, `telefone`, `conexao`, `canal`, `contatoSolicitanteId` | Integrações e Abrir ticket |
| **`TicketResponseDTO`:** `cliente`, `carteira`, `conexao`, `clienteId`, `clienteArteHeaderChatsUrl`, campos espelhados | Chats, modal, listagens |
| **Filtros relatórios/tickets:** `cliente`, `conexao`, `classificacaoCliente` (ignorado mas presente) | Contratos de busca |
| **`IntegracaoWhatsappMensagemRequestDTO.clienteId`** | Compatibilidade integração atual |
| **Grupo/Subgrupo** em encerramento | Fluxo produtivo |
| **`TicketSatisfacao`** atual | Até novo fluxo de avaliação |

---

## 9. Novas estruturas futuras (sugestão lógica / física)

| Estrutura | Propósito | Vínculo compatibilidade |
|-----------|-----------|-------------------------|
| **`cliente_contratante`** (opcional espelho) | Campos extras do contratante | 1:1 `carteira_id` na fase 1, ou usar só `carteiras` |
| **`whatsapp_matriz`** | Número API | FK `carteira_id` → contratante legado |
| **`contato_atendimento`** | Contato WhatsApp | FK contratante + `whatsapp_e164` UK; `legacy_cliente_id` nullable |
| **`contato_tag`** | Tags no contato | Migração futura a partir de `ticket_etiquetas` |
| **`ticket.contato_atendimento_id`** | Novo vínculo ticket | Nullable; convive com `cliente_id` |
| **`ticket.cliente_contratante_id`** ou derivar só de matriz | Contratante explícito | Nullable; derivável de matriz/carteira |
| **`motivo`** + FK em ticket encerramento | Terceiro nível encerramento | Novo catálogo ADMIN |
| **`interacao_indevida`** | Fluxo tag indevido | FK contratante + contato + matriz |
| **`avaliacao_atendimento`** (evolução `ticket_satisfacao`) | Status PENDENTE/RESPONDIDA/EXPIRADA, `expira_em`, opt-in | Convive com tabela atual; migrar depois |

---

## 10. Plano de migração gradual

| Fase | Nome | Atividades |
|------|------|------------|
| **0** | **Compatibilidade (documentação + resolver)** | Este plano; ADR interno; resolver Carteira/conexao/matriz; DTOs aditivos |
| **1** | **Preenchimento paralelo** | Cadastro matrizes; criar `contato_atendimento` na integração; preencher FKs novas em tickets **novos**; manter legado |
| **2** | **Adaptação telas** | Clientes → gestão contratante + Contatos; Chats usa contato/tags; Abrir ticket alinhado |
| **3** | **Adaptação relatórios/indicadores** | Colunas contratante/contato/motivo; agregação por `carteira_id` lógico → contratante |
| **4** | **Descontinuação legado** | Parar escrita em `TicketEtiqueta`; parar uso de `cliente_id` como pessoa WhatsApp; deprecar `conexao` só leitura |
| **5** | **Limpeza** | Remover colunas/tabelas obsoletas **apenas** com backup e métricas zeradas de uso legado |

**Backfill (futuro, não executar na Sprint 186):** script batch `cliente` legado → `contato_atendimento` por `(carteira_id, telefone)`; copiar últimas etiquetas de ticket para contato; associar `ticket.cliente_id` legado ao contato criado.

---

## 11. Riscos

### Baixo
- Documentação e resolver somente leitura mal configurado (corrigível sem dado).
- Campos DTO novos ignorados pelo frontend antigo.

### Médio
- Duplicação contratante (`Carteira` vs futura tabela espelho).
- Indicadores misturando “totalClientes” legado com contratante.
- Arte WL em Cliente legado vs contratante durante transição.

### Alto
- Rename imediato de `clientes` ou remoção de `tickets.cliente_id`.
- Mover etiquetas para Contato **sem** fallback em tickets antigos.
- Alterar assinatura obrigatória de webhook/integração sem versão.
- Desativar `Ticket.conexao` antes de dashboard/CSV migrarem.

---

## 12. Checklist para iniciar implementação

Antes do primeiro PR de código (entidade/migration):

- [ ] Product owner confirmou **Alternativa C** e mapeamento **Carteira = contratante fase 1**.
- [ ] Backup `BKP_Sprint_XX_*` definido no `AGENTS.md`.
- [ ] Plano de rollback: migration apenas ADD; feature flag para integração com `whatsappMatrizId`.
- [ ] Matriz de testes: ticket antigo sem FK nova; ticket novo com matriz; integração sem matriz (regressão); Chats header inalterado.
- [ ] Lista de endpoints **não breaking** revisada (coleta §3).
- [ ] Decisão sobre tabela `cliente_contratante` espelho vs usar só `carteiras` registrada.
- [ ] Política de telefone (E.164) documentada para UK de contato.
- [ ] Nenhuma tarefa de rename `Cliente`/`Carteira` na mesma sprint que cria matriz.

---

## 13. Regras para Cursor / IA

1. **Não** renomear entidades/tabelas `Cliente` ou `Carteira` sem sprint de limpeza dedicada e migração aprovada.
2. **Não** remover `Ticket.conexao`, `carteira_id`, `TicketEtiqueta`, nem campos de `TicketResponseDTO` existentes.
3. **Não** alterar comportamento padrão de integração WhatsApp quando `whatsappMatrizId` estiver ausente.
4. Implementações novas devem passar pelo **resolver de compatibilidade** para contratante e, quando existir, contato atendimento.
5. Preferir **ADD** em schema; evitar DROP/RENAME até fase 5.
6. Tags novas no **Contato**; não remover API de etiquetas do ticket até descontinuação explícita.
7. Atualizar este plano se a estratégia mudar; o modelo oficial prevalece para **regras de produto**.
8. Em sprints de código: seguir `AGENTS.md` (backup, `mvn clean install`, `npm test` se JS alterado).

---

## Pendências / perguntas abertas

1. Usar **somente `carteiras`** como contratante na fase 1 ou criar **`cliente_contratante`** 1:1 desde o início?
2. Momento exato de obrigar `whatsappMatrizId` na integração (versão 2 da API?).
3. `contato_atendimento.legacy_cliente_id` obrigatório no backfill ou só best-effort?
4. Feature flag global ou por carteira/contratante para fluxo novo de mensagem?

---

*Plano Sprint 186 — nenhuma alteração de código, schema ou dados.*
