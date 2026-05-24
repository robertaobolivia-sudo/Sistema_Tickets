import { MSG_ERRO, mensagemErroSessaoApi } from '@shared/ui/messages.js';
import {
    getLoggedAnalyst,
    setLoggedAnalystState,
    clearLoggedAnalystSession
} from '@shared/auth/state.js';
import {
    configureAuth,
    setLoggedAnalyst,
    loginAnalista,
    fetchAnalistaPorId,
    logout,
    restoreSessionFromServer
} from '@shared/auth/auth.js';
import { canAccessPage } from '@shared/permissions/permissions.js';
import {
    formatDateTime,
    formatDate,
    showAlert,
    clearAlert,
    resolveTicketNumber,
    displayValue,
    getAnalystInitials,
    renderAnalystAvatar,
    getAnalystDisplayName,
    renderStatusOperador,
    summarizeTicket,
    buildKanbanTicketMiniHtml,
    getStatusClass,
    formatPriority,
    formatPriorityBadgeHtml,
    formatSlaPrimeiroAtendimentoLabel,
    formatSlaBadgeHtml,
    formatSlaResolucaoBadge,
    formatSlaResolucaoCellHtml,
    formatSlaPausadoSimNao,
    formatSlaMinutosPausados,
    formatEscalonamentoBadgeHtml,
    sortTicketsByPriority,
    getTicketPriorityRowClass,
    escapeAttr
} from '@shared/ui/presentation.js';
import { applyAnalystAvatarToElement, getAnalystPhotoUrl } from '@shared/ui/analyst-avatar.js';
import { configureRouter, showPage, openApp, applyMenuPermissions } from '@shared/router/router.js';
import * as ticketService from '@features/tickets/ticket-service.js';
import { initAuditoriaPage, loadAuditoriaPage } from '@features/auditoria/auditoria-page.js';
import { initRelatoriosPage, initRelatoriosSidebarNav, loadRelatoriosPage } from '@features/relatorios/relatorios-page.js';
import {
    initIndicadoresPage,
    initIndicadoresSidebarNav,
    loadIndicadoresPage
} from '@features/indicadores/indicadores-page.js';
import { initClientesPage, initClientesSidebarNav, loadClientesPage, navegarParaContatosDoCliente } from '@features/clientes/clientes-page.js';
import { initConfiguracoesPage, loadConfiguracoesPage } from '@features/configuracoes/configuracoes-page.js';
import {
    initAtendentesPage,
    loadAtendentesPage,
    loadAnalistasKanban,
    loadAdminPerfisGestao,
    updateAdminPerfisSectionVisibility,
    closeAnalystQueue
} from '@features/atendentes/atendentes-page.js';
import { initTicketsPage, loadTicketTable } from '@features/tickets/tickets-page.js';
import { initChatsPage, loadChatsPage, scheduleOpenChatsConversation } from '@features/chats/chats-page.js';
import {
    initDashboardPage,
    loadDashboard,
    loadDashboardSla,
    updateDashboardFromResumo,
    parseDashboardMetricNumber,
    getDashboardAlertMetricElements,
    refreshDashboardStatusOperador
} from '@features/dashboard/dashboard-page.js';
import {
    initDashboardAcompanhamentoPage,
    initDashboardSidebarNav,
    loadDashboardAcompanhamentoPage
} from '@features/dashboard/dashboard-acompanhamento-page.js';
import { initAbrirTicketPage, loadAbrirTicketPage } from '@features/tickets/abrir-ticket-page.js';
import {
    initPerfilPage,
    loadPerfilPage,
    renderPerfil,
    applyPerfilFotoWrapperState,
    closePerfilOverlays
} from '@features/perfil/perfil-page.js';
import {
    initTicketDetailsModal,
    openDetails,
    openEncerramentoTicketModal,
    closeModalDetail,
    closeCloseTicketModal,
    fecharModalEscalonamentoUi
} from '@components/ticket-details-modal/ticket-details-modal.js';
import {
    initNotificacoesPanel,
    refreshNotificacoesUi,
    refreshNotificacoesContador,
    fecharNotificacoesPainel
} from '@components/notificacoes-panel/notificacoes-panel.js';
import {
    initTopbar,
    initUserPreferences,
    initThemeOnLoad,
    closeTopbarUserMenu,
    updateTopbarAvatar,
    refreshTopbarStatusMenuFromSession
} from '@components/topbar/topbar.js';
import {
    initAlertaTicket,
    initAlertasUsuarioOnOpenApp,
    initTicketAlertBaseline,
    checkTicketAlertVisual,
    acknowledgeTicketAlert,
    registrarTicketsNovosNoAlerta,
    verificarNovosTickets,
    stopTicketAlertPolling,
    hideTicketAlertToast,
    toggleAlertaTicket,
    isAlertaTicketEnabled,
    getTicketAlertKey
} from '@components/alerta-ticket/alerta-ticket.js';
import * as analistaService from '@features/atendentes/analista-service.js';
import { initAvaliacaoPublicaPage } from '@features/satisfacao/avaliacao-publica-page.js';
import { initClientePortalPage, loadPortalUsuariosAdmin } from '@features/cliente-portal/cliente-portal-page.js';

