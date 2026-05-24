/**
 * Sprint 286 — submenu Status operador (topbar / avatar).
 */

export const TOPBAR_STATUS_OPTIONS = [
    { code: 'ONLINE', label: 'Online', swatchClass: 'online' },
    { code: 'OCUPADO', label: 'Ocupado', swatchClass: 'ocupado' },
    { code: 'AUSENTE', label: 'Ausente', swatchClass: 'ausente' },
    { code: 'OFFLINE', label: 'Offline', swatchClass: 'offline' }
];

export function normalizeOperadorStatusCode(raw) {
    const s = String(raw ?? 'OFFLINE').trim().toUpperCase();
    if (TOPBAR_STATUS_OPTIONS.some(o => o.code === s)) {
        return s;
    }
    return 'OFFLINE';
}

/**
 * @param {string} currentCode
 * @param {{ menuRoot?: HTMLElement|null, currentSwatchEl?: HTMLElement|null }} refs
 */
export function applyTopbarStatusMenuUi(currentCode, refs = {}) {
    const code = normalizeOperadorStatusCode(currentCode);
    const root = refs.menuRoot;
    if (root) {
        root.querySelectorAll('.topbar-status-option').forEach(btn => {
            const opt = btn.dataset.userMenu?.replace('status-', '') ?? '';
            const isCurrent = opt === code;
            btn.classList.toggle('is-current', isCurrent);
            const check = btn.querySelector('.topbar-status-check');
            if (check) {
                check.classList.toggle('hidden', !isCurrent);
            }
            btn.setAttribute('aria-checked', isCurrent ? 'true' : 'false');
        });
    }
    const swatch = refs.currentSwatchEl;
    if (swatch) {
        swatch.className = 'topbar-status-swatch topbar-status-swatch--current';
        const opt = TOPBAR_STATUS_OPTIONS.find(o => o.code === code);
        if (opt) {
            swatch.classList.add(`topbar-status-swatch--${opt.swatchClass}`);
        } else {
            swatch.classList.add('topbar-status-swatch--offline');
        }
    }
}
