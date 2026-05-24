import { describe, expect, it } from 'vitest';
import {
    deveDesabilitarPesquisaSimEncerramento,
    validateEncerramentoPayload
} from '@features/satisfacao/encerramento-view.js';

describe('encerramentoView', () => {
    it('desabilita pesquisa Sim quando ticket sem contatoId', () => {
        expect(deveDesabilitarPesquisaSimEncerramento(null)).toBe(true);
        expect(deveDesabilitarPesquisaSimEncerramento('')).toBe(true);
        expect(deveDesabilitarPesquisaSimEncerramento(undefined)).toBe(true);
        expect(deveDesabilitarPesquisaSimEncerramento(42)).toBe(false);
    });

    it('exige motivo no encerramento', () => {
        const r = validateEncerramentoPayload({
            grupoId: 1,
            subgrupoId: 2,
            motivoId: null,
            comentarioEncerramento: 'ok'
        });
        expect(r.ok).toBe(false);
        expect(r.message).toContain('motivo');
    });

    it('aceita payload completo', () => {
        const r = validateEncerramentoPayload({
            grupoId: '1',
            subgrupoId: '2',
            motivoId: '7',
            comentarioEncerramento: '  encerrado  '
        });
        expect(r.ok).toBe(true);
        expect(r.motivoId).toBe(7);
        expect(r.comentarioEncerramento).toBe('encerrado');
        expect(r.enviarPesquisaSatisfacao).toBe(false);
    });

    it('propaga opt-in de pesquisa', () => {
        const r = validateEncerramentoPayload({
            grupoId: 1,
            subgrupoId: 2,
            motivoId: 3,
            comentarioEncerramento: 'fim',
            enviarPesquisaSatisfacao: 'true'
        });
        expect(r.ok).toBe(true);
        expect(r.enviarPesquisaSatisfacao).toBe(true);
    });
});