const pages = {
    dashboard: document.getElementById('page-dashboard'),
    'dashboard-acompanhamento': document.getElementById('page-dashboard-acompanhamento'),
    clientes: document.getElementById('page-clientes'),
    atendentes: document.getElementById('page-atendentes'),
    perfil: document.getElementById('page-perfil'),
    'abrir-ticket': document.getElementById('page-abrir-ticket'),
    tickets: document.getElementById('page-tickets'),
    chats: document.getElementById('page-chats'),
    relatorios: document.getElementById('page-relatorios'),
    'relatorios-evolucao': document.getElementById('page-relatorios-evolucao'),
    indicadores: document.getElementById('page-indicadores'),
    configuracoes: document.getElementById('page-configuracoes'),
    auditoria: document.getElementById('page-auditoria')
};

const pageButtons = document.querySelectorAll('.nav-item');
const logoutBtn = document.getElementById('logoutBtn');
const loginScreen = document.getElementById('loginScreen');
const appScreen = document.getElementById('appScreen');
const loginUser = document.getElementById('loginUser');
const loginPass = document.getElementById('loginPass');
const loginAlert = document.getElementById('loginAlert');

const refreshButton2 = document.getElementById('refreshButton2');
const alertBox = document.getElementById('alertBox');
const alertBoxTicket = document.getElementById('alertBoxTicket');
const alertBoxTickets = document.getElementById('alertBoxTickets');
const operadorLogadoNome = document.getElementById('operadorLogadoNome');
const operadorStatusSelect = document.getElementById('operadorStatusSelect');

const loginForm = document.getElementById('loginForm');

function updateLoggedAnalystUi() {
    const session = getLoggedAnalyst();
    if (operadorLogadoNome) {
        operadorLogadoNome.textContent = getAnalystDisplayName(session);
    }
    if (operadorStatusSelect && session?.statusOperador) {
        operadorStatusSelect.value = session.statusOperador;
    }
    updateTopbarAvatar(session);
    refreshTopbarStatusMenuFromSession();
    applyMenuPermissions();
    updateAdminPerfisSectionVisibility();
    if (pages.atendentes?.classList.contains('active')) {
        loadAdminPerfisGestao();
    }
    if (pages.perfil?.classList.contains('active')) {
        renderPerfil();
    }
    const activePage = Object.entries(pages).find(([, page]) => page?.classList.contains('active'));
    if (activePage && !canAccessPage(activePage[0])) {
        showPage('dashboard');
    }
}

function setAnalystAvatarElement(element, analyst) {
    applyAnalystAvatarToElement(element, analyst);
    if (element?.id === 'perfilFoto') {
        applyPerfilFotoWrapperState(Boolean(getAnalystPhotoUrl(analyst)));
    }
}

async function updateAnalystStatus(analystId, statusOperador) {
    return analistaService.updateStatus(analystId, statusOperador);
}

async function changeTicketStatus(ticketNumber, action) {
    try {
        let body = null;
        if (action === 'attend') {
            if (!getLoggedAnalyst()?.id) {
                showAlert(MSG_ERRO.SESSAO_EXPIRADA, alertBoxTickets);
                return;
            }
            body = { status: 'EM_ATENDIMENTO', analistaId: getLoggedAnalyst().id };
        }
        await ticketService.updateTicketStatus(ticketNumber, body);
        loadDashboard();
        if (pages.atendentes.classList.contains('active')) {
            loadAnalistasKanban();
        }
        if (pages.tickets.classList.contains('active')) {
            loadTicketTable();
        }
    } catch (error) {
        showAlert(error.message, alertBoxTickets);
    }
}

pageButtons.forEach(button => {
    if (
        !button.dataset.page ||
        button.dataset.indicadoresSub ||
        button.id === 'navIndicadoresToggle' ||
        button.dataset.clientesMode ||
        button.id === 'navClientesToggle' ||
        button.dataset.dashboardSub ||
        button.id === 'navDashboardToggle' ||
        button.dataset.relatoriosSub ||
        button.id === 'navRelatoriosToggle'
    ) {
        return;
    }
    button.addEventListener('click', () => showPage(button.dataset.page));
});

logoutBtn.addEventListener('click', logout);

refreshButton2?.addEventListener('click', loadTicketTable);

