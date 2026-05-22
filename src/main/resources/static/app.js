import { MSG_ERRO, mensagemErroSessaoApi } from './js/core/messages.js';
import { getLoggedAnalyst, setLoggedAnalystState } from './js/core/state.js';
import {
    configureAuth,
    setLoggedAnalyst,
    loginAnalista,
    fetchAnalistaPorId,
    logout,
    restoreSessionFromServer
} from './js/core/auth.js';
import { canAccessPage } from './js/core/permissions.js';
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
} from './js/core/presentation.js';
import { configureRouter, showPage, openApp, applyMenuPermissions } from './js/core/router.js';
import * as ticketService from './js/services/ticketService.js';
import { initAuditoriaPage, loadAuditoriaPage } from './js/pages/auditoriaPage.js';
import { initRelatoriosPage, loadRelatoriosPage } from './js/pages/relatoriosPage.js';
import {
    initIndicadoresPage,
    initIndicadoresSidebarNav,
    loadIndicadoresPage
} from './js/pages/indicadoresPage.js';
import { initClientesPage, loadClientesPage } from './js/pages/clientesPage.js';
import { initConfiguracoesPage, loadConfiguracoesPage } from './js/pages/configuracoesPage.js';
import {
    initAtendentesPage,
    loadAtendentesPage,
    loadAnalistasKanban,
    loadAdminPerfisGestao,
    updateAdminPerfisSectionVisibility,
    closeAnalystQueue
} from './js/pages/atendentesPage.js';
import { initTicketsPage, loadTicketTable } from './js/pages/ticketsPage.js';
import { initChatsPage, loadChatsPage } from './js/pages/chatsPage.js';
import {
    initDashboardPage,
    loadDashboard,
    loadDashboardSla,
    updateDashboardFromResumo,
    parseDashboardMetricNumber,
    getDashboardAlertMetricElements
} from './js/pages/dashboardPage.js';
import { initAbrirTicketPage, loadAbrirTicketPage } from './js/pages/abrirTicketPage.js';
import {
    initPerfilPage,
    loadPerfilPage,
    renderPerfil,
    applyPerfilFotoWrapperState,
    closePerfilOverlays
} from './js/pages/perfilPage.js';
import {
    initTicketDetailsModal,
    openDetails,
    openEncerramentoTicketModal,
    closeModalDetail,
    closeCloseTicketModal,
    fecharModalEscalonamentoUi
} from './js/components/ticketDetailsModal.js';
import {
    initNotificacoesPanel,
    refreshNotificacoesUi,
    refreshNotificacoesContador,
    fecharNotificacoesPainel
} from './js/components/notificacoesPanel.js';
import {
    initTopbar,
    initUserPreferences,
    initThemeOnLoad,
    closeTopbarUserMenu,
    updateTopbarAvatar,
} from './js/components/topbar.js';
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
} from './js/components/alertaTicket.js';
import * as analistaService from './js/services/analistaService.js';
import { initAvaliacaoPublicaPage } from './js/pages/avaliacaoPublicaPage.js';

const pages = {
    dashboard: document.getElementById('page-dashboard'),
    clientes: document.getElementById('page-clientes'),
    atendentes: document.getElementById('page-atendentes'),
    perfil: document.getElementById('page-perfil'),
    'abrir-ticket': document.getElementById('page-abrir-ticket'),
    tickets: document.getElementById('page-tickets'),
    chats: document.getElementById('page-chats'),
    relatorios: document.getElementById('page-relatorios'),
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
    if (!element) return;
    const isProfilePhoto = element.id === 'perfilFoto';
    const isTopbarAvatar = element.id === 'topbarPerfilAvatar';
    const hasPhoto = Boolean(analyst?.fotoUrl);
    element.className = isProfilePhoto
        ? 'analyst-avatar analyst-avatar-profile'
        : isTopbarAvatar
            ? 'analyst-avatar analyst-avatar-topbar'
            : 'analyst-avatar analyst-avatar-large';
    element.classList.toggle('has-photo', hasPhoto);
    if (isProfilePhoto) {
        applyPerfilFotoWrapperState(hasPhoto);
    }
    if (hasPhoto) {
        element.innerHTML = `<img src="${analyst.fotoUrl}" alt="${analyst.nome || 'Analista'}" />`;
    } else {
        element.textContent = getAnalystInitials(analyst?.nome);
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
    if (button.dataset.indicadoresSub || button.id === 'navIndicadoresToggle') {
        return;
    }
    button.addEventListener('click', () => showPage(button.dataset.page));
});

logoutBtn.addEventListener('click', logout);

refreshButton2?.addEventListener('click', loadTicketTable);

loginForm?.addEventListener('submit', async event => {
    event.preventDefault();
    try {
        clearAlert(loginAlert);
        const analista = await loginAnalista(loginUser.value.trim(), loginPass.value);
        setLoggedAnalyst(analista);
        try {
            const analistaAtualizado = await fetchAnalistaPorId(analista.id);
            setLoggedAnalyst(analistaAtualizado);
        } catch (refreshError) {
            console.warn('Nao foi possivel atualizar dados do analista apos login:', refreshError);
        }
        openApp();
    } catch (error) {
        setLoggedAnalystState(null);
        showAlert(error.message, loginAlert);
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

initTopbar({
    showPage,
    logout,
    loadTicketTable,
    toggleAlertaTicket,
    setAnalystAvatarElement
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

initClientesPage({
    showAlert,
    clearAlert,
    onRefreshDashboard: () => loadDashboard()
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

initAbrirTicketPage({
    showAlert,
    clearAlert,
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
        indicadores: () => loadIndicadoresPage(),
        configuracoes: () => loadConfiguracoesPage(),
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

window.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const page = params.get('page');
    const token = params.get('token');
    if (page === 'avaliacao' && token) {
        if (initAvaliacaoPublicaPage(token)) {
            return;
        }
    }
    setLoggedAnalystState(null);
    loginScreen.classList.add('screen-active');
    appScreen.classList.remove('screen-active');
    clearAlert(loginAlert);
    await restoreSessionFromServer();
});
