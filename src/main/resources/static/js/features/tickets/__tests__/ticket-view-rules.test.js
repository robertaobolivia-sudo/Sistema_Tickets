import { describe, expect, it } from 'vitest';
import {
    canAddTicketInteraction,
    canRegisterSatisfacao,
    getStatusClass,
    isTicketFinalizado
} from '@features/tickets/ticket-view-rules.js';

describe('ticketViewRules.js', () => {
    describe('isTicketFinalizado', () => {
        it('identifica status finalizados', () => {
            expect(isTicketFinalizado('RESOLVIDO')).toBe(true);
            expect(isTicketFinalizado('CANCELADO')).toBe(true);
            expect(isTicketFinalizado('INDEVIDO')).toBe(true);
        });

        it('ticket aberto não é finalizado', () => {
            expect(isTicketFinalizado('ABERTO')).toBe(false);
            expect(isTicketFinalizado('EM_ATENDIMENTO')).toBe(false);
        });
    });

    describe('satisfação', () => {
        it('ticket encerrado permite registrar satisfação sem avaliação', () => {
            expect(canRegisterSatisfacao('RESOLVIDO', false)).toBe(true);
        });

        it('ticket aberto não permite satisfação', () => {
            expect(canRegisterSatisfacao('ABERTO', false)).toBe(false);
        });

        it('com avaliação existente não permite novo formulário', () => {
            expect(canRegisterSatisfacao('RESOLVIDO', true)).toBe(false);
        });
    });

    describe('interações', () => {
        it('ticket aberto permite interação', () => {
            expect(canAddTicketInteraction('ABERTO')).toBe(true);
            expect(canAddTicketInteraction('INDEVIDO')).toBe(false);
        });

        it('ticket finalizado bloqueia interação', () => {
            expect(canAddTicketInteraction('RESOLVIDO')).toBe(false);
        });
    });

    describe('getStatusClass', () => {
        it('aplica classe por status válido', () => {
            expect(getStatusClass('ABERTO')).toBe('status-badge status-ABERTO');
        });

        it('fallback para status desconhecido', () => {
            expect(getStatusClass('INVALIDO')).toBe('status-badge');
        });
    });
});
