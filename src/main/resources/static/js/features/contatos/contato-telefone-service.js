import { apiFetch, API_BASE } from '@shared/api/api-client.js';

export function listarTelefonesAdicionais(contatoId) {
    return apiFetch(
        `${API_BASE}/contatos/${encodeURIComponent(contatoId)}/telefones-adicionais`
    ).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel carregar telefones adicionais.');
        }
        return res.json();
    });
}

export function adicionarTelefoneAdicional(contatoId, payload) {
    return apiFetch(
        `${API_BASE}/contatos/${encodeURIComponent(contatoId)}/telefones-adicionais`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        }
    ).then(async res => {
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Nao foi possivel incluir o telefone adicional.');
        }
        return res.json();
    });
}
