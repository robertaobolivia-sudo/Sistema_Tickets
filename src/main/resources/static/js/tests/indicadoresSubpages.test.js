import { describe, expect, it } from 'vitest';
import {
    getIndicadoresSubpageMeta,
    INDICADORES_SUBPAGE_DEFAULT,
    isIndicadoresSubpageKey
} from '../core/indicadoresSubpages.js';

describe('indicadoresSubpages.js', () => {
    it('reconhece subpáginas válidas', () => {
        expect(isIndicadoresSubpageKey('indicadores-chamados')).toBe(true);
        expect(isIndicadoresSubpageKey('indicadores-sla')).toBe(true);
        expect(isIndicadoresSubpageKey('indicadores-encerramento-satisfacao')).toBe(true);
        expect(isIndicadoresSubpageKey('outro')).toBe(false);
    });

    it('retorna meta da subpágina chamados', () => {
        const meta = getIndicadoresSubpageMeta('indicadores-chamados');
        expect(meta.titulo).toBe('Chamados');
    });

    it('usa default para chave inválida', () => {
        const meta = getIndicadoresSubpageMeta('invalido');
        expect(meta.titulo).toBe(getIndicadoresSubpageMeta(INDICADORES_SUBPAGE_DEFAULT).titulo);
    });
});
