/**
 * Etiquetas de cliente (legado: campo classificacaoCliente na API).
 * N1/N2 não são padrão de produto; exibição neutra até etiquetas flexíveis.
 */

export const ETIQUETA_CLIENTE_PADRAO = 'SEM_CLASSIFICACAO';

/** Valores ainda persistidos no banco (legado). */
export const CLASSIFICACAO_CLIENTE_VALUES = ['N1', 'N2', 'SEM_CLASSIFICACAO'];

export function normalizeClassificacaoCliente(value) {
    const raw = value == null ? '' : String(value).trim().toUpperCase();
    if (raw === 'N1' || raw === 'N2') {
        return ETIQUETA_CLIENTE_PADRAO;
    }
    if (raw === 'SEM_CLASSIFICACAO' || raw === 'SEM_ETIQUETA') {
        return ETIQUETA_CLIENTE_PADRAO;
    }
    return ETIQUETA_CLIENTE_PADRAO;
}

export function formatEtiquetaClienteLabel() {
    return 'Sem etiqueta';
}

/** @deprecated Use formatEtiquetaClienteLabel — não exibe N1/N2. */
export function formatClassificacaoClienteLabel(value) {
    return formatEtiquetaClienteLabel(value);
}

export function classificacaoClienteBadgeClass() {
    return 'cliente-classificacao-badge cliente-classificacao-sem';
}

export function renderClassificacaoClienteBadge() {
    const label = formatEtiquetaClienteLabel();
    const cls = classificacaoClienteBadgeClass();
    return `<span class="${cls}">${label}</span>`;
}
