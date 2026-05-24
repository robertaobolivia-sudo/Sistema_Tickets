/**
 * Paginação de listas no frontend (Sprint 243).
 */

export const LIST_PAGE_SIZE = 15;

/** Listagem de clientes (Sprint 252). */
export const CLIENTES_LIST_PAGE_SIZE = 10;

export function getTotalPages(totalItems, pageSize = LIST_PAGE_SIZE) {
    const total = Math.max(0, Number(totalItems) || 0);
    if (total === 0) {
        return 1;
    }
    return Math.ceil(total / pageSize);
}

export function clampPage(page, totalItems, pageSize = LIST_PAGE_SIZE) {
    const totalPages = getTotalPages(totalItems, pageSize);
    const n = Math.floor(Number(page)) || 1;
    return Math.min(Math.max(1, n), totalPages);
}

export function slicePageItems(items, page, pageSize = LIST_PAGE_SIZE) {
    const list = Array.isArray(items) ? items : [];
    const p = clampPage(page, list.length, pageSize);
    const start = (p - 1) * pageSize;
    return list.slice(start, start + pageSize);
}

export function getPaginationLabel(page, totalItems, pageSize = LIST_PAGE_SIZE) {
    const total = Math.max(0, Number(totalItems) || 0);
    if (total === 0) {
        return 'Nenhum registro';
    }
    const p = clampPage(page, total, pageSize);
    const start = (p - 1) * pageSize + 1;
    const end = Math.min(p * pageSize, total);
    const totalPages = getTotalPages(total, pageSize);
    return `Página ${p} de ${totalPages} · ${start}–${end} de ${total}`;
}

export function shouldShowListPagination(totalItems, pageSize = LIST_PAGE_SIZE) {
    return (Number(totalItems) || 0) > pageSize;
}
