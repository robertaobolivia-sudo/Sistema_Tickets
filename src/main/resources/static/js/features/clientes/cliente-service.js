import { API_BASE } from '@shared/api/api-client.js';
import { fetchJsonWithSession } from '@shared/api/http.js';

export function listOrSearch(termo = '', useBuscaEndpoint = false) {
    const url = termo
        ? `${API_BASE}/clientes/${useBuscaEndpoint ? 'busca' : 'buscar'}?termo=${encodeURIComponent(termo)}`
        : `${API_BASE}/clientes`;
    return fetchJsonWithSession(url, undefined, 'Falha ao buscar clientes');
}

export function getById(id) {
    return fetchJsonWithSession(`${API_BASE}/clientes/${id}`, undefined, 'Falha ao carregar cliente');
}

export function save(payload, editId) {
    const url = editId ? `${API_BASE}/clientes/${editId}` : `${API_BASE}/clientes`;
    const method = editId ? 'PUT' : 'POST';
    const msg = editId ? 'Falha ao atualizar cliente' : 'Falha ao cadastrar cliente';
    return fetchJsonWithSession(url, { method, body: JSON.stringify(payload) }, msg);
}

export function patchStatus(id, ativar) {
    const path = ativar ? 'ativar' : 'inativar';
    return fetchJsonWithSession(
        `${API_BASE}/clientes/${id}/${path}`,
        { method: 'PATCH' },
        'Falha ao atualizar status do cliente'
    );
}

export function uploadArteHeaderChats(clienteId, file) {
    const formData = new FormData();
    formData.append('arte', file);
    return fetchJsonWithSession(
        `${API_BASE}/clientes/${clienteId}/arte-header-chats`,
        { method: 'POST', body: formData },
        'Falha ao enviar arte do header do Chats'
    );
}
