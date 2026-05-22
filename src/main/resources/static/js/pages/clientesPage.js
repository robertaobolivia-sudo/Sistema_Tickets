import * as clienteService from '../services/clienteService.js';
import * as whatsappMatrizService from '../services/whatsappMatrizService.js';
import { normalizeClassificacaoCliente } from '../core/clienteView.js';
import {
    escapeClienteListHtml,
    formatClienteListContactLine,
    formatClienteListLocationLine,
    getClientesListaEmptyMessage
} from '../core/clienteListView.js';
import { validarArteHeaderChatsArquivo } from '../core/carteiraArteView.js';
import {
    CLIENTE_FORM_MSG_EMPTY,
    getClienteFormHeaderSubtitle,
    getClienteFormHeaderTitle,
    sanitizeClienteArteHeaderPublicUrl
} from '../core/clienteFormView.js';
import { createListPaginationController } from '../components/listPaginationBar.js';
import { slicePageItems } from '../core/listPagination.js';
import {
    CLIENTE_WHATSAPP_MATRIZ_MSG_EMPTY,
    CLIENTE_WHATSAPP_MATRIZ_MSG_HINT,
    CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST,
    buildWhatsappMatrizPayloadFromForm,
    formatWhatsappMatrizStatusClass,
    formatWhatsappMatrizStatusLabel,
    mapWhatsappMatrizApiError,
    validateWhatsappMatrizForm
} from '../core/clienteWhatsappMatrizView.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let onRefreshDashboardFn = () => {};

const clienteForm = document.getElementById('clienteForm');
const novoClienteBtn = document.getElementById('novoClienteBtn');
const clienteEditId = document.getElementById('clienteEditId');
const clienteListaBusca = document.getElementById('clienteListaBusca');
const clientesLista = document.getElementById('clientesLista');
const clientesListaPagination = document.getElementById('clientesListaPagination');
const clientesListaEmpty = document.getElementById('clientesListaEmpty');
const clientesListaErro = document.getElementById('clientesListaErro');
const clienteContatosSection = document.getElementById('clienteContatosSection');
const contatoClienteForm = document.getElementById('contatoClienteForm');
const contatoEditId = document.getElementById('contatoEditId');
const contatosClienteTableBody = document.getElementById('contatosClienteTableBody');
const contatosClienteEmpty = document.getElementById('contatosClienteEmpty');
const alertBoxContatos = document.getElementById('alertBoxContatos');
const contatoCancelarBtn = document.getElementById('contatoCancelarBtn');
const alertBoxClientes = document.getElementById('alertBoxClientes');
const clienteFormEmpty = document.getElementById('clienteFormEmpty');
const clienteFormWrap = document.getElementById('clienteFormWrap');
const clienteFormTitle = document.getElementById('clienteFormTitle');
const clienteFormSubtitle = document.getElementById('clienteFormSubtitle');
const clienteArteFile = document.getElementById('clienteArteFile');
const clienteIncluirArteBtn = document.getElementById('clienteIncluirArteBtn');
const clienteArtePreview = document.getElementById('clienteArtePreview');
const clienteArtePreviewImg = document.getElementById('clienteArtePreviewImg');
const clienteArteEmpty = document.getElementById('clienteArteEmpty');
const clienteArteUploadHint = document.getElementById('clienteArteUploadHint');
const clienteWhatsappMatrizSection = document.getElementById('clienteWhatsappMatrizSection');
const clienteWhatsappMatrizHint = document.getElementById('clienteWhatsappMatrizHint');
const clienteWhatsappMatrizSaveFirst = document.getElementById('clienteWhatsappMatrizSaveFirst');
const clienteWhatsappMatrizEmpty = document.getElementById('clienteWhatsappMatrizEmpty');
const clienteWhatsappMatrizList = document.getElementById('clienteWhatsappMatrizList');
const clienteWhatsappMatrizForm = document.getElementById('clienteWhatsappMatrizForm');
const clienteWhatsappMatrizEditId = document.getElementById('clienteWhatsappMatrizEditId');
const clienteWhatsappMatrizCancelarBtn = document.getElementById('clienteWhatsappMatrizCancelarBtn');
const alertBoxWhatsappMatriz = document.getElementById('alertBoxWhatsappMatriz');

let listenersBound = false;
let clienteArtePreviewObjectUrl = null;
let buscaClientesTimerId = null;
let selectedClienteListId = null;
let cachedClientesList = [];
let cachedClientesTermo = '';
let clientesListPage = 1;

