# Regra de produto — Tickets indevidos e etiquetas operacionais do Contato

**Sprint 273** — requisito normativo (sem implementação nesta sprint).  
**Base:** [MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md](./MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md) (§9 Interação indevida), Sprints 271–272 (etiquetas operacionais validadas na gestão de Contatos).  
**Status:** decisão de produto para próximas sprints funcionais.

---

## 1. Objetivo

Definir como as etiquetas operacionais do **Contato** — **Indevido**, **Contato Pessoal** e **Propaganda** — afetam **tickets**, **Chats**, **Dashboard**, **Indicadores**, **Relatórios**, **Avaliação** e **SLA**, sem automação silenciosa até implementação explícita.

---

## 2. Princípios (decisões transversais)

| # | Princípio | Decisão |
|---|-----------|---------|
| P1 | Etiqueta pertence ao **Contato**, não ao Ticket | Mantido (Sprint 271). Classificação do **ticket** é efeito derivado, auditável. |
| P2 | Automação ao **aplicar** etiqueta no Contato | **Não** alterar status do ticket **imediatamente** (validado na Sprint 272). |
| P3 | Ação sobre ticket indevido | Sempre com **confirmação do analista** (salvar etiqueta, encerrar, ou ação dedicada no Chats/Ticket). |
| P4 | Escopo B2B | **Regra única F5** na fase 1; parâmetro **por Cliente** (opt-in/opt-out de fluxo) fica para fase 2 em Configurações do Cliente. |
| P5 | Auditoria | Toda classificação indevida gera trilha (quem, quando, contato, ticket, etiqueta(s), motivo de encerramento se houver). |
| P6 | Alinhamento ao modelo §9 | **Indevido** e **Contato Pessoal** seguem lógica de **não contabilizar** atendimento; **Propaganda** com regra distinta (§4.3). |

---

## 3. Comportamento por etiqueta operacional

### 3.1 Indevido

| Dimensão | Comportamento esperado |
|----------|------------------------|
| **Significado** | Contato não é cliente legítimo do canal (erro de número, spam operacional, demanda fora do escopo do contratante). |
| **Ticket ativo existente** | Não muda sozinho ao marcar etiqueta. Analista vê **aviso** (já na UI) e, se confirmar, **encerra** o(s) ticket(s) ativo(s) como **indevido** (§5). |
| **Nova mensagem (entrada)** | Com etiqueta já no Contato: **não** abre ticket contabilizado; registra **interação indevida** (modelo §9). Sem etiqueta: fluxo normal até triagem. |
| **Chats** | Sai de **Fila** e **Atendendo** após classificação indevida; aparece em grupo/lista **“Não atendimento / Indevidos”** (somente leitura + encerrar/auditar). |
| **Métricas** | **Não** conta como atendimento resolvido, TMA, SLA de resolução, nem volume de chamados do Dashboard operacional. |
| **Avaliação** | **Não** enviar pesquisa; pendências existentes → **NAO_ENVIADA** ou cancelamento controlado. |
| **Relatórios** | Excluído por **padrão**; filtro “Incluir indevidos” para auditoria. |
| **Encerramento** | Motivo de encerramento obrigatório da família **“Indevido / Não atendimento”** (Categoria/Subcategoria/Motivo cadastrados pelo ADMIN). |

### 3.2 Contato Pessoal

| Dimensão | Comportamento esperado |
|----------|------------------------|
| **Significado** | Pessoa conhecida do time/contratante, conversa não comercial (ex.: familiar, contato interno no WhatsApp do Cliente). |
| **Ticket / entrada / Chats / métricas / avaliação / relatórios** | **Mesmas regras de Indevido** (P6), com rótulos e motivos de encerramento que permitam distinguir em relatório de auditoria (ex.: submotivo “Contato pessoal”). |
| **Diferença de negócio** | Indicador separado “fluxo pessoal” opcional no futuro; na fase 1 agrupa com indevidos em **não atendimento** para fila e SLA. |

### 3.3 Propaganda

