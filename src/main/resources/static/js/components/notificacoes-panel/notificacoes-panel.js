import * as notificacaoService from '@features/perfil/notificacao-service.js';
import { mensagemParaExibirUsuario } from '@shared/ui/messages.js';

const notificacoesBtn = document.getElementById('notificacoesBtn');
const notificacoesContador = document.getElementById('notificacoesContador');
const notificacoesPainel = document.getElementById('notificacoesPainel');
const notificacoesLista = document.getElementById('notificacoesLista');
const notificacoesMarcarTodasBtn = document.getElementById('notificacoesMarcarTodasBtn');
const notificacoesFiltros = document.querySelectorAll('[data-notif-filtro]');

const NOTIF_TIPOS_ESCALONAMENTO = ['TICKET_ESCALONADO', 'ESCALONAMENTO_REMOVIDO'];
const NOTIF_TIPOS_SLA = [
    'SLA_PRIMEIRO_ATENDIMENTO_VENCIDO',
    'SLA_RESOLUCAO_VENCIDA',
    'SLA_PRIMEIRO_ATENDIMENTO_PROXIMO',
    'SLA_RESOLUCAO_PROXIMA'
];

let notificacoesCache = [];
let notificacaoFiltroAtivo = 'todas';
let listenersBound = false;

let displayValueFn = (v) => String(v ?? '-');
let formatDateTimeFn = () => '-';
let resolveTicketNumberFn = () => '';
let escapeAttrFn = (v) => String(v ?? '');
let openDetailsFn = () => {};

function displayValue(value) {
    return displayValueFn(value);
}

function formatDateTime(value) {
    return formatDateTimeFn(value);
}

function resolveTicketNumber(value, fallbackSource) {
    return resolveTicketNumberFn(value, fallbackSource);
}

function escapeAttr(value) {
    return escapeAttrFn(value);
}

function isNotificacaoSla(tipo) {
    return NOTIF_TIPOS_SLA.includes(String(tipo || '').toUpperCase());
}

function filtrarNotificacoesPorTipo(lista, filtro) {
    if (!Array.isArray(lista)) return [];
    if (filtro === 'nao-lidas') {
        return lista.filter(item => !item.lida);
    }
    if (filtro === 'escalonamento') {
        return lista.filter(item => NOTIF_TIPOS_ESCALONAMENTO.includes(String(item.tipo || '').toUpperCase()));
    }
    if (filtro === 'sla') {
        return lista.filter(item => isNotificacaoSla(item.tipo));
    }
    return lista;
}

function mensagemListaNotificacoesVazia(filtro, totalCarregadas) {
    if (!totalCarregadas) {
        return 'Nenhuma notificação.';
    }
    if (filtro === 'nao-lidas') {
        return 'Nenhuma notificação não lida.';
    }
    if (filtro === 'escalonamento') {
        return 'Nenhuma notificação de escalonamento.';
    }
    if (filtro === 'sla') {
        return 'Nenhuma notificação de SLA.';
    }
    return 'Nenhuma notificação.';
}

function renderNotificacoesLista(lista) {
    if (!notificacoesLista) return;
    notificacoesLista.innerHTML = '';
    const filtrada = filtrarNotificacoesPorTipo(lista, notificacaoFiltroAtivo);
    if (!filtrada.length) {
        notificacoesLista.innerHTML = `<p class="empty-state">${mensagemListaNotificacoesVazia(notificacaoFiltroAtivo, lista.length)}</p>`;
        return;
    }
    filtrada.forEach(item => {
        const div = document.createElement('div');
        const naoLida = !item.lida;
        const ticketRef = resolveTicketNumber(item.ticketNumero, item);
        div.className = `notificacao-item${naoLida ? ' nao-lida' : ''}`;
        div.innerHTML = `
            <div class="notificacao-item-titulo">${displayValue(item.titulo)}</div>
            <div class="notificacao-item-msg">${displayValue(item.mensagem)}</div>
            <div class="notificacao-item-meta">${displayValue(ticketRef || item.ticketNumero)} · ${formatDateTime(item.criadoEm)}</div>
            <div class="notificacao-item-actions">
                ${naoLida ? `<button type="button" class="button button-secondary button-small" data-notif-lida="${item.id}">Marcar lida</button>` : ''}
                ${ticketRef ? `<button type="button" class="button button-secondary button-small" data-notif-ticket="${ticketRef}">Ver ticket</button>` : ''}
            </div>
        `;
        notificacoesLista.appendChild(div);
    });
}

