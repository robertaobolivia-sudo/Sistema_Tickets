/**
 * Opções de status manual no Chats (alinhado a TicketStatusTransicaoService — Sprint 295/297).
 */

import { isTicketStatusAtivo } from '@features/chats/chats-view.js';

const MANUAL_FROM_TO = {
    ABERTO: ['EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE'],
    EM_ATENDIMENTO: ['AGUARDANDO_CLIENTE'],
    AGUARDANDO_CLIENTE: ['EM_ATENDIMENTO']
};

const STATUS_LABEL = {
    EM_ATENDIMENTO: 'Em atendimento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente'
};

function normalizeStatus(status) {
    return status == null ? '' : String(status).trim().toUpperCase();
}

/** Destinos permitidos via PUT /status no Chats (sem RESOLVIDO/INDEVIDO). */
export function getChatsManualStatusOptions(ticket) {
    if (!ticket?.numeroTicket || !isTicketStatusAtivo(ticket?.status)) {
        return [];
    }
    const atual = normalizeStatus(ticket.status);
    const codes = MANUAL_FROM_TO[atual] || [];
    return codes.map(code => ({
        code,
        label: STATUS_LABEL[code] || code
    }));
}

export function buildChatsStatusUpdateBody(targetStatus, analistaId) {
    const status = normalizeStatus(targetStatus);
    if (status === 'EM_ATENDIMENTO') {
        return { status, analistaId: analistaId != null ? Number(analistaId) : null };
    }
    return { status };
}
