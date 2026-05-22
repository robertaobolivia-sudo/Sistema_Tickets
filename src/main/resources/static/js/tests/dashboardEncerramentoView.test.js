import { describe, expect, it } from 'vitest';
import {
    buildDashboardEncerramentoQueryParams,
    formatDashboardMediaNota,
    formatDashboardMotivoRecorrente,
    normalizeDashboardEncerramentoDias,
    normalizeDashboardEncerramentoResumo
} from '../core/dashboardEncerramentoView.js';

describe('dashboardEncerramentoView.js', () => {
    it('formatDashboardMediaNota', () => {
        expect(formatDashboardMediaNota(null)).toBe('—');
        expect(formatDashboardMediaNota(4.5)).toBe('4,5');
    });

    it('formatDashboardMotivoRecorrente', () => {
        expect(formatDashboardMotivoRecorrente(null, 0)).toBe('—');
        expect(formatDashboardMotivoRecorrente('Suporte', 3)).toBe('Suporte (3)');
    });

    it('normalizeDashboardEncerramentoResumo estado vazio', () => {
        const n = normalizeDashboardEncerramentoResumo(null);
        expect(n.motivoLabel).toBe('—');
        expect(n.mediaLabel).toBe('—');
        expect(n.respondidas).toBe(0);
    });

    it('normalizeDashboardEncerramentoDias', () => {
        expect(normalizeDashboardEncerramentoDias(7)).toBe(7);
        expect(normalizeDashboardEncerramentoDias(30)).toBe(30);
        expect(normalizeDashboardEncerramentoDias(90)).toBe(90);
        expect(normalizeDashboardEncerramentoDias(null)).toBe(30);
        expect(normalizeDashboardEncerramentoDias(15)).toBe(30);
    });

    it('buildDashboardEncerramentoQueryParams com e sem clienteId', () => {
        const comCliente = buildDashboardEncerramentoQueryParams({ dias: 7, clienteId: '12' });
        expect(comCliente.get('dias')).toBe('7');
        expect(comCliente.get('clienteId')).toBe('12');
        const semCliente = buildDashboardEncerramentoQueryParams({ dias: 30, clienteId: '' });
        expect(semCliente.get('dias')).toBe('30');
        expect(semCliente.has('clienteId')).toBe(false);
    });
});
