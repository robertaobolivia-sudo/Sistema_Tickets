import { describe, expect, it } from 'vitest';
import {
    buildHistoricoPanelHtml,
    formatHistoricoAtendimentoOrigem,
    formatHistoricoAvaliacao,
    formatHistoricoStatus,
    getHistoricoColspan
} from '@features/contatos/contato-gestao-historico-view.js';

describe('contatoGestaoHistoricoView', () => {
    it('colspan da tabela principal', () => {
        expect(getHistoricoColspan()).toBe(10);
    });

    it('painel historico inclui Ver conversa', () => {
        const html = buildHistoricoPanelHtml('Ana', [
            { protocolo: 'TK-000001', status: 'RESOLVIDO' }
        ]);
        expect(html).toContain('data-contato-ver-conversa');
        expect(html).toContain('Ver conversa');
        expect(html).toContain('TK-000001');
    });

    it('formata status', () => {
        expect(formatHistoricoStatus('EM_ATENDIMENTO')).toContain('EM ATENDIMENTO');
    });

    it('formata avaliacao com nota', () => {
        expect(formatHistoricoAvaliacao({ satisfacaoStatus: 'RESPONDIDA', satisfacaoNota: 5 })).toMatch(/5/);
    });

    it('avaliacao vazia', () => {
        expect(formatHistoricoAvaliacao({})).toBe('—');
    });

    it('formata origem principal e adicional', () => {
        expect(
            formatHistoricoAtendimentoOrigem({
                atendimentoTelefone: '5511999999999',
                atendimentoTelefoneTipo: 'PRINCIPAL'
            })
        ).toBe('5511999999999 (Principal)');
        expect(
            formatHistoricoAtendimentoOrigem({
                atendimentoTelefone: '5512942833853',
                atendimentoTelefoneTipo: 'ADICIONAL'
            })
        ).toContain('Adicional');
    });

    it('painel historico exibe coluna origem do atendimento', () => {
        const html = buildHistoricoPanelHtml('Ana', [
            {
                protocolo: 'TK-9',
                atendimentoTelefone: '551111',
                atendimentoTelefoneTipo: 'PRINCIPAL'
            }
        ]);
        expect(html).toContain('Origem do atendimento');
        expect(html).toContain('551111 (Principal)');
    });
});