const clientesListPagination = createListPaginationController(clientesListaPagination, {
    ariaLabel: 'Paginação da lista de clientes',
    onPageChange(page) {
        clientesListPage = page;
        renderClientesList(cachedClientesList, cachedClientesTermo);
    }
});

export function initClientesPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.onRefreshDashboard) onRefreshDashboardFn = deps.onRefreshDashboard;
    if (listenersBound) return;
    listenersBound = true;

    clienteForm?.addEventListener('submit', submitCliente);
    clienteForm?.addEventListener('reset', () => {
        window.setTimeout(() => {
            if (!getClienteEmEdicaoId()) {
                clienteContatosSection?.classList.add('hidden');
                if (clienteFormWrap && !clienteFormWrap.classList.contains('hidden')) {
                    setClienteFormUiState('novo');
                }
            }
        }, 0);
    });
    clienteListaBusca?.addEventListener('input', () => {
        if (buscaClientesTimerId) {
            clearTimeout(buscaClientesTimerId);
        }
        buscaClientesTimerId = setTimeout(() => {
            buscaClientesTimerId = null;
            loadClientesPage();
        }, 320);
    });
    clienteListaBusca?.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            if (buscaClientesTimerId) {
                clearTimeout(buscaClientesTimerId);
                buscaClientesTimerId = null;
            }
            loadClientesPage();
        }
    });
    contatoClienteForm?.addEventListener('submit', submitContatoCliente);
    contatoCancelarBtn?.addEventListener('click', resetContatoForm);
    contatosClienteTableBody?.addEventListener('click', event => {
        const button = event.target.closest('button[data-contato-acao]');
        if (!button) {
            return;
        }
        const id = button.dataset.id;
        const acao = button.dataset.contatoAcao;
        if (!id || !acao) {
            return;
        }
        if (acao === 'editar') {
            editarContatoNaPagina(id);
            return;
        }
        executarAcaoContato(id, acao);
    });
    clientesLista?.addEventListener('click', event => {
        const acaoBtn = event.target.closest('button[data-acao]');
        if (acaoBtn) {
            event.stopPropagation();
            const id = acaoBtn.dataset.id;
            const acao = acaoBtn.dataset.acao;
            if (id && (acao === 'ativar' || acao === 'inativar')) {
                alternarStatusCliente(id, acao);
            }
            return;
        }
        const wrap = event.target.closest('.clientes-list-item-wrap');
        if (!wrap?.dataset?.id) {
            return;
        }
        if (!event.target.closest('.clientes-list-item')) {
            return;
        }
        editarClienteNaPagina(wrap.dataset.id);
    });
    novoClienteBtn?.addEventListener('click', iniciarNovoCliente);
    clienteWhatsappMatrizForm?.addEventListener('submit', submitClienteWhatsappMatriz);
    clienteWhatsappMatrizCancelarBtn?.addEventListener('click', resetClienteWhatsappMatrizForm);
    clienteWhatsappMatrizList?.addEventListener('click', event => {
        const btn = event.target.closest('button[data-matriz-acao]');
        if (!btn) {
            return;
        }
        const id = btn.dataset.id;
        const acao = btn.dataset.matrizAcao;
        if (!id || !acao) {
            return;
        }
        if (acao === 'editar') {
            editarWhatsappMatrizNaPagina(id);
            return;
        }
        if (acao === 'ativar' || acao === 'inativar') {
            alternarStatusWhatsappMatriz(id, acao);
        }
    });
    clienteIncluirArteBtn?.addEventListener('click', () => clienteArteFile?.click());
    clienteArteFile?.addEventListener('change', onClienteArteFileSelected);
    setClienteFormUiState('idle');
    atualizarPreviewArteCliente(null);
}

/** @param {'idle'|'novo'|'edit'} mode @param {string} [nomeCliente] */
function setClienteFormUiState(mode, nomeCliente = '') {
    const showForm = mode === 'novo' || mode === 'edit';
    clienteFormEmpty?.classList.toggle('hidden', showForm);
    clienteFormWrap?.classList.toggle('hidden', !showForm);
    if (clienteFormEmpty && mode === 'idle') {
        clienteFormEmpty.textContent = CLIENTE_FORM_MSG_EMPTY;
    }
    if (clienteFormTitle) {
        clienteFormTitle.textContent = getClienteFormHeaderTitle(mode, nomeCliente);
    }
    const subtitle = getClienteFormHeaderSubtitle(mode, nomeCliente);
    if (clienteFormSubtitle) {
        if (subtitle) {
            clienteFormSubtitle.textContent = subtitle;
            clienteFormSubtitle.classList.remove('hidden');
        } else {
            clienteFormSubtitle.textContent = '';
            clienteFormSubtitle.classList.add('hidden');
        }
    }
    syncWhatsappMatrizSectionUi(mode);
}