| Dimensão | Comportamento esperado |
|----------|------------------------|
| **Significado** | Contato usado para divulgação, listas, ofertas em massa; não é demanda de suporte. |
| **Ticket ativo existente** | Igual P2/P3: **sem** mudança automática; confirmação para encerrar como indevido ou como **“Propaganda / sem atendimento”**. |
| **Nova mensagem** | Com etiqueta: **não** abre ticket contabilizado na fase 1 (extensão do §9); registro de interação indevida ou fila **“Propaganda”** somente leitura (decisão UX fase 2). |
| **Bloqueio de tickets futuros** | **Não** bloqueio técnico permanente na fase 1; **sim** impedir **abertura automática** de ticket ativo enquanto etiqueta estiver ativa. Remoção da etiqueta restaura fluxo normal na **próxima** mensagem. |
| **Chats** | Fora de Fila/Atendendo após classificação; grupo visual **“Propaganda”** ou junto a “Não atendimento”. |
| **Métricas / SLA / avaliação** | Igual indevido: **fora** de KPIs operacionais e SLA. |
| **Relatórios** | Excluído por padrão; contagem opcional em indicador “volume propaganda” por Cliente (fase 2). |

---

## 4. Respostas às perguntas de produto

| Pergunta | Decisão |
|----------|---------|
| Ao aplicar etiqueta operacional no Contato, o ticket ativo deve mudar **imediatamente**? | **Não.** Manter estado até ação confirmada. |
| O analista precisa **confirmar** antes de marcar o ticket como indevido? | **Sim**, sempre (modal ao salvar etiqueta com ticket ativo, ou botão “Classificar como indevido” no Chats/Ticket). |
| Ticket indevido deve **sair** da Fila/Atendendo? | **Sim**, após classificação. |
| Ticket indevido deve **contar como atendimento**? | **Não** nos indicadores operacionais e Dashboard gerencial padrão. |
| Ticket indevido deve **contar em SLA**? | **Não**; relógio de SLA **congela ou exclui** a partir da data/hora da classificação indevida (implementação: campo ou status terminal). |
| Ticket indevido deve **receber avaliação**? | **Não.** |
| Ticket indevido deve **aparecer em Relatórios**? | **Sim**, com **exclusão padrão** e filtro explícito para inclusão. |
| Deve existir motivo específico de encerramento **“Indevido”**? | **Sim** — catálogo ADMIN (Categoria → Subcategoria → Motivo), incluindo variantes Pessoal e Propaganda. |
| Contato **Propaganda** deve **bloquear** novos tickets futuros? | **Não** bloqueio de cadastro; **sim** bloqueio de **ticket ativo automático** e de contagem em KPIs enquanto a etiqueta existir. |
| A regra vale para todos os Clientes B2B ou pode variar? | **Todos** na fase 1; variação **por Cliente** = fase 2 (parâmetro em cadastro do Cliente). |

---

## 5. Status, invalidação e automação

### 5.1 O ticket deve ser “invalidado”?

| Modo | Uso |
|------|-----|
| **Nunca automaticamente** (só etiqueta no Contato) | Vigente até Sprint 272 — **mantido** como padrão ao salvar etiqueta. |
| **Com confirmação do analista** | **Padrão alvo** ao marcar Indevido/Pessoal/Propaganda com ticket ativo: “Encerrar chamado ativo como indevido?” |
| **Somente no encerramento** | Alternativa aceita se analista preferir fluxo único: encerrar pelo modal habitual escolhendo motivo **Indevido** (sem mudar regra de entrada). |
| **Automaticamente** | **Rejeitado** na fase 1 (risco operacional e conflito com Sprint 272). Reavaliar só com flag **por Cliente** + política escrita. |

### 5.2 Status / grupo visual do ticket

**Decisão de produto (fase 1 implementação):**

- Criar status terminal **`INDEVIDO`** (descrição UI: “Indevido” ou “Não atendimento”), **fora** de `STATUS_ATIVOS` (ABERTO, EM_ATENDIMENTO, AGUARDANDO_CLIENTE).
- **Alternativa técnica equivalente:** `CANCELADO` + flag `classificacaoOperacional = INDEVIDO | PESSOAL | PROPAGANDA` — aceita se evitar migração de enum; a UI deve tratar como **grupo visual único “Não atendimento”**, distinto de “Encerrados” resolvidos.
- **Chats:** terceira área ou aba colapsável — **Não atendimento** (Indevido + Pessoal + Propaganda), sem contador na fila operacional principal.
- **Tickets (listagem):** filtro status inclui “Indevido”; badge/cor dedicada (ex.: cinza/listrado), não confundir com RESOLVIDO.

Transição permitida:

