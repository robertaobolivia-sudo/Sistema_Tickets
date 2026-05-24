import * as clienteService from '@features/clientes/cliente-service.js';
import { closeOtherSidebarGroups } from '@shared/ui/sidebar-groups.js';
import * as contatoService from '@features/contatos/contato-service.js';
import * as contatoTelefoneService from '@features/contatos/contato-telefone-service.js';
import { buildHistoricoTelefonesReadonlyHtml } from '@features/contatos/contato-gestao-telefones-view.js';
import * as whatsappMatrizService from '@features/clientes/whatsapp-matriz-service.js';
import {
    CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO,
    catalogoTemEtiquetasOperacionais,
    contatoTemEtiquetaOperacional,
    escapeContatoGestaoHtml,
    formatContatoGestaoChamados,
    formatContatoGestaoLocal,
    formatContatoGestaoStatusHtml,
    formatEtiquetasGestaoCellHtml,
    getContatosGestaoEmptyMessage,
    isNomeEtiquetaOperacional
} from '@features/clientes/cliente-contatos-gestao-view.js';
import { hasContatosGestaoFiltrosAtivos } from '@features/contatos/contato-gestao-filtros-view.js';
import * as etiquetaService from '@features/configuracoes/etiqueta-service.js';
import {
    buildHistoricoPanelHtml,
    getHistoricoColspan
} from '@features/contatos/contato-gestao-historico-view.js';
import { mensagemParaExibirUsuario } from '@shared/ui/messages.js';
import { normalizeClassificacaoCliente } from '@features/clientes/cliente-view.js';
import {
    escapeClienteListHtml,
    formatClienteListContactLine,
    formatClienteListLocationLine,
    getClientesListaEmptyMessage
} from '@features/clientes/cliente-list-view.js';
import { validarArteHeaderChatsArquivo } from '@features/configuracoes/carteira-arte-view.js';
import {
    CLIENTE_FORM_MSG_EMPTY,
    getClienteFormHeaderSubtitle,
    getClienteFormHeaderTitle,
    sanitizeClienteArteHeaderPublicUrl
} from '@features/clientes/cliente-form-view.js';
import { createListPaginationController } from '@components/list-pagination-bar/list-pagination-bar.js';
import {
    abrirContatoGestaoEditModal,
    initContatoGestaoEditModal
} from '@components/contato-gestao-edit-modal/contato-gestao-edit-modal.js';
import { CLIENTES_LIST_PAGE_SIZE, slicePageItems } from '@shared/ui/list-pagination.js';
import {
    CLIENTE_WHATSAPP_MATRIZ_MSG_EMPTY,
    CLIENTE_WHATSAPP_MATRIZ_MSG_HINT,
    CLIENTE_WHATSAPP_MATRIZ_MSG_SAVE_FIRST,
    buildWhatsappMatrizPayloadFromForm,
    formatWhatsappMatrizStatusClass,
    formatWhatsappMatrizStatusLabel,
    mapWhatsappMatrizApiError,
    validateWhatsappMatrizForm
} from '@features/clientes/cliente-whatsapp-matriz-view.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let onRefreshDashboardFn = () => {};
let openChatsFromHistoricoFn = async () => {};

const clienteForm = document.getElementById('clienteForm');
const novoClienteBtn = document.getElementById('novoClienteBtn');
const clienteEditId = document.getElementById('clienteEditId');
const clienteListaBusca = document.getElementById('clienteListaBusca');
const clientesListaTableBody = document.getElementById('clientesListaTableBody');
const clientesListaPagination = document.getElementById('clientesListaPagination');
const clientesListaEmpty = document.getElementById('clientesListaEmpty');
const clientesListaErro = document.getElementById('clientesListaErro');
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

export const CLIENTES_CONTATOS_LIST_PAGE_SIZE = 12;

export { CLIENTES_LIST_PAGE_SIZE };

let clientesPageMode = 'listagem';
let cachedContatosGestao = [];
let contatosGestaoPage = 1;
let contatosGestaoBuscaTimerId = null;
let contatosHistoricoAbertoId = null;

function normalizeClientesPageMode(mode) {
    if (mode === 'cadastro' || mode === 'novo') {
        return 'cadastro';
    }
    if (mode === 'contatos') {
        return 'contatos';
    }
    return 'listagem';
}

