import { fecharNotificacoesPainel } from './notificacoesPanel.js';

const topbarPerfilBtn = document.getElementById('topbarPerfilBtn');
const topbarPerfilAvatar = document.getElementById('topbarPerfilAvatar');
const topbarUserMenu = document.getElementById('topbarUserMenu');

export const PREF_THEME = 'suporteTicketsTheme';
export const PREF_THEME_ALT = 'suporteTicketsTema';
export const PREF_ALERTA_CHAT = 'suporteTicketsAlertaChat';

let showPageFn = () => {};
let logoutFn = () => {};
let loadTicketTableFn = () => {};
let toggleAlertaTicketFn = async () => {};
let setAnalystAvatarElementFn = () => {};
let listenersBound = false;

export function isPrefEnabled(key, defaultValue = false) {
    const stored = localStorage.getItem(key);
    if (stored === null) return defaultValue;
    return stored === 'true';
}

export function setPrefEnabled(key, enabled) {
    localStorage.setItem(key, enabled ? 'true' : 'false');
}

export function getStoredTheme() {
    const raw = localStorage.getItem(PREF_THEME) || localStorage.getItem(PREF_THEME_ALT);
    if (raw === 'dark' || raw === 'escuro') return 'dark';
    return 'light';
}

export function applyTheme(theme) {
    const value = theme === 'dark' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', value);
    localStorage.setItem(PREF_THEME, value);
    localStorage.setItem(PREF_THEME_ALT, value === 'dark' ? 'escuro' : 'claro');
    updateTopbarUserMenuLabels();
}

export function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    applyTheme(current === 'dark' ? 'light' : 'dark');
}

export function initUserPreferences() {
    applyTheme(getStoredTheme());
    updateTopbarUserMenuLabels();
}

export function initThemeOnLoad() {
    const theme = getStoredTheme();
    document.documentElement.setAttribute('data-theme', theme);
}

export function updateTopbarAvatar(analyst) {
    setAnalystAvatarElementFn(topbarPerfilAvatar, analyst);
}

export function updateTopbarMenuToggleStyles(extra = {}) {
    if (!topbarUserMenu) return;
    const chatBtn = topbarUserMenu.querySelector('[data-user-menu="alerta-chat"]');
    const ticketBtn = topbarUserMenu.querySelector('[data-user-menu="alerta-ticket"]');
    chatBtn?.classList.toggle('pref-toggle-on', isPrefEnabled(PREF_ALERTA_CHAT, false));
    const ticketOn = extra.isAlertaTicketEnabled?.() ?? false;
    const ticketSyncing = extra.isAlertaTicketSyncing?.() ?? false;
    ticketBtn?.classList.toggle('pref-toggle-on', ticketOn && !ticketSyncing);
    ticketBtn?.classList.toggle('pref-toggle-sync', ticketSyncing);
    if (ticketBtn) {
        ticketBtn.disabled = ticketSyncing;
    }
}

export function updateTopbarUserMenuLabels(extra = {}) {
    if (!topbarUserMenu) return;
    const themeBtn = topbarUserMenu.querySelector('[data-user-menu="tema"]');
    const chatBtn = topbarUserMenu.querySelector('[data-user-menu="alerta-chat"]');
    const ticketBtn = topbarUserMenu.querySelector('[data-user-menu="alerta-ticket"]');
    const theme = document.documentElement.getAttribute('data-theme') || 'light';
    if (themeBtn) {
        themeBtn.textContent = theme === 'dark' ? 'Tema Claro' : 'Tema Escuro';
    }
    if (chatBtn) {
        chatBtn.textContent = `Alerta Chat: ${isPrefEnabled(PREF_ALERTA_CHAT, false) ? 'ON' : 'OFF'}`;
    }
    if (ticketBtn) {
        const syncing = extra.isAlertaTicketSyncing?.() ?? false;
        const ticketOn = extra.isAlertaTicketEnabled?.() ?? false;
        if (syncing) {
            ticketBtn.textContent = 'Alerta Ticket: Sincronizando...';
        } else {
            ticketBtn.textContent = `Alerta Ticket: ${ticketOn ? 'ON' : 'OFF'}`;
        }
    }
    updateTopbarMenuToggleStyles(extra);
}

export function toggleTopbarUserMenu(forceOpen = null) {
    if (!topbarUserMenu) return;
    const shouldOpen = forceOpen === null
        ? topbarUserMenu.classList.contains('hidden')
        : forceOpen;
    topbarUserMenu.classList.toggle('hidden', !shouldOpen);
    topbarPerfilBtn?.setAttribute('aria-expanded', shouldOpen ? 'true' : 'false');
    if (shouldOpen) {
        updateTopbarUserMenuLabels();
        fecharNotificacoesPainel();
    }
}

export function closeTopbarUserMenu() {
    toggleTopbarUserMenu(false);
}

function handleTopbarUserMenuAction(action) {
    closeTopbarUserMenu();
    if (action === 'perfil') {
        showPageFn('perfil');
        return;
    }
    if (action === 'tema') {
        toggleTheme();
        return;
    }
    if (action === 'alerta-chat') {
        setPrefEnabled(PREF_ALERTA_CHAT, !isPrefEnabled(PREF_ALERTA_CHAT, false));
        updateTopbarUserMenuLabels();
        return;
    }
    if (action === 'alerta-ticket') {
        void toggleAlertaTicketFn();
        return;
    }
    if (action === 'logout') {
        logoutFn();
    }
}

function bindListeners() {
    if (listenersBound) return;
    listenersBound = true;

    document.addEventListener('click', event => {
        if (topbarUserMenu && !topbarUserMenu.classList.contains('hidden')) {
            if (!event.target.closest('.topbar-perfil-wrap')) {
                closeTopbarUserMenu();
            }
        }
    });
    topbarPerfilBtn?.addEventListener('click', event => {
        event.stopPropagation();
        toggleTopbarUserMenu();
    });
    topbarUserMenu?.addEventListener('click', event => {
        const btn = event.target.closest('[data-user-menu]');
        if (!btn) return;
        handleTopbarUserMenuAction(btn.dataset.userMenu);
    });
}

export function initTopbar(deps) {
    showPageFn = deps.showPage || showPageFn;
    logoutFn = deps.logout || logoutFn;
    loadTicketTableFn = deps.loadTicketTable || loadTicketTableFn;
    toggleAlertaTicketFn = deps.toggleAlertaTicket || toggleAlertaTicketFn;
    setAnalystAvatarElementFn = deps.setAnalystAvatarElement || setAnalystAvatarElementFn;
    bindListeners();
}
