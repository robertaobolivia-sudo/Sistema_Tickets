/**
 * Regras puras — classificar ticket como indevido (Sprint 276).
 */

export const MOTIVOS_OPERACIONAIS_CLASSIFICACAO = [
    { value: 'INDEVIDO', label: 'Indevido' },
    { value: 'CONTATO_PESSOAL', label: 'Contato Pessoal' },
    { value: 'PROPAGANDA', label: 'Propaganda' }
];

export function buildClassificarIndevidoPayload({ confirmacao, motivoOperacional, comentario }) {
    if (!confirmacao) {
        throw new Error('Confirme a classificação como indevido.');
    }
    const motivo = String(motivoOperacional ?? '').trim().toUpperCase();
    if (!motivo) {
        throw new Error('Selecione o motivo operacional.');
    }
    const allowed = MOTIVOS_OPERACIONAIS_CLASSIFICACAO.map(m => m.value);
    if (!allowed.includes(motivo)) {
        throw new Error('Motivo operacional inválido.');
    }
    const payload = {
        confirmacao: true,
        motivoOperacional: motivo
    };
    const comentarioTrim = String(comentario ?? '').trim();
    if (comentarioTrim) {
        payload.comentario = comentarioTrim;
    }
    return payload;
}
