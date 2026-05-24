import { API_BASE, apiFetch } from '@shared/api/api-client.js';
import { fetchJsonWithSession, readJson } from '@shared/api/http.js';
import { mensagemErroApi } from '@shared/ui/messages.js';

export function listAnalistas() {
    return fetchJsonWithSession(`${API_BASE}/analistas`, undefined, 'Falha ao listar analistas');
}

export async function listAnalistasAll() {
    const response = await apiFetch(`${API_BASE}/analistas`);
    if (!response.ok) {
        const data = await readJson(response);
        throw new Error(mensagemErroApi(response, data, 'Falha ao listar analistas'));
    }
    const data = await readJson(response);
    return Array.isArray(data) ? data : [];
}

export function create(payload) {
    return fetchJsonWithSession(
        `${API_BASE}/analistas`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Falha ao cadastrar analista'
    );
}

export function update(analystId, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/analistas/${analystId}`,
        { method: 'PUT', body: JSON.stringify(payload) },
        'Falha ao atualizar analista'
    );
}

export function updateStatus(analystId, statusOperador) {
    return fetchJsonWithSession(
        `${API_BASE}/analistas/${analystId}/status`,
        { method: 'PUT', body: JSON.stringify({ statusOperador }) },
        'Falha ao atualizar status do operador'
    );
}

export function updatePerfilAcesso(analystId, perfilAcesso) {
    return fetchJsonWithSession(
        `${API_BASE}/analistas/${analystId}/perfil-acesso`,
        { method: 'PUT', body: JSON.stringify({ perfilAcesso }) },
        'Falha ao atualizar perfil de acesso'
    );
}

export function uploadPhoto(analystId, file) {
    const formData = new FormData();
    formData.append('foto', file);
    return fetchJsonWithSession(
        `${API_BASE}/analistas/${analystId}/foto`,
        { method: 'POST', body: formData },
        'Falha ao enviar foto de perfil'
    );
}

export function removePhoto(analystId) {
    return fetchJsonWithSession(
        `${API_BASE}/analistas/${analystId}/foto`,
        { method: 'DELETE' },
        'Falha ao remover foto de perfil'
    );
}
