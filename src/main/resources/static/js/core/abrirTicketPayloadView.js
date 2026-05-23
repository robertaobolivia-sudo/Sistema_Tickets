/**
 * Sprint F3: montagem do payload de abertura manual (testável sem DOM).
 */

export const CONTATO_SOURCE_WHATSAPP = 'contato';
export const CONTATO_SOURCE_SOLICITANTE_LEGADO = 'solicitante';

/** Sprint F4: UI com Contatos reais — legado fica fora do select principal. */
export const ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY = 'whatsapp_primary';
export const ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY = 'legado_only';

export function resolveAbrirTicketContatoUiMode(contatosReaisAtivosCount) {
    return contatosReaisAtivosCount > 0
        ? ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY
        : ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY;
}

/** Legado no mesmo select só quando não há Contato real (F4). */
export function shouldInlineLegadoContatoClienteOptions(uiMode) {
    return uiMode === ABIR_TICKET_CONTATO_MODE_LEGADO_ONLY;
}

export function getAbrirTicketContatoLabel(uiMode) {
    if (uiMode === ABIR_TICKET_CONTATO_MODE_WHATSAPP_PRIMARY) {
        return 'Contato atendido (WhatsApp) — recomendado';
    }
    return 'Contato solicitante (legado, opcional)';
}

export const ABIR_TICKET_LEGADO_PANEL_SUMMARY =
    'Usar solicitante de cadastro legado (ContatoCliente) — somente se necessário';

export function formatContatoWhatsappLabel(contato) {
    const nome = contato?.nome && String(contato.nome).trim() ? contato.nome.trim() : 'Contato';
    const wa = contato?.whatsapp && String(contato.whatsapp).trim() ? contato.whatsapp.trim() : '-';
    return `${nome} - ${wa}`;
}

export function formatContatoClienteLegadoLabel(contato) {
    const telefone = contato?.telefone && contato.telefone !== '-' ? contato.telefone : '';
    const celular = contato?.celular && contato.celular !== '-' ? contato.celular : '';
    const fone =
        telefone && celular ? `${telefone} / ${celular}` : telefone || celular || '-';
    const nome = contato?.nome && String(contato.nome).trim() ? contato.nome.trim() : 'Contato';
    return `${nome} - ${fone}`;
}

/**
 * @param {object} basePayload
 * @param {{ source: string, id: string|number, whatsapp?: string, nome?: string }|null} selection
 */
export function applyContatoSelectionToPayload(basePayload, selection) {
    const payload = { ...basePayload };
    if (!selection?.id) {
        return payload;
    }
    const id = Number(selection.id);
    if (!Number.isFinite(id) || id <= 0) {
        return payload;
    }
    if (selection.source === CONTATO_SOURCE_WHATSAPP) {
        payload.contatoWhatsappId = id;
        delete payload.contatoSolicitanteId;
        if (selection.whatsapp) {
            payload.telefone = selection.whatsapp;
        }
        if (selection.nome) {
            payload.nomeContato = selection.nome;
        }
        return payload;
    }
    if (selection.source === CONTATO_SOURCE_SOLICITANTE_LEGADO) {
        payload.contatoSolicitanteId = id;
        delete payload.contatoWhatsappId;
        return payload;
    }
    return payload;
}

export function filtrarContatosWhatsappAtivos(contatos) {
    if (!Array.isArray(contatos)) {
        return [];
    }
    return contatos.filter(c => c && c.id != null && c.ativo !== false);
}

export function buildAbrirTicketPayloadFromForm(cliente, formValues, contatoSelection) {
    const base = {
        cliente: cliente.nome,
        clienteContratanteId: cliente.id,
        telefone: cliente.telefone,
        canal: formValues.canal || '',
        conexao: formValues.conexao || cliente.carteira || '',
        mensagem: formValues.mensagem || '',
        prioridade: formValues.prioridade || 'MEDIA'
    };
    return applyContatoSelectionToPayload(base, contatoSelection);
}
