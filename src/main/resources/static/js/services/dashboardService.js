import { API_BASE } from '../core/api.js';
import { fetchJsonWithDashboard } from './httpUtil.js';

export function getResumo() {
    return fetchJsonWithDashboard(`${API_BASE}/dashboard/resumo`, 'Falha ao carregar resumo do dashboard');
}

export function getGerencial() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/gerencial`,
        'Falha ao carregar indicadores gerenciais do dashboard'
    );
}

export function getFilasAnalistas() {
    return fetchJsonWithDashboard(`${API_BASE}/dashboard/filas-analistas`, 'Falha ao buscar filas de analistas');
}

export function getConexoesPendencias() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/conexoes-pendencias`,
        'Falha ao buscar pendências por conexão'
    );
}

export function getSla() {
    return fetchJsonWithDashboard(`${API_BASE}/dashboard/sla`, 'Falha ao carregar indicadores de SLA');
}

export function getEncerramentoSatisfacao(queryString = '') {
    const qs = queryString && String(queryString).trim() !== '' ? `?${queryString}` : '';
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/encerramento-satisfacao${qs}`,
        'Falha ao carregar resumo de encerramento e satisfação'
    );
}
