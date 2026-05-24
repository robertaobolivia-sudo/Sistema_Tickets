/**
 * Montagem de query params (funções puras).
 */

export function appendQueryParam(params, key, value) {
    if (value == null) {
        return;
    }
    const text = typeof value === 'string' ? value.trim() : String(value);
    if (!text) {
        return;
    }
    params.set(key, text);
}

export function appendAuditoriaDateRange(params, dataInicio, dataFim) {
    if (dataInicio) {
        params.set('dataInicio', `${dataInicio}T00:00:00`);
    }
    if (dataFim) {
        params.set('dataFim', `${dataFim}T23:59:59`);
    }
}

export function appendPlainDateRange(params, dataInicio, dataFim) {
    if (dataInicio) {
        params.set('dataInicio', dataInicio);
    }
    if (dataFim) {
        params.set('dataFim', dataFim);
    }
}

/** @param {Record<string, string | undefined | null>} filters */
export function buildAuditoriaFilterParams(filters = {}) {
    const params = new URLSearchParams();
    appendAuditoriaDateRange(params, filters.dataInicio, filters.dataFim);
    appendQueryParam(params, 'analistaId', filters.analistaId);
    appendQueryParam(params, 'acao', filters.acao);
    appendQueryParam(params, 'entidade', filters.entidade);
    appendQueryParam(params, 'entidadeId', filters.entidadeId);
    return params;
}

/** @param {Record<string, string | undefined | null>} filters */
export function buildRelatorioBuscaParams(filters = {}) {
    const params = new URLSearchParams();
    appendPlainDateRange(params, filters.dataInicio, filters.dataFim);
    appendQueryParam(params, 'status', filters.status);
    appendQueryParam(params, 'clienteId', filters.clienteId);
    if (!params.has('clienteId')) {
        appendQueryParam(params, 'cliente', filters.cliente);
    }
    appendQueryParam(params, 'analistaId', filters.analistaId);
    appendQueryParam(params, 'grupo', filters.grupo);
    appendQueryParam(params, 'subgrupo', filters.subgrupo);
    appendQueryParam(params, 'prioridade', filters.prioridade);
    appendQueryParam(params, 'slaPrimeiroAtendimentoStatus', filters.slaPrimeiroAtendimentoStatus);
    appendQueryParam(params, 'slaResolucaoStatus', filters.slaResolucaoStatus);
    const escalonado = filters.escalonado?.trim?.() ?? filters.escalonado;
    if (escalonado === 'true' || escalonado === 'false') {
        params.set('escalonado', escalonado);
    }
    appendQueryParam(params, 'motivoId', filters.motivoId);
    appendQueryParam(params, 'statusPesquisa', filters.statusPesquisa);
    appendQueryParam(params, 'notaAvaliacao', filters.notaAvaliacao);
    appendQueryParam(params, 'envioStatus', filters.envioStatus);
    appendQueryParam(params, 'origemTicket', filters.origemTicket);
    return params;
}

/** @param {Record<string, string | undefined | null>} filters */
export function buildTicketBuscaParams(filters = {}) {
    const params = new URLSearchParams();
    appendQueryParam(params, 'textoLivre', filters.textoLivre);
    appendQueryParam(params, 'status', filters.status);
    appendQueryParam(params, 'cliente', filters.cliente);
    appendQueryParam(params, 'analistaId', filters.analistaId);
    appendPlainDateRange(params, filters.dataInicio, filters.dataFim);
    appendQueryParam(params, 'prioridade', filters.prioridade);
    return params;
}

/** Formata data ISO (yyyy-MM-dd) para exibição dd/MM/yyyy. */
export function formatDateIsoToBr(isoDate) {
    if (isoDate == null || isoDate === '') {
        return '-';
    }
    const text = String(isoDate).trim();
    const parts = text.split('-');
    if (parts.length !== 3) {
        return text;
    }
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

/** @param {Record<string, string | undefined | null>} filters */
export function buildIndicadoresChamadosParams({ dataInicio, dataFim } = {}) {
    const params = new URLSearchParams();
    appendPlainDateRange(params, dataInicio, dataFim);
    return params;
}

export function buildSatisfacaoResumoParams({
    dataInicio,
    dataFim,
    nota,
    statusTicket,
    termoCliente,
    clienteId
} = {}) {
    const params = new URLSearchParams();
    appendPlainDateRange(params, dataInicio, dataFim);
    appendQueryParam(params, 'nota', nota);
    appendQueryParam(params, 'statusTicket', statusTicket);
    appendQueryParam(params, 'clienteId', clienteId);
    if (!params.has('clienteId')) {
        appendQueryParam(params, 'termoCliente', termoCliente);
    }
    return params;
}
