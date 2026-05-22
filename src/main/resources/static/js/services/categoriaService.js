import { API_BASE } from '../core/api.js';
import { fetchJsonWithSession } from './httpUtil.js';

export function listGrupos() {
    return fetchJsonWithSession(`${API_BASE}/grupos-categoria`, undefined, 'Falha ao buscar grupos');
}

export function listAllSubgrupos() {
    return fetchJsonWithSession(`${API_BASE}/subgrupos-categoria`, undefined, 'Falha ao buscar subcategorias');
}

export function listSubgruposByGrupo(grupoId) {
    return fetchJsonWithSession(
        `${API_BASE}/subgrupos-categoria/grupo/${grupoId}`,
        undefined,
        'Falha ao buscar subgrupos'
    );
}

export function getMotivo(id) {
    return fetchJsonWithSession(
        `${API_BASE}/motivos/${encodeURIComponent(id)}`,
        undefined,
        'Falha ao buscar motivo'
    );
}

export function listMotivos(subgrupoId) {
    const qs =
        subgrupoId != null && subgrupoId !== ''
            ? `?subgrupoId=${encodeURIComponent(subgrupoId)}`
            : '';
    return fetchJsonWithSession(`${API_BASE}/motivos${qs}`, undefined, 'Falha ao buscar motivos');
}

export function createMotivo(payload) {
    return fetchJsonWithSession(
        `${API_BASE}/motivos`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        },
        'Falha ao criar motivo'
    );
}

export function updateMotivo(id, payload) {
    return fetchJsonWithSession(
        `${API_BASE}/motivos/${encodeURIComponent(id)}`,
        {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        },
        'Falha ao atualizar motivo'
    );
}

export function ativarMotivo(id) {
    return fetchJsonWithSession(
        `${API_BASE}/motivos/${encodeURIComponent(id)}/ativar`,
        { method: 'PATCH' },
        'Falha ao ativar motivo'
    );
}

export function inativarMotivo(id) {
    return fetchJsonWithSession(
        `${API_BASE}/motivos/${encodeURIComponent(id)}/inativar`,
        { method: 'PATCH' },
        'Falha ao inativar motivo'
    );
}