function syncWhatsappMatrizSectionUi(mode) {
    const clienteId = getClienteEmEdicaoId();
    const showSection = mode === 'novo' || mode === 'edit';
    clienteWhatsappMatrizSection?.classList.toggle('hidden', !showSection);
    if (clienteWhatsappMatrizHint) {
        clienteWhatsappMatrizHint.textContent = CLIENTE_WHATSAPP_MATRIZ_MSG_HINT;
    }
    const needsSaveFirst = mode === 'novo' && !clienteId;
    clienteWhatsappMatrizSaveFirst?.classList.toggle('hidden', !needsSaveFirst);
    if (clienteWhatsappMatrizSaveFirst) {
        clienteWhatsappMatrizSaveFirst.textContent = CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST;
    }
    const canManage = mode === 'edit' && clienteId;
    clienteWhatsappMatrizForm?.classList.toggle('hidden', !canManage);
    clienteWhatsappMatrizList?.classList.toggle('hidden', !canManage);
    clienteWhatsappMatrizEmpty?.classList.add('hidden');
    if (!canManage) {
        resetClienteWhatsappMatrizForm();
        if (clienteWhatsappMatrizList) {
            clienteWhatsappMatrizList.innerHTML = '';
        }
    }
}

function resetClienteWhatsappMatrizForm() {
    clienteWhatsappMatrizForm?.reset();
    if (clienteWhatsappMatrizEditId) {
        clienteWhatsappMatrizEditId.value = '';
    }
    clienteWhatsappMatrizCancelarBtn?.classList.add('hidden');
    const numeroEl = document.getElementById('clienteWhatsappMatrizNumero');
    if (numeroEl) {
        numeroEl.readOnly = false;
    }
    clearAlertFn(alertBoxWhatsappMatriz);
}

function renderWhatsappMatrizList(matrizes) {
    if (!clienteWhatsappMatrizList) {
        return;
    }
    clienteWhatsappMatrizList.innerHTML = '';
    const items = Array.isArray(matrizes) ? matrizes : [];
    if (!items.length) {
        clienteWhatsappMatrizEmpty?.classList.remove('hidden');
        if (clienteWhatsappMatrizEmpty) {
            clienteWhatsappMatrizEmpty.textContent = CLIENTE_WHATSAPP_MATRIZ_MSG_EMPTY;
        }
        return;
    }
    clienteWhatsappMatrizEmpty?.classList.add('hidden');
    items.forEach(m => {
        const item = document.createElement('div');
        item.className = 'clientes-whatsapp-matriz-item';
        item.setAttribute('role', 'listitem');
        const nome = m.nome && m.nome !== '-' ? escapeClienteListHtml(m.nome) : 'WhatsApp matriz';
        const numero = escapeClienteListHtml(m.numero || '-');
        const statusLabel = formatWhatsappMatrizStatusLabel(m.ativo);
        const statusClass = formatWhatsappMatrizStatusClass(m.ativo);
        const meta = [m.provedor, m.identificadorExterno].filter(v => v && v !== '-').join(' · ');
        item.innerHTML = `
            <div class="clientes-whatsapp-matriz-item-main">
                <strong>${nome}</strong>
                <span>${numero}${meta ? ` · ${escapeClienteListHtml(meta)}` : ''}</span>
                <span class="${statusClass}">${statusLabel}</span>
            </div>
            <div class="clientes-whatsapp-matriz-item-actions">
                <button type="button" class="button button-secondary button-sm" data-matriz-acao="editar" data-id="${m.id}">Editar</button>
                ${
                    m.ativo === false
                        ? `<button type="button" class="button button-primary button-sm" data-matriz-acao="ativar" data-id="${m.id}">Ativar</button>`
                        : `<button type="button" class="button button-secondary button-sm" data-matriz-acao="inativar" data-id="${m.id}">Inativar</button>`
                }
            </div>
        `;
        clienteWhatsappMatrizList.appendChild(item);
    });
}

