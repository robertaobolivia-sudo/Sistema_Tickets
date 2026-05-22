import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

const BASE = `${API_BASE}/etiquetas`;

export function listAll() {
    return fetchJsonWithSession(BASE, undefined, 'Não foi possível carregar as etiquetas.');
}

export function listActive() {
    return fetchJsonWithSession(`${BASE}/ativas`, undefined, 'Não foi possível carregar as etiquetas ativas.');
}

export function create(payload) {
    return fetchJsonWithSession(
        BASE,
        { method: 'POST', body: JSON.stringify(payload) },
        'Não foi possível cadastrar a etiqueta.'
    );
}

export function update(id, payload) {
    return fetchJsonWithSession(
        `${BASE}/${id}`,
        { method: 'PUT', body: JSON.stringify(payload) },
        'Não foi possível atualizar a etiqueta.'
    );
}

export function activate(id) {
    return fetchJsonWithSession(
        `${BASE}/${id}/ativar`,
        { method: 'PATCH' },
        'Não foi possível ativar a etiqueta.'
    );
}

export function deactivate(id) {
    return fetchJsonWithSession(
        `${BASE}/${id}/inativar`,
        { method: 'PATCH' },
        'Não foi possível inativar a etiqueta.'
    );
}
