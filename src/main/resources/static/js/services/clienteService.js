import { API_BASE, apiFetch } from '../core/api.js';
import { fetchJsonWithSession, readJson } from './httpUtil.js';

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

export function listContatos(clienteId) {
    return fetchJsonWithSession(
        `${API_BASE}/clientes/${clienteId}/contatos`,
        undefined,
        'Falha ao carregar contatos'
    );
}

export async function listContatosAtivos(clienteId) {
    const response = await apiFetch(`${API_BASE}/clientes/${clienteId}/contatos/ativos`);
    if (!response.ok) {
        return null;
    }
    return readJson(response);
}

export function createContato(clienteId, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/clientes/${clienteId}/contatos`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Falha ao salvar contato'
    );
}

export function updateContato(contatoId, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/contatos-clientes/${contatoId}`,
        { method: 'PUT', body: JSON.stringify(payload) },
        'Falha ao salvar contato'
    );
}

export function getContato(contatoId) {
    return fetchJsonWithSession(
        `${API_BASE}/contatos-clientes/${contatoId}`,
        undefined,
        'Falha ao carregar contato'
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

export function patchContato(contatoId, acao) {
    const paths = { ativar: 'ativar', inativar: 'inativar', principal: 'principal' };
    const path = paths[acao];
    if (!path) {
        return Promise.reject(new Error('Ação de contato inválida'));
    }
    return fetchJsonWithSession(
        `${API_BASE}/contatos-clientes/${contatoId}/${path}`,
        { method: 'PATCH' },
        'Falha na operacao do contato'
    );
}
