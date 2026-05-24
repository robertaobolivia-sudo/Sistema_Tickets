/**
 * Validação pura do payload de encerramento de ticket.
 */

/** Ticket sem Contato WhatsApp vinculado não pode optar por enviar pesquisa na UI. */
export function deveDesabilitarPesquisaSimEncerramento(contatoId) {
    return contatoId == null || contatoId === '';
}

export function validateEncerramentoPayload(payload) {
    const grupoId = payload?.grupoId != null ? Number(payload.grupoId) : null;
    const subgrupoId = payload?.subgrupoId != null ? Number(payload.subgrupoId) : null;
    const motivoId = payload?.motivoId != null ? Number(payload.motivoId) : null;
    const comentario = payload?.comentarioEncerramento != null
        ? String(payload.comentarioEncerramento).trim()
        : '';

    if (!grupoId || !Number.isFinite(grupoId)) {
        return { ok: false, message: 'Selecione a categoria.' };
    }
    if (!subgrupoId || !Number.isFinite(subgrupoId)) {
        return { ok: false, message: 'Selecione a subcategoria.' };
    }
    if (!motivoId || !Number.isFinite(motivoId)) {
        return { ok: false, message: 'Selecione o motivo.' };
    }
    if (!comentario) {
        return { ok: false, message: 'Informe o comentário de encerramento.' };
    }
    const enviarRaw = payload?.enviarPesquisaSatisfacao;
    const enviarPesquisaSatisfacao = enviarRaw === true
        || enviarRaw === 'true'
        || enviarRaw === 'on';
    return {
        ok: true,
        grupoId,
        subgrupoId,
        motivoId,
        comentarioEncerramento: comentario,
        enviarPesquisaSatisfacao
    };
}
