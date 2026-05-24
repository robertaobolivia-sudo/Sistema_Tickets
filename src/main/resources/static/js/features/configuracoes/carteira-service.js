import { API_BASE } from '@shared/api/api-client.js';
import { fetchJsonWithSession } from '@shared/api/http.js';

export function listCarteiras() {
    return fetchJsonWithSession(`${API_BASE}/carteiras`, undefined, 'Falha ao listar conexões/revendas');
}

export function getCarteira(id) {
    return fetchJsonWithSession(`${API_BASE}/carteiras/${id}`, undefined, 'Falha ao carregar conexão/revenda');
}

export function createCarteira(payload) {
    return fetchJsonWithSession(
        `${API_BASE}/carteiras`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Falha ao cadastrar conexão/revenda'
    );
}

export function updateCarteira(id, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/carteiras/${id}`,
        { method: 'PUT', body: JSON.stringify(payload) },
        'Falha ao atualizar conexão/revenda'
    );
}

export function uploadArteHeaderChats(id, file) {
    const formData = new FormData();
    formData.append('arte', file);
    return fetchJsonWithSession(
        `${API_BASE}/carteiras/${id}/arte-header-chats`,
        { method: 'POST', body: formData },
        'Falha ao enviar arte do header do Chats'
    );
}
