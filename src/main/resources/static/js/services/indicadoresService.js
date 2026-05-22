import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function getChamados(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/chamados?${queryString}`
        : `${API_BASE}/indicadores/chamados`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar os indicadores de chamados.');
}

export function getEncerramentoAvaliacao(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/encerramento-avaliacao?${queryString}`
        : `${API_BASE}/indicadores/encerramento-avaliacao`;
    return fetchJsonWithSession(
        url,
        undefined,
        'Não foi possível carregar os indicadores de encerramento e satisfação.'
    );
}
