import { MSG_ERRO } from './messages.js';
import { canAccessPage, applyNavPermissions, applyConfiguracoesPermissions } from './permissions.js';

/** @type {{ pages?: Record<string, HTMLElement|null>, pageButtons?: NodeListOf<Element>|Element[], loginScreen?: HTMLElement|null, appScreen?: HTMLElement|null, alertBox?: HTMLElement|null, showAlert?: (msg: string, el: HTMLElement, type?: string) => void, pageLoaders?: Record<string, () => void>, onOpenApp?: () => void, afterShowPageBlocked?: () => void }} */
let routerConfig = {};

export function configureRouter(config = {}) {
    routerConfig = { ...routerConfig, ...config };
}

export function applyMenuPermissions() {
    applyNavPermissions(routerConfig.pageButtons);
    applyConfiguracoesPermissions();
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

    const pageButtons = routerConfig.pageButtons;
    if (pageButtons?.forEach) {
        pageButtons.forEach(button => {
            if (button.dataset.indicadoresSub) {
                return;
            }
            const isIndicadoresParent =
                pageKey === 'indicadores' && button.id === 'navIndicadoresToggle';
            button.classList.toggle(
                'active',
                button.dataset.page === pageKey || isIndicadoresParent
            );
        });
    }
    if (pageKey === 'indicadores') {
        const group = document.getElementById('navIndicadoresGroup');
        group?.classList.add('is-open');
        const submenu = document.getElementById('navIndicadoresSubmenu');
        submenu?.classList.remove('hidden');
        const toggle = document.getElementById('navIndicadoresToggle');
        toggle?.setAttribute('aria-expanded', 'true');
    }

    const loader = routerConfig.pageLoaders?.[pageKey];
    if (typeof loader === 'function') {
        loader();
    }
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
