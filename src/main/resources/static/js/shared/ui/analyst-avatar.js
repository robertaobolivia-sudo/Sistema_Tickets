/**
 * Sprint 287 — fonte única de foto do operador (sessão / API / localStorage).
 */

import { getAnalystInitials } from '@shared/ui/presentation.js';

/**
 * @param {Record<string, unknown> | null | undefined} analyst
 * @returns {string | null}
 */
export function getAnalystPhotoUrl(analyst) {
    if (!analyst) return null;
    const raw = analyst.fotoUrl ?? analyst.fotoPerfilUrl;
    if (typeof raw !== 'string') return null;
    const trimmed = raw.trim();
    return trimmed || null;
}

/**
 * Preserva foto na sessão quando a API omite o campo (JsonInclude.NON_NULL).
 * @param {Record<string, unknown> | null | undefined} incoming
 * @param {Record<string, unknown> | null | undefined} previous
 * @returns {string | null | undefined} undefined = não alterar foto no merge
 */
export function resolveMergedAnalystPhotoUrl(incoming, previous) {
    if (!incoming) return getAnalystPhotoUrl(previous) ?? null;
    const incomingHasFotoKey =
        Object.prototype.hasOwnProperty.call(incoming, 'fotoUrl') ||
        Object.prototype.hasOwnProperty.call(incoming, 'fotoPerfilUrl');
    if (incomingHasFotoKey) {
        return getAnalystPhotoUrl(incoming);
    }
    const fromIncoming = getAnalystPhotoUrl(incoming);
    if (fromIncoming) return fromIncoming;
    return getAnalystPhotoUrl(previous) ?? null;
}

/**
 * @param {string | null} photoUrl
 * @param {number | string | null | undefined} analystId
 * @returns {string | null}
 */
export function buildAnalystPhotoSrc(photoUrl, analystId) {
    if (!photoUrl) return null;
    if (photoUrl.startsWith('data:') || photoUrl.startsWith('blob:')) {
        return photoUrl;
    }
    const version =
        photoUrl.split('/').filter(Boolean).pop() ||
        (analystId != null ? String(analystId) : '1');
    const sep = photoUrl.includes('?') ? '&' : '?';
    return `${photoUrl}${sep}v=${encodeURIComponent(version)}`;
}

/**
 * @param {HTMLElement | null} element
 * @param {Record<string, unknown> | null | undefined} analyst
 */
export function applyAnalystAvatarToElement(element, analyst) {
    if (!element) return;

    const isProfilePhoto = element.id === 'perfilFoto';
    const isTopbarAvatar = element.id === 'topbarPerfilAvatar';
    const photoUrl = getAnalystPhotoUrl(analyst);
    const hasPhoto = Boolean(photoUrl);

    element.className = isProfilePhoto
        ? 'analyst-avatar analyst-avatar-profile'
        : isTopbarAvatar
          ? 'analyst-avatar analyst-avatar-topbar'
          : 'analyst-avatar analyst-avatar-large';
    element.classList.toggle('has-photo', hasPhoto);
    element.classList.toggle('analyst-avatar-photo', hasPhoto);

    if (hasPhoto) {
        const src = buildAnalystPhotoSrc(photoUrl, analyst?.id);
        const existingImg = element.querySelector('img');
        if (existingImg && existingImg.getAttribute('src') === src) {
            return;
        }
        const img = document.createElement('img');
        img.src = src;
        img.alt = analyst?.nome ? String(analyst.nome) : 'Analista';
        img.loading = 'eager';
        img.decoding = 'async';
        img.dataset.analystPhoto = '1';
        img.addEventListener(
            'error',
            () => {
                const fallback = element.dataset.fallbackPhotoSrc;
                if (fallback && img.src !== fallback) {
                    img.src = fallback;
                    return;
                }
            },
            { once: true }
        );
        element.dataset.fallbackPhotoSrc = photoUrl;
        element.replaceChildren(img);
        element.textContent = '';
    } else {
        delete element.dataset.fallbackPhotoSrc;
        element.replaceChildren();
        element.textContent = getAnalystInitials(
            typeof analyst?.nome === 'string' ? analyst.nome : undefined
        );
    }
}

/**
 * @param {Record<string, unknown> | null | undefined} analyst
 * @param {boolean} [large]
 * @returns {string}
 */
export function renderAnalystAvatarHtml(analyst, large = false) {
    const classes = `analyst-avatar${large ? ' analyst-avatar-large' : ''}`;
    const photoUrl = getAnalystPhotoUrl(analyst);
    if (photoUrl) {
        const src = buildAnalystPhotoSrc(photoUrl, analyst?.id);
        const alt = analyst?.nome ? String(analyst.nome) : 'Analista';
        return `<div class="${classes} analyst-avatar-photo has-photo"><img src="${src}" alt="${alt}" loading="lazy" decoding="async" /></div>`;
    }
    return `<div class="${classes}">${getAnalystInitials(
        typeof analyst?.nome === 'string' ? analyst.nome : undefined
    )}</div>`;
}