function setNotificacaoFiltro(filtro) {
    notificacaoFiltroAtivo = filtro || 'todas';
    notificacoesFiltros?.forEach(btn => {
        const ativo = btn.dataset.notifFiltro === notificacaoFiltroAtivo;
        btn.classList.toggle('ativo', ativo);
        btn.setAttribute('aria-selected', ativo ? 'true' : 'false');
    });
    renderNotificacoesLista(notificacoesCache);
}

async function loadNotificacoesLista() {
    if (!notificacoesLista) return;
    try {
        const lista = await notificacaoService.listar();
        notificacoesCache = Array.isArray(lista) ? lista : [];
        renderNotificacoesLista(notificacoesCache);
    } catch (error) {
        notificacoesCache = [];
        notificacoesLista.innerHTML = `<p class="empty-state">${escapeAttr(mensagemParaExibirUsuario(error.message))}</p>`;
    }
}

async function marcarNotificacaoComoLida(id) {
    await notificacaoService.marcarLida(id);
}

async function marcarTodasNotificacoesLidas() {
    await notificacaoService.marcarTodasLidas();
}

export async function refreshNotificacoesContador() {
    if (!notificacoesContador) return;
    try {
        const data = await notificacaoService.getContadorNaoLidas();
        if (!data) return;
        const total = Number(data?.naoLidas ?? 0);
        notificacoesContador.textContent = String(total);
        notificacoesContador.classList.toggle('hidden', total <= 0);
    } catch (error) {
        console.warn('Falha ao atualizar contador de notificações', error);
    }
}

export async function refreshNotificacoesUi() {
    await refreshNotificacoesContador();
    await loadNotificacoesLista();
}

export function fecharNotificacoesPainel() {
    notificacoesPainel?.classList.add('hidden');
    notificacoesBtn?.setAttribute('aria-expanded', 'false');
}

async function toggleNotificacoesPainel() {
    if (!notificacoesPainel) return;
    const aberto = !notificacoesPainel.classList.contains('hidden');
    if (aberto) {
        fecharNotificacoesPainel();
        return;
    }
    notificacoesPainel.classList.remove('hidden');
    notificacoesBtn?.setAttribute('aria-expanded', 'true');
    await loadNotificacoesLista();
}

function bindListeners() {
    if (listenersBound) return;
    listenersBound = true;

    notificacoesFiltros?.forEach(btn => {
        btn.addEventListener('click', () => setNotificacaoFiltro(btn.dataset.notifFiltro));
    });
    notificacoesBtn?.addEventListener('click', event => {
        event.stopPropagation();
        toggleNotificacoesPainel();
    });
    notificacoesMarcarTodasBtn?.addEventListener('click', async () => {
        try {
            await marcarTodasNotificacoesLidas();
            await refreshNotificacoesUi();
        } catch (error) {
            console.warn(error.message);
        }
    });
    notificacoesLista?.addEventListener('click', async event => {
        const lidaBtn = event.target.closest('[data-notif-lida]');
        if (lidaBtn) {
            try {
                await marcarNotificacaoComoLida(lidaBtn.dataset.notifLida);
                await refreshNotificacoesUi();
            } catch (error) {
                console.warn(error.message);
            }
            return;
        }
        const ticketBtn = event.target.closest('[data-notif-ticket]');
        if (ticketBtn?.dataset.notifTicket) {
            fecharNotificacoesPainel();
            openDetailsFn(ticketBtn.dataset.notifTicket, { ticketNumero: ticketBtn.dataset.notifTicket });
        }
    });
    document.addEventListener('click', event => {
        if (notificacoesPainel && !notificacoesPainel.classList.contains('hidden')) {
            if (!event.target.closest('.notificacoes-wrap')) {
                fecharNotificacoesPainel();
            }
        }
    });
}

export function initNotificacoesPanel(deps) {
    displayValueFn = deps.displayValue || displayValueFn;
    formatDateTimeFn = deps.formatDateTime || formatDateTimeFn;
    resolveTicketNumberFn = deps.resolveTicketNumber || resolveTicketNumberFn;
    escapeAttrFn = deps.escapeAttr || escapeAttrFn;
    openDetailsFn = deps.openDetails || openDetailsFn;
    bindListeners();
}
