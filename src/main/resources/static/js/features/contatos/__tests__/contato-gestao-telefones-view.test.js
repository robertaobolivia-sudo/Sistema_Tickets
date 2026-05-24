import { describe, expect, it } from 'vitest';
import {
    buildChatsListTelefonesBadge,
    enrichChatsPanelContatoRows,
    mapOrigemSelectParaApi,
    rotuloOrigemTelefone,
    validarTelefoneAdicionalInformado
} from '@features/contatos/contato-gestao-telefones-view.js';

describe('contatoGestaoTelefonesView', () => {
    it('mapeia origem do select para API', () => {
        expect(mapOrigemSelectParaApi('CADASTRO_MANUAL')).toBe('CADASTRO_MANUAL');
        expect(mapOrigemSelectParaApi('UNIFICACAO')).toBe('ADICIONAL');
        expect(mapOrigemSelectParaApi('ADICIONAL')).toBe('ADICIONAL');
    });

    it('rotula origem vinda da API', () => {
        expect(rotuloOrigemTelefone('CADASTRO_MANUAL')).toBe('Cadastro manual');
        expect(rotuloOrigemTelefone('ADICIONAL')).toBe('Informado pelo cliente');
    });

    it('valida telefone informado', () => {
        expect(() => validarTelefoneAdicionalInformado('')).toThrow(/Informe o número/);
        expect(() => validarTelefoneAdicionalInformado('abc')).toThrow(/válido/);
        expect(() => validarTelefoneAdicionalInformado('(11) 98888-7777')).not.toThrow();
    });

    it('enriquece painel Chats com principal e adicionais', () => {
        const rows = enrichChatsPanelContatoRows(
            [{ label: 'WhatsApp', value: '5511111111111' }],
            [{ telefone: '(22) 22222-2222', origem: 'CADASTRO_MANUAL' }]
        );
        expect(rows[0].label).toBe('WhatsApp (principal)');
        expect(rows.some(r => r.label === 'Telefone adicional')).toBe(true);
    });

    it('badge da lista vazio sem adicionais', () => {
        expect(buildChatsListTelefonesBadge(0)).toBe('');
        expect(buildChatsListTelefonesBadge(2)).toContain('tel. adicionais');
    });
});
