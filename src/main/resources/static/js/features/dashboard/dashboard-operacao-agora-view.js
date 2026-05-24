/**
 * Sprint 280 — Operação Agora (Dashboard).
 */

export function renderOperacaoAgoraCard(cardEl, data) {
    if (!cardEl?.quantidadeEl) {
        return;
    }
    const qtd = data?.quantidade ?? 0;
    cardEl.quantidadeEl.textContent = String(qtd);
    if (cardEl.rotuloEl) {
        cardEl.rotuloEl.textContent = data?.tempoMedioRotulo || '—';
    }
    if (cardEl.tempoEl) {
        const tempo = data?.tempoMedioFormatado;
        cardEl.tempoEl.textContent =
            tempo === undefined || tempo === null || tempo === '' ? '—' : String(tempo);
    }
}