const clientesPageEl = document.getElementById('page-clientes');
const clientesViewListagem = document.getElementById('clientesViewListagem');
const clientesViewCadastro = document.getElementById('clientesViewCadastro');
const clientesViewContatos = document.getElementById('clientesViewContatos');
const clienteContatosResumoSection = document.getElementById('clienteContatosResumoSection');
const clienteContatosResumoTableBody = document.getElementById('clienteContatosResumoTableBody');
const clienteContatosResumoEmpty = document.getElementById('clienteContatosResumoEmpty');
const contatosGestaoFiltroCliente = document.getElementById('contatosGestaoFiltroCliente');
const contatosGestaoBusca = document.getElementById('contatosGestaoBusca');
const contatosGestaoFiltroEtiqueta = document.getElementById('contatosGestaoFiltroEtiqueta');
const contatosGestaoFiltroCidade = document.getElementById('contatosGestaoFiltroCidade');
const contatosGestaoFiltroUf = document.getElementById('contatosGestaoFiltroUf');
const contatosGestaoFiltroTicketsAbertos = document.getElementById('contatosGestaoFiltroTicketsAbertos');
const contatosGestaoFiltroAvaliacaoRuim = document.getElementById('contatosGestaoFiltroAvaliacaoRuim');
const contatosGestaoFiltroSemEtiqueta = document.getElementById('contatosGestaoFiltroSemEtiqueta');
const contatosGestaoAplicarFiltros = document.getElementById('contatosGestaoAplicarFiltros');
const contatosGestaoLimparFiltrosAvancados = document.getElementById('contatosGestaoLimparFiltrosAvancados');
let contatosGestaoEtiquetasCarregadas = false;
const contatosGestaoTableBody = document.getElementById('contatosGestaoTableBody');
const contatosGestaoPagination = document.getElementById('contatosGestaoPagination');
const contatosGestaoEmpty = document.getElementById('contatosGestaoEmpty');
const contatosGestaoErro = document.getElementById('contatosGestaoErro');
const contatosGestaoEtiquetasOperacionaisHint = document.getElementById(
    'contatosGestaoEtiquetasOperacionaisHint'
);
const clientesPageTitle = document.getElementById('clientesPageTitle');
const clientesPageSubtitle = document.getElementById('clientesPageSubtitle');
const clientesVoltarListagemBtn = document.getElementById('clientesVoltarListagemBtn');


const clientesListPagination = createListPaginationController(clientesListaPagination, {
    pageSize: CLIENTES_LIST_PAGE_SIZE,
    ariaLabel: 'Paginação da lista de clientes',
    onPageChange(page) {
        clientesListPage = page;
        renderClientesList(cachedClientesList, cachedClientesTermo);
    }
});

const contatosGestaoListPagination = createListPaginationController(contatosGestaoPagination, {
    pageSize: CLIENTES_CONTATOS_LIST_PAGE_SIZE,
    ariaLabel: 'Paginação da lista de contatos',
    onPageChange(page) {
        contatosGestaoPage = page;
        renderContatosGestaoTable(cachedContatosGestao);
    }
});

function clienteDtoCampo(valor) {
    if (valor == null || valor === '' || valor === '-') {
        return '';
    }
    return String(valor).trim();
}

export function getClientesPageMode() {
    return clientesPageMode;
}

export function setClientesPageMode(mode) {
    const next = normalizeClientesPageMode(mode);
    clientesPageMode = next;
    clientesPageEl?.setAttribute('data-clientes-mode', next);
    clientesViewListagem?.classList.toggle('hidden', next !== 'listagem');
    clientesViewCadastro?.classList.toggle('hidden', next !== 'cadastro');
    clientesViewContatos?.classList.toggle('hidden', next !== 'contatos');
    clienteContatosResumoSection?.classList.toggle('hidden', next !== 'cadastro');
    if (clientesPageTitle) {
        clientesPageTitle.textContent = 'Clientes';
    }
    if (clientesPageSubtitle) {
        const hints = {
            listagem: 'Contratantes B2B da F5 — largura total da área útil.',
            cadastro: 'Cadastro do contratante e resumo dos contatos atendidos.',
            contatos: 'Pessoas atendidas via WhatsApp, filtradas por contratante.'
        };
        clientesPageSubtitle.textContent = hints[next] || hints.listagem;
    }
    if (next === 'listagem') {
        loadClientesPage();
    } else if (next === 'contatos') {
        loadClientesContatosPage();
    }
}

