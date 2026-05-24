import { describe, expect, it } from 'vitest';
import {
    buildAnalystPhotoSrc,
    getAnalystPhotoUrl,
    resolveMergedAnalystPhotoUrl
} from '@shared/ui/analyst-avatar.js';

describe('analystAvatar', () => {
    it('resolve fotoUrl e alias fotoPerfilUrl', () => {
        expect(getAnalystPhotoUrl({ fotoUrl: '/uploads/a.png' })).toBe('/uploads/a.png');
        expect(getAnalystPhotoUrl({ fotoPerfilUrl: '/uploads/b.jpg' })).toBe('/uploads/b.jpg');
        expect(getAnalystPhotoUrl({ fotoUrl: '  ' })).toBeNull();
    });

    it('preserva foto quando API omite campo', () => {
        expect(
            resolveMergedAnalystPhotoUrl({ statusOperador: 'ONLINE' }, { fotoUrl: '/uploads/x.png' })
        ).toBe('/uploads/x.png');
    });

    it('aceita remoção explícita de foto', () => {
        expect(resolveMergedAnalystPhotoUrl({ fotoUrl: null }, { fotoUrl: '/uploads/x.png' })).toBeNull();
    });

    it('adiciona versão na URL pública', () => {
        const src = buildAnalystPhotoSrc('/uploads/analistas/foto-1.jpg', 9);
        expect(src).toContain('/uploads/analistas/foto-1.jpg');
        expect(src).toMatch(/[?&]v=/);
    });
});
