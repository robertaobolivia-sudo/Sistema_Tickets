/**
 * Rótulos gerenciais — tickets INDEVIDO / Não atendimento (Sprint 277).
 */

export function formatIndicadorStatusRotulo(rotulo) {
    if (!rotulo) {
        return '-';
    }
    const key = String(rotulo).trim();
    if (key === 'NAO_ATENDIMENTO_INDEVIDO' || key === 'INDEVIDO') {
        return 'Não atendimento (Indevido)';
    }
    return key;
}

export function formatTicketStatusExibicao(status) {
    if (!status) {
        return '-';
    }
    if (String(status).trim().toUpperCase() === 'INDEVIDO') {
        return 'Não atendimento';
    }
    return String(status);
}
