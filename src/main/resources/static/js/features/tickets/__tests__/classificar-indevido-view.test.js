import { describe, it, expect } from 'vitest';
import { buildClassificarIndevidoPayload } from '@features/tickets/classificar-indevido-view.js';

describe('classificarIndevidoView', () => {
    it('exige confirmacao', () => {
        expect(() =>
            buildClassificarIndevidoPayload({
                confirmacao: false,
                motivoOperacional: 'INDEVIDO'
            })
        ).toThrow(/Confirme/);
    });

    it('monta payload valido', () => {
        const p = buildClassificarIndevidoPayload({
            confirmacao: true,
            motivoOperacional: 'PROPAGANDA',
            comentario: ' spam '
        });
        expect(p.confirmacao).toBe(true);
        expect(p.motivoOperacional).toBe('PROPAGANDA');
        expect(p.comentario).toBe('spam');
    });
});
