import { describe, expect, it } from 'vitest';
import {
    CLIENTES_LISTA_MSG_BUSCA,
    CLIENTES_LISTA_MSG_VAZIO,
    filterClientesByTermo,
    formatClienteListContactLine,
    formatClienteListLocationLine,
    getClientesListaEmptyMessage
} from '@features/clientes/cliente-list-view.js';

describe('clienteListView', () => {
    it('formata contato e localização', () => {
        expect(
            formatClienteListContactLine({
                telefone: '(11) 99999-0000',
                email: 'a@b.com'
            })
        ).toBe('(11) 99999-0000 · a@b.com');
        expect(formatClienteListLocationLine({ cidade: 'São Paulo', uf: 'SP' })).toBe('São Paulo/SP');
    });

    it('mensagens de lista vazia', () => {
        expect(getClientesListaEmptyMessage('', false)).toBe(CLIENTES_LISTA_MSG_VAZIO);
        expect(getClientesListaEmptyMessage('xyz', false)).toBe(CLIENTES_LISTA_MSG_BUSCA);
        expect(getClientesListaEmptyMessage('x', true)).toBeNull();
    });

    it('filterClientesByTermo', () => {
        const list = [
            { nome: 'Maria', empresa: 'Acme', email: 'm@a.com' },
            { nome: 'João', cidade: 'Campinas', uf: 'SP' }
        ];
        expect(filterClientesByTermo(list, 'acme')).toHaveLength(1);
        expect(filterClientesByTermo(list, 'campinas')).toHaveLength(1);
        expect(filterClientesByTermo(list, '')).toHaveLength(2);
    });
});
