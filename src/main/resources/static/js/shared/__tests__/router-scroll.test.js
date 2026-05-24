import { describe, expect, it } from 'vitest';
import { resetMainContentScroll } from '@shared/router/router.js';

describe('resetMainContentScroll', () => {
    it('não lança quando não há .main-content no documento', () => {
        expect(() => resetMainContentScroll()).not.toThrow();
    });
});