export function initClientesSidebarNav({ showPageFn }) {
    const group = document.getElementById('navClientesGroup');
    const toggle = document.getElementById('navClientesToggle');
    const submenu = document.getElementById('navClientesSubmenu');
    if (!group || !toggle || !submenu) {
        return;
    }
    const setOpen = open => {
        group.classList.toggle('is-open', open);
        toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        submenu.classList.toggle('hidden', !open);
    };
    toggle.addEventListener('click', () => {
        const willOpen = !group.classList.contains('is-open');
        if (willOpen) closeOtherSidebarGroups('navClientesGroup');
        setOpen(willOpen);
    });
    document.querySelectorAll('[data-clientes-mode]').forEach(btn => {
        btn.addEventListener('click', () => {
            const mode = normalizeClientesPageMode(btn.getAttribute('data-clientes-mode'));
            setClientesPageMode(mode);
            setOpen(true);
            showPageFn('clientes');
            btn.classList.add('is-active-page');
            if (mode === 'cadastro') {
                iniciarNovoCliente();
            }
        });
    });
}

export function initClientesPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.onRefreshDashboard) onRefreshDashboardFn = deps.onRefreshDashboard;
    if (deps.openChatsFromHistoricoFn) openChatsFromHistoricoFn = deps.openChatsFromHistoricoFn;
    initContatoGestaoEditModal({
        showAlert: showAlertFn,
        onSaved: () => loadClientesContatosPage()
    });
    if (listenersBound) return;
    listenersBound = true;

    clienteForm?.addEventListener('submit', submitCliente);
    clienteForm?.addEventListener('reset', () => {
        window.setTimeout(() => {
            if (!getClienteEmEdicaoId()) {
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
    clientesListaTableBody?.addEventListener('click', event => {
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
        const row = event.target.closest('tr[data-cliente-id]');
        if (row?.dataset?.clienteId) {
            editarClienteNaPagina(row.dataset.clienteId);
        }
    });
    contatosGestaoFiltroCliente?.addEventListener('change', () => loadClientesContatosPage());
    contatosGestaoAplicarFiltros?.addEventListener('click', () => loadClientesContatosPage());
    contatosGestaoLimparFiltrosAvancados?.addEventListener('click', () => {
        limparContatosGestaoFiltrosAvancados();
        loadClientesContatosPage();
    });
    contatosGestaoBusca?.addEventListener('input', () => {
        if (contatosGestaoBuscaTimerId) {
            clearTimeout(contatosGestaoBuscaTimerId);
        }
        contatosGestaoBuscaTimerId = setTimeout(() => {
            contatosGestaoBuscaTimerId = null;
            loadClientesContatosPage();
        }, 320);
    });
    contatosGestaoTableBody?.addEventListener('click', event => {
        const verConversaBtn = event.target.closest('button[data-contato-ver-conversa]');
        if (verConversaBtn) {
            event.stopPropagation();
            const protocolo = verConversaBtn.dataset.protocolo?.trim();
            const status = verConversaBtn.dataset.status?.trim() || '';
            if (protocolo) {
                void openChatsFromHistoricoFn(protocolo, status);
            }
            return;
        }
        const btn = event.target.closest('button[data-contato-gestao-acao]');
        if (!btn) {
            return;
        }
        const id = Number(btn.dataset.id);
        const acao = btn.dataset.contatoGestaoAcao;
        if (!id || !acao) {
            return;
        }
        const contato = (cachedContatosGestao || []).find(c => Number(c.id) === id);
        if (!contato) {
            return;
        }
        if (acao === 'historico') {
            void toggleContatoGestaoHistorico(contato);
            return;
        }
        if (acao === 'editar') {
            abrirContatoGestaoEditModal(contato);
            return;
        }
        if (acao === 'ativar' || acao === 'inativar') {
            void alternarStatusContatoGestao(id, acao);
        }
    });
    clientesVoltarListagemBtn?.addEventListener('click', () => setClientesPageMode('listagem'));
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
    clientesVoltarListagemBtn?.addEventListener('click', () => setClientesPageMode('listagem'));
    setClientesPageMode('listagem');
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
    setClientesPageMode('cadastro');
    resetClienteForm({ keepFormVisible: true });
    setClienteFormUiState('novo');
    atualizarPreviewArteCliente(null);
    clearClienteContatosResumo();
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
    if (clientesPageMode !== 'listagem' || !clientesListaTableBody) {
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
        clientesListaTableBody.innerHTML = '';
        cachedClientesList = [];
        clientesListPagination.setState({ page: 1, total: 0 });
        clientesListaEmpty?.classList.add('hidden');
        clientesListaErro?.classList.remove('hidden');
        showAlertFn(error.message, alertBoxClientes);
    }
}

function setSelectedClienteListItem(id) {
    selectedClienteListId = id == null ? null : String(id);
    if (!clientesListaTableBody) {
        return;
    }
    clientesListaTableBody.querySelectorAll('tr[data-cliente-id]').forEach(row => {
        row.classList.toggle('is-selected', row.dataset.clienteId === selectedClienteListId);
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
    if (!clientesListaTableBody) {
        return;
    }
    clientesListaTableBody.innerHTML = '';
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
    const pageItems = slicePageItems(lista, clientesListPage, CLIENTES_LIST_PAGE_SIZE);
    pageItems.forEach(cliente => {
        const ativo = cliente.ativo !== false && cliente.status !== 'INATIVO';
        const id = cliente.id;
        const razaoLabel =
            clienteDtoCampo(cliente.razaoSocial) ||
            clienteDtoCampo(cliente.empresa) ||
            clienteDtoCampo(cliente.nome);
        const responsavel = clienteDtoCampo(cliente.responsavel) || clienteDtoCampo(cliente.nome);
        const whatsapp =
            clienteDtoCampo(cliente.whatsapp) || clienteDtoCampo(cliente.telefone) || '—';
        const email = clienteDtoCampo(cliente.email) || '—';
        const local = formatClienteListLocationLine(cliente) || '—';
        const tr = document.createElement('tr');
        tr.dataset.clienteId = String(id);
        tr.className = `clientes-row-open${ativo ? '' : ' is-inativo'}`;
        tr.innerHTML = `
            <td>${escapeClienteListHtml(razaoLabel)}</td>
            <td>${escapeClienteListHtml(responsavel)}</td>
            <td>${escapeClienteListHtml(whatsapp)}</td>
            <td>${escapeClienteListHtml(email)}</td>
            <td>${escapeClienteListHtml(local)}</td>
            <td>${ativo ? 'Ativo' : 'Inativo'}</td>
            <td class="clientes-row-action">
                <button type="button" class="button button-secondary button-small" data-acao="${
                    ativo ? 'inativar' : 'ativar'
                }" data-id="${id}">${ativo ? 'Inativar' : 'Ativar'}</button>
            </td>
        `;
        clientesListaTableBody.appendChild(tr);
    });
    if (selectedClienteListId) {
        setSelectedClienteListItem(selectedClienteListId);
    }
}

function clearClienteContatosResumo() {
    if (clienteContatosResumoTableBody) {
        clienteContatosResumoTableBody.innerHTML = '';
    }
    clienteContatosResumoEmpty?.classList.add('hidden');
    clienteContatosResumoSection?.classList.add('hidden');
}

async function loadClienteContatosResumo(clienteId) {
    if (!clienteId || !clienteContatosResumoTableBody) {
        clearClienteContatosResumo();
        return;
    }
    clienteContatosResumoSection?.classList.remove('hidden');
    try {
        const contatos = await contatoService.listarGestao({ clienteId });
        renderContatosResumoTable(contatos);
    } catch {
        clienteContatosResumoTableBody.innerHTML = '';
        if (clienteContatosResumoEmpty) {
            clienteContatosResumoEmpty.textContent = 'Não foi possível carregar o resumo de contatos.';
            clienteContatosResumoEmpty.classList.remove('hidden');
        }
    }
}

function renderContatosResumoTable(contatos) {
    if (!clienteContatosResumoTableBody) {
        return;
    }
    clienteContatosResumoTableBody.innerHTML = '';
    const lista = Array.isArray(contatos) ? contatos : [];
    if (!lista.length) {
        if (clienteContatosResumoEmpty) {
            clienteContatosResumoEmpty.textContent =
                'Nenhum contato atendido vinculado a este contratante.';
            clienteContatosResumoEmpty.classList.remove('hidden');
        }
        return;
    }
    clienteContatosResumoEmpty?.classList.add('hidden');
    lista.slice(0, 8).forEach(contato => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${escapeContatoGestaoHtml(contato.nome || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.whatsapp || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.email || '—')}</td>
            <td>${escapeContatoGestaoHtml(formatContatoGestaoLocal(contato))}</td>
            <td>${escapeContatoGestaoHtml(contato.etiquetasResumo || '—')}</td>
            <td>${escapeContatoGestaoHtml(formatContatoGestaoChamados(contato))}</td>
        `;
        clienteContatosResumoTableBody.appendChild(tr);
    });
}

async function ensureContatosFiltroClientesOptions() {
    const sel = contatosGestaoFiltroCliente;
    if (!sel || sel.dataset.loaded === '1') {
        return;
    }
    try {
        const clientes = await clienteService.listOrSearch('', true);
        const opts = ['<option value="">Todos os contratantes</option>'];
        (clientes || []).forEach(c => {
            const label =
                clienteDtoCampo(c.razaoSocial) ||
                clienteDtoCampo(c.empresa) ||
                clienteDtoCampo(c.nome) ||
                `Cliente #${c.id}`;
            opts.push(`<option value="${c.id}">${escapeClienteListHtml(label)}</option>`);
        });
        sel.innerHTML = opts.join('');
        sel.dataset.loaded = '1';
    } catch {
        /* mantém opção padrão */
    }
}

export function removerLinhasHistoricoContato() {
    contatosGestaoTableBody
        ?.querySelectorAll('tr.contato-historico-row')
        .forEach(row => row.remove());
    contatosHistoricoAbertoId = null;
}

async function montarPainelHistoricoParaContato(contato) {
    const id = Number(contato?.id);
    if (!id || !contatosGestaoTableBody) {
        return;
    }
    contatosHistoricoAbertoId = id;
    const mainRow = contatosGestaoTableBody
        .querySelector(`button[data-contato-gestao-acao="historico"][data-id="${id}"]`)
        ?.closest('tr');
    if (!mainRow) {
        contatosHistoricoAbertoId = null;
        return;
    }
    const panelRow = document.createElement('tr');
    panelRow.className = 'contato-historico-row';
    const colspan = getHistoricoColspan();
    panelRow.innerHTML = `<td colspan="${colspan}">${buildHistoricoPanelHtml(contato.nome, [], { loading: true })}</td>`;
    mainRow.insertAdjacentElement('afterend', panelRow);
    try {
        const [tickets, telefonesAdicionais] = await Promise.all([
            contatoService.listarHistoricoTickets(id),
            contatoTelefoneService.listarTelefonesAdicionais(id).catch(() => [])
        ]);
        if (contatosHistoricoAbertoId !== id) {
            return;
        }
        const telefonesHtml = buildHistoricoTelefonesReadonlyHtml(
            contato.whatsapp,
            telefonesAdicionais
        );
        const cell = panelRow.querySelector('td');
        if (cell) {
            cell.innerHTML = buildHistoricoPanelHtml(contato.nome, tickets, { telefonesHtml });
        }
    } catch (error) {
        if (contatosHistoricoAbertoId !== id) {
            return;
        }
        const cell = panelRow.querySelector('td');
        if (cell) {
            cell.innerHTML = buildHistoricoPanelHtml(contato.nome, [], {
                erro: mensagemParaExibirUsuario(error?.message || error)
            });
        }
    }
}

async function toggleContatoGestaoHistorico(contato) {
    const id = Number(contato?.id);
    if (!id || !contatosGestaoTableBody) {
        return;
    }
    if (contatosHistoricoAbertoId === id) {
        removerLinhasHistoricoContato();
        return;
    }
    removerLinhasHistoricoContato();
    await montarPainelHistoricoParaContato(contato);
}

function getContatosGestaoFiltrosFromUi() {
    return {
        clienteId: contatosGestaoFiltroCliente?.value?.trim() || null,
        busca: contatosGestaoBusca?.value?.trim() || '',
        etiquetaId: contatosGestaoFiltroEtiqueta?.value?.trim() || null,
        cidade: contatosGestaoFiltroCidade?.value?.trim() || '',
        uf: contatosGestaoFiltroUf?.value?.trim() || '',
        comTicketsAbertos: Boolean(contatosGestaoFiltroTicketsAbertos?.checked),
        comAvaliacaoRuim: Boolean(contatosGestaoFiltroAvaliacaoRuim?.checked),
        semEtiqueta: Boolean(contatosGestaoFiltroSemEtiqueta?.checked)
    };
}

function limparContatosGestaoFiltrosAvancados() {
    if (contatosGestaoFiltroEtiqueta) {
        contatosGestaoFiltroEtiqueta.value = '';
    }
    if (contatosGestaoFiltroCidade) {
        contatosGestaoFiltroCidade.value = '';
    }
    if (contatosGestaoFiltroUf) {
        contatosGestaoFiltroUf.value = '';
    }
    if (contatosGestaoFiltroTicketsAbertos) {
        contatosGestaoFiltroTicketsAbertos.checked = false;
    }
    if (contatosGestaoFiltroAvaliacaoRuim) {
        contatosGestaoFiltroAvaliacaoRuim.checked = false;
    }
    if (contatosGestaoFiltroSemEtiqueta) {
        contatosGestaoFiltroSemEtiqueta.checked = false;
    }
}

async function ensureContatosGestaoFiltroEtiquetasOptions() {
    if (!contatosGestaoFiltroEtiqueta || contatosGestaoEtiquetasCarregadas) {
        return;
    }
    try {
        const etiquetas = await etiquetaService.listActive();
        const lista = Array.isArray(etiquetas) ? etiquetas : [];
        const atual = contatosGestaoFiltroEtiqueta.value;
        contatosGestaoFiltroEtiqueta.innerHTML = '<option value="">Qualquer etiqueta</option>';
        lista.forEach(e => {
            if (!e?.id) {
                return;
            }
            const opt = document.createElement('option');
            opt.value = String(e.id);
            const nome = e.nome || `Etiqueta ${e.id}`;
            opt.textContent = isNomeEtiquetaOperacional(nome) ? `${nome} (operacional)` : nome;
            if (isNomeEtiquetaOperacional(nome)) {
                opt.dataset.etiquetaOperacional = '1';
            }
            contatosGestaoFiltroEtiqueta.appendChild(opt);
        });
        if (atual) {
            contatosGestaoFiltroEtiqueta.value = atual;
        }
        contatosGestaoEtiquetasCarregadas = true;
    } catch {
        /* etiquetas opcionais no filtro */
    }
}

async function refreshContatosGestaoOperacionalCatalogoHint() {
    if (!contatosGestaoEtiquetasOperacionaisHint) {
        return;
    }
    try {
        const etiquetas = await etiquetaService.listActive();
        const ok = catalogoTemEtiquetasOperacionais(etiquetas);
        contatosGestaoEtiquetasOperacionaisHint.classList.toggle('hidden', ok);
        if (!ok) {
            contatosGestaoEtiquetasOperacionaisHint.textContent = `Cadastre em Configurações as etiquetas operacionais: ${CONTATO_ETIQUETAS_OPERACIONAIS_SUGESTAO.join(', ')}.`;
        }
    } catch {
        contatosGestaoEtiquetasOperacionaisHint.classList.add('hidden');
    }
}

async function loadClientesContatosPage() {
    if (clientesPageMode !== 'contatos' || !contatosGestaoTableBody) {
        return;
    }
    contatosGestaoErro?.classList.add('hidden');
    contatosGestaoEmpty?.classList.add('hidden');
    await ensureContatosFiltroClientesOptions();
    await ensureContatosGestaoFiltroEtiquetasOptions();
    await refreshContatosGestaoOperacionalCatalogoHint();
    const filtros = getContatosGestaoFiltrosFromUi();
    contatosGestaoPage = 1;
    try {
        const contatos = await contatoService.listarGestao(filtros);
        cachedContatosGestao = Array.isArray(contatos) ? contatos : [];
        renderContatosGestaoTable(cachedContatosGestao);
    } catch (error) {
        contatosGestaoTableBody.innerHTML = '';
        cachedContatosGestao = [];
        contatosGestaoListPagination.setState({ page: 1, total: 0 });
        contatosGestaoEmpty?.classList.add('hidden');
        contatosGestaoErro?.classList.remove('hidden');
        showAlertFn(error.message, alertBoxClientes);
    }
}

function renderContatosGestaoTable(contatos) {
    if (!contatosGestaoTableBody) {
        return;
    }
    const reabrirHistoricoId = contatosHistoricoAbertoId;
    contatosHistoricoAbertoId = null;
    contatosGestaoTableBody.innerHTML = '';
    const lista = Array.isArray(contatos) ? contatos : [];
    const filtrosAtivos = hasContatosGestaoFiltrosAtivos(getContatosGestaoFiltrosFromUi());
    const msg = getContatosGestaoEmptyMessage(filtrosAtivos, lista.length > 0);
    if (msg) {
        contatosGestaoListPagination.setState({ page: 1, total: 0 });
        if (contatosGestaoEmpty) {
            contatosGestaoEmpty.textContent = msg;
            contatosGestaoEmpty.classList.remove('hidden');
        }
        return;
    }
    contatosGestaoEmpty?.classList.add('hidden');
    contatosGestaoListPagination.setState({ page: contatosGestaoPage, total: lista.length });
    contatosGestaoPage = contatosGestaoListPagination.getPage();
    const pageItems = slicePageItems(lista, contatosGestaoPage, CLIENTES_CONTATOS_LIST_PAGE_SIZE);
    pageItems.forEach(contato => {
        const ativo = contato.ativo !== false;
        const tr = document.createElement('tr');
        if (!ativo) {
            tr.classList.add('is-inativo');
        }
        if (contatoTemEtiquetaOperacional(contato)) {
            tr.classList.add('contato-gestao-row--operacional');
        }
        const toggleBtn = ativo
            ? `<button type="button" class="button button-secondary button-small" data-contato-gestao-acao="inativar" data-id="${contato.id}">Inativar</button>`
            : `<button type="button" class="button button-primary button-small" data-contato-gestao-acao="ativar" data-id="${contato.id}">Ativar</button>`;
        tr.innerHTML = `
            <td>${escapeContatoGestaoHtml(contato.clienteRazaoSocial || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.nome || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.whatsapp || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.email || '—')}</td>
            <td>${escapeContatoGestaoHtml(contato.empresaLocal || '—')}</td>
            <td>${escapeContatoGestaoHtml(formatContatoGestaoLocal(contato))}</td>
            <td>${formatEtiquetasGestaoCellHtml(contato)}</td>
            <td>${escapeContatoGestaoHtml(formatContatoGestaoChamados(contato))}</td>
            <td>${formatContatoGestaoStatusHtml(ativo)}</td>
            <td class="table-actions">
                <button type="button" class="button button-secondary button-small" data-contato-gestao-acao="historico" data-id="${contato.id}">Histórico</button>
                <button type="button" class="button button-secondary button-small" data-contato-gestao-acao="editar" data-id="${contato.id}">Ver/Editar</button>
                ${toggleBtn}
            </td>
        `;
        contatosGestaoTableBody.appendChild(tr);
    });
    if (reabrirHistoricoId) {
        const contatoReabrir = lista.find(c => Number(c.id) === Number(reabrirHistoricoId));
        const naPagina = pageItems.some(c => Number(c.id) === Number(reabrirHistoricoId));
        if (contatoReabrir && naPagina) {
            void montarPainelHistoricoParaContato(contatoReabrir);
        }
    }
}

async function alternarStatusContatoGestao(contatoId, acao) {
    const id = Number(contatoId);
    if (!id) {
        return;
    }
    const nomeAcao = acao === 'ativar' ? 'ativar' : 'inativar';
    if (!window.confirm(`Deseja ${nomeAcao} este contato? O histórico e os chamados serão preservados.`)) {
        return;
    }
    try {
        if (acao === 'ativar') {
            await contatoService.patchAtivar(id);
        } else {
            await contatoService.patchInativar(id);
        }
        showAlertFn(
            acao === 'ativar' ? 'Contato ativado.' : 'Contato inativado.',
            alertBoxClientes,
            'success'
        );
        await loadClientesContatosPage();
    } catch (error) {
        showAlertFn(mensagemParaExibirUsuario(error?.message || error), alertBoxClientes);
    }
}

function getClienteEmEdicaoId() {
    const id = clienteEditId?.value?.trim();
    return id ? Number(id) : null;
}

async function editarClienteNaPagina(id) {
    setClientesPageMode('cadastro');
    setSelectedClienteListItem(id);
    try {
        const cliente = await clienteService.getById(id);
        if (clienteEditId) {
            clienteEditId.value = cliente.id ?? '';
        }
        document.getElementById('nomeCliente').value = clienteCampoForm(
            clienteDtoCampo(cliente.responsavel) || clienteDtoCampo(cliente.nome)
        );
        document.getElementById('emailCliente').value = clienteCampoForm(cliente.email);
        document.getElementById('telefonePrincipal').value = clienteCampoForm(
            clienteDtoCampo(cliente.whatsapp) || clienteDtoCampo(cliente.telefone)
        );
        document.getElementById('telefoneContato').value = clienteCampoForm(cliente.telefoneContato);
        document.getElementById('empresa').value = clienteCampoForm(
            clienteDtoCampo(cliente.razaoSocial) || clienteDtoCampo(cliente.empresa)
        );
        document.getElementById('cnpj').value = clienteCampoForm(cliente.cnpj);
        document.getElementById('clienteIe').value = clienteCampoForm(cliente.inscricaoEstadual);
        document.getElementById('clienteCep').value = clienteCampoForm(cliente.cep);
        document.getElementById('clienteSite').value = clienteCampoForm(cliente.site);
        document.getElementById('clienteHorarioFuncionamento').value = clienteCampoForm(
            cliente.horarioFuncionamento
        );
        document.getElementById('cidade').value = clienteCampoForm(cliente.cidade);
        document.getElementById('uf').value = clienteCampoForm(cliente.uf);
        document.getElementById('endereco').value = clienteCampoForm(cliente.endereco);
        const obsEl = document.getElementById('observacoes');
        if (obsEl) {
            obsEl.value = clienteDtoCampo(cliente.observacoes);
        }
        const statusEl = document.getElementById('statusCliente');
        if (statusEl) {
            statusEl.value = cliente.status === 'INATIVO' ? 'INATIVO' : 'ATIVO';
        }
        const classificacaoEl = document.getElementById('classificacaoCliente');
        if (classificacaoEl) {
            classificacaoEl.value = normalizeClassificacaoCliente(cliente.classificacaoCliente);
        }
        setClienteFormUiState('edit', cliente.nome);
        clienteWhatsappMatrizSection?.classList.add('hidden');
        await loadClienteContatosResumo(cliente.id);
        clearAlertFn(alertBoxClientes);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
        showAlertFn(error.message, alertBoxClientes);
    }
}

/** Sprint F12/F37: abertura manual → cadastro contratante + resumo Contatos reais. */
export async function navegarParaContatosDoCliente(clienteId) {
    const id = Number(clienteId);
    if (!Number.isFinite(id) || id <= 0) {
        return;
    }
    await editarClienteNaPagina(id);
    clienteContatosResumoSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
    const telPrincipal = document.getElementById('telefonePrincipal').value.trim();
    const telContatoEl = document.getElementById('telefoneContato');
    if (telContatoEl) {
        telContatoEl.value = telPrincipal;
    }
    const responsavel = document.getElementById('nomeCliente').value.trim();
    const razaoSocial = document.getElementById('empresa').value.trim();
    const payload = {
        nome: responsavel,
        responsavel,
        razaoSocial,
        email: document.getElementById('emailCliente').value.trim(),
        whatsapp: telPrincipal,
        telefone: telPrincipal,
        telefoneContato: telContatoEl?.value?.trim() || telPrincipal,
        empresa: razaoSocial,
        cnpj: document.getElementById('cnpj').value.trim(),
        inscricaoEstadual: document.getElementById('clienteIe')?.value?.trim() || '',
        cep: document.getElementById('clienteCep')?.value?.trim() || '',
        site: document.getElementById('clienteSite')?.value?.trim() || '',
        horarioFuncionamento:
            document.getElementById('clienteHorarioFuncionamento')?.value?.trim() || '',
        cidade: document.getElementById('cidade').value.trim(),
        uf: document.getElementById('uf').value.trim(),
        endereco: document.getElementById('endereco').value.trim(),
        observacoes: document.getElementById('observacoes')?.value?.trim() || '',
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
            await loadWhatsappMatrizesCliente(saved.id);
            await loadClienteContatosResumo(saved.id);
            resetClienteWhatsappMatrizForm();
        } else if (isEdit && editId) {
            await loadWhatsappMatrizesCliente(Number(editId));
            await loadClienteContatosResumo(Number(editId));
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
    document.getElementById('clienteIe').value = '';
    document.getElementById('clienteCep').value = '';
    document.getElementById('clienteSite').value = '';
    document.getElementById('clienteHorarioFuncionamento').value = '';
    const obsEl = document.getElementById('observacoes');
    if (obsEl) {
        obsEl.value = '';
    }
    clienteWhatsappMatrizSection?.classList.add('hidden');
    resetClienteWhatsappMatrizForm();
    if (clienteWhatsappMatrizList) {
        clienteWhatsappMatrizList.innerHTML = '';
    }
    setSelectedClienteListItem(null);
    clearClienteContatosResumo();
    clearAlertFn(alertBoxClientes);
    if (clienteArteFile) {
        clienteArteFile.value = '';
    }
    atualizarPreviewArteCliente(null);
    if (!keepFormVisible) {
        setClienteFormUiState('idle');
    }
}
