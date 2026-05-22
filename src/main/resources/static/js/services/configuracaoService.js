import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function getHorarioUtilPadrao() {
    return fetchJsonWithSession(`${API_BASE}/horarios-uteis/padrao`, undefined, 'Falha ao carregar horário útil');
}

export function saveHorarioUtilPadrao(body) {
    return fetchJsonWithSession(
        `${API_BASE}/horarios-uteis/padrao`,
        { method: 'PUT', body: JSON.stringify(body) },
        'Falha ao salvar horário útil'
    );
}

export function listFeriados() {
    return fetchJsonWithSession(`${API_BASE}/feriados`, undefined, 'Falha ao carregar feriados');
}

export function createFeriado(body) {
    return fetchJsonWithSession(
        `${API_BASE}/feriados`,
        { method: 'POST', body: JSON.stringify(body) },
        'Falha ao cadastrar feriado'
    );
}

export function updateFeriado(id, body) {
    return fetchJsonWithSession(
        `${API_BASE}/feriados/${id}`,
        { method: 'PUT', body: JSON.stringify(body) },
        'Falha ao atualizar feriado'
    );
}

export function patchFeriadoStatus(id, ativo) {
    const acao = ativo ? 'ativar' : 'inativar';
    return fetchJsonWithSession(
        `${API_BASE}/feriados/${id}/${acao}`,
        { method: 'PATCH' },
        'Falha ao alterar status do feriado'
    );
}

export function seedFeriadosSaoPaulo2026() {
    return fetchJsonWithSession(
        `${API_BASE}/feriados/seed/2026-sao-paulo`,
        { method: 'POST' },
        'Falha ao carregar feriados de 2026'
    );
}

export function listSlaMetas() {
    return fetchJsonWithSession(`${API_BASE}/sla-metas`, undefined, 'Falha ao carregar metas de SLA');
}

export function updateSlaMetaPrioridade(prioridade, body) {
    return fetchJsonWithSession(
        `${API_BASE}/sla-metas/prioridade/${prioridade}`,
        { method: 'PUT', body: JSON.stringify(body) },
        'Falha ao salvar meta de SLA'
    );
}

export function seedSlaMetasDefault() {
    return fetchJsonWithSession(
        `${API_BASE}/sla-metas/seed-default`,
        { method: 'POST' },
        'Falha ao carregar metas padrão de SLA'
    );
}
