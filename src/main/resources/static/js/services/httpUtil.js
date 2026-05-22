import { apiFetch } from '../core/api.js';
import { mensagemErroSessaoApi, mensagemErroDashboardApi } from '../core/messages.js';

export async function readJson(response) {
    return response.json().catch(() => null);
}

export async function fetchJsonWithSession(url, options, errorMessage) {
    const response = await apiFetch(url, options);
    const data = await readJson(response);
    if (!response.ok) {
        throw new Error(mensagemErroSessaoApi(response, data, errorMessage));
    }
    return data;
}

export async function fetchJsonWithDashboard(url, errorMessage) {
    const response = await apiFetch(url);
    const data = await readJson(response);
    if (!response.ok) {
        throw new Error(mensagemErroDashboardApi(response, data, errorMessage));
    }
    return data;
}

export { apiFetch };
