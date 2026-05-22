/**
 * Camada de dados da página Chats.
 * Sprint 132: reutiliza tickets existentes; integração WhatsApp/API em sprints futuras.
 */

import { API_BASE, apiFetch } from '../core/api.js';
import { readJson } from './httpUtil.js';
import * as ticketService from './ticketService.js';
import * as anexoService from './anexoService.js';
import { getSatisfacaoByTicket } from './satisfacaoService.js';

export function listTicketsBase() {
    return ticketService.listTickets('ALL');
}

export function getTicketDetail(numeroTicket) {
    return ticketService.getTicketByNumero(numeroTicket);
}

/** Histórico resumido do cliente para o painel direito (carregado ao selecionar conversa). */
export async function getHistoricoResumido(numeroTicket) {
    if (!numeroTicket) {
        return null;
    }
    const encoded = encodeURIComponent(numeroTicket);
    const response = await apiFetch(`${API_BASE}/chats/${encoded}/historico-resumido`);
    if (!response.ok) {
        const data = await readJson(response);
        const msg =
            (data && (data.message || data.erro)) ||
            'Não foi possível carregar o histórico resumido.';
        throw new Error(msg);
    }
    return readJson(response);
}

export function listTicketInteracoes(numeroTicket) {
    return ticketService.listInteracoes(numeroTicket);
}

export function getTicketSatisfacao(numeroTicket) {
    return getSatisfacaoByTicket(numeroTicket);
}

export function listTicketAnexos(numeroTicket) {
    return anexoService.listTicketAnexos(numeroTicket);
}

export function uploadTicketAnexo(numeroTicket, file) {
    return anexoService.uploadTicketAnexo(numeroTicket, file);
}

export function downloadTicketAnexoBlob(numeroTicket, anexoId, nomeArquivo) {
    return anexoService.downloadTicketAnexoBlob(numeroTicket, anexoId, nomeArquivo);
}

/** Reservado para envio em tempo real (não implementado). */
export function sendChatMessage() {
    return Promise.reject(new Error('Envio de mensagens pelo Chats será habilitado após integração com a API.'));
}

/** Reservado para anexos (não implementado). */
export function uploadChatAttachment() {
    return Promise.reject(new Error('Anexos serão habilitados após integração com a API.'));
}
