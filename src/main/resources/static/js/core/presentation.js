/**
 * Formatação e helpers de UI compartilhados (sem DOM), injetados nas páginas via app.js.
 */
import { mensagemParaExibirUsuario } from './messages.js';
import { getStatusClass as getStatusClassFromRules } from '../rules/ticketViewRules.js';
import {
    formatSlaBadgeHtml as formatSlaBadgeHtmlRule,
    formatSlaResolucaoBadge as formatSlaResolucaoBadgeRule,
    formatSlaStatusLabel as formatSlaStatusLabelRule
} from '../rules/slaViewRules.js';

export { formatSlaResolucaoBadgeRule as formatSlaResolucaoBadge };

export function formatDateTime(value) {
    if (value === null || value === undefined || value === '') return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function formatDate(value) {
    if (value === null || value === undefined || value === '') return '-';
    const [year, month, day] = String(value).split('-');
    if (!year || !month || !day) return '-';
    return `${day}/${month}/${year}`;
}

export function showAlert(message, container, type = 'error') {
    if (!container) return;
    container.textContent = mensagemParaExibirUsuario(message);
    container.className = `alert ${type}`;
    container.classList.remove('hidden');
    setTimeout(() => container.classList.add('hidden'), 6000);
}

export function clearAlert(container) {
    if (!container) return;
    container.classList.add('hidden');
}

export function resolveTicketNumber(value, fallbackSource) {
    const direct = value === null || value === undefined ? '' : String(value).trim();
    if (direct && direct !== '-') {
        return direct;
    }
    const fromSource = fallbackSource?.ticketNumero ?? fallbackSource?.numero ?? '';
    const resolved = String(fromSource).trim();
    return resolved && resolved !== '-' ? resolved : '';
}

export function displayValue(value) {
    if (value === null || value === undefined) return '-';
    const text = String(value).trim();
    if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined' || text === 'NaN') {
        return '-';
    }
    return text;
}

export function getStatusClass(status) {
    const valor = displayValue(status);
    if (valor === '-') {
        return getStatusClassFromRules(null);
    }
    return getStatusClassFromRules(valor);
}

export function formatPriority(value) {
    if (!value || value === '-') {
        return '-';
    }
    const labels = {
        BAIXA: 'Baixa',
        MEDIA: 'Média',
        ALTA: 'Alta',
        CRITICA: 'Crítica'
    };
    return labels[value] || value;
}

export function getPriorityClass(priority) {
    if (!priority || priority === '-') {
        return 'priority-badge priority-NA';
    }
    return `priority-badge priority-${priority}`;
}

function getPriorityRank(prioridade) {
    const ranks = {
        CRITICA: 0,
        ALTA: 1,
        MEDIA: 2,
        BAIXA: 3
    };
    if (!prioridade || prioridade === '-') {
        return 4;
    }
    return ranks[prioridade] ?? 4;
}

function getTicketAberturaTimestamp(ticket) {
    if (!ticket?.dataAbertura) {
        return Number.MAX_SAFE_INTEGER;
    }
    const time = new Date(ticket.dataAbertura).getTime();
    return Number.isNaN(time) ? Number.MAX_SAFE_INTEGER : time;
}

export function sortTicketsByPriority(tickets) {
    if (!Array.isArray(tickets)) {
        return [];
    }
    return [...tickets].sort((left, right) => {
        const rankDiff = getPriorityRank(left.prioridade) - getPriorityRank(right.prioridade);
        if (rankDiff !== 0) {
            return rankDiff;
        }
        return getTicketAberturaTimestamp(left) - getTicketAberturaTimestamp(right);
    });
}

export function formatPriorityBadgeHtml(prioridade) {
    return `<span class="${getPriorityClass(prioridade)}">${formatPriority(prioridade)}</span>`;
}

export function formatSlaStatusLabel(status) {
    const label = formatSlaStatusLabelRule(status);
    if (label === '-' && status && status !== 'NAO_CALCULADO') {
        return displayValue(status);
    }
    return label;
}

export const formatSlaPrimeiroAtendimentoLabel = formatSlaStatusLabel;

