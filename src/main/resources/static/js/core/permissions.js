import { getLoggedAnalyst } from './state.js';

export const PAGE_ACCESS_BY_PERFIL = {
    dashboard: ['ADMIN', 'SUPERVISOR', 'ANALISTA'],
    clientes: ['ADMIN'],
    atendentes: ['ADMIN', 'SUPERVISOR'],
    perfil: ['ADMIN', 'SUPERVISOR', 'ANALISTA'],
    'abrir-ticket': ['ADMIN', 'ANALISTA'],
    tickets: ['ADMIN', 'SUPERVISOR', 'ANALISTA'],
    chats: ['ADMIN', 'SUPERVISOR', 'ANALISTA'],
    relatorios: ['ADMIN', 'SUPERVISOR'],
    indicadores: ['ADMIN', 'SUPERVISOR'],
    configuracoes: ['ADMIN', 'SUPERVISOR'],
    auditoria: ['ADMIN']
};

export function resolvePerfilAcessoCode(analyst) {
    const raw = analyst?.perfilAcesso;
    if (raw === 'ADMIN' || raw === 'SUPERVISOR' || raw === 'ANALISTA') {
        return raw;
    }
    return 'ANALISTA';
}

export function formatPerfilAcessoLabel(code) {
    const map = {
        ADMIN: 'Administrador',
        SUPERVISOR: 'Supervisor',
        ANALISTA: 'Analista'
    };
    return map[code] || 'Analista';
}

export function renderPerfilAcessoBadge(code) {
    const resolved = code === 'ADMIN' || code === 'SUPERVISOR' || code === 'ANALISTA' ? code : 'ANALISTA';
    const modifier = resolved.toLowerCase();
    return `<span class="perfil-acesso-badge perfil-acesso-${modifier}">${formatPerfilAcessoLabel(resolved)}</span>`;
}

export function getLoggedPerfilCode() {
    return resolvePerfilAcessoCode(getLoggedAnalyst());
}

export function isAdminPerfil() {
    return getLoggedPerfilCode() === 'ADMIN';
}

export function canManageConfiguracoes() {
    return isAdminPerfil();
}

/** Gestão de etiquetas (Configurações): ADMIN e SUPERVISOR. */
export function canManageEtiquetas() {
    const code = getLoggedPerfilCode();
    return code === 'ADMIN' || code === 'SUPERVISOR';
}

export function canAccessPage(pageKey) {
    const allowed = PAGE_ACCESS_BY_PERFIL[pageKey];
    if (!allowed) {
        return true;
    }
    return allowed.includes(getLoggedPerfilCode());
}

export function canAccessAuditoria() {
    return isAdminPerfil();
}

export function applyNavPermissions(pageButtons) {
    const indicadoresGroup = document.getElementById('navIndicadoresGroup');
    const indicadoresAllowed = canAccessPage('indicadores');
    if (indicadoresGroup) {
        indicadoresGroup.classList.toggle('hidden', !indicadoresAllowed);
    }

    if (!pageButtons?.forEach) {
        return;
    }
    pageButtons.forEach(button => {
        const pageKey = button.dataset.page;
        if (!pageKey) {
            return;
        }
        if (button.dataset.indicadoresSub) {
            const allowed = canAccessPage('indicadores');
            button.classList.toggle('hidden', !allowed);
            button.disabled = !allowed;
            return;
        }
        if (button.id === 'navIndicadoresToggle') {
            button.classList.toggle('hidden', !indicadoresAllowed);
            button.disabled = !indicadoresAllowed;
            return;
        }
        const allowed = canAccessPage(pageKey);
        button.classList.toggle('hidden', !allowed);
        button.disabled = !allowed;
    });
}

export function applyConfiguracoesAdminSectionsVisibility() {
    const admin = canManageConfiguracoes();
    document.querySelectorAll('#page-configuracoes .config-admin-only').forEach(el => {
        el.classList.toggle('hidden', !admin);
    });
}

export function applyConfiguracoesPermissions() {
    applyConfiguracoesAdminSectionsVisibility();
    const admin = canManageConfiguracoes();
    const ids = [
        'horarioUtilSalvarBtn',
        'horarioUtilRecarregarBtn',
        'feriadoSalvarBtn',
        'feriadoLimparBtn',
        'feriadoSeedBtn',
        'slaMetasSeedBtn'
    ];
    ids.forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.disabled = !admin;
            el.classList.toggle('hidden', !admin);
        }
    });
    const configForm = document.getElementById('page-configuracoes');
    if (configForm) {
        configForm.querySelectorAll('input, select, textarea, button').forEach(control => {
            if (control.id === 'horarioUtilRecarregarBtn' || control.closest('#alertBoxConfiguracoes')) {
                return;
            }
            if (control.tagName === 'BUTTON' && control.classList.contains('sla-meta-salvar')) {
                control.disabled = !admin;
                return;
            }
            if (!admin && control.closest('#horarioUtilForm, #feriadoForm, #slaMetasTableBody')) {
                control.disabled = true;
            }
        });
    }
}
