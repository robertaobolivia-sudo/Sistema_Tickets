import { API_BASE, apiFetch } from '@shared/api/api-client.js';
import { fetchJsonWithSession, readJson } from '@shared/api/http.js';
import { mensagemErroSessaoApi } from '@shared/ui/messages.js';

function baseUrl(numeroTicket) {
    return `${API_BASE}/tickets/${encodeURIComponent(numeroTicket)}/anexos`;
}

export function listTicketAnexos(numeroTicket) {
    return fetchJsonWithSession(
        baseUrl(numeroTicket),
        undefined,
        'Não foi possível carregar os anexos do ticket.'
    );
}

export function uploadTicketAnexo(numeroTicket, file) {
    const formData = new FormData();
    formData.append('arquivo', file);
    return fetchJsonWithSession(
        baseUrl(numeroTicket),
        { method: 'POST', body: formData },
        'Não foi possível enviar o arquivo.'
    );
}

export function downloadTicketAnexo(numeroTicket, anexoId) {
    return apiFetch(`${baseUrl(numeroTicket)}/${anexoId}/download`);
}

export async function downloadTicketAnexoBlob(numeroTicket, anexoId, nomeArquivo) {
    const response = await downloadTicketAnexo(numeroTicket, anexoId);
    if (!response.ok) {
        const data = await readJson(response);
        throw new Error(
            mensagemErroSessaoApi(response, data, 'Não foi possível baixar o arquivo.')
        );
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo || 'anexo';
    link.rel = 'noopener';
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}
