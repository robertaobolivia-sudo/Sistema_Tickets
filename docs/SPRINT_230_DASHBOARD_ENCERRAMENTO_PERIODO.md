# Sprint 230 — Dashboard: período e cliente (Encerramento e satisfação)

## Objetivo

Permitir no Dashboard filtro de período (7 / 30 / 90 dias, padrão 30) e cliente opcional, reutilizando `IndicadoresEncerramentoAvaliacaoService` sem alterar regra de cálculo.

## Alterações

- **API:** `GET /api/dashboard/encerramento-satisfacao?dias=&clienteId=` (opcionais).
- **Backend:** `DashboardService.normalizarDiasPeriodoEncerramento` + repasse de `clienteId`.
- **Frontend:** selects no bloco do Dashboard; `buildDashboardEncerramentoQueryParams` em `dashboardEncerramentoView.js`.

## Backup

`Sistemas_BKP/BKP_Sprint_230_Dashboard_Enc_Periodo`

## Validação visual (Sprint 231)

- Aprovada — ver `docs/SPRINT_231_VALIDACAO_DASHBOARD_ENC.md`
