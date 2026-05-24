import { describe, expect, it } from 'vitest';
import { CLIENTES_LIST_PAGE_SIZE, slicePageItems } from '@shared/ui/list-pagination.js';

describe('clientes listagem paginação', () => {
    it('usa 10 itens por página no frontend', () => {
        expect(CLIENTES_LIST_PAGE_SIZE).toBe(10);
        const items = Array.from({ length: 25 }, (_, i) => i + 1);
        expect(slicePageItems(items, 1, CLIENTES_LIST_PAGE_SIZE)).toHaveLength(10);
        expect(slicePageItems(items, 2, CLIENTES_LIST_PAGE_SIZE)[0]).toBe(11);
        expect(slicePageItems(items, 3, CLIENTES_LIST_PAGE_SIZE)).toHaveLength(5);
    });
});
