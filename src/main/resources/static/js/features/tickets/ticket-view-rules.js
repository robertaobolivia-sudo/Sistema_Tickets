/**
 * Regras visuais e de fluxo de ticket (sem DOM).
 */

export const TICKET_STATUS_VALIDOS = [
    'ABERTO',
    'EM_ATENDIMENTO',
    'AGUARDANDO_CLIENTE',
    'RESOLVIDO',
    'CANCELADO',
    'INDEVIDO'
];

const TICKET_STATUS_SET = new Set(TICKET_STATUS_VALIDOS);

export function isTicketFinalizado(status) {
    return status === 'RESOLVIDO' || status === 'CANCELADO' || status === 'INDEVIDO';
}

/** Formulário de satisfação só para ticket encerrado (resolvido/cancelado) sem avaliação. */
export function canRegisterSatisfacao(ticketStatus, hasExistingSatisfacao = false) {
    if (hasExistingSatisfacao) {
        return false;
    }
    return isTicketFinalizado(ticketStatus);
}

export function canAddTicketInteraction(ticketStatus) {
    if (ticketStatus === 'INDEVIDO') {
        return false;
    }
    return !isTicketFinalizado(ticketStatus);
}

export function getStatusClass(status) {
    const valor = status == null || status === '' ? '-' : String(status);
    if (valor === '-' || !TICKET_STATUS_SET.has(valor)) {
        return 'status-badge';
    }
    return `status-badge status-${valor}`;
}
