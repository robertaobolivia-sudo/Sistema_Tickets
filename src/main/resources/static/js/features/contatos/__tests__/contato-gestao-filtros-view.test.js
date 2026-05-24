import { describe, expect, it } from 'vitest';
import {
    buildContatoGestaoGestaoQueryParams,
    hasContatosGestaoFiltrosAtivos
} from '@features/contatos/contato-gestao-filtros-view.js';

describe('contatoGestaoFiltrosView', () => {
    it('monta query gestao com filtros avancados', () => {
        const params = buildContatoGestaoGestaoQueryParams({
            clienteId: 3,
            busca: 'Ana',
            etiquetaId: 5,
            cidade: 'Campinas',
            uf: 'sp',
            comTicketsAbertos: true,
            comAvaliacaoRuim: true,
            semEtiqueta: true
        });
        expect(params.get('gestao')).toBe('true');
        expect(params.get('clienteId')).toBe('3');
        expect(params.get('busca')).toBe('Ana');
        expect(params.get('etiquetaId')).toBe('5');
        expect(params.get('cidade')).toBe('Campinas');
        expect(params.get('uf')).toBe('sp');
        expect(params.get('comTicketsAbertos')).toBe('true');
        expect(params.get('comAvaliacaoRuim')).toBe('true');
        expect(params.get('semEtiqueta')).toBe('true');
    });

    it('detecta filtros ativos', () => {
        expect(hasContatosGestaoFiltrosAtivos({})).toBe(false);
        expect(hasContatosGestaoFiltrosAtivos({ comTicketsAbertos: true })).toBe(true);
        expect(hasContatosGestaoFiltrosAtivos({ clienteId: 1 })).toBe(true);
    });
});
