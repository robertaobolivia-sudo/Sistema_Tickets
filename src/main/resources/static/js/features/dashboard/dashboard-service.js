import { API_BASE } from '@shared/api/api-client.js';
import { fetchJsonWithDashboard } from '@shared/api/http.js';

export function getResumo() {
    return fetchJsonWithDashboard(`${API_BASE}/dashboard/resumo`, 'Falha ao carregar resumo do dashboard');
}

export function getOperacaoAgora() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/operacao-agora`,
        'Falha ao carregar operação agora do dashboard'
    );
}

export function getAnalistasOnline() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/analistas-online`,
        'Falha ao carregar analistas online do dashboard'
    );
}

export function getOperacaoClienteB2b() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/operacao-cliente-b2b`,
        'Falha ao carregar operação por cliente B2B'
    );
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

export function getClientesPendencias() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/clientes-pendencias`,
        'Falha ao buscar pendências por cliente'
    );
}

export function getSla() {
    return fetchJsonWithDashboard(`${API_BASE}/dashboard/sla`, 'Falha ao carregar indicadores de SLA');
}

export function getEncerramentosDia() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/encerramentos-dia`,
        'Falha ao carregar encerramentos do dia'
    );
}

export function getAvaliacaoTempoReal() {
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/avaliacao-tempo-real`,
        'Falha ao carregar avaliação em tempo real'
    );
}

export function getEncerramentoSatisfacao(queryString = '') {
    const qs = queryString && String(queryString).trim() !== '' ? `?${queryString}` : '';
    return fetchJsonWithDashboard(
        `${API_BASE}/dashboard/encerramento-satisfacao${qs}`,
        'Falha ao carregar resumo de encerramento e satisfação'
    );
}
