import { describe, expect, it } from 'vitest';
import {
    buildIndicadoresEncerramentoAvaliacaoParams,
    filterClientesAtivosIndicadores,
    hasEncerramentoAvaliacaoData
} from '../core/encerramentoAvaliacaoView.js';

describe('encerramentoAvaliacaoView.js', () => {
    it('monta query params de encerramento', () => {
        const params = buildIndicadoresEncerramentoAvaliacaoParams({
            dataInicio: '2026-05-01',
            dataFim: '2026-05-20',
            clienteId: '42',
            motivoId: '3',
            statusPesquisa: 'RESPONDIDA',
            notaAvaliacao: '5'
        });
        expect(params.get('dataInicio')).toBe('2026-05-01');
        expect(params.get('clienteId')).toBe('42');
        expect(params.get('motivoId')).toBe('3');
        expect(params.get('statusPesquisa')).toBe('RESPONDIDA');
        expect(params.get('notaAvaliacao')).toBe('5');
    });

    it('não envia clienteId vazio na query', () => {
        const params = buildIndicadoresEncerramentoAvaliacaoParams({
            clienteId: '',
            motivoId: '1'
        });
        expect(params.has('clienteId')).toBe(false);
        expect(params.get('motivoId')).toBe('1');
    });

    it('filterClientesAtivosIndicadores exclui inativos', () => {
        const filtrados = filterClientesAtivosIndicadores([
            { id: 1, nome: 'A', ativo: true },
            { id: 2, nome: 'B', ativo: false },
            { id: 3, nome: 'C', status: 'INATIVO' },
            { id: 4, nome: 'D' }
        ]);
        expect(filtrados.map(c => c.id)).toEqual([1, 4]);
    });

    it('hasEncerramentoAvaliacaoData detecta vazio e com dados', () => {
        expect(hasEncerramentoAvaliacaoData(null)).toBe(false);
        expect(hasEncerramentoAvaliacaoData({ topMotivos: [], pesquisa: { totalPesquisas: 0 } })).toBe(
            false
        );
        expect(
            hasEncerramentoAvaliacaoData({
                topMotivos: [{ motivoId: 1, totalTickets: 2 }],
                pesquisa: {},
                envio: {}
            })
        ).toBe(true);
        expect(
            hasEncerramentoAvaliacaoData({
                topMotivos: [],
                pesquisa: { totalPesquisas: 1 },
                envio: {}
            })
        ).toBe(true);
    });
});
