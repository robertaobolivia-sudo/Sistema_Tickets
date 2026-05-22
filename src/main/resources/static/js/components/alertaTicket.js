import * as ticketService from '../services/ticketService.js';
import * as dashboardService from '../services/dashboardService.js';
import {
    updateTopbarUserMenuLabels,
    isPrefEnabled,
    setPrefEnabled
} from './topbar.js';

const topbarTicketAlertBadge = document.getElementById('topbarTicketAlertBadge');
const topbarTicketToast = document.getElementById('topbarTicketToast');

export const PREF_ALERTA_TICKET = 'suporteTicketsAlertaTicket';
const PREF_TICKET_BASELINE_TOTAL = 'suporteTicketsTicketBaselineTotal';
const PREF_TICKET_BASELINE_HOJE = 'suporteTicketsTicketBaselineHoje';
const PREF_ULTIMO_TICKET_CONHECIDO = 'suporteTicketsUltimoTicketConhecido';
const PREF_ULTIMO_TICKET_ID = 'suporteTicketsUltimoTicketId';
const TICKET_ALERT_POLL_MS = 30000;

let ticketAlertPollTimer = null;
let ticketAlertToastTimer = null;
let ticketAlertCheckInFlight = false;
let ticketAlertVisibilityBound = false;
let ticketAlertBaselineSyncing = false;
let ticketAlertBaselinePromise = null;
let toastListenersBound = false;

let showPageFn = () => {};
let loadTicketTableFn = () => {};
let closeTopbarUserMenuFn = () => {};
let fecharNotificacoesPainelFn = () => {};
let pagesRef = {};
let loadDashboardFn = () => {};
let updateDashboardFromResumoFn = () => {};
let parseDashboardMetricNumberFn = () => 0;
let getDashboardAlertMetricElementsFn = () => ({});
let resolveTicketNumberFn = () => '';

function menuLabelExtra() {
    return {
        isAlertaTicketEnabled: () => isPrefEnabled(PREF_ALERTA_TICKET, false),
        isAlertaTicketSyncing: () => ticketAlertBaselineSyncing
    };
}

function refreshMenuLabels() {
    updateTopbarUserMenuLabels(menuLabelExtra());
}

export function isAlertaTicketEnabled() {
    return isPrefEnabled(PREF_ALERTA_TICKET, false);
}

function extractTicketNumero(ticket) {
    return resolveTicketNumberFn(ticket?.numeroTicket, ticket);
}

export function getTicketAlertKey(ticket) {
    const numero = extractTicketNumero(ticket);
    if (numero) {
        return `n:${numero}`;
    }
    if (ticket?.id !== undefined && ticket?.id !== null) {
        return `id:${ticket.id}`;
    }
    return '';
}

function loadKnownTicketKeys() {
    try {
        const raw = localStorage.getItem(PREF_ULTIMO_TICKET_CONHECIDO);
        if (!raw) return new Set();
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) return new Set();
        const normalized = parsed
            .map(item => {
                if (typeof item !== 'string' || !item) return '';
                if (item.startsWith('n:') || item.startsWith('id:')) return item;
                return `n:${item}`;
            })
            .filter(Boolean);
        return new Set(normalized);
    } catch (error) {
        return new Set();
    }
}

function saveKnownTicketKeys(keysSet) {
    localStorage.setItem(PREF_ULTIMO_TICKET_CONHECIDO, JSON.stringify([...keysSet]));
}

async function fetchTicketsForAlert() {
    return ticketService.listTicketsForAlert();
}

function getUltimoTicketIdArmazenado() {
    const valor = Number(localStorage.getItem(PREF_ULTIMO_TICKET_ID));
    return Number.isFinite(valor) && valor >= 0 ? valor : 0;
}

function atualizarUltimoTicketIdAposNovos(tickets) {
    let max = getUltimoTicketIdArmazenado();
    (tickets || []).forEach(ticket => {
        const id = Number(ticket?.id);
        if (Number.isFinite(id) && id > max) {
            max = id;
        }
    });
    localStorage.setItem(PREF_ULTIMO_TICKET_ID, String(max));
}

