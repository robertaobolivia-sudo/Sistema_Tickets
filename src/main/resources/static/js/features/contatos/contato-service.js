import { apiFetch, API_BASE } from '@shared/api/api-client.js';
import { buildContatoGestaoGestaoQueryParams } from '@features/contatos/contato-gestao-filtros-view.js';

export function listarGestao(filtros = {}) {
    const params = buildContatoGestaoGestaoQueryParams(filtros);
    return apiFetch(`${API_BASE}/contatos?${params.toString()}`).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel carregar contatos.');
        }
        return res.json();
    });
}

export function listarPorCliente(clienteId) {
    return apiFetch(`${API_BASE}/contatos?clienteId=${encodeURIComponent(clienteId)}`).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel carregar contatos do cliente.');
        }
        return res.json();
    });
}

export function buscarPorId(contatoId) {
    return apiFetch(`${API_BASE}/contatos/${encodeURIComponent(contatoId)}`).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel carregar o contato.');
        }
        return res.json();
    });
}

export function listarHistoricoTickets(contatoId) {
    return apiFetch(`${API_BASE}/contatos/${encodeURIComponent(contatoId)}/historico-tickets`).then(
        async res => {
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Nao foi possivel carregar o historico de chamados.');
            }
            return res.json();
        }
    );
}

export function patchAtivar(contatoId) {
    return apiFetch(`${API_BASE}/contatos/${encodeURIComponent(contatoId)}/ativar`, {
        method: 'PATCH'
    }).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel ativar o contato.');
        }
        return res.json();
    });
}

export function patchInativar(contatoId) {
    return apiFetch(`${API_BASE}/contatos/${encodeURIComponent(contatoId)}/inativar`, {
        method: 'PATCH'
    }).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel inativar o contato.');
        }
        return res.json();
    });
}

export function atualizar(contatoId, payload) {
    return apiFetch(`${API_BASE}/contatos/${encodeURIComponent(contatoId)}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    }).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel salvar o contato.');
        }
        return res.json();
    });
}
