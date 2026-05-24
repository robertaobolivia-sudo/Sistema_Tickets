import { MSG_ERRO } from '@shared/ui/messages.js';
import { canAccessPage, applyNavPermissions, applyConfiguracoesPermissions } from '@shared/permissions/permissions.js';

/** @type {{ pages?: Record<string, HTMLElement|null>, pageButtons?: NodeListOf<Element>|Element[], loginScreen?: HTMLElement|null, appScreen?: HTMLElement|null, alertBox?: HTMLElement|null, showAlert?: (msg: string, el: HTMLElement, type?: string) => void, pageLoaders?: Record<string, () => void>, onOpenApp?: () => void, afterShowPageBlocked?: () => void }} */
let routerConfig = {};

export function configureRouter(config = {}) {
    routerConfig = { ...routerConfig, ...config };
}

export function applyMenuPermissions() {
    applyNavPermissions(routerConfig.pageButtons);
    applyConfiguracoesPermissions();
}

/** Volta o scroll da área de conteúdo ao topo ao trocar de página (evita “página abaixo”). */
export function resetMainContentScroll() {
    if (typeof document === 'undefined') {
        return;
    }
    const main = document.querySelector('.main-content');
    if (main) {
        main.scrollTop = 0;
    }
    document.querySelectorAll('.main-content > .page').forEach(el => {
        if (typeof el.scrollTop === 'number') {
            el.scrollTop = 0;
        }
    });
    if (typeof window !== 'undefined' && typeof window.scrollTo === 'function') {
        window.scrollTo(0, 0);
    }
}

export function showPage(pageKey) {
    const pages = routerConfig.pages;
    if (!pages?.[pageKey]) {
        return;
    }
    const showAlert = routerConfig.showAlert;
    const alertBox = routerConfig.alertBox;
    if (!canAccessPage(pageKey)) {
        if (showAlert && alertBox) {
            showAlert(MSG_ERRO.SEM_PERMISSAO_PAGINA, alertBox);
        }
        if (pageKey !== 'dashboard' && canAccessPage('dashboard')) {
            showPage('dashboard');
        }
        return;
    }
    Object.values(pages).forEach(page => page?.classList.remove('active'));
    pages[pageKey]?.classList.add('active');
    resetMainContentScroll();

    updateSidebarActiveState(pageKey);
    openSubmenuIfNeeded(pageKey);

    const loader = routerConfig.pageLoaders?.[pageKey];
    if (typeof loader === 'function') {
        try {
            const result = loader();
            if (result && typeof result.then === 'function') {
                result.catch(err => {
                    const msg =
                        err && typeof err.message === 'string'
                            ? err.message
                            : 'Não foi possível carregar esta página.';
                    if (showAlert && alertBox) {
                        showAlert(msg, alertBox);
                    }
                });
            }
        } catch (err) {
            const msg =
                err && typeof err.message === 'string'
                    ? err.message
                    : 'Não foi possível carregar esta página.';
            if (showAlert && alertBox) {
                showAlert(msg, alertBox);
            }
        }
    }
}

const SIDEBAR_GROUP_MAP = {
    'dashboard':                { toggleId: 'navDashboardToggle',  subAttr: 'data-dashboard-sub',  subValue: 'visao-geral' },
    'dashboard-acompanhamento': { toggleId: 'navDashboardToggle',  subAttr: 'data-dashboard-sub',  subValue: 'acompanhamento' },
    'relatorios':               { toggleId: 'navRelatoriosToggle', subAttr: 'data-relatorios-sub', subValue: 'detalhado' },
    'relatorios-evolucao':      { toggleId: 'navRelatoriosToggle', subAttr: 'data-relatorios-sub', subValue: 'evolucao' },
    'indicadores':              { toggleId: 'navIndicadoresToggle', subAttr: null, subValue: null },
    'clientes':                 { toggleId: 'navClientesToggle',   subAttr: null, subValue: null },
};

const SUBMENU_PREFIX_MAP = {
    'clientes':                 'navClientes',
    'indicadores':              'navIndicadores',
    'relatorios':               'navRelatorios',
    'relatorios-evolucao':      'navRelatorios',
    'dashboard':                'navDashboard',
    'dashboard-acompanhamento': 'navDashboard',
};

function updateSidebarActiveState(pageKey) {
    const pageButtons = routerConfig.pageButtons;
    const sidebarItems =
        typeof document !== 'undefined'
            ? document.querySelectorAll('.sidebar-nav .nav-item')
            : pageButtons;
    sidebarItems?.forEach?.(btn => {
        btn.classList.remove('active', 'is-active-page', 'is-active-parent');
    });
    if (!pageButtons?.forEach) return;
    // Toggles de grupo não têm data-page — limpar explicitamente
    const groupInfo = SIDEBAR_GROUP_MAP[pageKey];
    if (groupInfo) {
        const toggle = document.getElementById(groupInfo.toggleId);
        toggle?.classList.add('active', 'is-active-parent');
        if (groupInfo.subAttr && groupInfo.subValue) {
            document.querySelectorAll(`[${groupInfo.subAttr}="${groupInfo.subValue}"]`).forEach(btn => {
                btn.classList.add('is-active-page');
            });
        }
    } else {
        pageButtons.forEach(btn => {
            if (
                btn.dataset.page === pageKey &&
                !btn.dataset.indicadoresSub &&
                !btn.dataset.clientesMode &&
                !btn.dataset.dashboardSub &&
                !btn.dataset.relatoriosSub
            ) {
                btn.classList.add('is-active-page');
            }
        });
    }
}

function openSubmenuIfNeeded(pageKey) {
    const prefix = SUBMENU_PREFIX_MAP[pageKey];
    if (!prefix) {
        // Página flat — fecha todos os grupos abertos
        [...new Set(Object.values(SUBMENU_PREFIX_MAP))].forEach(p => {
            document.getElementById(`${p}Group`)?.classList.remove('is-open');
            document.getElementById(`${p}Submenu`)?.classList.add('hidden');
            document.getElementById(`${p}Toggle`)?.setAttribute('aria-expanded', 'false');
        });
        return;
    }
    document.getElementById(`${prefix}Group`)?.classList.add('is-open');
    document.getElementById(`${prefix}Submenu`)?.classList.remove('hidden');
    document.getElementById(`${prefix}Toggle`)?.setAttribute('aria-expanded', 'true');
}

export function openApp() {
    routerConfig.loginScreen?.classList.remove('screen-active');
    routerConfig.appScreen?.classList.add('screen-active');
    if (routerConfig.onOpenApp) {
        routerConfig.onOpenApp();
    }
    showPage('dashboard');
}

export function redirectIfCurrentPageBlocked() {
    const pages = routerConfig.pages;
    if (!pages) {
        return;
    }
    const activePage = Object.entries(pages).find(([, page]) => page?.classList.contains('active'));
    if (activePage && !canAccessPage(activePage[0])) {
        showPage('dashboard');
    }
}