async function loadWhatsappMatrizesCliente(clienteId) {
    if (!clienteId) {
        renderWhatsappMatrizList([]);
        return;
    }
    try {
        const lista = await whatsappMatrizService.listByCliente(clienteId);
        renderWhatsappMatrizList(lista);
        clearAlertFn(alertBoxWhatsappMatriz);
    } catch (error) {
        renderWhatsappMatrizList([]);
        showAlertFn(mapWhatsappMatrizApiError(error.message), alertBoxWhatsappMatriz);
    }
}

async function submitClienteWhatsappMatriz(event) {
    event.preventDefault();
    const clienteId = getClienteEmEdicaoId();
    if (!clienteId) {
        showAlertFn(CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST, alertBoxWhatsappMatriz);
        return;
    }
    const numero = document.getElementById('clienteWhatsappMatrizNumero')?.value;
    const validation = validateWhatsappMatrizForm(numero);
    if (validation) {
        showAlertFn(validation, alertBoxWhatsappMatriz);
        return;
    }
    const payload = buildWhatsappMatrizPayloadFromForm({
        clienteId: Number(clienteId),
        nome: document.getElementById('clienteWhatsappMatrizNome')?.value,
        numero,
        provedor: document.getElementById('clienteWhatsappMatrizProvedor')?.value,
        identificadorExterno: document.getElementById('clienteWhatsappMatrizIdExterno')?.value
    });
    const editId = clienteWhatsappMatrizEditId?.value?.trim();
    try {
        if (editId) {
            await whatsappMatrizService.update(editId, payload);
            showAlertFn('WhatsApp matriz atualizado.', alertBoxWhatsappMatriz, 'success');
        } else {
            await whatsappMatrizService.create(payload);
            showAlertFn('WhatsApp matriz cadastrado.', alertBoxWhatsappMatriz, 'success');
        }
        resetClienteWhatsappMatrizForm();
        await loadWhatsappMatrizesCliente(clienteId);
    } catch (error) {
        showAlertFn(mapWhatsappMatrizApiError(error.message), alertBoxWhatsappMatriz);
    }
}

async function editarWhatsappMatrizNaPagina(matrizId) {
    try {
        const m = await whatsappMatrizService.getById(matrizId);
        if (clienteWhatsappMatrizEditId) {
            clienteWhatsappMatrizEditId.value = m.id ?? '';
        }
        document.getElementById('clienteWhatsappMatrizNome').value =
            m.nome && m.nome !== '-' ? m.nome : '';
        document.getElementById('clienteWhatsappMatrizNumero').value = m.numero || '';
        document.getElementById('clienteWhatsappMatrizProvedor').value =
            m.provedor && m.provedor !== '-' ? m.provedor : '';
        document.getElementById('clienteWhatsappMatrizIdExterno').value =
            m.identificadorExterno && m.identificadorExterno !== '-' ? m.identificadorExterno : '';
        clienteWhatsappMatrizCancelarBtn?.classList.remove('hidden');
        clearAlertFn(alertBoxWhatsappMatriz);
    } catch (error) {
        showAlertFn(mapWhatsappMatrizApiError(error.message), alertBoxWhatsappMatriz);
    }
}

async function alternarStatusWhatsappMatriz(matrizId, acao) {
    const clienteId = getClienteEmEdicaoId();
    try {
        if (acao === 'ativar') {
            await whatsappMatrizService.patchAtivar(matrizId);
        } else {
            await whatsappMatrizService.patchInativar(matrizId);
        }
        if (clienteId) {
            await loadWhatsappMatrizesCliente(clienteId);
        }
        showAlertFn(
            acao === 'ativar' ? 'WhatsApp matriz ativado.' : 'WhatsApp matriz inativado.',
            alertBoxWhatsappMatriz,
            'success'
        );
    } catch (error) {
        showAlertFn(mapWhatsappMatrizApiError(error.message), alertBoxWhatsappMatriz);
    }
}

function iniciarNovoCliente() {
    resetClienteForm({ keepFormVisible: true });
    setClienteFormUiState('novo');
    atualizarPreviewArteCliente(null);
    document.getElementById('nomeCliente')?.focus();
}

function liberarClienteArtePreviewObjectUrl() {
    if (clienteArtePreviewObjectUrl) {
        URL.revokeObjectURL(clienteArtePreviewObjectUrl);
        clienteArtePreviewObjectUrl = null;
    }
}

function setClienteArteUploadHint(message, visible) {
    if (!clienteArteUploadHint) {
        return;
    }
    clienteArteUploadHint.textContent = message || '';
    clienteArteUploadHint.classList.toggle('hidden', !visible);
}

