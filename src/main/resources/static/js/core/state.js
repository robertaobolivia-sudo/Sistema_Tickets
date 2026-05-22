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
    if (!token) {
        return analyst;
    }
    return { ...analyst, authToken: token };
}
