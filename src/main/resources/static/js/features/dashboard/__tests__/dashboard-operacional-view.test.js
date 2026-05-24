import { describe, expect, it } from 'vitest';
import {
    buildRecorrenciasHtml,
    formatMediaAtual,
    normalizeAvaliacaoTempoReal,
    normalizeEncerramentosDia,
    normalizeSlaVivoFromApi
} from '@features/dashboard/dashboard-operacional-view.js';

describe('dashboardOperacionalView', () => {
    it('normaliza SLA vivo da API', () => {
        const vivo = normalizeSlaVivoFromApi({
            vivo: { dentroDoPrazo: 2, proximosDoLimite: 1, vencidos: 3 },
            ticketsCriticosSla: [{ numeroTicket: 'TK-1', cliente: 'A', prioridade: 'ALTA' }]
        });
        expect(vivo.dentroDoPrazo).toBe(2);
        expect(vivo.proximosDoLimite).toBe(1);
        expect(vivo.vencidos).toBe(3);
        expect(vivo.ticketMaisCritico?.numeroTicket).toBe('TK-1');
    });

    it('normaliza encerramentos do dia', () => {
        const enc = normalizeEncerramentosDia({
            finalizados: 5,
            naoResolvidos: 1,
            escalonados: 2,
            abandonados: 0,
            recorrencias: [{ rotulo: 'Dúvida', total: 3 }]
        });
        expect(enc.finalizados).toBe(5);
        expect(enc.recorrencias).toHaveLength(1);
    });

    it('formata media atual', () => {
        expect(formatMediaAtual(null)).toBe('—');
        expect(formatMediaAtual(4.2)).toBe('4,2');
    });

    it('normaliza avaliacao tempo real', () => {
        const a = normalizeAvaliacaoTempoReal({
            mediaAtual: 3.5,
            avaliacoesRuins: 2,
            pesquisasPendentes: 1
        });
        expect(a.pesquisasPendentes).toBe(1);
        expect(a.avaliacoesRuins).toBe(2);
    });

    it('monta HTML curto de recorrências', () => {
        const html = buildRecorrenciasHtml([{ rotulo: 'Falha', total: 2 }], t => t);
        expect(html).toContain('Falha');
        expect(html).toContain('2');
    });
});