export const formatSlaBadgeHtml = formatSlaBadgeHtmlRule;

export function formatSlaResolucaoCellHtml(ticket) {
    return appendSlaEscalonamentoBadgesHtml(formatSlaResolucaoBadgeRule(ticket), ticket);
}

export function formatSlaPausadoSimNao(valor) {
    return valor ? 'Sim' : 'Não';
}

export function formatSlaMinutosPausados(valor) {
    const minutos = Number(valor);
    if (!Number.isFinite(minutos) || minutos <= 0) {
        return '0 min';
    }
    return `${minutos} min`;
}

export function getTicketPriorityRowClass(prioridade, scope = 'table') {
    if (!prioridade || prioridade === '-') {
        return '';
    }
    if (prioridade === 'CRITICA') {
        return scope === 'kanban' ? 'kanban-ticket-priority-critica' : 'ticket-row-priority-critica';
    }
    if (prioridade === 'ALTA') {
        return scope === 'kanban' ? 'kanban-ticket-priority-alta' : 'ticket-row-priority-alta';
    }
    return '';
}

export function escapeAttr(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;');
}

export function formatEscalonamentoBadgeHtml(ticket) {
    if (!ticket?.escalonado) {
        return '';
    }
    return '<span class="sla-badge sla-escalonado">Escalonado</span>';
}

export function appendSlaEscalonamentoBadgesHtml(slaHtml, ticket) {
    const escalonamento = formatEscalonamentoBadgeHtml(ticket);
    if (slaHtml === '-' && !escalonamento) {
        return '-';
    }
    return `${slaHtml}${escalonamento}`;
}

export function getAnalystInitials(name) {
    if (!name) return 'AN';
    return name
        .split(' ')
        .filter(Boolean)
        .slice(0, 2)
        .map(part => part.charAt(0).toUpperCase())
        .join('');
}

export function renderAnalystAvatar(analyst, large = false) {
    const classes = `analyst-avatar${large ? ' analyst-avatar-large' : ''}`;
    if (analyst?.fotoUrl) {
        return `<div class="${classes} analyst-avatar-photo"><img src="${analyst.fotoUrl}" alt="${analyst.nome || 'Analista'}" /></div>`;
    }
    return `<div class="${classes}">${getAnalystInitials(analyst?.nome)}</div>`;
}

export function getAnalystDisplayName(analyst) {
    return analyst?.nomeCompleto || analyst?.nome || '-';
}

export function getStatusOperadorLabel(status) {
    if (status === 'AUSENTE') return 'AUSENTE';
    if (status === 'OFFLINE') return 'OFFLINE';
    return 'ONLINE';
}

export function renderStatusOperador(status) {
    const normalized = getStatusOperadorLabel(status);
    return `
        <span class="operator-status operator-status-${normalized.toLowerCase()}">
            <span class="operator-status-dot"></span>
            ${normalized}
        </span>
    `;
}

export function summarizeTicket(ticket) {
    const text = ticket?.mensagemInicial || '-';
    return text.length > 70 ? `${text.substring(0, 67)}...` : text;
}

export function buildKanbanTicketMiniHtml(ticket) {
    const statusClass = getStatusClass(ticket.status);
    return `
        <div class="kanban-ticket-mini ${getTicketPriorityRowClass(ticket.prioridade, 'kanban')}">
            <div class="kanban-ticket-mini-head">
                <strong>${ticket.numeroTicket}</strong>
                ${formatPriorityBadgeHtml(ticket.prioridade)}
                <span class="${statusClass}">${displayValue(ticket.status)}</span>
            </div>
            <p class="kanban-ticket-title">${summarizeTicket(ticket)}</p>
            <div class="kanban-ticket-mini-meta">
                <span>Cliente: ${ticket.cliente || '-'}</span>
                <span>Origem: ${ticket.canal || '-'}</span>
            </div>
            <button type="button" class="button button-secondary" data-action="details" data-ticket="${ticket.numeroTicket}">Ver Detalhes</button>
        </div>
    `;
}
