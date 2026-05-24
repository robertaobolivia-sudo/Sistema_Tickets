/**
 * Helpers puros para gestão de etiquetas (Configurações).
 */
import { sanitizeEtiquetaCor } from '@features/chats/chats-view.js';

export function formatEtiquetaStatusLabel(ativo) {
    return ativo === true ? 'Ativa' : 'Inativa';
}

export function formatEtiquetaStatusClass(ativo) {
    return ativo === true ? 'etiqueta-status-ativa' : 'etiqueta-status-inativa';
}

/** Valida payload de cadastro/edição. */
export function validateEtiquetaFormPayload({ nome, descricao, cor }) {
    const n = nome == null ? '' : String(nome).trim();
    if (!n) {
        return { ok: false, message: 'Informe o nome da etiqueta.' };
    }
    if (n.length > 80) {
        return { ok: false, message: 'O nome pode ter no máximo 80 caracteres.' };
    }
    const desc = descricao == null ? '' : String(descricao).trim();
    if (desc.length > 255) {
        return { ok: false, message: 'A descrição pode ter no máximo 255 caracteres.' };
    }
    const corNorm = sanitizeEtiquetaCor(cor);
    if (cor != null && String(cor).trim() !== '' && !corNorm) {
        return { ok: false, message: 'Use uma cor em formato hexadecimal (#RRGGBB).' };
    }
    return {
        ok: true,
        payload: {
            nome: n,
            descricao: desc || null,
            cor: corNorm || null
        }
    };
}

export function etiquetaCorParaExibicao(cor) {
    const c = sanitizeEtiquetaCor(cor);
    return c || '#94a3b8';
}
