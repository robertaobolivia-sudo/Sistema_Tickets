/**
 * Sprint 286 — Status Operador (Dashboard grid único).
 */

import { getAnalystInitials } from '@shared/ui/presentation.js';
import { buildAnalystPhotoSrc, getAnalystPhotoUrl } from '@shared/ui/analyst-avatar.js';

export const DASHBOARD_ANALISTA_STATUS_ORDER = ['ONLINE', 'OCUPADO', 'AUSENTE', 'OFFLINE'];

export const DASHBOARD_ANALISTA_STATUS_LABELS = {
    ONLINE: 'Online',
    OCUPADO: 'Ocupado',
    AUSENTE: 'Ausente',
    OFFLINE: 'Offline'
};

export function flattenOperadoresDashboard(data) {
    if (Array.isArray(data?.operadores) && data.operadores.length) {
        return [...data.operadores];
    }
    const cols = {
        ONLINE: Array.isArray(data?.online) ? data.online : [],
        OCUPADO: Array.isArray(data?.ocupado) ? data.ocupado : [],
        AUSENTE: Array.isArray(data?.ausente) ? data.ausente : [],
        OFFLINE: Array.isArray(data?.offline) ? data.offline : []
    };
    const out = [];
    DASHBOARD_ANALISTA_STATUS_ORDER.forEach(status => {
        cols[status].forEach(item => out.push(item));
    });
    return out;
}

/** @deprecated use flattenOperadoresDashboard */
export function mapAnalistasOnlineColumns(data) {
    return {
        ONLINE: Array.isArray(data?.online) ? data.online : [],
        OCUPADO: Array.isArray(data?.ocupado) ? data.ocupado : [],
        AUSENTE: Array.isArray(data?.ausente) ? data.ausente : [],
        OFFLINE: Array.isArray(data?.offline) ? data.offline : []
    };
}

export function buildAnalistaOnlineCardElement(analista) {
    const status = analista?.statusExibicao || 'OFFLINE';
    const article = document.createElement('article');
    article.className = 'dashboard-analista-card';
    article.setAttribute('data-status', status);

    const avatarWrap = document.createElement('div');
    avatarWrap.className = 'dashboard-analista-avatar';
    const photoUrl = getAnalystPhotoUrl(analista);
    if (photoUrl) {
        const img = document.createElement('img');
        img.src = buildAnalystPhotoSrc(photoUrl, analista?.id);
        img.alt = '';
        avatarWrap.appendChild(img);
    } else {
        avatarWrap.textContent = getAnalystInitials(analista?.nome);
    }

    const statusRow = document.createElement('div');
    statusRow.className = 'dashboard-analista-card-status-row';
    const dot = document.createElement('span');
    dot.className = `dashboard-analista-status-dot dashboard-analista-status-dot--${status.toLowerCase()}`;
    dot.setAttribute('aria-hidden', 'true');
    const statusEl = document.createElement('span');
    statusEl.className = 'dashboard-analista-card-status';
    statusEl.textContent = (DASHBOARD_ANALISTA_STATUS_LABELS[status] || status).toUpperCase();
    statusRow.appendChild(dot);
    statusRow.appendChild(statusEl);
    article.appendChild(statusRow);

    const mainRow = document.createElement('div');
    mainRow.className = 'dashboard-analista-card-main';

    const body = document.createElement('div');
    body.className = 'dashboard-analista-card-body';
    const nameEl = document.createElement('div');
    nameEl.className = 'dashboard-analista-card-name';
    nameEl.textContent = analista?.nome || '—';
    const cargoEl = document.createElement('div');
    cargoEl.className = 'dashboard-analista-card-cargo';
    cargoEl.textContent = analista?.cargo || '—';
    body.appendChild(nameEl);
    body.appendChild(cargoEl);

    mainRow.appendChild(avatarWrap);
    mainRow.appendChild(body);
    article.appendChild(mainRow);
    return article;
}
