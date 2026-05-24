/**
 * Abertura manual: Cliente contratante + Contato atendido (WhatsApp).
 */

export const MSG_CADASTRE_CONTATO_WHATSAPP =
    'Cadastre um Contato com WhatsApp para abrir ticket deste Cliente.';

export const MSG_SEM_CONTATO_ORIENTACAO =
    'Este Cliente ainda não possui Contato com WhatsApp. Cadastre um Contato para abrir o ticket.';

export function shouldShowCadastrarContatoLink(contatosReaisAtivosCount, clienteId) {
    const id = Number(clienteId);
    return contatosReaisAtivosCount <= 0 && Number.isFinite(id) && id > 0;
}

export function getAbrirTicketContatoLabel(contatosReaisAtivosCount) {
    if (contatosReaisAtivosCount > 0) {
        return 'Contato atendido (WhatsApp) — obrigatório';
    }
    return 'Contato atendido (WhatsApp)';
}

export function getAbrirTicketSemContatosOrientacao() {
    return MSG_SEM_CONTATO_ORIENTACAO;
}

export function validateAbrirTicketSubmit(contatosReaisAtivosCount, contatoWhatsappId) {
    if (contatosReaisAtivosCount <= 0) {
        return MSG_CADASTRE_CONTATO_WHATSAPP;
    }
    const id = Number(contatoWhatsappId);
    if (!Number.isFinite(id) || id <= 0) {
        return 'Selecione o Contato atendido (WhatsApp) para abrir o ticket.';
    }
    return null;
}

export function formatContatoWhatsappLabel(contato) {
    const nome = contato?.nome && String(contato.nome).trim() ? contato.nome.trim() : 'Contato';
    const wa = contato?.whatsapp && String(contato.whatsapp).trim() ? contato.whatsapp.trim() : '-';
    return `${nome} - ${wa}`;
}

export function filtrarContatosWhatsappAtivos(contatos) {
    if (!Array.isArray(contatos)) {
        return [];
    }
    return contatos.filter(c => c && c.id != null && c.ativo !== false);
}

/**
 * @param {{ id: number|string, whatsapp?: string, nome?: string }|null} contato
 */
export function buildAbrirTicketPayloadFromForm(cliente, formValues, contato) {
    const payload = {
        cliente: cliente.nome,
        clienteContratanteId: cliente.id,
        canal: formValues.canal || '',
        mensagem: formValues.mensagem || '',
        prioridade: formValues.prioridade || 'MEDIA'
    };
    if (!contato?.id) {
        return payload;
    }
    const id = Number(contato.id);
    if (!Number.isFinite(id) || id <= 0) {
        return payload;
    }
    payload.contatoWhatsappId = id;
    if (contato.whatsapp) {
        payload.telefone = contato.whatsapp;
    }
    if (contato.nome) {
        payload.nomeContato = contato.nome;
    }
    return payload;
}
