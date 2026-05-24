import * as chatsService from '@features/chats/chats-service.js';
import { showPage } from '@shared/router/router.js';
import { closeOtherSidebarGroups } from '@shared/ui/sidebar-groups.js';
import {
    buildResumoRows,
    calcularTemposAcompanhamento,
    renderReadOnlyTimeline
} from '@features/dashboard/dashboard-acompanhamento-view.js';

let showAlertFn = () => {};
let displayValueFn = v => (v == null ? '-' : String(v));
let formatDateTimeFn = () => '—';

let pendingNumeroTicket = null;
let pendingClienteNome = null;

const pageEl = document.getElementById('page-dashboard-acompanhamento');
const emptyEl = document.getElementById('dashboardAcompEmpty');
const contentEl = document.getElementById('dashboardAcompContent');
const resumoEl = document.getElementById('dashboardAcompResumo');
const timelineEl = document.getElementById('dashboardAcompTimeline');
const timelineEmptyEl = document.getElementById('dashboardAcompTimelineEmpty');
const voltarBtn = document.getElementById('dashboardAcompVoltar');
const alertBox = document.getElementById('alertBox');

export function openDashboardAcompanhamento(numeroTicket, clienteNome) {
    pendingNumeroTicket = numeroTicket ? String(numeroTicket).trim() : null;
    pendingClienteNome = clienteNome ? String(clienteNome).trim() : null;
    showPage('dashboard-acompanhamento');
}

export function initDashboardAcompanhamentoPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDateTime) formatDateTimeFn = deps.formatDateTime;

    voltarBtn?.addEventListener('click', () => showPage('dashboard'));
}

export async function loadDashboardAcompanhamentoPage() {
    if (!pageEl) {
        return;
    }
    const numero = pendingNumeroTicket;
    if (!numero) {
        contentEl?.classList.add('hidden');
        emptyEl?.classList.remove('hidden');
        if (emptyEl) {
            emptyEl.textContent =
                'Selecione um chamado em Operação por Cliente B2B e use Acompanhar, ou escolha um ticket ativo no Dashboard.';
        }
        return;
    }

    emptyEl?.classList.add('hidden');
    contentEl?.classList.remove('hidden');
    if (resumoEl) {
        resumoEl.innerHTML = '<p class="empty-state">Carregando…</p>';
    }
    if (timelineEl) {
        timelineEl.innerHTML = '';
    }

    try {
        const [ticket, interacoes, satisfacao] = await Promise.all([
            chatsService.getTicketDetail(numero),
            chatsService.listTicketInteracoes(numero).catch(() => []),
            chatsService.getTicketSatisfacao(numero).catch(() => null)
        ]);

        const tempos = calcularTemposAcompanhamento(ticket);
        const rows = buildResumoRows(ticket, pendingClienteNome, tempos);
        if (resumoEl) {
            resumoEl.innerHTML = rows
                .map(
                    r =>
                        `<div class="dashboard-acomp-meta-row"><dt>${escape(r.label)}</dt><dd>${escape(r.value)}</dd></div>`
                )
                .join('');
        }

        renderReadOnlyTimeline(timelineEl, timelineEmptyEl, ticket, interacoes, satisfacao, {
            displayValue: displayValueFn,
            formatDateTime: formatDateTimeFn
        });
    } catch (error) {
        contentEl?.classList.add('hidden');
        emptyEl?.classList.remove('hidden');
        if (emptyEl) {
            emptyEl.textContent = 'Não foi possível carregar o acompanhamento deste chamado.';
        }
        showAlertFn(error?.message || 'Erro ao carregar acompanhamento.', alertBox);
    }
}

function escape(text) {
    return String(text ?? '—')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

export function initDashboardSidebarNav({ showPageFn }) {
    const group = document.getElementById('navDashboardGroup');
    const toggle = document.getElementById('navDashboardToggle');
    const submenu = document.getElementById('navDashboardSubmenu');
    if (!group || !toggle || !submenu) {
        return;
    }

    const setOpen = open => {
        group.classList.toggle('is-open', open);
        toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        submenu.classList.toggle('hidden', !open);
    };

    toggle.addEventListener('click', () => {
        const willOpen = !group.classList.contains('is-open');
        if (willOpen) closeOtherSidebarGroups('navDashboardGroup');
        setOpen(willOpen);
    });

    document.querySelectorAll('[data-dashboard-sub]').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = btn.getAttribute('data-page');
            if (btn.getAttribute('data-dashboard-sub') === 'acompanhamento') {
                pendingNumeroTicket = null;
                pendingClienteNome = null;
            }
            setOpen(true);
            showPageFn(page || 'dashboard');
        });
    });
}
