import { describe, it, expect } from 'vitest';
import { isArteHeaderChatsMimePermitido, validarArteHeaderChatsArquivo } from '../core/carteiraArteView.js';

describe('carteiraArteView', () => {
    it('aceita png, jpeg e webp', () => {
        expect(isArteHeaderChatsMimePermitido('image/png')).toBe(true);
        expect(isArteHeaderChatsMimePermitido('image/jpeg')).toBe(true);
        expect(isArteHeaderChatsMimePermitido('image/webp')).toBe(true);
        expect(isArteHeaderChatsMimePermitido('application/pdf')).toBe(false);
    });

    it('validarArteHeaderChatsArquivo rejeita tipo inválido', () => {
        const file = { type: 'text/plain', size: 100 };
        expect(validarArteHeaderChatsArquivo(file).ok).toBe(false);
    });

    it('validarArteHeaderChatsArquivo aceita arquivo pequeno válido', () => {
        const file = { type: 'image/png', size: 1024 };
        expect(validarArteHeaderChatsArquivo(file).ok).toBe(true);
    });
});
