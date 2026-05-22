/** Sprint 179+ — textos do formulário Clientes (contratante F5, Sprint 188). */

export const CLIENTE_FORM_MSG_EMPTY =
    'Selecione um contratante na lista à esquerda ou use Novo para cadastrar um cliente da F5.';

export const CLIENTE_FORM_MSG_ARTE_HINT =
    'Recomendado: imagem horizontal 1200 x 240 px, proporção 5:1.';

export const CLIENTE_FORM_MSG_ARTE_VAZIO = 'Nenhuma arte cadastrada.';

export const CLIENTE_ARTE_URL_PREFIX = '/uploads/clientes/header-chats/';

export function sanitizeClienteArteHeaderPublicUrl(url) {
    if (url == null || url === '' || url === '-') {
        return null;
    }
    const s = String(url).trim();
    if (!s.startsWith(CLIENTE_ARTE_URL_PREFIX) || s.includes('..')) {
        return null;
    }
    return s;
}

/** @param {string|null|undefined} arteUrl */
export function clienteArtePreviewTemImagem(arteUrl) {
    return Boolean(sanitizeClienteArteHeaderPublicUrl(arteUrl));
}

/**
 * @param {'idle'|'novo'|'edit'} mode
 * @param {string} [nome]
 */
export function getClienteFormHeaderTitle(mode, nome = '') {
    if (mode === 'novo') {
        return 'Novo cliente contratante';
    }
    if (mode === 'edit') {
        return 'Editando cliente contratante';
    }
    return 'Cadastro de cliente contratante';
}

/**
 * @param {'idle'|'novo'|'edit'} mode
 * @param {string} [nome]
 * @returns {string|null}
 */
/** Termos que não devem aparecer como rótulo visível na página Clientes (Sprint 182). */
export const CLIENTES_UI_TERMOS_LEGADO_PROIBIDOS = ['Carteira', 'Revenda', 'Conexão', 'Subcliente'];

/**
 * Garante que o trecho HTML da página Clientes não expõe rótulo legado (ex.: Carteira).
 * Seção de formulário não deve usar o rótulo "Contato" (reservado ao Contato WhatsApp); usar Comunicação.
 * @param {string} pageClientesHtml
 */
export function assertClientesPageSemNomenclaturaLegadaVisivel(pageClientesHtml) {
    const html = pageClientesHtml == null ? '' : String(pageClientesHtml);
    if (/<label[^>]*\sfor=["']carteiraCliente["']/i.test(html)) {
        throw new Error('Rótulo Carteira não deve aparecer na tela Clientes.');
    }
    for (const termo of CLIENTES_UI_TERMOS_LEGADO_PROIBIDOS) {
        const re = new RegExp(`<label[^>]*>[^<]*${termo}`, 'i');
        if (re.test(html)) {
            throw new Error(`Nomenclatura legada "${termo}" não deve aparecer na tela Clientes.`);
        }
    }
}

/** Valor de carteira legado enviado no payload (preserva dado ao editar). */
export function normalizeClienteCarteiraLegadoParaPayload(valor) {
    if (valor == null || valor === '' || valor === '-') {
        return '';
    }
    return String(valor).trim();
}

export function getClienteFormHeaderSubtitle(mode, nome = '') {
    if (mode !== 'edit') {
        return null;
    }
    const n = nome == null ? '' : String(nome).trim();
    return n || null;
}
