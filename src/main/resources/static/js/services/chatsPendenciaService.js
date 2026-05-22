import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function listInteracoesPendentes() {
    return fetchJsonWithSession(
        `${API_BASE}/chats/interacoes-pendentes`,
        undefined,
        'Não foi possível carregar pendências de decisão.'
    );
}

export function vincularAoTicketAnterior(pendenciaId) {
    return fetchJsonWithSession(
        `${API_BASE}/chats/interacoes-pendentes/${encodeURIComponent(pendenciaId)}/vincular-anterior`,
        { method: 'POST' },
        'Não foi possível vincular a mensagem ao ticket anterior.'
    );
}

export function gerarNovoTicket(pendenciaId) {
    return fetchJsonWithSession(
        `${API_BASE}/chats/interacoes-pendentes/${encodeURIComponent(pendenciaId)}/gerar-ticket`,
        { method: 'POST' },
        'Não foi possível gerar novo ticket.'
    );
}
