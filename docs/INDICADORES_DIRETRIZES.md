# Indicadores — diretrizes de produto e evolução

## Papéis das áreas

| Área | Papel |
|------|--------|
| **Dashboard** | Operação rápida: visão do dia, filas, alertas. |
| **Relatórios** | Listagens detalhadas, exportações (CSV etc.). |
| **Indicadores** | BI gerencial: agregações por período, rankings, comparativos. |

## Contexto F5 (suporte terceirizado)

- A F5 atende empresas contratantes; esses cadastros em **Clientes** não são “revenda” como entidade separada.
- Consumidores finais entram por **WhatsApp/API** e geram **tickets**.
- Segmentação gerencial futura será por **etiquetas flexíveis** (cliente, ticket, atendimento), não por faixas fixas.

## Etiquetas flexíveis (Sprint 131+)

- Cadastro técnico em **`Etiqueta`** (`GET/POST/PUT/PATCH /api/etiquetas`).
- **Leitura** (`GET /api/etiquetas`, `GET /api/etiquetas/ativas`): qualquer analista com **sessão válida**.
- **Escrita** (criar, editar, ativar/inativar): **ADMIN** ou **SUPERVISOR**.
- **Vínculo ticket ↔ etiqueta** já disponível (`GET/PUT /api/tickets/{numero}/etiquetas`), uso operacional no painel **Chats**.
- Indicadores por etiqueta serão **sprint futura** (após regras de agregação estáveis).
- Associação futura prevista: **cliente** e **atendimento** (além do ticket).
- Gestão visual: **Configurações → Etiquetas** (Sprint 148; `etiquetasConfigSection.js` + `etiquetaService.js`).
- Anexos do atendimento no Chats: ver `docs/CHATS_DIRETRIZES.md`.

## Legado N1/N2 (Sprint 130)

- **N1/N2 não são regra fixa** nem padrão de relatório/Indicadores.
- Campo `classificacao_cliente` no cliente permanece só por compatibilidade de banco; UI neutra.
- **Indicadores > Chamados** mantém placeholder até filtros por etiqueta existirem.
- Parâmetro legado `classificacaoCliente` em indicadores de chamados é ignorado.

## O que não fazer agora

- Não criar entidade **Revenda**.
- Não implementar painel lateral completo de WhatsApp/atendimento nesta linha de sprints.
- Não reintroduzir N1/N2 como filtro ou card padrão em Indicadores.
- Não ligar Indicadores a etiquetas até filtros/agregações por etiqueta estarem implementados.

## Sprint 202 — Indicadores de Motivo e Pesquisa

- **Endpoint:** `GET /api/indicadores/encerramento-avaliacao` (ADMIN ou SUPERVISOR).
- **Parâmetros:** `dataInicio`, `dataFim` (ISO date), `clienteId`, `motivoId`, `statusPesquisa`, `notaAvaliacao`.
- **Métricas:** top motivos (tickets encerrados no período com motivo); totais por `TicketSatisfacaoStatus`; média e contagem por nota 1–5; resumo de envio (`SIMULADO`, `FALHA`, sem tentativa) para PENDENTE/NAO_ENVIADA.
- **Regra da média:** apenas `RESPONDIDA` e `REGISTRADA_MANUALMENTE` com nota; `PENDENTE` sem nota não entra.
- **Pesquisas:** filtro por `criadoEm` da satisfação; motivos por `dataEncerramento` do ticket.
- **UI:** subpágina **Indicadores → Encerramento e satisfação** (`indicadores-encerramento-satisfacao`).
- **Pendência:** filtro por **Cliente** no endpoint existe; seletor na UI fica para sprint futura (mesma ressalva dos Relatórios).
- **Dashboard:** Sprint 203 — cards via `GET /api/dashboard/encerramento-satisfacao` (reuso interno da mesma agregação; período fixo 30 dias).

## Sprint 203 — Dashboard com resumo de Encerramento e satisfação

- **Endpoint:** `GET /api/dashboard/encerramento-satisfacao` (sessão válida; qualquer perfil com acesso ao Dashboard).
- **Service:** `DashboardService.obterEncerramentoSatisfacaoResumo()` → `IndicadoresEncerramentoAvaliacaoService` (sem duplicar regra de média).
- **Período:** últimos **30 dias** (`DashboardService.DIAS_PERIODO_ENCERRAMENTO_SATISFACAO`), sem filtro na UI.
- **Cards:** motivo mais recorrente, média, respondidas, pendentes, expiradas, falhas de envio.
- **Pendência:** filtro de período no Dashboard; alinhar copy com Indicadores se mudar janela padrão.

## Próximos passos (futuro)

- Vínculo etiqueta ↔ cliente / atendimento.
- Tela de gestão de etiquetas e uso operacional no atendimento.
- Indicadores por etiqueta após API e dados estarem estáveis.
- Atualizar `docs/API_CONTRATOS.md` quando o contrato de vínculos for definido.
