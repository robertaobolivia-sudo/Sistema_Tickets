/**
 * Sprint 289 — telefones adicionais no modal Ver/Editar Contato.
 */

export const CONTATO_TELEFONE_ORIGEM_OPCOES = [
    { value: 'ADICIONAL', label: 'Informado pelo cliente' },
    { value: 'CADASTRO_MANUAL', label: 'Cadastro manual' },
    { value: 'UNIFICACAO', label: 'Unificação' }
];

/**
 * @param {string} selectValue
 * @returns {string|undefined}
 */
export function mapOrigemSelectParaApi(selectValue) {
    const v = String(selectValue ?? '').trim();
    if (v === 'CADASTRO_MANUAL') {
        return 'CADASTRO_MANUAL';
    }
    if (v === 'UNIFICACAO' || v === 'ADICIONAL') {
        return 'ADICIONAL';
    }
    return v ? 'ADICIONAL' : undefined;
}

/**
 * @param {string|null|undefined} origemApi
 * @returns {string}
 */
export function rotuloOrigemTelefone(origemApi) {
    const o = String(origemApi ?? '').trim().toUpperCase();
    if (o === 'CADASTRO_MANUAL') {
        return 'Cadastro manual';
    }
    if (o === 'UNIFICACAO') {
        return 'Unificação';
    }
    return 'Informado pelo cliente';
}

/**
 * @param {string} telefone
 */
export function validarTelefoneAdicionalInformado(telefone) {
    const bruto = String(telefone ?? '').trim();
    if (!bruto) {
        throw new Error('Informe o número do telefone adicional.');
    }
    const digitos = bruto.replace(/\D/g, '');
    if (!digitos || digitos.length < 8) {
        throw new Error('Informe um telefone válido (mínimo 8 dígitos).');
    }
}

/**
 * @param {HTMLElement|null} listaEl
 * @param {HTMLElement|null} emptyEl
 * @param {Array<{ telefone?: string, telefoneNormalizado?: string, origem?: string }>} itens
 */
/**
 * @param {Array<{ label?: string, value?: unknown, text?: string, html?: string }>} baseRows
 * @param {Array<{ telefone?: string, telefoneNormalizado?: string, origem?: string }>} telefonesAdicionais
 */
export function enrichChatsPanelContatoRows(baseRows, telefonesAdicionais) {
    const rows = Array.isArray(baseRows) ? [...baseRows] : [];
    rows.forEach(row => {
        if (row?.label === 'WhatsApp') {
            row.label = 'WhatsApp (principal)';
        }
    });
    const extras = Array.isArray(telefonesAdicionais) ? telefonesAdicionais : [];
    extras.forEach((item, index) => {
        const numero = item?.telefone || item?.telefoneNormalizado || '—';
        const origem = rotuloOrigemTelefone(item?.origem);
        const label =
            extras.length === 1 ? 'Telefone adicional' : `Telefone adicional ${index + 1}`;
        rows.push({
            label,
            value: numero,
            html: `<span class="chats-panel-telefone-adicional">${escapeTelefoneDisplayHtml(
                numero
            )}</span> <span class="chats-panel-telefone-origem-hint">(${escapeTelefoneDisplayHtml(
                origem
            )})</span>`
        });
    });
    return rows;
}

function escapeTelefoneDisplayHtml(value) {
    return String(value ?? '—')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/**
 * @param {number} count
 * @returns {string}
 */
export function buildChatsListTelefonesBadge(count) {
    const n = Number(count);
    if (!Number.isFinite(n) || n < 1) {
        return '';
    }
    const label = n === 1 ? '1 tel. adicional' : `${n} tel. adicionais`;
    return `<span class="chats-list-item-tel-extra" title="Contato com telefones adicionais">${escapeTelefoneDisplayHtml(
        label
    )}</span>`;
}

/**
 * Bloco somente leitura para histórico do contato (Clientes → Contatos).
 * @param {string|null|undefined} whatsappPrincipal
 * @param {Array<{ telefone?: string, origem?: string }>} telefonesAdicionais
 */
export function buildHistoricoTelefonesReadonlyHtml(whatsappPrincipal, telefonesAdicionais) {
    const extras = Array.isArray(telefonesAdicionais) ? telefonesAdicionais : [];
    const parts = [];
    const principal = String(whatsappPrincipal ?? '').trim();
    if (principal) {
        parts.push(
            `<p class="contato-historico-telefone-line"><span class="contato-historico-telefone-label">WhatsApp principal</span> <span class="contato-historico-telefone-valor">${escapeTelefoneDisplayHtml(
                principal
            )}</span></p>`
        );
    }
    if (extras.length) {
        const itens = extras
            .map(
                item => `<li><span class="contato-historico-telefone-valor">${escapeTelefoneDisplayHtml(
                    item.telefone || item.telefoneNormalizado || '—'
                )}</span> <span class="contato-historico-telefone-origem">(${escapeTelefoneDisplayHtml(
                    rotuloOrigemTelefone(item.origem)
                )})</span></li>`
            )
            .join('');
        parts.push(
            `<p class="contato-historico-telefone-label-block">Telefones adicionais</p><ul class="contato-historico-telefones-adicionais">${itens}</ul>`
        );
    }
    if (!parts.length) {
        return '';
    }
    return `<div class="contato-historico-telefones-readonly">${parts.join('')}</div>`;
}

export function renderListaTelefonesAdicionais(listaEl, emptyEl, itens) {
    const lista = Array.isArray(itens) ? itens : [];
    if (emptyEl) {
        emptyEl.classList.toggle('hidden', lista.length > 0);
    }
    if (!listaEl) {
        return;
    }
    listaEl.innerHTML = '';
    if (!lista.length) {
        return;
    }
    lista.forEach(item => {
        const li = document.createElement('li');
        li.className = 'contato-gestao-telefone-item';
        const num = document.createElement('span');
        num.className = 'contato-gestao-telefone-numero';
        num.textContent = item.telefone || item.telefoneNormalizado || '—';
        const origem = document.createElement('span');
        origem.className = 'contato-gestao-telefone-origem';
        origem.textContent = rotuloOrigemTelefone(item.origem);
        li.appendChild(num);
        li.appendChild(origem);
        listaEl.appendChild(li);
    });
}
