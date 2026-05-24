import { describe, expect, it } from 'vitest';
import {
    calcularTemposAcompanhamento,
    extractNumeroTicketAcompanhar
} from '@features/dashboard/dashboard-acompanhamento-view.js';

describe('dashboardAcompanhamentoView', () => {
    it('extrai numero TK de chamado ticket', () => {
        expect(
            extractNumeroTicketAcompanhar({
                tipo: 'TICKET',
                numeroTicket: 'TK-000176'
            })
        ).toBe('TK-000176');
    });

    it('ignora pendencia', () => {
        expect(
            extractNumeroTicketAcompanhar({
                tipo: 'PENDENCIA_DECISAO',
                numeroTicket: 'TK-000100 · pendência'
            })
        ).toBeNull();
    });

    it('calcula TME e TMA para em atendimento', () => {
        const now = new Date();
        const abertura = new Date(now.getTime() - 120_000);
        const tempos = calcularTemposAcompanhamento({
            status: 'EM_ATENDIMENTO',
            dataAbertura: abertura.toISOString(),
            dataPrimeiroAtendimento: abertura.toISOString()
        });
        expect(tempos.tme).toMatch(/^\d{2}:\d{2}:\d{2}$/);
        expect(tempos.tma).toMatch(/^\d{2}:\d{2}:\d{2}$/);
    });
});