- Ativo → **INDEVIDO** (com confirmação + motivo encerramento).
- **INDEVIDO** → não reabre como ativo sem **novo ticket** explícito (UC-M4) ou política futura de reabertura restrita a SUPERVISOR.

---

## 6. Impacto por área

| Área | Impacto | Fase 1 (resumo) |
|------|---------|------------------|
| **Chats** | Fila/Atendendo sem tickets classificados indevido; área Não atendimento; aviso se Contato tem etiqueta operacional; ação confirmada para classificar. | Sprint funcional dedicada |
| **Tickets** | Status/filtro Indevido; encerramento com motivo; histórico preservado; sem reabertura silenciosa. | Junto backend + UI ticket |
| **Dashboard** | Contagens de abertos/em atendimento **excluem** indevidos; card opcional “Não atendimento (hoje)”. | Após status definido |
| **Indicadores** | KPIs de volume, TMA, SLA, satisfação **excluem** indevidos; série “fluxo indevido” por Cliente (volume). | Filtro query + doc métricas |
| **Relatórios** | Default sem indevidos; coluna/filtro etiqueta operacional do Contato; export respeita filtro. | Relatórios operacionais |
| **Avaliação** | Bloqueio de envio; pendente cancelada; link público não criado para indevido. | Regra no encerramento/classificação |
| **SLA** | Não conta tempo após classificação; primeiro atendimento/resolução não meta se só indevido. | Campos data + exclusão em cálculo |
| **Entrada de mensagem** | Com etiqueta Indevido/Pessoal/Propaganda: interação indevida, sem ticket ativo novo (alinha §7 item 7 do modelo). | Sprint integração |
| **Contatos (gestão)** | Mantém aviso e destaque (271); adiciona fluxo de confirmação encerramento (273→implementação). | UX pós-regra |

---

## 7. Automação — registro explícito

| Gatilho | Automação fase 1 | Fase 2 (opcional) |
|---------|------------------|-------------------|
| Salvar etiqueta operacional no Contato | Apenas **aviso** + oferta de ação (confirmada) | Política por Cliente |
| Nova mensagem com Contato etiquetado | **Não** criar ticket contabilizado (interação indevida) | Regras finas Propaganda vs Indevido |
| Encerramento manual com motivo Indevido | Transição para status/flag indevido | — |
| Scheduler / jobs | Nenhum | Expurgo/arquivamento indevidos antigos |

---

## 8. Próximas sprints funcionais sugeridas

| Sprint | Escopo | Dependência |
|--------|--------|-------------|
| **274** | Backend: status ou flag `INDEVIDO`, motivos de encerramento (seed ADMIN), API classificar com confirmação, exclusão de `STATUS_ATIVOS`, testes unitários | Este documento |
| **275** | Front Chats + Contatos: confirmação ao salvar etiqueta, área Não atendimento, remover da fila, Vitest | 274 |
| **276** | Dashboard + Indicadores + Relatórios: filtros default, fluxo indevido por Cliente | 274 |
| **277** | Entrada de mensagem (integração): interação indevida + Propaganda sem ticket ativo; SLA congelado | 274 |
| **278** | Smoke E2E + documentação operacional para analistas | 275–277 |

Ordem pode ser ajustada se prioridade for só Chats antes de relatórios.

---

## 9. Riscos e pendências

| Risco | Mitigação |
|-------|-----------|
| Tickets ativos “esquecidos” com etiqueta só no Contato | Oferta de confirmação ao salvar etiqueta + relatório “Ativos com etiqueta operacional” |
| Duplicidade CANCELADO vs INDEVIDO | Decidir na 274 um único caminho; UI unificada |
| Dados legados com etiqueta e ticket ativo | Job de relatório (não auto-encerrar) na primeira implantação |
| Propaganda vs Indevido na mesma pessoa | Prioridade: **Indevido** > **Pessoal** > **Propaganda** se múltiplas etiquetas (fase 2); fase 1: analista remove etiqueta incorreta |

**Pendência de produto:** texto exato dos modais de confirmação e se SUPERVISOR pode reverter INDEVIDO → novo ticket sem ADMIN.

---

## 10. Referências

- Sprints 271 (etiquetas), 272 (smoke uso).
- Modelo oficial §§ 7, 9, 10, 11.
- Implementação atual: **sem** status `INDEVIDO`; tickets **não** alterados ao marcar etiqueta (evidência Sprint 272).
