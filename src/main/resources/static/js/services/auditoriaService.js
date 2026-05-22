import { API_BASE, apiFetch } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function listEventos(queryString) {
    const suffix = queryString ? `?${queryString}` : '';
    return fetchJsonWithSession(
        `${API_BASE}/auditoria/eventos${suffix}`,
        undefined,
        'Falha ao carregar auditoria'
    );
}

export function contarEventosAntigos(queryString) {
    const suffix = queryString ? `?${queryString}` : '';
    return fetchJsonWithSession(
        `${API_BASE}/auditoria/eventos/contar-antigos${suffix}`,
        undefined,
        'Não foi possível contar eventos antigos.'
    );
}

export function excluirEventosAntigos(queryString) {
    const suffix = queryString ? `?${queryString}` : '';
    return fetchJsonWithSession(
        `${API_BASE}/auditoria/eventos/antigos${suffix}`,
        { method: 'DELETE' },
        'Não foi possível excluir eventos antigos.'
    );
}

export function fetchEventosCsv(queryString) {
    const url = queryString
        ? `${API_BASE}/auditoria/eventos/csv?${queryString}`
        : `${API_BASE}/auditoria/eventos/csv`;
    return apiFetch(url);
}
