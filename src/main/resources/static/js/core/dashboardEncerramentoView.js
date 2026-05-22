/**
 * Cards de encerramento e satisfação no Dashboard (Sprint 203 / 230).
 */

export const DASHBOARD_ENC_DIAS_PADRAO = 30;
export const DASHBOARD_ENC_DIAS_PERMITIDOS = [7, 30, 90];

/** @param {number|string|null|undefined} dias */
export function normalizeDashboardEncerramentoDias(dias) {
    const n = Number(dias);
    if (DASHBOARD_ENC_DIAS_PERMITIDOS.includes(n)) {
        return n;
    }
    return DASHBOARD_ENC_DIAS_PADRAO;
}

/** @param {{ dias?: number|string, clienteId?: string|number|null }} filtros */
export function buildDashboardEncerramentoQueryParams(filtros = {}) {
    const params = new URLSearchParams();
    params.set('dias', String(normalizeDashboardEncerramentoDias(filtros.dias)));
    const clienteId = filtros.clienteId;
    if (clienteId != null && String(clienteId).trim() !== '') {
        params.set('clienteId', String(clienteId).trim());
    }
    return params;
}

export function formatDashboardMediaNota(media) {
    if (media == null) {
        return '—';
    }
    return String(media).replace('.', ',');
}

export function formatDashboardMotivoRecorrente(motivoNome, total) {
    const nome = motivoNome != null && String(motivoNome).trim() !== '' ? String(motivoNome).trim() : null;
    if (!nome) {
        return '—';
    }
    const qtd = total != null && total > 0 ? ` (${total})` : '';
    return `${nome}${qtd}`;
}

export function buildDashboardEncerramentoPeriodoHint(dataInicio, dataFim) {
    if (dataInicio && dataFim) {
        return `Período: ${formatBrDate(dataInicio)} a ${formatBrDate(dataFim)}`;
    }
    return 'Últimos 30 dias';
}

function formatBrDate(iso) {
    const parts = String(iso).trim().split('-');
    if (parts.length !== 3) {
        return iso;
    }
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

/** Valores seguros para exibição em cards (nunca null/undefined na UI). */
export function normalizeDashboardEncerramentoResumo(dto) {
    const d = dto || {};
    return {
        motivoLabel: formatDashboardMotivoRecorrente(d.motivoMaisRecorrente, d.totalMotivoMaisRecorrente),
        mediaLabel: formatDashboardMediaNota(d.mediaNota),
        respondidas: d.totalRespondidas ?? 0,
        pendentes: d.totalPendentes ?? 0,
        expiradas: d.totalExpiradas ?? 0,
        falhasEnvio: d.totalFalhasEnvio ?? 0,
        periodoHint: buildDashboardEncerramentoPeriodoHint(d.dataInicio, d.dataFim)
    };
}