async function fetchAlertaReferencia() {
    return ticketService.getAlertaReferencia();
}

async function fetchTicketsNovosAlerta() {
    const aposId = getUltimoTicketIdArmazenado();
    return ticketService.listNovosAlerta(aposId, 50);
}

function mergeKnownTicketKeysFromTickets(tickets) {
    const keys = loadKnownTicketKeys();
    (tickets || []).forEach(ticket => {
        const key = getTicketAlertKey(ticket);
        if (key) {
            keys.add(key);
        }
    });
    saveKnownTicketKeys(keys);
    atualizarUltimoTicketIdAposNovos(tickets);
}

async function capturarBaselineTickets() {
    const ref = await fetchAlertaReferencia();
    const tickets = await fetchTicketsForAlert();
    if (tickets === null) {
        return false;
    }
    if (ref !== null && ref.ultimoId !== undefined && ref.ultimoId !== null) {
        const refId = Number(ref.ultimoId);
        let maxId = Number.isFinite(refId) ? refId : 0;
        tickets.forEach(ticket => {
            const id = Number(ticket?.id);
            if (Number.isFinite(id) && id > maxId) {
                maxId = id;
            }
        });
        localStorage.setItem(PREF_ULTIMO_TICKET_ID, String(maxId));
    }
    mergeKnownTicketKeysFromTickets(tickets);
    return true;
}

async function sincronizarBaselineAlertaTicket() {
    if (ticketAlertBaselinePromise) {
        return ticketAlertBaselinePromise;
    }
    ticketAlertBaselineSyncing = true;
    refreshMenuLabels();
    ticketAlertBaselinePromise = (async () => {
        try {
            return await capturarBaselineTickets();
        } finally {
            ticketAlertBaselineSyncing = false;
            ticketAlertBaselinePromise = null;
            refreshMenuLabels();
        }
    })();
    return ticketAlertBaselinePromise;
}

async function rebuildTicketAlertBaseline() {
    await capturarBaselineTickets();
}

export function initTicketAlertBaseline(resumo) {
    if (sessionStorage.getItem(PREF_TICKET_BASELINE_TOTAL) !== null) {
        return;
    }
    const total = Number(resumo?.totalTickets ?? 0);
    const hoje = Number(resumo?.ticketsAbertosHoje ?? 0);
    sessionStorage.setItem(PREF_TICKET_BASELINE_TOTAL, String(total));
    sessionStorage.setItem(PREF_TICKET_BASELINE_HOJE, String(hoje));
}

export function checkTicketAlertVisual(resumo) {
    if (!isAlertaTicketEnabled()) {
        clearTicketAlertVisual();
        return;
    }
    const { totalTickets: totalTicketsEl, openedTodayTickets: openedTodayEl } = getDashboardAlertMetricElementsFn();
    const total = Number(resumo?.totalTickets ?? parseDashboardMetricNumberFn(totalTicketsEl));
    const hoje = Number(resumo?.ticketsAbertosHoje ?? parseDashboardMetricNumberFn(openedTodayEl));
    const baselineTotal = Number(sessionStorage.getItem(PREF_TICKET_BASELINE_TOTAL) ?? total);
    const baselineHoje = Number(sessionStorage.getItem(PREF_TICKET_BASELINE_HOJE) ?? hoje);
    if (total > baselineTotal || hoje > baselineHoje) {
        document.body.classList.add('ticket-alert-active');
        topbarTicketAlertBadge?.classList.remove('hidden');
    } else {
        clearTicketAlertVisual();
    }
}

export function clearTicketAlertVisual() {
    document.body.classList.remove('ticket-alert-active');
    topbarTicketAlertBadge?.classList.add('hidden');
}

