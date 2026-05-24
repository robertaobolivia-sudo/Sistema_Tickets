import { describe, expect, it } from 'vitest';
import {
    formatEtiquetaStatusLabel,
    validateEtiquetaFormPayload,
    etiquetaCorParaExibicao
} from '@features/configuracoes/etiquetas-gestao-view.js';

describe('etiquetasGestaoView', () => {
    it('formata status ativa/inativa', () => {
        expect(formatEtiquetaStatusLabel(true)).toBe('Ativa');
        expect(formatEtiquetaStatusLabel(false)).toBe('Inativa');
    });

    it('valida nome obrigatório', () => {
        const r = validateEtiquetaFormPayload({ nome: '  ', descricao: '', cor: '' });
        expect(r.ok).toBe(false);
    });

    it('aceita payload válido e cor hex', () => {
        const r = validateEtiquetaFormPayload({
            nome: 'VIP',
            descricao: 'Clientes prioritários',
            cor: '#ff00aa'
        });
        expect(r.ok).toBe(true);
        expect(r.payload.nome).toBe('VIP');
        expect(r.payload.cor).toBe('#ff00aa');
    });

    it('cor inválida rejeita', () => {
        const r = validateEtiquetaFormPayload({ nome: 'X', descricao: '', cor: 'vermelho' });
        expect(r.ok).toBe(false);
    });

    it('cor padrão quando vazia', () => {
        expect(etiquetaCorParaExibicao(null)).toBe('#94a3b8');
        expect(etiquetaCorParaExibicao('#112233')).toBe('#112233');
    });
});
