/**
 * Rótulos e classes CSS de SLA (funções puras).
 */

export const SLA_VISUAL_STATUSES = [
    'DENTRO_DO_PRAZO',
    'PROXIMO_DO_VENCIMENTO',
    'VENCIDO',
    'PAUSADO',
    'CUMPRIDO',
    'VIOLADO',
    'NAO_CALCULADO'
];

const SLA_LABELS = {
    DENTRO_DO_PRAZO: 'Dentro do prazo',
    PROXIMO_DO_VENCIMENTO: 'Próximo do vencimento',
    VENCIDO: 'Vencido',
    PAUSADO: 'Pausado',
    CUMPRIDO: 'Cumprido',
    VIOLADO: 'Violado'
};

export function formatSlaStatusLabel(status) {
    if (!status || status === 'NAO_CALCULADO') {
        return '-';
    }
    return SLA_LABELS[status] || String(status);
}

export function getSlaBadgeCssClass(status) {
    if (!status || status === 'NAO_CALCULADO') {
        return null;
    }
    return `sla-badge sla-${String(status).toLowerCase().replace(/_/g, '-')}`;
}

export function formatSlaBadgeHtml(status) {
    const label = formatSlaStatusLabel(status);
    if (label === '-') {
        return '-';
    }
    const css = getSlaBadgeCssClass(status);
    return `<span class="${css}">${label}</span>`;
}

export function getSlaResolucaoVisualStatus(ticket) {
    if (!ticket) {
        return null;
    }
    if (ticket.slaResolucaoStatus) {
        return ticket.slaResolucaoStatus;
    }
    return ticket.slaPausado ? 'PAUSADO' : null;
}

export function formatSlaResolucaoBadge(ticket) {
    return formatSlaBadgeHtml(getSlaResolucaoVisualStatus(ticket));
}
