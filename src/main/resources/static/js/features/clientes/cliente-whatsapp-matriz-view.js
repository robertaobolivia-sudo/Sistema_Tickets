/** Sprint 192 — WhatsApps Matriz na tela Clientes (funções puras). */

export const CLIENTE_WHATSAPP_MATRIZ_MSG_HINT =
    'Números do cliente conectados à API para entrada de atendimentos.';

export const CLIENTE_WHATSAPP_MATRIZ_MSG_EMPTY =
    'Nenhum WhatsApp matriz cadastrado para este cliente.';

export const CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST =
    'Salve o cliente antes de cadastrar WhatsApps Matriz.';

export function formatWhatsappMatrizStatusLabel(ativo) {
    return ativo === false ? 'Inativo' : 'Ativo';
}

export function formatWhatsappMatrizStatusClass(ativo) {
    return ativo === false ? 'clientes-matriz-status-inativo' : 'clientes-matriz-status-ativo';
}

export function buildWhatsappMatrizPayloadFromForm(form) {
    return {
        clienteId: form.clienteId,
        nome: form.nome?.trim() || '',
        numero: form.numero?.trim() || '',
        provedor: form.provedor?.trim() || '',
        identificadorExterno: form.identificadorExterno?.trim() || ''
    };
}

export function validateWhatsappMatrizForm(numero) {
    const n = numero == null ? '' : String(numero).trim();
    if (!n) {
        return 'Informe o número do WhatsApp matriz.';
    }
    const digits = n.replace(/\D/g, '');
    if (!digits) {
        return 'Número inválido. Use apenas dígitos.';
    }
    return null;
}

export function mapWhatsappMatrizApiError(message) {
    const raw = message == null ? '' : String(message);
    if (/ja existe whatsapp matriz/i.test(raw) || /numero/i.test(raw) && /duplic/i.test(raw)) {
        return 'Este número já está cadastrado para outro cliente ou para este cliente.';
    }
    return raw || 'Não foi possível salvar o WhatsApp matriz.';
}
