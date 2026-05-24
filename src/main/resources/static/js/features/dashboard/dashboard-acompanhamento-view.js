/**
 * Sprint 283 — Dashboard Acompanhamento (somente leitura).
 */

import {
    buildChatsTimelineEntries,
    isInteracaoFromCliente
} from '@features/chats/chats-view.js';
import { formatChamadoStatusLabel } from '@features/dashboard/dashboard-operacao-cliente-b2b-view.js';

export function extractNumeroTicketAcompanhar(chamado) {
    if (!chamado || chamado.tipo !== 'TICKET') {
        return null;
    }
    const raw = String(chamado.numeroTicket ?? '').trim();
    const match = raw.match(/^(TK-[\w-]+)/i);
    return match ? match[1].toUpperCase() : raw || null;
}

export function calcularTemposAcompanhamento(ticket) {
    const agora = Date.now();
    const abertura = ticket?.dataAbertura ? new Date(ticket.dataAbertura).getTime() : NaN;
    let tme = '-';
    if (!Number.isNaN(abertura)) {
        tme = formatarDuracaoMs(agora - abertura);
    }
    const status = String(ticket?.status ?? '').toUpperCase();
    const inicioAtend =
        ticket?.dataPrimeiroAtendimento != null
            ? new Date(ticket.dataPrimeiroAtendimento).getTime()
            : status === 'EM_ATENDIMENTO' && !Number.isNaN(abertura)
              ? abertura
              : NaN;
    let tma = '-';
    if (status === 'EM_ATENDIMENTO' && !Number.isNaN(inicioAtend)) {
        tma = formatarDuracaoMs(agora - inicioAtend);
    }
    return { tme, tma };
}

function formatarDuracaoMs(ms) {
    if (!Number.isFinite(ms) || ms < 0) {
        return '-';
    }
    const totalSec = Math.floor(ms / 1000);
    const h = Math.floor(totalSec / 3600);
    const m = Math.floor((totalSec % 3600) / 60);
    const s = totalSec % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function escapeHtml(text) {
    return String(text ?? '—')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

export function renderReadOnlyTimeline(
    container,
    emptyEl,
    ticket,
    interacoes,
    satisfacao,
    { displayValue, formatDateTime }
) {
    if (!container) {
        return;
    }
    container.innerHTML = '';
    const entries = buildChatsTimelineEntries(ticket, interacoes, { satisfacao });
    if (!entries.length) {
        emptyEl?.classList.remove('hidden');
        if (emptyEl) {
            emptyEl.textContent = 'Nenhuma mensagem ou evento registrado.';
        }
        return;
    }
    emptyEl?.classList.add('hidden');

    entries.forEach(entry => {
        if (entry.kind === 'date') {
            const sep = document.createElement('div');
            sep.className = 'dashboard-acomp-date-separator';
            sep.textContent = entry.label;
            container.appendChild(sep);
            return;
        }
        if (entry.kind === 'event') {
            const ev = document.createElement('div');
            ev.className = 'dashboard-acomp-event';
            const subHtml = entry.sub
                ? `<p class="dashboard-acomp-event-desc">${escapeHtml(entry.sub)}</p>`
                : '';
            ev.innerHTML = `<span class="dashboard-acomp-event-marker" aria-hidden="true"></span>
                <div class="dashboard-acomp-event-body">
                    <p class="dashboard-acomp-event-title">${escapeHtml(entry.text)}</p>
                    ${subHtml}
                    <time class="dashboard-acomp-event-time">${escapeHtml(formatDateTime(entry.at))}</time>
                </div>`;
            container.appendChild(ev);
            return;
        }
        const interacao = entry.interacao;
        const fromCliente = isInteracaoFromCliente(interacao);
        const bubble = document.createElement('div');
        bubble.className = fromCliente
            ? 'dashboard-acomp-msg dashboard-acomp-msg-in'
            : 'dashboard-acomp-msg dashboard-acomp-msg-out';
        const body = document.createElement('div');
        body.className = 'dashboard-acomp-msg-body';
        body.textContent = displayValue(interacao.mensagem);
        const meta = document.createElement('div');
        meta.className = 'dashboard-acomp-msg-meta';
        meta.innerHTML = `<time>${escapeHtml(formatDateTime(interacao.criadoEm))}</time><span>${escapeHtml(displayValue(interacao.tipoInteracao))}</span>`;
        bubble.appendChild(body);
        bubble.appendChild(meta);
        container.appendChild(bubble);
    });
    const wrap = container.parentElement;
    if (wrap) {
        wrap.scrollTop = wrap.scrollHeight;
    }
}

export function buildResumoRows(ticket, clienteNome, tempos) {
    const contato =
        ticket?.contatoNome ||
        ticket?.contato?.nome ||
        ticket?.cliente ||
        '—';
    return [
        { label: 'Protocolo', value: ticket?.numeroTicket },
        { label: 'Cliente B2B', value: clienteNome || ticket?.cliente },
        { label: 'Contato', value: contato },
        { label: 'Status', value: formatChamadoStatusLabel(ticket?.status) },
        { label: 'Analista', value: ticket?.analistaResponsavelNome || '—' },
        { label: 'TME', value: tempos?.tme ?? '—' },
        { label: 'TMA', value: tempos?.tma ?? '—' }
    ];
}
