import { API_BASE } from '@shared/api/api-client.js';
import { getLoggedAnalyst } from '@shared/auth/state.js';

const PORTAL_BASE = `${API_BASE}/cliente-portal`;
const ADMIN_BASE = `${API_BASE}/admin/cliente-portal/usuarios`;

export const STORAGE_KEY_PORTAL = 'clientePortalSession';

export function getPortalSession() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY_PORTAL);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function savePortalSession(session) {
    localStorage.setItem(STORAGE_KEY_PORTAL, JSON.stringify(session));
}

export function clearPortalSession() {
    localStorage.removeItem(STORAGE_KEY_PORTAL);
}

function portalHeaders() {
    const s = getPortalSession();
    if (!s?.id || !s?.authToken) {
        throw new Error('Sessão do portal inválida. Faça login novamente.');
    }
    return { 'X-Portal-Id': String(s.id), 'X-Portal-Token': s.authToken };
}

async function portalFetch(url, options = {}, errorMsg = 'Erro ao comunicar com o servidor.') {
    const res = await fetch(url, {
        ...options,
        headers: { ...portalHeaders(), ...(options.headers || {}) }
    });
    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new Error(data?.message || data?.erro || errorMsg);
    }
    return res.status === 204 ? null : res.json();
}

export async function loginPortal(email, senha) {
    const res = await fetch(`${PORTAL_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
    });
    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new Error(data?.message || data?.erro || 'E-mail ou senha inválidos.');
    }
    return res.json();
}

export function getDashboardPortal() {
    return portalFetch(`${PORTAL_BASE}/dashboard`, {}, 'Não foi possível carregar o dashboard do portal.');
}

export function getTicketsPortal() {
    return portalFetch(`${PORTAL_BASE}/tickets`, {}, 'Não foi possível carregar os tickets.');
}

// ---- Admin (usa sessão de analista) ----

function adminHeaders() {
    const s = getLoggedAnalyst();
    if (!s?.id || !s?.authToken) return {};
    return {
        'X-Analista-Id': String(s.id),
        'X-Analista-Token': s.authToken
    };
}

async function adminFetch(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: { ...adminHeaders(), ...(options.headers || {}) }
    });
    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new Error(data?.message || data?.erro || 'Erro na operação do portal.');
    }
    return res.status === 204 ? null : res.json();
}

export function listarUsuariosPortal(clienteId) {
    const url = clienteId ? `${ADMIN_BASE}?clienteId=${clienteId}` : ADMIN_BASE;
    return adminFetch(url);
}

export function criarUsuarioPortal(payload) {
    return adminFetch(ADMIN_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
}

export function atualizarUsuarioPortal(id, payload) {
    return adminFetch(`${ADMIN_BASE}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
}
