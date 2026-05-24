import { describe, expect, it } from 'vitest';
import {
    CLIENTES_CONTATOS_MSG_VAZIO,
    CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO,
    contatoTemEtiquetaOperacional,
    formatContatoGestaoChamados,
    formatContatoGestaoLocal,
    formatContatoGestaoStatusHtml,
    formatContatoGestaoStatusLabel,
    formatEtiquetasGestaoCellHtml,
    getContatosGestaoEmptyMessage,
    isNomeEtiquetaOperacional
} from '@features/clientes/cliente-contatos-gestao-view.js';

describe('clienteContatosGestaoView', () => {
    it('formata local cidade/UF', () => {
        expect(formatContatoGestaoLocal({ cidade: 'SP', uf: 'SP' })).toBe('SP/SP');
        expect(formatContatoGestaoLocal({})).toBe('—');
    });

    it('formata resumo de chamados', () => {
        expect(formatContatoGestaoChamados({ totalChamados: 0 })).toBe('—');
        expect(formatContatoGestaoChamados({ chamadosAtivos: 2, totalChamados: 5 })).toBe(
            '2 ativo(s) · 5 total'
        );
    });

    it('mensagem vazia com filtros', () => {
        expect(getContatosGestaoEmptyMessage(false, false)).toBe(CLIENTES_CONTATOS_MSG_VAZIO);
        expect(getContatosGestaoEmptyMessage(true, false)).not.toBeNull();
    });

    it('status ativo/inativo na gestao', () => {
        expect(formatContatoGestaoStatusLabel(true)).toBe('Ativo');
        expect(formatContatoGestaoStatusLabel(false)).toBe('Inativo');
        expect(formatContatoGestaoStatusHtml(false)).toContain('Inativo');
        expect(formatContatoGestaoStatusHtml(false)).toContain('inativo');
    });

    it('sugestao de etiquetas operacionais', () => {
        expect(CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO).toContain('Indevido');
        expect(isNomeEtiquetaOperacional('Propaganda')).toBe(true);
    });

    it('detecta contato com etiqueta operacional', () => {
        expect(
            contatoTemEtiquetaOperacional({ temEtiquetaOperacional: true, etiquetasResumo: 'VIP' })
        ).toBe(true);
        expect(contatoTemEtiquetaOperacional({ etiquetasResumo: 'VIP, Indevido' })).toBe(true);
        expect(contatoTemEtiquetaOperacional({ etiquetasResumo: 'VIP' })).toBe(false);
    });

    it('renderiza chips operacionais na celula', () => {
        const html = formatEtiquetasGestaoCellHtml({ etiquetasResumo: 'Indevido, VIP' });
        expect(html).toContain('contato-gestao-etiqueta-badge--operacional');
        expect(html).toContain('VIP');
    });
});
