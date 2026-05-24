/**
 * Filtros Clientes → Contatos (Sprint 265).
 */

export function buildContatoGestaoGestaoQueryParams({
    clienteId = null,
    busca = '',
    etiquetaId = null,
    cidade = '',
    uf = '',
    comTicketsAbertos = false,
    comAvaliacaoRuim = false,
    semEtiqueta = false
} = {}) {
    const params = new URLSearchParams();
    params.set('gestao', 'true');
    if (clienteId != null && clienteId !== '') {
        params.set('clienteId', String(clienteId));
    }
    const termo = String(busca || '').trim();
    if (termo) {
        params.set('busca', termo);
    }
    if (etiquetaId != null && etiquetaId !== '') {
        params.set('etiquetaId', String(etiquetaId));
    }
    const cidadeTrim = String(cidade || '').trim();
    if (cidadeTrim) {
        params.set('cidade', cidadeTrim);
    }
    const ufTrim = String(uf || '').trim();
    if (ufTrim) {
        params.set('uf', ufTrim);
    }
    if (comTicketsAbertos) {
        params.set('comTicketsAbertos', 'true');
    }
    if (comAvaliacaoRuim) {
        params.set('comAvaliacaoRuim', 'true');
    }
    if (semEtiqueta) {
        params.set('semEtiqueta', 'true');
    }
    return params;
}

export function hasContatosGestaoFiltrosAtivos(filtros = {}) {
    const busca = String(filtros.busca || '').trim();
    const clienteId = filtros.clienteId != null && String(filtros.clienteId).trim() !== '';
    const etiquetaId = filtros.etiquetaId != null && String(filtros.etiquetaId).trim() !== '';
    const cidade = String(filtros.cidade || '').trim();
    const uf = String(filtros.uf || '').trim();
    return (
        busca.length > 0 ||
        clienteId ||
        etiquetaId ||
        cidade.length > 0 ||
        uf.length > 0 ||
        Boolean(filtros.comTicketsAbertos) ||
        Boolean(filtros.comAvaliacaoRuim) ||
        Boolean(filtros.semEtiqueta)
    );
}
