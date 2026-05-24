import { API_BASE, apiFetch } from '@shared/api/api-client.js';
import { mensagemErroLogin, mensagemErroSessaoApi } from '@shared/ui/messages.js';
import {
    STORAGE_KEY_ANALISTA,
    getLoggedAnalyst,
    setLoggedAnalystState,
    clearLoggedAnalystSession,
    persistLoggedAnalystToStorage,
    mergeAnalystSession
} from '@shared/auth/state.js';

/** @type {{ onAnalystChanged?: () => void, openApp?: () => void | Promise<void>, beforeLogout?: () => void | Promise<void>, afterLogoutUi?: () => void, setAnalystOffline?: () => void | Promise<void> }} */
let authHooks = {};

export function configureAuth(hooks = {}) {
    authHooks = { ...authHooks, ...hooks };
}

export function setLoggedAnalyst(analyst) {
    setLoggedAnalystState(mergeAnalystSession(analyst));
    persistLoggedAnalystToStorage();
    authHooks.onAnalystChanged?.();
}

export async function loginAnalista(email, senha) {
    const response = await fetch(`${API_BASE}/analistas/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    });
    if (!response.ok) {
        const data = await response.json().catch(() => null);
        throw new Error(mensagemErroLogin(response, data));
    }
    return await response.json();
}

export async function fetchAnalistaPorId(analystId) {
    const response = await apiFetch(`${API_BASE}/analistas/${analystId}`);
    if (!response.ok) {
        const data = await response.json().catch(() => null);
        throw new Error(mensagemErroSessaoApi(response, data, 'Falha ao carregar dados do analista'));
    }
    return await response.json();
}

export async function logoutAnalistaSessao() {
    const session = getLoggedAnalyst();
    if (!session?.id || !session?.authToken) {
        return;
    }
    await apiFetch(`${API_BASE}/analistas/logout`, { method: 'POST' }).catch(() => null);
}

export async function logout() {
    if (authHooks.beforeLogout) {
        await authHooks.beforeLogout();
    }
    await logoutAnalistaSessao();
    if (authHooks.setAnalystOffline) {
        await authHooks.setAnalystOffline();
    }
    clearLoggedAnalystSession();
    authHooks.afterLogoutUi?.();
}

export async function refreshLoggedAnalystFromServer() {
    const session = getLoggedAnalyst();
    if (!session?.id || !session?.authToken) {
        return null;
    }
    const analista = await fetchAnalistaPorId(session.id);
    setLoggedAnalyst(mergeAnalystSession(analista));
    return getLoggedAnalyst();
}

export async function restoreSessionFromServer() {
    const raw = localStorage.getItem(STORAGE_KEY_ANALISTA);
    if (!raw) {
        return false;
    }
    let cached = null;
    try {
        cached = JSON.parse(raw);
    } catch (error) {
        clearLoggedAnalystSession();
        return false;
    }
    if (!cached?.id || !cached?.authToken) {
        clearLoggedAnalystSession();
        return false;
    }
    setLoggedAnalyst(cached);
    try {
        const analista = await fetchAnalistaPorId(cached.id);
        setLoggedAnalyst(mergeAnalystSession(analista));
        if (authHooks.openApp) {
            await authHooks.openApp();
        }
        return true;
    } catch (error) {
        clearLoggedAnalystSession();
        return false;
    }
}
