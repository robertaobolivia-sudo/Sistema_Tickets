/**
 * Sprint 284 — SLA vivo e encerramentos do dia (Visão geral Dashboard).
 */

export function normalizeSlaVivoFromApi(slaPayload) {
    const vivo = slaPayload?.vivo ?? {};
    const critico = slaPayload?.ticketsCriticosSla?.[0] ?? vivo?.ticketMaisCritico ?? null;
    return {
        dentroDoPrazo: Number(vivo.dentroDoPrazo ?? 0),
        proximosDoLimite: Number(vivo.proximosDoLimite ?? 0),
        vencidos: Number(vivo.vencidos ?? 0),
        ticketMaisCritico: critico
    };
}

export function formatTicketCriticoLinha(ticket, displayValue) {
    if (!ticket) {
        return null;
    }
    const numero = displayValue(ticket.numeroTicket);
    const cliente = displayValue(ticket.cliente);
    const prioridade = displayValue(ticket.prioridade);
    return `${numero} · ${cliente} · ${prioridade}`;
}

export function normalizeEncerramentosDia(data) {
    const rec = Array.isArray(data?.recorrencias) ? data.recorrencias : [];
    return {
        finalizados: Number(data?.finalizados ?? 0),
        naoResolvidos: Number(data?.naoResolvidos ?? 0),
        escalonados: Number(data?.escalonados ?? 0),
        abandonados: Number(data?.abandonados ?? 0),
        recorrencias: rec
            .map(r => ({
                rotulo: String(r?.rotulo ?? '—').trim() || '—',
                total: Number(r?.total ?? 0)
            }))
            .filter(r => r.total > 0)
    };
}

export function formatMediaAtual(media) {
    if (media == null || Number.isNaN(Number(media))) {
        return '—';
    }
    const n = Number(media);
    return Number.isInteger(n) ? String(n) : n.toFixed(1).replace('.', ',');
}

export function normalizeAvaliacaoTempoReal(data) {
    return {
        mediaAtual: data?.mediaAtual ?? null,
        avaliacoesRuins: Number(data?.avaliacoesRuins ?? 0),
        pesquisasRespondidas: Number(data?.pesquisasRespondidas ?? 0),
        pesquisasPendentes: Number(data?.pesquisasPendentes ?? 0),
        pesquisasExpiradas: Number(data?.pesquisasExpiradas ?? 0)
    };
}

export function buildRecorrenciasHtml(recorrencias, escapeHtml) {
    if (!recorrencias?.length) {
        return '<li class="empty-state">Nenhuma recorrência registrada hoje.</li>';
    }
    return recorrencias
        .map(
            r =>
                `<li><span>${escapeHtml(r.rotulo)}</span><strong>${escapeHtml(String(r.total))}</strong></li>`
        )
        .join('');
}
