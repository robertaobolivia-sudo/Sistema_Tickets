# Dashboard Operacional — Blueprint (Sprint 279)

**Status:** documento oficial de produto/UX para a próxima fase de implementação do Dashboard.  
**Tipo:** sprint documental — **sem** alteração de regras de negócio (incl. INDEVIDO), **sem** mudança em Indicadores/Relatórios, **sem** implementação pesada nesta sprint.

---

## 1. Papel do Dashboard vs Indicadores

| Tela | Papel | Filtros |
|------|--------|---------|
| **Dashboard** | Visão **operacional em tempo real** — o que está acontecendo **agora** na fila, no atendimento, no SLA vivo, nos encerramentos **de hoje** e na avaliação **em curso**. | **Sem** filtros analíticos de período (7/30/90 dias, intervalo customizado, etc.). Cortes permitidos: **hoje** (implícito onde couber), **Cliente B2B** no bloco dedicado e drill-down **Acompanhamento** (somente leitura). |
| **Indicadores** | Análise gerencial com **período**, comparativos, rankings, satisfação agregada, encerramento por motivo, etc. | Filtros de data, cliente, motivo, nota, status de pesquisa — **concentrados aqui**. |
| **Relatórios** | Exportação e listagem operacional **detalhada** com filtros amplos. | Idem Indicadores (não duplicar no Dashboard). |

**Regra registrada (obrigatória):**  
> O Dashboard **não** é tela de consulta histórica. Qualquer necessidade de “como foi no mês”, “top motivos no trimestre” ou “evolução de nota” → **Indicadores** ou **Relatórios**.

**Escopo operacional válido no Dashboard (contagens “agora”):**

- Chamados **abertos** de dias anteriores ainda na fila ou em atendimento.
- Chamados **reabertos** (após encerramento, aguardando decisão ou novo ciclo — alinhado ao fluxo Chats “pendência de decisão”).
- Chamados **abertos hoje** (volume do dia, não série histórica).
- **Fila** e **Em atendimento** (estados ativos).
- **SLA atual** (primeiro atendimento e resolução **vivos**, não médias históricas de período).
- **Encerramentos do dia** (volume e resumo do dia corrente, timezone `America/Sao_Paulo`).
- **Avaliação em tempo real** (pendentes, enviadas hoje, respondidas hoje — sem painel de período 30/90 dias).

**Fora do painel operacional principal (gerencial):** tickets **INDEVIDO** / Não atendimento — tratados em Chats, Indicadores e Relatórios (Sprints 274–277). **Não** abrir o Dashboard com bloco “Tickets Devidos/Indevidos”.

---

## 2. Remoção do blueprint anterior

| Item removido | Motivo |
|---------------|--------|
| Bloco inicial **“Tickets Devidos / Indevidos”** | Mistura conceito de **classificação terminal** (Não atendimento) com **operação ao vivo**. Polui a entrada da tela e confunde com fila/atendimento. |
| Cards genéricos “Total / Resolvidos / Cancelados” como **hero** da página | Substituídos pela narrativa **Operação Agora**; totais históricos permanecem em Indicadores. |

O layout **atual** em produção (cards legados + encerramento com seletor 7/30/90 dias) é **transição** até as sprints de implementação abaixo; este blueprint descreve o **alvo**.

---

## 3. Ordem da página — começa em “Operação Agora”

A primeira dobra visível após o título **Dashboard** (subpágina **Visão geral**) é:

### Bloco 0 — Operação Agora (hero operacional)

Dois painéis lado a lado (ou empilhados em mobile):

| Painel | Conteúdo | Fonte de dados (alvo) |
|--------|----------|------------------------|
| **Em Atendimento** | Contagem + lista resumida (top N) de tickets `EM_ATENDIMENTO` com analista, cliente, tempo no status | API operacional tempo real (nova ou extensão de dashboard) |
| **Em Fila** | Contagem + lista resumida de tickets `ABERTO` + `AGUARDANDO_CLIENTE` (fila operacional) | Idem |

**Inclui explicitamente:** tickets abertos em dias anteriores ainda ativos; reabertos/pendência de decisão conforme regra Chats (badge/lista dedicada se necessário).

