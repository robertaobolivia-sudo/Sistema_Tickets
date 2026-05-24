const API_BASE = '/api/public/avaliacoes';

export async function fetchAvaliacaoPublica(token) {
    const response = await fetch(`${API_BASE}/${encodeURIComponent(token)}`);
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const msg = data?.message || data?.error || 'Link inválido ou indisponível.';
        throw new Error(typeof msg === 'string' ? msg : 'Link inválido ou indisponível.');
    }
    return data;
}

export async function responderAvaliacaoPublica(token, payload) {
    const response = await fetch(`${API_BASE}/${encodeURIComponent(token)}/responder`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const msg = data?.message || data?.error || 'Não foi possível enviar sua avaliação.';
        throw new Error(typeof msg === 'string' ? msg : 'Não foi possível enviar sua avaliação.');
    }
    return data;
}
