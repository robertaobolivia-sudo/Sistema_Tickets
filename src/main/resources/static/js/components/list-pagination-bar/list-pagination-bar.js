import {
    LIST_PAGE_SIZE,
    clampPage,
    getPaginationLabel,
    getTotalPages,
    shouldShowListPagination
} from '@shared/ui/list-pagination.js';

const ACTIONS = {
    first: 'first',
    prev: 'prev',
    next: 'next',
    last: 'last'
};

function createNavButton(action, label) {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'button button-secondary button-small list-pagination-btn';
    btn.dataset.pagination = action;
    btn.textContent = label;
    btn.setAttribute('aria-label', label);
    return btn;
}

/**
 * @param {HTMLElement|null} hostEl
 * @param {{ pageSize?: number, ariaLabel?: string, onPageChange?: (page: number) => void }} [options]
 */
export function createListPaginationController(hostEl, options = {}) {
    const pageSize = options.pageSize ?? LIST_PAGE_SIZE;
    let totalItems = 0;
    let currentPage = 1;
    const onPageChange = options.onPageChange ?? (() => {});

    if (!hostEl) {
        return {
            setState() {},
            getPage: () => 1,
            resetToFirstPage() {}
        };
    }

    hostEl.classList.add('list-pagination-bar', 'hidden');
    hostEl.setAttribute('role', 'navigation');
    hostEl.setAttribute('aria-label', options.ariaLabel ?? 'Paginação da lista');

    const inner = document.createElement('div');
    inner.className = 'list-pagination-inner';

    const info = document.createElement('span');
    info.className = 'list-pagination-info';

    const actions = document.createElement('div');
    actions.className = 'list-pagination-actions';

    const btnFirst = createNavButton(ACTIONS.first, 'Primeira');
    const btnPrev = createNavButton(ACTIONS.prev, 'Anterior');
    const btnNext = createNavButton(ACTIONS.next, 'Próxima');
    const btnLast = createNavButton(ACTIONS.last, 'Última');

    actions.append(btnFirst, btnPrev, btnNext, btnLast);
    inner.append(info, actions);
    hostEl.replaceChildren(inner);

    function syncButtons() {
        const totalPages = getTotalPages(totalItems, pageSize);
        const page = clampPage(currentPage, totalItems, pageSize);
        btnFirst.disabled = page <= 1;
        btnPrev.disabled = page <= 1;
        btnNext.disabled = page >= totalPages;
        btnLast.disabled = page >= totalPages;
    }

    function goToPage(page) {
        const next = clampPage(page, totalItems, pageSize);
        if (next === currentPage) {
            syncButtons();
            return;
        }
        currentPage = next;
        syncButtons();
        info.textContent = getPaginationLabel(currentPage, totalItems, pageSize);
        onPageChange(currentPage);
    }

    function setState({ page, total }) {
        totalItems = Math.max(0, Number(total) || 0);
        currentPage = clampPage(page, totalItems, pageSize);
        const visible = shouldShowListPagination(totalItems, pageSize);
        hostEl.classList.toggle('hidden', !visible);
        info.textContent = getPaginationLabel(currentPage, totalItems, pageSize);
        syncButtons();
    }

    hostEl.addEventListener('click', event => {
        const btn = event.target.closest('[data-pagination]');
        if (!btn || btn.disabled) {
            return;
        }
        const action = btn.dataset.pagination;
        const totalPages = getTotalPages(totalItems, pageSize);
        const page = clampPage(currentPage, totalItems, pageSize);
        if (action === ACTIONS.first) {
            goToPage(1);
        } else if (action === ACTIONS.prev) {
            goToPage(page - 1);
        } else if (action === ACTIONS.next) {
            goToPage(page + 1);
        } else if (action === ACTIONS.last) {
            goToPage(totalPages);
        }
    });

    return {
        setState,
        getPage: () => clampPage(currentPage, totalItems, pageSize),
        resetToFirstPage() {
            setState({ page: 1, total: totalItems });
        }
    };
}
