import { describe, expect, it } from 'vitest';
import { normalizeOperadorStatusCode } from '@components/topbar/topbar-status-menu-view.js';

describe('topbarStatusMenuView', () => {
    it('normaliza status operador', () => {
        expect(normalizeOperadorStatusCode('ocupado')).toBe('OCUPADO');
        expect(normalizeOperadorStatusCode('invalid')).toBe('OFFLINE');
    });
});
