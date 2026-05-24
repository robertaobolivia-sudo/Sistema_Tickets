import { describe, expect, it } from 'vitest';
import { buildContatoUpdatePayload } from '@features/contatos/contato-gestao-edit-view.js';

describe('contatoGestaoEditView', () => {
    it('monta payload de atualização com whatsapp original', () => {
        const payload = buildContatoUpdatePayload({
            nome: ' Maria ',
            email: 'a@b.com',
            empresaLocal: 'Loja',
            cidade: 'SP',
            uf: 'sp',
            observacoes: ' obs ',
            whatsapp: '5511999990001'
        });
        expect(payload.nome).toBe('Maria');
        expect(payload.uf).toBe('sp');
        expect(payload.whatsapp).toBe('5511999990001');
        expect(payload.observacoes).toBe('obs');
    });

    it('exige nome', () => {
        expect(() => buildContatoUpdatePayload({ nome: '  ' })).toThrow(/nome/i);
    });

    it('valida UF', () => {
        expect(() => buildContatoUpdatePayload({ nome: 'X', uf: 'SPP' })).toThrow(/UF/i);
    });
});