**Atualização:** polling leve (ex. 30–60 s) ou SSE futuro; botão **Atualizar** mantém override manual.

---

## 4. Blocos finais (Visão geral)

### 4.1 Operação Agora

Ver §3. Sem filtro de período.

### 4.2 Analistas Online

**Objetivo:** quadro humano da capacidade operacional.

| Elemento | Especificação |
|----------|-------------|
| Layout | Kanban ou grade por **status operador** |
| Colunas / faixas | **Online**, **Ocupado**, **Ausente**, **Offline** |
| Card do analista | Foto (avatar), **nome**, **cargo** (perfil ou função exibível), **status** |
| Indicador visual (quadrado/círculo de status) | **Online** = verde · **Ocupado** = amarelo · **Ausente** = vermelho · **Offline** = preto/cinza |
| Métrica auxiliar (opcional no card) | Qtd tickets em atendimento atribuídos |

**Nota:** hoje o enum backend expõe `ONLINE`, `AUSENTE`, `OFFLINE`. **Ocupado** é decisão de produto: derivar de “tem ticket EM_ATENDIMENTO” ou novo `StatusOperador` — ver §8.

### 4.3 Operação por Cliente B2B

**Objetivo:** visão por **Cliente** contratante (não por string “conexão” solta).

Por linha/card de cliente:

| Campo | Descrição |
|-------|-----------|
| Chamados atuais | Ativos (fila + atendimento) daquele cliente |
| Pendentes | Aguardando cliente / sem primeiro atendimento |
| Reabertos | Pendência pós-encerramento ou ticket novo após encerrado (regra Chats) |
| **TME** | Tempo médio de espera **progressivo** no dia (ou desde abertura do ticket ativo) — definir fórmula na 280 |
| **TMA** | Tempo médio de atendimento **progressivo** no dia — idem |
| Ação | Botão **Acompanhar** → abre subpágina **Dashboard → Acompanhamento** (somente leitura) |

Ordenação sugerida: maior pressão (fila + SLA vencido) primeiro.

### 4.4 SLA

**Objetivo:** saúde do SLA **no momento**, não relatório de período.

- Blocos **1º atendimento** e **Resolução**: dentro do prazo, próximo do vencimento, vencido, violado, pausado (espelhar linguagem atual).
- Lista **críticos** (top tickets com SLA vermelho).
- **Excluir** tickets `INDEVIDO`, `RESOLVIDO`, `CANCELADO` do cômputo operacional (alinhado Sprint 277).

Sem seletor 7/30/90 dias neste bloco.

### 4.5 Encerramentos do dia

**Objetivo:** retrato do **dia corrente** (calendário operacional SP).

- Total encerrados hoje (`RESOLVIDO` / fluxo de encerramento válido).
- Opcional: por analista, por cliente (top 5), por categoria — **somente dia**, sem filtro de período customizado.
- Distinto do card “Encerramento e satisfação” com período 30 dias (esse card **sai** da Visão geral ou vira link “Ver em Indicadores”).

### 4.6 Avaliação em tempo real

**Objetivo:** fila de pesquisa **viva**, não NPS histórico.

- Pendentes de resposta, enviadas hoje, respondidas hoje, expiradas hoje (contagens + lista curta).
- **Sem** média de nota do trimestre (→ Indicadores → Satisfação).
- Tickets `INDEVIDO` não geram nova avaliação operacional (regra 274).

---

## 5. Submenu Dashboard

Estrutura alvo no menu (espelho do grupo **Indicadores**):

| Item | `data-page` / rota lógica | Função |
|------|---------------------------|--------|
| **Visão geral** | `dashboard` (default) | Blueprint §3–§4 — operação em tempo real |
| **Acompanhamento** | `dashboard-acompanhamento` (novo) | **Somente leitura** — detalhe do Cliente B2B escolhido em “Acompanhar” |

### 5.1 Acompanhamento (somente leitura)

