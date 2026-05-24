/**
 * Visão de indicadores: encerramento (motivo) e pesquisa de satisfação.
 */

export const STATUS_PESQUISA_LABELS = {
    NAO_ENVIADA: 'Não enviada',
    PENDENTE: 'Pendente',
    RESPONDIDA: 'Respondida',
    EXPIRADA: 'Expirada',
    REGISTRADA_MANUALMENTE: 'Registrada manualmente'
};

/** @param {Record<string, string | undefined | null>} filters */
export function buildIndicadoresEncerramentoAvaliacaoParams(filters = {}) {
    const params = new URLSearchParams();
    if (filters.dataInicio) {
        params.set('dataInicio', filters.dataInicio);
    }
    if (filters.dataFim) {
        params.set('dataFim', filters.dataFim);
    }
    if (filters.clienteId != null && String(filters.clienteId).trim() !== '') {
        params.set('clienteId', String(filters.clienteId).trim());
    }
    if (filters.motivoId != null && String(filters.motivoId).trim() !== '') {
        params.set('motivoId', String(filters.motivoId).trim());
    }
    if (filters.statusPesquisa != null && String(filters.statusPesquisa).trim() !== '') {
        params.set('statusPesquisa', String(filters.statusPesquisa).trim());
    }
    if (filters.notaAvaliacao != null && String(filters.notaAvaliacao).trim() !== '') {
        params.set('notaAvaliacao', String(filters.notaAvaliacao).trim());
    }
    return params;
}

export function hasEncerramentoAvaliacaoData(dto) {
    if (!dto) {
        return false;
    }
    const top = dto.topMotivos;
    if (Array.isArray(top) && top.length > 0) {
        return true;
    }
    const p = dto.pesquisa;
    if (p && (p.totalPesquisas > 0 || p.mediaNota != null)) {
        return true;
    }
    const e = dto.envio;
    if (e && (e.simuladas > 0 || e.falhas > 0 || e.semTentativa > 0)) {
        return true;
    }
    return false;
}

export function formatMediaNota(media) {
    if (media == null) {
        return '—';
    }
    return String(media).replace('.', ',');
}

/** Clientes elegíveis no filtro de indicadores (ativos). */
export function filterClientesAtivosIndicadores(clientes) {
    const lista = Array.isArray(clientes) ? clientes : [];
    return lista.filter(c => {
        if (c?.id == null) {
            return false;
        }
        if (c.ativo === false) {
            return false;
        }
        const status = String(c.status ?? '').trim().toUpperCase();
        if (status === 'INATIVO') {
            return false;
        }
        return true;
    });
}
