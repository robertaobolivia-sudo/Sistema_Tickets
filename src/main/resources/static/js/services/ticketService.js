import { API_BASE, apiFetch } from '../core/api.js';
import { fetchJsonWithSession, readJson } from './httpUtil.js';

export function listTickets(status = 'ALL') {
    let url = `${API_BASE}/tickets`;
    if (status !== 'ALL') {
        url = `${API_BASE}/tickets/status/${status}`;
    }
    return fetchJsonWithSession(url, undefined, 'Falha ao buscar tickets');
}

export function searchTickets(queryString) {
    const url = queryString ? `${API_BASE}/tickets/busca?${queryString}` : `${API_BASE}/tickets/busca`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível buscar os tickets.');
}

export function searchTicketsRelatorio(queryString) {
    const url = queryString ? `${API_BASE}/tickets/busca?${queryString}` : `${API_BASE}/tickets/busca`;
    return fetchJsonWithSession(url, undefined, 'Não foi possível gerar o relatório.');
}

export async function getTicketAtivo(params = {}) {
    const qs = new URLSearchParams();
    if (params.telefone) {
        qs.set('telefone', params.telefone);
    }
    if (params.clienteId != null) {
        qs.set('clienteId', String(params.clienteId));
    }
    if (params.contatoSolicitanteId != null) {
        qs.set('contatoSolicitanteId', String(params.contatoSolicitanteId));
    }
    const query = qs.toString();
    const url = query ? `${API_BASE}/tickets/ativo?${query}` : `${API_BASE}/tickets/ativo`;
    const response = await apiFetch(url);
    if (response.status === 204) {
        return null;
    }
    if (!response.ok) {
        const data = await readJson(response);
        throw new Error(
            (data && data.message) || 'Não foi possível consultar ticket ativo.'
        );
    }
    return readJson(response);
}

export function getTicketByNumero(numeroTicket) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${encodeURIComponent(numeroTicket)}`,
        undefined,
        'Falha ao carregar detalhes'
    );
}

export function createTicket(payload) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Falha ao criar ticket'
    );
}

export function updateTicketStatus(ticketNumber, body) {
    const options = { method: 'PUT' };
    if (body != null) {
        options.headers = { 'Content-Type': 'application/json' };
        options.body = JSON.stringify(body);
    }
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/status`,
        options,
        'Falha ao atualizar ticket'
    );
}

export function encerrarTicket(ticketNumber, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/encerrar`,
        { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
        'Falha ao encerrar ticket'
    );
}

export function reabrirTicket(ticketNumber) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/reabrir`,
        { method: 'PUT' },
        'Falha ao reabrir ticket'
    );
}

export function escalonarTicket(ticketNumber, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/escalonar`,
        { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
        'Falha ao escalonar ticket'
    );
}

export function removerEscalonamentoTicket(ticketNumber) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/remover-escalonamento`,
        { method: 'PUT' },
        'Falha ao remover escalonamento'
    );
}

export function getTicketEtiquetas(ticketNumber) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${encodeURIComponent(ticketNumber)}/etiquetas`,
        undefined,
        'Não foi possível carregar as etiquetas do ticket.'
    );
}

export function saveTicketEtiquetas(ticketNumber, etiquetaIds) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${encodeURIComponent(ticketNumber)}/etiquetas`,
        {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ etiquetaIds: Array.isArray(etiquetaIds) ? etiquetaIds : [] })
        },
        'Não foi possível salvar as etiquetas do ticket.'
    );
}

export function saveObservacaoAtendimento(ticketNumber, observacao) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${encodeURIComponent(ticketNumber)}/observacao-atendimento`,
        {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ observacao: observacao ?? '' })
        },
        'Não foi possível salvar a observação do atendimento.'
    );
}

export function listInteracoes(ticketNumber) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/interacoes`,
        undefined,
        'Falha ao buscar interações'
    );
}

export function createInteracao(ticketNumber, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${ticketNumber}/interacoes`,
        { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) },
        'Falha ao salvar interação'
    );
}

export function registrarSatisfacao(ticketNumber, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/tickets/${encodeURIComponent(ticketNumber)}/satisfacao`,
        { method: 'POST', body: JSON.stringify(payload) },
        'Não foi possível registrar a satisfação.'
    );
}

export function fetchTicketPdf(numeroTicket) {
    return apiFetch(`${API_BASE}/tickets/${encodeURIComponent(numeroTicket)}/pdf`);
}

export function fetchRelatorioCsv(queryString) {
    const url = queryString
        ? `${API_BASE}/tickets/relatorios/csv?${queryString}`
        : `${API_BASE}/tickets/relatorios/csv`;
    return apiFetch(url);
}

export async function listTicketsForAlert() {
    try {
        const response = await apiFetch(`${API_BASE}/tickets`);
        if (!response.ok) {
            return null;
        }
        const data = await readJson(response);
        return Array.isArray(data) ? data : [];
    } catch (error) {
        return null;
    }
}

export async function getAlertaReferencia() {
    try {
        const response = await apiFetch(`${API_BASE}/tickets/alerta-referencia`);
        if (!response.ok) {
            return null;
        }
        return await readJson(response);
    } catch (error) {
        return null;
    }
}

export async function listNovosAlerta(aposId, limite = 50) {
    try {
        const url = `${API_BASE}/tickets/novos-alerta?aposId=${encodeURIComponent(aposId)}&limite=${limite}`;
        const response = await apiFetch(url);
        if (!response.ok) {
            return null;
        }
        const data = await readJson(response);
        return Array.isArray(data) ? data : [];
    } catch (error) {
        return null;
    }
}
