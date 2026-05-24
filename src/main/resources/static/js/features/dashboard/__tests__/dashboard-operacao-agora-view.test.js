import { describe, expect, it } from 'vitest';
import { renderOperacaoAgoraCard } from '@features/dashboard/dashboard-operacao-agora-view.js';

describe('dashboardOperacaoAgoraView', () => {
    it('preenche quantidade e TMA', () => {
        const qtd = { textContent: '' };
        const tempo = { textContent: '' };
        const rotulo = { textContent: '' };
        renderOperacaoAgoraCard(
            { quantidadeEl: qtd, tempoEl: tempo, rotuloEl: rotulo },
            { quantidade: 3, tempoMedioFormatado: '00:15:30', tempoMedioRotulo: 'TMA' }
        );
        expect(qtd.textContent).toBe('3');
        expect(tempo.textContent).toBe('00:15:30');
        expect(rotulo.textContent).toBe('TMA');
    });
});
