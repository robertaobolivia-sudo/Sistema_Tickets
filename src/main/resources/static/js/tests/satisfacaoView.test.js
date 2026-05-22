import { describe, expect, it } from 'vitest';
import {
    buildIndicadoresSatisfacaoParams,
    formatSatisfacaoPercent,
    formatSatisfacaoNotaExibicao,
    formatSatisfacaoStatusLabel,
    hasSatisfacaoResumoData
} from '../core/satisfacaoView.js';

describe('satisfacaoView.js', () => {
    it('monta query de satisfação para Indicadores', () => {
        const qs = buildIndicadoresSatisfacaoParams({
            dataInicio: '2026-01-01',
            dataFim: '2026-01-31',
            nota: '5',
            statusTicket: 'RESOLVIDO',
            termoCliente: 'Acme'
        }).toString();
        expect(qs).toContain('dataInicio=2026-01-01');
        expect(qs).toContain('nota=5');
        expect(qs).toContain('termoCliente=Acme');
    });

    it('formata percentual com vírgula', () => {
        expect(formatSatisfacaoPercent(42.5)).toBe('42,5');
    });

    it('detecta resumo com dados', () => {
        expect(hasSatisfacaoResumoData({ totalAvaliacoes: 1 })).toBe(true);
        expect(hasSatisfacaoResumoData({ totalAvaliacoes: 0 })).toBe(false);
    });

    it('formata status de envio da pesquisa', () => {
        expect(formatSatisfacaoStatusLabel('PENDENTE')).toBe('Pendente');
        expect(formatSatisfacaoStatusLabel('NAO_ENVIADA')).toBe('Não enviada');
    });

    it('formata nota no detalhe por status', () => {
        expect(formatSatisfacaoNotaExibicao(5, 'RESPONDIDA')).toBe('5 / 5');
        expect(formatSatisfacaoNotaExibicao(null, 'PENDENTE')).toBe('Aguardando resposta');
        expect(formatSatisfacaoNotaExibicao(null, 'EXPIRADA')).toBe('—');
    });
});
