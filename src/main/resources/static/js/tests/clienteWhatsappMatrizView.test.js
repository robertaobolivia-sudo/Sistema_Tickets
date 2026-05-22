import { describe, expect, it } from 'vitest';
import {
    CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST,
    formatWhatsappMatrizStatusLabel,
    mapWhatsappMatrizApiError,
    validateWhatsappMatrizForm
} from '../core/clienteWhatsappMatrizView.js';

describe('clienteWhatsappMatrizView', () => {
    it('valida número obrigatório', () => {
        expect(validateWhatsappMatrizForm('')).toBeTruthy();
        expect(validateWhatsappMatrizForm('(11) 98888-7777')).toBeNull();
    });

    it('status ativo/inativo', () => {
        expect(formatWhatsappMatrizStatusLabel(true)).toBe('Ativo');
        expect(formatWhatsappMatrizStatusLabel(false)).toBe('Inativo');
    });

    it('mapeia erro de duplicidade', () => {
        expect(mapWhatsappMatrizApiError('Ja existe WhatsApp matriz com este numero.')).toContain(
            'já está cadastrado'
        );
    });

    it('mensagem salvar cliente primeiro', () => {
        expect(CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST).toContain('Salve o cliente');
    });
});