loginForm?.addEventListener('submit', async event => {
    event.preventDefault();
    const email = loginUser?.value?.trim() || '';
    const senha = loginPass?.value || '';
    if (!email || !senha) {
        showAlert(MSG_ERRO.CAMPOS_OBRIGATORIOS, loginAlert);
        return;
    }
    try {
        clearAlert(loginAlert);
        const analista = await loginAnalista(email, senha);
        setLoggedAnalyst(analista);
        try {
            const analistaAtualizado = await fetchAnalistaPorId(analista.id);
            setLoggedAnalyst(analistaAtualizado);
        } catch (refreshError) {
            console.warn('Nao foi possivel atualizar dados do analista apos login:', refreshError);
        }
        openApp();
    } catch (error) {
        clearLoggedAnalystSession();
        const msg =
            error instanceof Error && error.message
                ? error.message
                : MSG_ERRO.OPERACAO_FALHOU;
        showAlert(msg, loginAlert);
    }
});

operadorStatusSelect?.addEventListener('change', async event => {
    if (!getLoggedAnalyst()?.id) return;
    try {
        const analista = await updateAnalystStatus(getLoggedAnalyst().id, event.target.value);
        setLoggedAnalyst(analista);
        await loadAnalistasKanban();
    } catch (error) {
        showAlert(error.message, alertBoxTickets);
        updateLoggedAnalystUi();
    }
});

document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
        fecharModalEscalonamentoUi();
        closeModalDetail();
        closeCloseTicketModal();
        closeAnalystQueue();
        closePerfilOverlays();
        closeTopbarUserMenu();
    }
});

initThemeOnLoad();

initNotificacoesPanel({
    displayValue,
    formatDateTime,
    resolveTicketNumber,
    escapeAttr,
    openDetails
});

initAlertaTicket({
    showPage,
    loadTicketTable,
    closeTopbarUserMenu,
    fecharNotificacoesPainel,
    pages,
    loadDashboard,
    updateDashboardFromResumo,
    parseDashboardMetricNumber,
    getDashboardAlertMetricElements,
    resolveTicketNumber
});

async function updateOperadorStatusFromTopbar(statusOperador) {
    const session = getLoggedAnalyst();
    if (!session?.id) {
        showAlert('Sessão inválida.', alertBox);
        return statusOperador;
    }
    try {
        const updated = await updateAnalystStatus(session.id, statusOperador);
        setLoggedAnalyst({
            ...session,
            statusOperador: updated.statusOperador,
            online: updated.online
        });
        refreshTopbarStatusMenuFromSession();
        if (pages.dashboard?.classList.contains('active')) {
            await refreshDashboardStatusOperador();
        }
        showAlert('Status operador atualizado.', alertBox, 'success');
    } catch (error) {
        showAlert(error?.message || 'Não foi possível atualizar o status.', alertBox);
    }
    return statusOperador;
}

initTopbar({
    showPage,
    logout,
    loadTicketTable,
    toggleAlertaTicket,
    setAnalystAvatarElement,
    getLoggedAnalyst,
    updateOperadorStatus: updateOperadorStatusFromTopbar
});

initTicketDetailsModal({
    showAlert,
    clearAlert,
    displayValue,
    resolveTicketNumber,
    formatDateTime,
    formatPriority,
    formatSlaPrimeiroAtendimentoLabel,
    formatSlaBadgeHtml,
    formatSlaResolucaoBadge,
    formatSlaPausadoSimNao,
    formatSlaMinutosPausados,
    formatEscalonamentoBadgeHtml,
    getLoggedAnalyst,
    alertBoxTickets,
    pages,
    loadDashboard,
    loadTicketTable,
    loadAnalistasKanban,
    loadDashboardSla,
    refreshNotificacoesUi
});

initAuditoriaPage({
    showAlert,
    clearAlert,
    displayValue,
    formatDateTime,
    mensagemErroSessaoApi
});

initRelatoriosPage({
    showAlert,
    clearAlert,
    displayValue,
    formatDateTime,
    mensagemErroSessaoApi,
    sortTicketsByPriority,
    getTicketPriorityRowClass,
    getStatusClass,
    formatPriorityBadgeHtml,
    formatSlaBadgeHtml,
    formatSlaResolucaoCellHtml
});

initIndicadoresPage({
    showAlert,
    clearAlert,
    displayValue
});

initIndicadoresSidebarNav({
    showPageFn: showPage,
    canAccessIndicadoresFn: () => canAccessPage('indicadores')
});

initRelatoriosSidebarNav({ showPageFn: showPage });

initClientesPage({
    showAlert,
    clearAlert,
    onRefreshDashboard: () => loadDashboard(),
    openChatsFromHistoricoFn: async (protocolo, status) => {
        scheduleOpenChatsConversation(protocolo, status);
        showPage('chats');
        await loadChatsPage();
    }
});

initClientesSidebarNav({
    showPageFn: showPage
});