export function acknowledgeTicketAlert(resumo) {
    const { totalTickets: totalTicketsEl, openedTodayTickets: openedTodayEl } = getDashboardAlertMetricElementsFn();
    const total = Number(resumo?.totalTickets ?? parseDashboardMetricNumberFn(totalTicketsEl));
    const hoje = Number(resumo?.ticketsAbertosHoje ?? parseDashboardMetricNumberFn(openedTodayEl));
    sessionStorage.setItem(PREF_TICKET_BASELINE_TOTAL, String(total));
    sessionStorage.setItem(PREF_TICKET_BASELINE_HOJE, String(hoje));
    clearTicketAlertVisual();
    hideTicketAlertToast();
    if (isAlertaTicketEnabled()) {
        rebuildTicketAlertBaseline();
    }
}

function showTicketAlertToast(message) {
    if (!topbarTicketToast || !message) return;
    topbarTicketToast.textContent = message;
    topbarTicketToast.classList.remove('hidden');
    topbarTicketToast.setAttribute('tabindex', '0');
    topbarTicketToast.setAttribute('role', 'button');
    if (ticketAlertToastTimer) {
        clearTimeout(ticketAlertToastTimer);
    }
    ticketAlertToastTimer = setTimeout(() => {
        hideTicketAlertToast();
    }, 8000);
}

export function registrarTicketsNovosNoAlerta(ticketsNovos) {
    if (!ticketsNovos?.length || !isAlertaTicketEnabled()) {
        return;
    }
    const known = loadKnownTicketKeys();
    const numerosExibicao = [];
    ticketsNovos.forEach(ticket => {
        const key = getTicketAlertKey(ticket);
        if (!key || known.has(key)) {
            return;
        }
        known.add(key);
        const numero = extractTicketNumero(ticket);
        numerosExibicao.push(numero || key.replace(/^n:|^id:/, ''));
    });
    if (!numerosExibicao.length) {
        return;
    }
    saveKnownTicketKeys(known);
    atualizarUltimoTicketIdAposNovos(ticketsNovos);
    document.body.classList.add('ticket-alert-active');
    topbarTicketAlertBadge?.classList.remove('hidden');
    const msg = numerosExibicao.length === 1
        ? `Novo ticket recebido: ${numerosExibicao[0]}`
        : `${numerosExibicao.length} novos tickets recebidos`;
    showTicketAlertToast(msg);
    if (pagesRef.tickets?.classList.contains('active')) {
        loadTicketTableFn();
    } else if (pagesRef.dashboard?.classList.contains('active')) {
        dashboardService
            .getResumo()
            .then(resumo => updateDashboardFromResumoFn(resumo))
            .catch(() => null);
    }
}

export function hideTicketAlertToast() {
    topbarTicketToast?.classList.add('hidden');
    if (ticketAlertToastTimer) {
        clearTimeout(ticketAlertToastTimer);
        ticketAlertToastTimer = null;
    }
}

async function fetchTicketsForAlertFallbackIncremental() {
    const aposId = getUltimoTicketIdArmazenado();
    const tickets = await fetchTicketsForAlert();
    if (tickets === null) {
        return null;
    }
    return tickets.filter(ticket => {
        const id = Number(ticket?.id);
        return Number.isFinite(id) && id > aposId;
    });
}

export async function verificarNovosTickets() {
    if (!isAlertaTicketEnabled() || ticketAlertCheckInFlight || ticketAlertBaselineSyncing) {
        return;
    }
    ticketAlertCheckInFlight = true;
    try {
        let novos = await fetchTicketsNovosAlerta();
        if (novos === null) {
            novos = await fetchTicketsForAlertFallbackIncremental();
            if (novos === null) {
                return;
            }
        }
        if (!novos.length) {
            return;
        }
        const known = loadKnownTicketKeys();
        const pendentes = novos.filter(ticket => {
            const key = getTicketAlertKey(ticket);
            return key && !known.has(key);
        });
        registrarTicketsNovosNoAlerta(pendentes);
    } finally {
        ticketAlertCheckInFlight = false;
    }
}