- Entrada: botão **Acompanhar** no bloco Cliente B2B (parâmetro `clienteId`).
- Conteúdo: tickets ativos e pendências daquele cliente, SLA resumido, últimas interações (read-only), sem editar ticket, sem encerrar, sem classificar indevido.
- Saída: voltar à Visão geral.
- Permissões: mesmas do Dashboard (SUPERVISOR/ADMIN/ANALISTA conforme política atual).

---

## 6. Wireframe lógico (ordem vertical)

```
[ Dashboard — Visão geral                    ] [ Atualizar ]
┌─ Operação Agora ─────────────────────────────────────────┐
│  Em Atendimento          │  Em Fila                      │
└──────────────────────────┴───────────────────────────────┘
┌─ Analistas Online (kanban/status) ───────────────────────┐
└──────────────────────────────────────────────────────────┘
┌─ Operação por Cliente B2B ────────────────────────────────┐
│  Cliente A | atuais | pend. | reab. | TME | TMA | [Acompanhar] │
└──────────────────────────────────────────────────────────┘
┌─ SLA (vivo) ─────────────────────────────────────────────┐
└──────────────────────────────────────────────────────────┘
┌─ Encerramentos do dia ───────────────────────────────────┐
└──────────────────────────────────────────────────────────┘
┌─ Avaliação em tempo real ─────────────────────────────────┐
└──────────────────────────────────────────────────────────┘
```

---

## 7. Próximas sprints de implementação (planejamento)

| Sprint | Entrega | Observação |
|--------|---------|------------|
| **280** | API `GET /api/dashboard/operacao-agora` + front bloco **Operação Agora** | Contagens fila/atendimento; lista top N; polling |
| **281** | Bloco **Analistas Online** (UI kanban + cores de status) | Resolver enum **Ocupado** |
| **282** | Bloco **Operação por Cliente B2B** + TME/TMA progressivos | Agregação por `clienteId` |
| **283** | Submenu + página **Acompanhamento** (read-only) | Router + `dashboardAcompanhamentoPage.js` |
| **284** | Reorganizar **SLA** + **Encerramentos do dia** na Visão geral | Remover seletor período do Dashboard |
| **285** | **Avaliação em tempo real** + limpeza cards legados / link para Indicadores | Copy “análise histórica → Indicadores” |

Smoke funcional por sprint; backup ritual nas sprints com código.

---

## 8. Decisões pendentes (produto/arquitetura)

1. **Status Ocupado:** novo valor em `StatusOperador` vs derivado de carga (`EM_ATENDIMENTO` > 0).
2. **TME / TMA progressivos:** relógio desde abertura do ticket vs média apenas dos tickets abertos hoje.
3. **Intervalo de refresh:** 30 s vs 60 s; impacto em carga do `dashboard` polling.
4. **Reabertos:** mesma lista que Chats “pendência de decisão” ou contagem separada no card Cliente B2B.
5. **Card “Não atendimento” (Sprint 277):** manter link discreto no rodapé da Visão geral ou apenas Indicadores — **não** no hero.
6. **Encerramento+satisfação 30 dias:** remover do HTML do Dashboard ou card com CTA “Abrir Indicadores → Encerramento e satisfação”.

---

## 9. Referências

- `docs/BLUEPRINT_TELAS_NOVO_MODELO.md` — inventário de telas (atualizar § Dashboard para apontar aqui).
- `docs/REGRA_TICKETS_INDEVIDOS_ETIQUETAS_OPERACIONAIS.md` — INDEVIDO fora da operação válida.
- `docs/MODELO_CLIENTE_CONTATO_WHATSAPP_TICKET.md` — domínio Cliente / Ticket.
- Implementação atual: `dashboardPage.js`, `DashboardService.java`, `DashboardSlaService.java`.

---

## 10. Sprint 279 — critérios de aceite (documental)

- [x] Blueprint do Dashboard atualizado (este arquivo).
- [x] Regra “sem filtros analíticos no Dashboard” registrada.
- [x] Bloco Devidos/Indevidos **removido** do desenho alvo; entrada por **Operação Agora**.
- [x] Seis blocos finais definidos + submenu Visão geral / Acompanhamento.
- [x] Roadmap 280–285 esboçado.
- [x] Nenhum código funcional alterado nesta sprint.