function atualizarPreviewArteCliente(url) {
    liberarClienteArtePreviewObjectUrl();
    const safeUrl = sanitizeClienteArteHeaderPublicUrl(url);
    if (clienteArtePreviewImg) {
        if (safeUrl) {
            clienteArtePreviewImg.src = safeUrl;
        } else {
            clienteArtePreviewImg.removeAttribute('src');
        }
    }
    if (safeUrl) {
        clienteArtePreview?.classList.remove('hidden');
        clienteArteEmpty?.classList.add('hidden');
    } else {
        clienteArtePreview?.classList.add('hidden');
        clienteArteEmpty?.classList.remove('hidden');
    }
    setClienteArteUploadHint('', false);
}

function mostrarPreviewArteClienteLocal(file) {
    liberarClienteArtePreviewObjectUrl();
    clienteArtePreviewObjectUrl = URL.createObjectURL(file);
    if (clienteArtePreviewImg) {
        clienteArtePreviewImg.src = clienteArtePreviewObjectUrl;
    }
    clienteArtePreview?.classList.remove('hidden');
    clienteArteEmpty?.classList.add('hidden');
}

async function onClienteArteFileSelected() {
    const file = clienteArteFile?.files?.[0];
    if (!file) {
        return;
    }
    const validacao = validarArteHeaderChatsArquivo(file);
    if (!validacao.ok) {
        showAlertFn(validacao.message, alertBoxClientes);
        if (clienteArteFile) {
            clienteArteFile.value = '';
        }
        return;
    }
    const clienteId = getClienteEmEdicaoId();
    if (!clienteId) {
        mostrarPreviewArteClienteLocal(file);
        setClienteArteUploadHint('Salve o cliente antes de enviar a arte.', true);
        if (clienteArteFile) {
            clienteArteFile.value = '';
        }
        return;
    }
    try {
        const atualizado = await clienteService.uploadArteHeaderChats(clienteId, file);
        atualizarPreviewArteCliente(atualizado?.arteHeaderChatsUrl || null);
        showAlertFn('Arte do header salva.', alertBoxClientes, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxClientes);
    } finally {
        if (clienteArteFile) {
            clienteArteFile.value = '';
        }
    }
}

export async function loadClientesPage() {
    if (!clientesLista) {
        return;
    }
    const termo = clienteListaBusca?.value?.trim() || '';
    clientesListPage = 1;
    clientesListaErro?.classList.add('hidden');
    clientesListaEmpty?.classList.add('hidden');
    try {
        const clientes = await clienteService.listOrSearch(termo, true);
        cachedClientesList = Array.isArray(clientes) ? clientes : [];
        cachedClientesTermo = termo;
        renderClientesList(cachedClientesList, cachedClientesTermo);
    } catch (error) {
        clientesLista.innerHTML = '';
        cachedClientesList = [];
        clientesListPagination.setState({ page: 1, total: 0 });
        clientesListaEmpty?.classList.add('hidden');
        clientesListaErro?.classList.remove('hidden');
        showAlertFn(error.message, alertBoxClientes);
    }
}

function setSelectedClienteListItem(id) {
    selectedClienteListId = id == null ? null : String(id);
    if (!clientesLista) {
        return;
    }
    clientesLista.querySelectorAll('.clientes-list-item-wrap').forEach(wrap => {
        const match = wrap.dataset.id === selectedClienteListId;
        wrap.classList.toggle('is-selected', match);
        const option = wrap.querySelector('.clientes-list-item');
        if (option) {
            option.setAttribute('aria-selected', match ? 'true' : 'false');
        }
    });
}

function clienteCampoForm(valor) {
    if (!valor || valor === '-') {
        return '';
    }
    return valor;
}

function displayClienteValor(valor) {
    if (valor === null || valor === undefined || valor === '') {
        return '-';
    }
    return valor;
}

