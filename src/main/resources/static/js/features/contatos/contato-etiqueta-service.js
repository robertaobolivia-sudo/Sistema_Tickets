import { API_BASE } from '@shared/api/api-client.js';
import { fetchJsonWithSession } from '@shared/api/http.js';

export function listarEtiquetasContato(contatoId) {
    return fetchJsonWithSession(
        `${API_BASE}/contatos/${encodeURIComponent(contatoId)}/etiquetas`,
        undefined,
        'Não foi possível carregar as etiquetas do contato.'
    );
}

export function salvarEtiquetasContato(contatoId, etiquetaIds) {
    return fetchJsonWithSession(
        `${API_BASE}/contatos/${encodeURIComponent(contatoId)}/etiquetas`,
        {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ etiquetaIds: Array.isArray(etiquetaIds) ? etiquetaIds : [] })
        },
        'Não foi possível salvar as etiquetas do contato.'
    );
}