function pararMonitoramentoNovosTickets() {
    if (ticketAlertPollTimer) {
        clearInterval(ticketAlertPollTimer);
        ticketAlertPollTimer = null;
    }
}

function iniciarMonitoramentoNovosTickets() {
    pararMonitoramentoNovosTickets();
    if (!isAlertaTicketEnabled()) {
        return;
    }
    verificarNovosTickets();
    ticketAlertPollTimer = setInterval(() => {
        verificarNovosTickets();
    }, TICKET_ALERT_POLL_MS);
}

function onTicketAlertVisibilityChange() {
    if (document.visibilityState !== 'visible') {
        return;
    }
    if (!isAlertaTicketEnabled()) {
        return;
    }
    verificarNovosTickets();
}

export function stopTicketAlertPolling() {
    pararMonitoramentoNovosTickets();
}

async function prepararAlertaTicketAoCarregar() {
    pararMonitoramentoNovosTickets();
    const ok = await sincronizarBaselineAlertaTicket();
    if (isAlertaTicketEnabled() && ok) {
        iniciarMonitoramentoNovosTickets();
    }
}

export async function toggleAlertaTicket() {
    if (ticketAlertBaselineSyncing) {
        return;
    }
    const next = !isAlertaTicketEnabled();
    setPrefEnabled(PREF_ALERTA_TICKET, next);
    refreshMenuLabels();
    if (next) {
        pararMonitoramentoNovosTickets();
        const ok = await sincronizarBaselineAlertaTicket();
        if (isAlertaTicketEnabled() && ok) {
            iniciarMonitoramentoNovosTickets();
        }
    } else {
        pararMonitoramentoNovosTickets();
        clearTicketAlertVisual();
        hideTicketAlertToast();
    }
}

function bindTicketAlertToast() {
    if (!topbarTicketToast || topbarTicketToast.dataset.bound === 'true') {
        return;
    }
    topbarTicketToast.dataset.bound = 'true';
    topbarTicketToast.classList.add('topbar-ticket-toast-clickable');
    topbarTicketToast.setAttribute('title', 'Clique para abrir a página Tickets');
    topbarTicketToast.addEventListener('click', () => {
        hideTicketAlertToast();
        clearTicketAlertVisual();
        closeTopbarUserMenuFn();
        fecharNotificacoesPainelFn();
        showPageFn('tickets');
        loadTicketTableFn();
    });
    topbarTicketToast.addEventListener('keydown', event => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            topbarTicketToast.click();
        }
    });
}

export function initAlertaTicket(deps) {
    showPageFn = deps.showPage || showPageFn;
    loadTicketTableFn = deps.loadTicketTable || loadTicketTableFn;
    closeTopbarUserMenuFn = deps.closeTopbarUserMenu || closeTopbarUserMenuFn;
    fecharNotificacoesPainelFn = deps.fecharNotificacoesPainel || fecharNotificacoesPainelFn;
    pagesRef = deps.pages || pagesRef;
    loadDashboardFn = deps.loadDashboard || loadDashboardFn;
    updateDashboardFromResumoFn = deps.updateDashboardFromResumo || updateDashboardFromResumoFn;
    parseDashboardMetricNumberFn = deps.parseDashboardMetricNumber || parseDashboardMetricNumberFn;
    getDashboardAlertMetricElementsFn = deps.getDashboardAlertMetricElements || getDashboardAlertMetricElementsFn;
    resolveTicketNumberFn = deps.resolveTicketNumber || resolveTicketNumberFn;

    bindTicketAlertToast();
    if (!ticketAlertVisibilityBound) {
        ticketAlertVisibilityBound = true;
        document.addEventListener('visibilitychange', onTicketAlertVisibilityChange);
    }
    refreshMenuLabels();
}

export function initAlertasUsuarioOnOpenApp() {
    if (isAlertaTicketEnabled()) {
        void prepararAlertaTicketAoCarregar().catch(() => null);
    } else {
        pararMonitoramentoNovosTickets();
    }
}
