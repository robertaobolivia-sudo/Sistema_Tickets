/** Validação de arquivo da arte do header Chats (cadastro Conexão/Revenda). */

const TIPOS_ARTE_HEADER = ['image/png', 'image/jpeg', 'image/jpg', 'image/webp'];
const MAX_ARTE_BYTES = 5 * 1024 * 1024;

export function isArteHeaderChatsMimePermitido(type) {
    if (!type) {
        return false;
    }
    return TIPOS_ARTE_HEADER.includes(String(type).toLowerCase());
}

export function validarArteHeaderChatsArquivo(file) {
    if (!file) {
        return { ok: false, message: 'Selecione uma imagem.' };
    }
    if (!isArteHeaderChatsMimePermitido(file.type)) {
        return { ok: false, message: 'Use imagem PNG, JPG/JPEG ou WEBP.' };
    }
    if (file.size > MAX_ARTE_BYTES) {
        return { ok: false, message: 'A imagem deve ter no máximo 5 MB.' };
    }
    return { ok: true, message: '' };
}
