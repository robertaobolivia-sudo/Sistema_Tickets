import { API_BASE } from '@shared/api/api-client.js';
import { apiFetch, fetchJsonWithSession } from '@shared/api/http.js';

export async function getContadorNaoLidas() {
    const response = await apiFetch(`${API_BASE}/notificacoes/contador-nao-lidas`);
    if (!response.ok) {
        return null;
    }
    return response.json().catch(() => null);
}

export function listar() {
    return fetchJsonWithSession(
        `${API_BASE}/notificacoes`,
        undefined,
        'Não foi possível carregar as notificações.'
    );
}

export function marcarLida(id) {
    return fetchJsonWithSession(
        `${API_BASE}/notificacoes/${id}/marcar-lida`,
        { method: 'PUT' },
        'Falha ao marcar notificação como lida'
    );
}

export function marcarTodasLidas() {
    return fetchJsonWithSession(
        `${API_BASE}/notificacoes/marcar-todas-lidas`,
        { method: 'PUT' },
        'Falha ao marcar todas como lidas'
    );
}

export function verificarSlaCritico() {
    return fetchJsonWithSession(
        `${API_BASE}/notificacoes/sla/verificar`,
        { method: 'POST' },
        'Falha ao verificar SLA crítico'
    );
}
