import { API_BASE, apiFetch } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';
import { mensagemErroSessaoApi } from '../core/messages.js';

export async function getSatisfacaoByTicket(numeroTicket) {
    const response = await apiFetch(`${API_BASE}/tickets/${encodeURIComponent(numeroTicket)}/satisfacao`);
    if (response.status === 204 || response.status === 404) {
        return null;
    }
    if (!response.ok) {
        const data = await response.json().catch(() => null);
        throw new Error(
            mensagemErroSessaoApi(response, data, 'Não foi possível carregar a satisfação do ticket.')
        );
    }
    const text = await response.text();
    if (!text || !text.trim()) {
        return null;
    }
    return JSON.parse(text);
}

export function getResumo(queryString) {
    const url = queryString
        ? `${API_BASE}/tickets/satisfacao/resumo?${queryString}`
        : `${API_BASE}/tickets/satisfacao/resumo`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar o resumo de satisfação.');
}

export function getEvolucao(queryString) {
    const url = queryString
        ? `${API_BASE}/tickets/satisfacao/evolucao?${queryString}`
        : `${API_BASE}/tickets/satisfacao/evolucao`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível carregar a evolução de satisfação.');
}

export function fetchCsv(queryString) {
    const url = queryString
        ? `${API_BASE}/tickets/satisfacao/csv?${queryString}`
        : `${API_BASE}/tickets/satisfacao/csv`;
    return apiFetch(url);
}
