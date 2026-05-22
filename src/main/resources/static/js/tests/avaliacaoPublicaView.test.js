import { describe, expect, it } from 'vitest';
import { resolveAvaliacaoPublicaUiState } from '../core/avaliacaoPublicaView.js';

describe('avaliacaoPublicaView', () => {
    it('exibe formulário para PENDENTE', () => {
        const ui = resolveAvaliacaoPublicaUiState({ status: 'PENDENTE' });
        expect(ui.mostrarFormulario).toBe(true);
        expect(ui.mostrarEstadoFinal).toBe(false);
        expect(ui.estadoVariant).toBe('pendente');
    });

    it('exibe obrigado quando respondida', () => {
        const ui = resolveAvaliacaoPublicaUiState({ jaRespondida: true, status: 'RESPONDIDA' });
        expect(ui.mostrarFormulario).toBe(false);
        expect(ui.textoEstadoFinal).toContain('Obrigado');
        expect(ui.estadoVariant).toBe('respondida');
    });

    it('exibe expirada', () => {
        const ui = resolveAvaliacaoPublicaUiState({ expirada: true, status: 'PENDENTE' });
        expect(ui.mostrarFormulario).toBe(false);
        expect(ui.textoEstadoFinal).toContain('prazo');
        expect(ui.estadoVariant).toBe('expirada');
    });

    it('link inválido usa variant invalida', () => {
        const ui = resolveAvaliacaoPublicaUiState({ status: 'FOO' });
        expect(ui.estadoVariant).toBe('invalida');
    });
});