function renderClientesList(clientes, termoBusca) {
    if (!clientesLista) {
        return;
    }
    clientesLista.innerHTML = '';
    const lista = Array.isArray(clientes) ? clientes : [];
    const msg = getClientesListaEmptyMessage(termoBusca, lista.length > 0);
    if (msg) {
        clientesListPagination.setState({ page: 1, total: 0 });
        if (clientesListaEmpty) {
            clientesListaEmpty.textContent = msg;
            clientesListaEmpty.classList.remove('hidden');
        }
        setSelectedClienteListItem(null);
        return;
    }
    clientesListaEmpty?.classList.add('hidden');
    clientesListPagination.setState({ page: clientesListPage, total: lista.length });
    clientesListPage = clientesListPagination.getPage();
    const pageItems = slicePageItems(lista, clientesListPage);
    pageItems.forEach(cliente => {
        const ativo = cliente.ativo !== false && cliente.status !== 'INATIVO';
        const id = cliente.id;
        const nome = escapeClienteListHtml(displayClienteValor(cliente.nome));
        const empresa =
            cliente.empresa && cliente.empresa !== '-'
                ? `<span class="clientes-list-item-meta">${escapeClienteListHtml(cliente.empresa)}</span>`
                : '';
        const contato = formatClienteListContactLine(cliente);
        const contatoHtml = contato
            ? `<span class="clientes-list-item-meta">${escapeClienteListHtml(contato)}</span>`
            : '';
        const local = formatClienteListLocationLine(cliente);
        const localHtml = local
            ? `<span class="clientes-list-item-meta">${escapeClienteListHtml(local)}</span>`
            : '';
        const statusBadge = ativo
            ? ''
            : '<span class="clientes-list-item-badge">Inativo</span>';
        const wrap = document.createElement('div');
        wrap.className = `clientes-list-item-wrap${ativo ? '' : ' is-inativo'}`;
        wrap.dataset.id = String(id);
        wrap.setAttribute('role', 'presentation');
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'clientes-list-item';
        btn.setAttribute('role', 'option');
        btn.setAttribute('aria-selected', 'false');
        btn.innerHTML = `
            <span class="clientes-list-item-name">${nome}</span>
            ${empresa}
            ${contatoHtml}
            ${localHtml}
            ${statusBadge}
        `;
        const acaoBtn = document.createElement('button');
        acaoBtn.type = 'button';
        acaoBtn.className = 'clientes-list-item-action';
        acaoBtn.dataset.acao = ativo ? 'inativar' : 'ativar';
        acaoBtn.dataset.id = String(id);
        acaoBtn.textContent = ativo ? 'Inativar' : 'Ativar';
        wrap.append(btn, acaoBtn);
        clientesLista.appendChild(wrap);
    });
    if (selectedClienteListId) {
        setSelectedClienteListItem(selectedClienteListId);
    }
}

function getClienteEmEdicaoId() {
    const id = clienteEditId?.value?.trim();
    return id ? Number(id) : null;
}

function formatContatoTelefoneCelular(contato) {
    const telefone = contato?.telefone && contato.telefone !== '-' ? contato.telefone : '';
    const celular = contato?.celular && contato.celular !== '-' ? contato.celular : '';
    if (telefone && celular) {
        return `${telefone} / ${celular}`;
    }
    return telefone || celular || '-';
}

async function loadContatosCliente(clienteId) {
    if (!clienteId || !contatosClienteTableBody) {
        return;
    }
    try {
        const contatos = await clienteService.listContatos(clienteId);
        renderContatosClienteTable(contatos);
    } catch (error) {
        contatosClienteTableBody.innerHTML = '';
        contatosClienteEmpty?.classList.add('hidden');
        showAlertFn(error.message, alertBoxContatos);
    }
}

function renderContatosClienteTable(contatos) {
    if (!contatosClienteTableBody) {
        return;
    }
    contatosClienteTableBody.innerHTML = '';
    if (!contatos?.length) {
        contatosClienteEmpty?.classList.remove('hidden');
        return;
    }
    contatosClienteEmpty?.classList.add('hidden');
    contatos.forEach(contato => {
        const tr = document.createElement('tr');
        const ativo = contato.ativo !== false;
        const principal = contato.principal === true;
        const toggleBtn = ativo
            ? `<button type="button" class="button button-secondary button-small" data-contato-acao="inativar" data-id="${contato.id}">Inativar</button>`
            : `<button type="button" class="button button-secondary button-small" data-contato-acao="ativar" data-id="${contato.id}">Ativar</button>`;
        const principalBtn = principal
            ? '<span class="badge">Principal</span>'
            : `<button type="button" class="button button-secondary button-small" data-contato-acao="principal" data-id="${contato.id}">Definir principal</button>`;
        tr.innerHTML = `
            <td>${displayClienteValor(contato.nome)}</td>
            <td>${displayClienteValor(contato.cargo)}</td>
            <td>${formatContatoTelefoneCelular(contato)}</td>
            <td>${displayClienteValor(contato.email)}</td>
            <td>${principal ? 'Sim' : 'Não'}</td>
            <td>${ativo ? 'Sim' : 'Não'}</td>
            <td class="table-actions">
                <button type="button" class="button button-secondary button-small" data-contato-acao="editar" data-id="${contato.id}">Editar</button>
                ${toggleBtn}
                ${principalBtn}
            </td>
        `;
        contatosClienteTableBody.appendChild(tr);
    });
}

