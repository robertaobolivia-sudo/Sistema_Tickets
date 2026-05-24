import { API_BASE } from '@shared/api/api-client.js';
import { fetchJsonWithSession } from '@shared/api/http.js';

export function getChamados(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/chamados?${queryString}`
        : `${API_BASE}/indicadores/chamados`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar os indicadores de chamados.');
}

export function getClientes(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/clientes?${queryString}`
        : `${API_BASE}/indicadores/clientes`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar os indicadores de clientes.');
}

export function getAtendentes(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/atendentes?${queryString}`
        : `${API_BASE}/indicadores/atendentes`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar os indicadores de atendentes.');
}

export function getSla(queryString) {
    const url = queryString
        ? `${API_BASE}/indicadores/sla?${queryString}`
        : `${API_BASE}/indicadores/sla`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar os indicadores de SLA.');
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
