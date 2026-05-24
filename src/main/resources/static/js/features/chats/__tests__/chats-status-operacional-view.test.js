import { describe, it, expect } from 'vitest';
import {
    getChatsManualStatusOptions,
    buildChatsStatusUpdateBody
} from '@features/chats/chats-status-operacional-view.js';

describe('chatsStatusOperacionalView', () => {
    it('oferece início de atendimento e aguardando a partir de ABERTO', () => {
        const opts = getChatsManualStatusOptions({ numeroTicket: 'TK-1', status: 'ABERTO' });
        expect(opts.map(o => o.code)).toEqual(['EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE']);
    });

    it('não oferece transição manual para ticket resolvido', () => {
        expect(getChatsManualStatusOptions({ numeroTicket: 'TK-1', status: 'RESOLVIDO' })).toEqual([]);
    });

    it('inclui analistaId ao ir para EM_ATENDIMENTO', () => {
        expect(buildChatsStatusUpdateBody('EM_ATENDIMENTO', 7)).toEqual({
            status: 'EM_ATENDIMENTO',
            analistaId: 7
        });
        expect(buildChatsStatusUpdateBody('AGUARDANDO_CLIENTE', 7)).toEqual({
            status: 'AGUARDANDO_CLIENTE'
        });
    });
});