function resetContatoForm() {
    contatoClienteForm?.reset();
    if (contatoEditId) {
        contatoEditId.value = '';
    }
    const principalCheckbox = document.getElementById('contatoPrincipal');
    if (principalCheckbox) {
        principalCheckbox.checked = false;
    }
    clearAlertFn(alertBoxContatos);
}

async function submitContatoCliente(event) {
    event.preventDefault();
    const clienteId = getClienteEmEdicaoId();
    if (!clienteId) {
        showAlertFn('Selecione ou edite um cliente antes de salvar o contato.', alertBoxContatos);
        return;
    }
    const payload = {
        clienteId,
        nome: document.getElementById('contatoNome').value.trim(),
        cargo: document.getElementById('contatoCargo').value.trim(),
        telefone: document.getElementById('contatoTelefone').value.trim(),
        celular: document.getElementById('contatoCelular').value.trim(),
        email: document.getElementById('contatoEmail').value.trim(),
        principal: document.getElementById('contatoPrincipal')?.checked === true,
        observacoes: document.getElementById('contatoObservacoes').value.trim()
    };
    const editId = contatoEditId?.value?.trim();
    const isEdit = Boolean(editId);
    try {
        if (isEdit) {
            await clienteService.updateContato(editId, payload);
        } else {
            await clienteService.createContato(clienteId, payload);
        }
        resetContatoForm();
        showAlertFn('Contato salvo com sucesso!', alertBoxContatos, 'success');
        await loadContatosCliente(clienteId);
    } catch (error) {
        showAlertFn(error.message, alertBoxContatos);
    }
}

async function editarContatoNaPagina(contatoId) {
    try {
        const contato = await clienteService.getContato(contatoId);
        if (contatoEditId) {
            contatoEditId.value = contato.id ?? '';
        }
        document.getElementById('contatoNome').value = clienteCampoForm(contato.nome);
        document.getElementById('contatoCargo').value = clienteCampoForm(contato.cargo);
        document.getElementById('contatoTelefone').value = clienteCampoForm(contato.telefone);
        document.getElementById('contatoCelular').value = clienteCampoForm(contato.celular);
        document.getElementById('contatoEmail').value = clienteCampoForm(contato.email);
        document.getElementById('contatoObservacoes').value = clienteCampoForm(contato.observacoes);
        const principalCheckbox = document.getElementById('contatoPrincipal');
        if (principalCheckbox) {
            principalCheckbox.checked = contato.principal === true;
        }
    } catch (error) {
        showAlertFn(error.message, alertBoxContatos);
    }
}

async function executarAcaoContato(contatoId, acao) {
    const clienteId = getClienteEmEdicaoId();
    try {
        await clienteService.patchContato(contatoId, acao);
        if (clienteId) {
            await loadContatosCliente(clienteId);
        }
        showAlertFn('Contato atualizado com sucesso!', alertBoxContatos, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxContatos);
    }
}

async function editarClienteNaPagina(id) {
    setSelectedClienteListItem(id);
    try {
        const cliente = await clienteService.getById(id);
        if (clienteEditId) {
            clienteEditId.value = cliente.id ?? '';
        }
        clienteContatosSection?.classList.remove('hidden');
        resetContatoForm();
        await loadContatosCliente(cliente.id);
        document.getElementById('nomeCliente').value = clienteCampoForm(cliente.nome);
        document.getElementById('emailCliente').value = clienteCampoForm(cliente.email);
        document.getElementById('telefonePrincipal').value = clienteCampoForm(cliente.telefone);
        document.getElementById('telefoneContato').value = clienteCampoForm(cliente.telefoneContato);
        document.getElementById('empresa').value = clienteCampoForm(cliente.empresa);
        document.getElementById('cnpj').value = clienteCampoForm(cliente.cnpj);
        document.getElementById('cidade').value = clienteCampoForm(cliente.cidade);
        document.getElementById('uf').value = clienteCampoForm(cliente.uf);
        document.getElementById('endereco').value = clienteCampoForm(cliente.endereco);
        document.getElementById('observacoes').value = clienteCampoForm(cliente.observacoes);
        const statusEl = document.getElementById('statusCliente');
        if (statusEl) {
            statusEl.value = cliente.status === 'INATIVO' ? 'INATIVO' : 'ATIVO';
        }
        const classificacaoEl = document.getElementById('classificacaoCliente');
        if (classificacaoEl) {
            classificacaoEl.value = normalizeClassificacaoCliente(cliente.classificacaoCliente);
        }
        setClienteFormUiState('edit', cliente.nome);
        await loadWhatsappMatrizesCliente(cliente.id);
        resetClienteWhatsappMatrizForm();
        atualizarPreviewArteCliente(cliente.arteHeaderChatsUrl);
        clearAlertFn(alertBoxClientes);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
        showAlertFn(error.message, alertBoxClientes);
    }
}

