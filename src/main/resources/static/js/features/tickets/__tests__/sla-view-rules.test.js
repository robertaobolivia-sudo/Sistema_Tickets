import { describe, expect, it } from 'vitest';
import {
    formatSlaBadgeHtml,
    formatSlaStatusLabel,
    getSlaBadgeCssClass,
    getSlaResolucaoVisualStatus
} from '@features/tickets/sla-view-rules.js';

describe('slaViewRules.js', () => {
    it('NAO_CALCULADO exibe traço', () => {
        expect(formatSlaStatusLabel('NAO_CALCULADO')).toBe('-');
        expect(formatSlaBadgeHtml('NAO_CALCULADO')).toBe('-');
    });

    it.each([
        ['VENCIDO', 'Vencido', 'sla-badge sla-vencido'],
        ['PAUSADO', 'Pausado', 'sla-badge sla-pausado'],
        ['PROXIMO_DO_VENCIMENTO', 'Próximo do vencimento', 'sla-badge sla-proximo-do-vencimento'],
        ['DENTRO_DO_PRAZO', 'Dentro do prazo', 'sla-badge sla-dentro-do-prazo']
    ])('status %s', (status, label, css) => {
        expect(formatSlaStatusLabel(status)).toBe(label);
        expect(getSlaBadgeCssClass(status)).toBe(css);
        expect(formatSlaBadgeHtml(status)).toContain(label);
        expect(formatSlaBadgeHtml(status)).toContain(css);
    });

    it('resolução usa slaResolucaoStatus quando presente', () => {
        expect(getSlaResolucaoVisualStatus({ slaResolucaoStatus: 'VENCIDO' })).toBe('VENCIDO');
    });

    it('resolução pausada sem status explícito', () => {
        expect(getSlaResolucaoVisualStatus({ slaPausado: true })).toBe('PAUSADO');
    });
});
