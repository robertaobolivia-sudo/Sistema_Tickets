import { getLoggedAnalyst } from './state.js';

export const API_BASE = '/api';
export const HEADER_ANALISTA_ID = 'X-Analista-Id';
export const HEADER_ANALISTA_TOKEN = 'X-Analista-Token';

export async function apiFetch(url, options = {}) {
    const headers = new Headers(options.headers || {});
    const session = getLoggedAnalyst();
    if (session?.id != null) {
        headers.set(HEADER_ANALISTA_ID, String(session.id));
    }
    const sessionToken = session?.authToken;
    if (sessionToken) {
        headers.set(HEADER_ANALISTA_TOKEN, sessionToken);
    }
    if (options.body && !headers.has('Content-Type') && typeof options.body === 'string') {
        headers.set('Content-Type', 'application/json');
    }
    return fetch(url, { ...options, headers });
}