initConfiguracoesPage({
    showAlert,
    clearAlert,
    displayValue,
    formatPriority,
    formatPriorityBadgeHtml
});

initAtendentesPage({
    showAlert,
    clearAlert,
    displayValue,
    formatPriorityBadgeHtml,
    getStatusClass,
    getTicketPriorityRowClass,
    sortTicketsByPriority,
    renderAnalystAvatar,
    getAnalystDisplayName,
    renderStatusOperador,
    summarizeTicket,
    buildKanbanTicketMiniHtml,
    setAnalystAvatarElement,
    openDetails,
    alertBox
});

initTicketsPage({
    showAlert,
    displayValue,
    formatDateTime,
    sortTicketsByPriority,
    getTicketPriorityRowClass,
    getStatusClass,
    formatPriorityBadgeHtml,
    formatSlaBadgeHtml,
    formatSlaResolucaoCellHtml,
    openDetails,
    changeTicketStatus
});

initChatsPage({
    showAlert,
    displayValue,
    formatDateTime,
    formatPriorityBadgeHtml,
    getStatusClass,
    openDetails,
    openEncerramento: openEncerramentoTicketModal
});

initDashboardPage({
    showAlert,
    displayValue,
    formatDateTime,
    sortTicketsByPriority,
    getTicketPriorityRowClass,
    getStatusClass,
    formatPriorityBadgeHtml,
    formatSlaBadgeHtml,
    formatSlaResolucaoCellHtml,
    openDetails,
    escapeAttr,
    initTicketAlertBaseline,
    checkTicketAlertVisual,
    refreshNotificacoesUi
});

initDashboardAcompanhamentoPage({
    showAlert,
    displayValue,
    formatDateTime
});
initDashboardSidebarNav({ showPageFn: showPage });

initAbrirTicketPage({
    showAlert,
    clearAlert,
    onCadastrarContato: async clienteId => {
        showPage('clientes');
        await navegarParaContatosDoCliente(clienteId);
    },
    onTicketCreatedSuccess: async createdTicket => {
        loadDashboard();
        if (isAlertaTicketEnabled()) {
            if (createdTicket && getTicketAlertKey(createdTicket)) {
                registrarTicketsNovosNoAlerta([createdTicket]);
            } else {
                await verificarNovosTickets();
            }
        }
        if (pages.atendentes?.classList.contains('active')) {
            loadAnalistasKanban();
        }
    }
});

initPerfilPage({
    showAlert,
    clearAlert,
    displayValue,
    formatDate,
    setAnalystAvatarElement,
    getAnalystDisplayName,
    renderStatusOperador,
    loadAnalistasKanban
});

configureRouter({
    pages,
    pageButtons,
    loginScreen,
    appScreen,
    alertBox,
    showAlert,
    pageLoaders: {
        dashboard: () => loadDashboard(),
        'dashboard-acompanhamento': () => loadDashboardAcompanhamentoPage(),
        atendentes: () => loadAtendentesPage(),
        clientes: () => loadClientesPage(),
        'abrir-ticket': () => loadAbrirTicketPage(),
        perfil: () => loadPerfilPage(),
        tickets: () => {
            acknowledgeTicketAlert();
            loadTicketTable();
        },
        chats: () => loadChatsPage(),
        relatorios: () => loadRelatoriosPage(),
        'relatorios-evolucao': () => {},
        indicadores: () => loadIndicadoresPage(),
        configuracoes: () => { loadConfiguracoesPage(); loadPortalUsuariosAdmin(); },
        auditoria: () => loadAuditoriaPage()
    },
    onOpenApp: () => {
        initUserPreferences();
        initAlertasUsuarioOnOpenApp();
        updateLoggedAnalystUi();
        refreshNotificacoesContador();
    }
});

configureAuth({
    onAnalystChanged: updateLoggedAnalystUi,
    openApp,
    beforeLogout: async () => {
        stopTicketAlertPolling();
        hideTicketAlertToast();
    },
    setAnalystOffline: async () => {
        const session = getLoggedAnalyst();
        if (session?.id) {
            await updateAnalystStatus(session.id, 'OFFLINE').catch(() => null);
        }
    },
    afterLogoutUi: () => {
        appScreen.classList.remove('screen-active');
        loginScreen.classList.add('screen-active');
        loginForm?.reset();
    }
});

const { tryRestorePortalSession } = initClientePortalPage({ loginScreen });

window.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const page = params.get('page');
    const token = params.get('token');
    if (page === 'avaliacao' && token) {
        if (initAvaliacaoPublicaPage(token)) {
            return;
        }
    }
    if (tryRestorePortalSession && tryRestorePortalSession()) {
        return;
    }
    setLoggedAnalystState(null);
    loginScreen.classList.add('screen-active');
    appScreen.classList.remove('screen-active');
    clearAlert(loginAlert);
    await restoreSessionFromServer();
});
