import { afterEach, describe, expect, it, vi } from 'vitest';

vi.mock('@shared/permissions/permissions.js', () => ({
    canAccessPage: () => true,
    applyNavPermissions: vi.fn(),
    applyConfiguracoesPermissions: vi.fn()
}));

import { configureRouter, showPage } from '@shared/router/router.js';

function createClassList(initial = []) {
    const classes = new Set(initial);
    return {
        add: (...names) => names.forEach(name => classes.add(name)),
        remove: (...names) => names.forEach(name => classes.delete(name)),
        contains: name => classes.has(name),
        toggle: (name, force) => {
            if (force === undefined) {
                if (classes.has(name)) {
                    classes.delete(name);
                    return false;
                }
                classes.add(name);
                return true;
            }
            if (force) classes.add(name);
            else classes.delete(name);
            return Boolean(force);
        }
    };
}

function createElement({ id = '', dataset = {}, attrs = {}, classes = [] } = {}) {
    return {
        id,
        dataset,
        classList: createClassList(classes),
        scrollTop: 10,
        attrs: { ...attrs },
        getAttribute(name) {
            return this.attrs[name] ?? null;
        },
        setAttribute(name, value) {
            this.attrs[name] = String(value);
        }
    };
}

function installDocument({ elementsById, navItems }) {
    globalThis.document = {
        getElementById: id => elementsById[id] ?? null,
        querySelector: () => null,
        querySelectorAll: selector => {
            if (selector === '.main-content > .page') {
                return [];
            }
            if (selector === '.sidebar-nav .nav-item') {
                return navItems;
            }
            if (selector === '[data-dashboard-sub="visao-geral"]') {
                return navItems.filter(item => item.dataset.dashboardSub === 'visao-geral');
            }
            if (selector === '[data-dashboard-sub="acompanhamento"]') {
                return navItems.filter(item => item.dataset.dashboardSub === 'acompanhamento');
            }
            if (selector === '[data-relatorios-sub="detalhado"]') {
                return navItems.filter(item => item.dataset.relatoriosSub === 'detalhado');
            }
            if (selector === '[data-relatorios-sub="evolucao"]') {
                return navItems.filter(item => item.dataset.relatoriosSub === 'evolucao');
            }
            return [];
        }
    };
}

describe('sidebar active state', () => {
    afterEach(() => {
        delete globalThis.document;
        delete globalThis.window;
        configureRouter({});
    });

    it('remove estado ativo legado do Dashboard ao abrir pagina direta', () => {
        const dashboardPage = createElement({ classes: ['page'] });
        const ticketsPage = createElement({ classes: ['page'] });
        const dashboardToggle = createElement({
            id: 'navDashboardToggle',
            classes: ['nav-item', 'nav-item-parent', 'active']
        });
        const dashboardGroup = createElement({ id: 'navDashboardGroup', classes: ['nav-group', 'is-open'] });
        const dashboardSubmenu = createElement({ id: 'navDashboardSubmenu' });
        const dashboardSub = createElement({
            dataset: { page: 'dashboard', dashboardSub: 'visao-geral' },
            attrs: { 'data-dashboard-sub': 'visao-geral' },
            classes: ['nav-item', 'nav-item-sub', 'active']
        });
        const ticketsButton = createElement({
            dataset: { page: 'tickets' },
            classes: ['nav-item']
        });

        installDocument({
            elementsById: {
                navDashboardToggle: dashboardToggle,
                navDashboardGroup: dashboardGroup,
                navDashboardSubmenu: dashboardSubmenu
            },
            navItems: [dashboardToggle, dashboardSub, ticketsButton]
        });
        globalThis.window = { scrollTo: vi.fn() };

        configureRouter({
            pages: { dashboard: dashboardPage, tickets: ticketsPage },
            pageButtons: [dashboardToggle, dashboardSub, ticketsButton],
            pageLoaders: {}
        });

        showPage('tickets');

        expect(dashboardToggle.classList.contains('active')).toBe(false);
        expect(dashboardToggle.classList.contains('is-active-parent')).toBe(false);
        expect(dashboardSub.classList.contains('active')).toBe(false);
        expect(dashboardSub.classList.contains('is-active-page')).toBe(false);
        expect(ticketsButton.classList.contains('is-active-page')).toBe(true);
    });

    it('mantem active no item pai quando a rota pertence ao submenu', () => {
        const relatoriosPage = createElement({ classes: ['page'] });
        const relatoriosToggle = createElement({
            id: 'navRelatoriosToggle',
            classes: ['nav-item', 'nav-item-parent']
        });
        const relatoriosGroup = createElement({ id: 'navRelatoriosGroup', classes: ['nav-group'] });
        const relatoriosSubmenu = createElement({ id: 'navRelatoriosSubmenu', classes: ['hidden'] });
        const detalhadoSub = createElement({
            dataset: { page: 'relatorios', relatoriosSub: 'detalhado' },
            attrs: { 'data-relatorios-sub': 'detalhado' },
            classes: ['nav-item', 'nav-item-sub']
        });

        installDocument({
            elementsById: {
                navRelatoriosToggle: relatoriosToggle,
                navRelatoriosGroup: relatoriosGroup,
                navRelatoriosSubmenu: relatoriosSubmenu
            },
            navItems: [relatoriosToggle, detalhadoSub]
        });
        globalThis.window = { scrollTo: vi.fn() };

        configureRouter({
            pages: { relatorios: relatoriosPage },
            pageButtons: [relatoriosToggle, detalhadoSub],
            pageLoaders: {}
        });

        showPage('relatorios');

        expect(relatoriosToggle.classList.contains('active')).toBe(true);
        expect(relatoriosToggle.classList.contains('is-active-parent')).toBe(true);
        expect(detalhadoSub.classList.contains('is-active-page')).toBe(true);
    });
});
