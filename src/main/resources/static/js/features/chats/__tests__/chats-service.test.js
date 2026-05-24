import { describe, expect, it } from 'vitest';
import { coerceTicketsList } from '@features/chats/chats-service.js';

describe('chatsService.coerceTicketsList', () => {
    it('retorna array quando já é lista', () => {
        const list = [{ numeroTicket: 'TK-1' }];
        expect(coerceTicketsList(list)).toBe(list);
    });

    it('extrai content/tickets/items de envelope', () => {
        expect(coerceTicketsList({ content: [{ numeroTicket: 'A' }] })).toHaveLength(1);
        expect(coerceTicketsList({ tickets: [{ numeroTicket: 'B' }] })).toHaveLength(1);
        expect(coerceTicketsList({ items: [{ numeroTicket: 'C' }] })).toHaveLength(1);
    });

    it('retorna vazio para payload inválido', () => {
        expect(coerceTicketsList(null)).toEqual([]);
        expect(coerceTicketsList({})).toEqual([]);
    });
});
