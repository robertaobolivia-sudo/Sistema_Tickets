import { resolveMergedAnalystPhotoUrl } from '@shared/ui/analyst-avatar.js';

export const STORAGE_KEY_ANALISTA = 'suporteTicketsAnalista';

let loggedAnalyst = null;

export function getLoggedAnalyst() {
    return loggedAnalyst;
}

export function setLoggedAnalystState(analyst) {
    loggedAnalyst = analyst;
}

export function clearLoggedAnalystSession() {
    loggedAnalyst = null;
    localStorage.removeItem(STORAGE_KEY_ANALISTA);
}

export function persistLoggedAnalystToStorage() {
    if (loggedAnalyst) {
        localStorage.setItem(STORAGE_KEY_ANALISTA, JSON.stringify(loggedAnalyst));
    }
}

export function getStoredAuthToken() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY_ANALISTA);
        if (!raw) {
            return null;
        }
        const parsed = JSON.parse(raw);
        return parsed?.authToken || null;
    } catch (error) {
        return null;
    }
}

export function mergeAnalystSession(analyst) {
    if (!analyst) {
        return analyst;
    }
    const token = analyst.authToken || loggedAnalyst?.authToken || getStoredAuthToken();
    const base = loggedAnalyst && typeof loggedAnalyst === 'object' ? loggedAnalyst : {};
    const merged = { ...base, ...analyst };
    if (token) {
        merged.authToken = token;
    }
    const fotoResolved = resolveMergedAnalystPhotoUrl(analyst, loggedAnalyst);
    const incomingHasFotoKey =
        Object.prototype.hasOwnProperty.call(analyst, 'fotoUrl') ||
        Object.prototype.hasOwnProperty.call(analyst, 'fotoPerfilUrl');
    if (incomingHasFotoKey) {
        if (fotoResolved) {
            merged.fotoUrl = fotoResolved;
        } else {
            delete merged.fotoUrl;
            delete merged.fotoPerfilUrl;
        }
    } else if (fotoResolved) {
        merged.fotoUrl = fotoResolved;
    }
    return merged;
}
