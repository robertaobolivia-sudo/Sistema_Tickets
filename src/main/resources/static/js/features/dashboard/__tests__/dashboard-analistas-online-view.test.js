import { describe, expect, it } from 'vitest';
import {
    DASHBOARD_ANALISTA_STATUS_LABELS,
    flattenOperadoresDashboard
} from '@features/dashboard/dashboard-analistas-online-view.js';

describe('dashboardAnalistasOnlineView', () => {
    it('usa lista operadores quando presente', () => {
        const list = flattenOperadoresDashboard({
            operadores: [
                { nome: 'B', statusExibicao: 'OFFLINE' },
                { nome: 'A', statusExibicao: 'ONLINE' }
            ]
        });
        expect(list).toHaveLength(2);
        expect(list[0].nome).toBe('B');
    });

    it('fallback colunas legadas', () => {
        const list = flattenOperadoresDashboard({
            online: [{ nome: 'A', statusExibicao: 'ONLINE' }],
            offline: [{ nome: 'B', statusExibicao: 'OFFLINE' }]
        });
        expect(list).toHaveLength(2);
        expect(list[0].statusExibicao).toBe('ONLINE');
    });

    it('rotulos de status', () => {
        expect(DASHBOARD_ANALISTA_STATUS_LABELS.OCUPADO).toBe('Ocupado');
    });
});