async function alternarStatusCliente(id, acao) {
    try {
        await clienteService.patchStatus(id, acao === 'ativar');
        showAlertFn(
            acao === 'ativar' ? 'Cliente ativado com sucesso!' : 'Cliente inativado com sucesso!',
            alertBoxClientes,
            'success'
        );
        await loadClientesPage();
        onRefreshDashboardFn();
    } catch (error) {
        showAlertFn(error.message, alertBoxClientes);
    }
}

async function submitCliente(event) {
    event.preventDefault();
    const statusEl = document.getElementById('statusCliente');
    const classificacaoEl = document.getElementById('classificacaoCliente');
    const payload = {
        nome: document.getElementById('nomeCliente').value.trim(),
        email: document.getElementById('emailCliente').value.trim(),
        telefone: document.getElementById('telefonePrincipal').value.trim(),
        telefoneContato: document.getElementById('telefoneContato').value.trim(),
        empresa: document.getElementById('empresa').value.trim(),
        cnpj: document.getElementById('cnpj').value.trim(),
        cidade: document.getElementById('cidade').value.trim(),
        uf: document.getElementById('uf').value.trim(),
        endereco: document.getElementById('endereco').value.trim(),
        observacoes: document.getElementById('observacoes').value.trim(),
        status: statusEl?.value || 'ATIVO',
        classificacaoCliente: normalizeClassificacaoCliente(classificacaoEl?.value)
    };

    const editId = clienteEditId?.value?.trim();
    const isEdit = Boolean(editId);

    try {
        const saved = await clienteService.save(payload, isEdit ? editId : null);
        showAlertFn(
            isEdit ? 'Cliente atualizado com sucesso!' : 'Cliente cadastrado com sucesso!',
            alertBoxClientes,
            'success'
        );
        await loadClientesPage();
        onRefreshDashboardFn();
        if (!isEdit && saved?.id) {
            if (clienteEditId) {
                clienteEditId.value = String(saved.id);
            }
            setSelectedClienteListItem(saved.id);
            setClienteFormUiState('edit', saved.nome || payload.nome);
            clienteContatosSection?.classList.remove('hidden');
            await loadWhatsappMatrizesCliente(saved.id);
            resetClienteWhatsappMatrizForm();
        } else if (isEdit && editId) {
            await loadWhatsappMatrizesCliente(Number(editId));
        } else {
            resetClienteForm();
        }
    } catch (error) {
        showAlertFn(error.message, alertBoxClientes);
    }
}

function resetClienteForm(options = {}) {
    const { keepFormVisible = false } = options;
    clienteForm?.reset();
    if (clienteEditId) {
        clienteEditId.value = '';
    }
    const statusEl = document.getElementById('statusCliente');
    if (statusEl) {
        statusEl.value = 'ATIVO';
    }
    const classificacaoEl = document.getElementById('classificacaoCliente');
    if (classificacaoEl) {
        classificacaoEl.value = 'SEM_CLASSIFICACAO';
    }
    clienteContatosSection?.classList.add('hidden');
    clienteWhatsappMatrizSection?.classList.add('hidden');
    resetClienteWhatsappMatrizForm();
    if (clienteWhatsappMatrizList) {
        clienteWhatsappMatrizList.innerHTML = '';
    }
    setSelectedClienteListItem(null);
    resetContatoForm();
    if (contatosClienteTableBody) {
        contatosClienteTableBody.innerHTML = '';
    }
    contatosClienteEmpty?.classList.add('hidden');
    clearAlertFn(alertBoxClientes);
    if (clienteArteFile) {
        clienteArteFile.value = '';
    }
    atualizarPreviewArteCliente(null);
    if (!keepFormVisible) {
        setClienteFormUiState('idle');
    }
}
