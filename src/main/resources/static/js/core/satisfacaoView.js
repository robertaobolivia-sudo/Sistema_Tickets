/**
 * Helpers puros para exibição de indicadores de satisfação.
 */

import { buildSatisfacaoResumoParams } from './queryParams.js';

/** @param {Record<string, string | undefined | null>} filters */
export function buildIndicadoresSatisfacaoParams(filters = {}) {
    return buildSatisfacaoResumoParams(filters);
}

export function formatSatisfacaoPercent(value) {
    const n = Number(value);
    if (!Number.isFinite(n)) {
        return '0';
    }
    return String(n).replace('.', ',');
}

export function hasSatisfacaoResumoData(resumo) {
    return (resumo?.totalAvaliacoes ?? 0) > 0;
}

const STATUS_LABELS = {
    NAO_ENVIADA: 'Não enviada',
    PENDENTE: 'Pendente',
    RESPONDIDA: 'Respondida',
    EXPIRADA: 'Expirada',
    REGISTRADA_MANUALMENTE: 'Registrada manualmente'
};

export function formatSatisfacaoStatusLabel(status) {
    if (status == null || status === '') {
        return '—';
    }
    const key = String(status).toUpperCase();
    return STATUS_LABELS[key] ?? String(status);
}

/** Exibição da nota no detalhe (PENDENTE/EXPIRADA sem nota). */
export function formatSatisfacaoNotaExibicao(nota, status) {
    const n = Number(nota);
    if (Number.isFinite(n) && n >= 1 && n <= 5) {
        return `${n} / 5`;
    }
    const st = status != null ? String(status).toUpperCase() : '';
    if (st === 'PENDENTE') {
        return 'Aguardando resposta';
    }
    if (st === 'EXPIRADA' || st === 'NAO_ENVIADA') {
        return '—';
    }
    return '—';
}
