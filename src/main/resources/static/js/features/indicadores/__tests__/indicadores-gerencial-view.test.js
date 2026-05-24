import { describe, expect, it } from 'vitest';
import {
    formatIndicadorStatusRotulo,
    formatTicketStatusExibicao
} from '@features/indicadores/indicadores-gerencial-view.js';

describe('indicadoresGerencialView', () => {
    it('formata grupo não atendimento', () => {
        expect(formatIndicadorStatusRotulo('NAO_ATENDIMENTO_INDEVIDO')).toBe(
            'Não atendimento (Indevido)'
        );
        expect(formatTicketStatusExibicao('INDEVIDO')).toBe('Não atendimento');
    });
});
