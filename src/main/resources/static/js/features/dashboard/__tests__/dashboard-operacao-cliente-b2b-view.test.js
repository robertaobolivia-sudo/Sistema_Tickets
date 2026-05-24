import { describe, expect, it } from 'vitest';
import { formatChamadoStatusLabel } from '@features/dashboard/dashboard-operacao-cliente-b2b-view.js';

describe('dashboardOperacaoClienteB2bView', () => {
    it('formata status pendencia', () => {
        expect(formatChamadoStatusLabel('PENDENCIA_DECISAO')).toBe('Pendência pós-encerramento');
    });
});
