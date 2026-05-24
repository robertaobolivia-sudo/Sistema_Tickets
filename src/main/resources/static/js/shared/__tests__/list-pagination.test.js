import { describe, expect, it } from 'vitest';
import {
    LIST_PAGE_SIZE,
    clampPage,
    getPaginationLabel,
    getTotalPages,
    shouldShowListPagination,
    slicePageItems
} from '@shared/ui/list-pagination.js';

describe('listPagination', () => {
    it('slicePageItems limita a 15 por página', () => {
        const items = Array.from({ length: 40 }, (_, i) => i + 1);
        expect(slicePageItems(items, 1)).toHaveLength(LIST_PAGE_SIZE);
        expect(slicePageItems(items, 1)[0]).toBe(1);
        expect(slicePageItems(items, 2)[0]).toBe(16);
        expect(slicePageItems(items, 3)).toHaveLength(10);
    });

    it('clampPage e totalPages', () => {
        expect(getTotalPages(0)).toBe(1);
        expect(getTotalPages(15)).toBe(1);
        expect(getTotalPages(16)).toBe(2);
        expect(clampPage(99, 10)).toBe(1);
        expect(clampPage(2, 40)).toBe(2);
        expect(clampPage(0, 40)).toBe(1);
    });

    it('shouldShowListPagination', () => {
        expect(shouldShowListPagination(15)).toBe(false);
        expect(shouldShowListPagination(16)).toBe(true);
    });

    it('getPaginationLabel', () => {
        expect(getPaginationLabel(1, 0)).toContain('Nenhum');
        expect(getPaginationLabel(2, 40)).toContain('16–30');
    });
});
