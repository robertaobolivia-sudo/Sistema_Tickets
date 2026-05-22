import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function listByCliente(clienteId) {
    const url = `${API_BASE}/whatsapp-matrizes?clienteId=${encodeURIComponent(clienteId)}`;
    return fetchJsonWithSession(url, undefined, 'Falha ao carregar WhatsApps matriz');
}

export function getById(id) {
    return fetchJsonWithSession(
        `${API_BASE}/whatsapp-matrizes/${id}`,
        undefined,
        'Falha ao carregar WhatsApp matriz'
    );
}

export function create(payload) {
    return fetchJsonWithSession(
        `${API_BASE}/whatsapp-matrizes`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Falha ao cadastrar WhatsApp matriz'
    );
}

export function update(id, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/whatsapp-matrizes/${id}`,
        { method: 'PUT', body: JSON.stringify(payload) },
        'Falha ao atualizar WhatsApp matriz'
    );
}

export function patchAtivar(id) {
    return fetchJsonWithSession(
        `${API_BASE}/whatsapp-matrizes/${id}/ativar`,
        { method: 'PATCH' },
        'Falha ao ativar WhatsApp matriz'
    );
}

export function patchInativar(id) {
    return fetchJsonWithSession(
        `${API_BASE}/whatsapp-matrizes/${id}/inativar`,
        { method: 'PATCH' },
        'Falha ao inativar WhatsApp matriz'
    );
}
