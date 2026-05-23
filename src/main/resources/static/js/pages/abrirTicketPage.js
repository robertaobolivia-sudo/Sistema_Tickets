import * as ticketService from '../services/ticketService.js';
import * as clienteService from '../services/clienteService.js';
import * as contatoService from '../services/contatoService.js';
import {
    ABIR_TICKET_LEGADO_PANEL_SUMMARY,
    buildAbrirTicketPayloadFromForm,
    CONTATO_SOURCE_SOLICITANTE_LEGADO,
    CONTATO_SOURCE_WHATSAPP,
    filtrarContatosWhatsappAtivos,
    formatContatoClienteLegadoLabel,
    formatContatoWhatsappLabel,
    getAbrirTicketContatoLabel,
    resolveAbrirTicketContatoUiMode,
    shouldInlineLegadoContatoClienteOptions
} from '../core/abrirTicketPayloadView.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let onTicketCreatedSuccessFn = async () => {};

const ticketForm = document.getElementById('ticketForm');
const ticketContatoGroup = document.getElementById('ticketContatoGroup');
const ticketContatoSolicitante = document.getElementById('ticketContatoSolicitante');
const ticketContatoLabel = document.querySelector('label[for="ticketContatoSolicitante"]');
const ticketPrioridade = document.getElementById('ticketPrioridade');
const clienteBusca = document.getElementById('clienteBusca');
const clienteResultados = document.getElementById('clienteResultados');
const clienteSelecionadoBox = document.getElementById('clienteSelecionadoBox');
const alertBoxTicket = document.getElementById('alertBoxTicket');

let clienteSelecionado = null;
let listenersBound = false;
let legadoPanelToggleBound = false;
let legadoOptionsLoadedForClienteId = null;

function getTicketContatoLegadoPanel() {
    return document.getElementById('ticketContatoLegadoPanel');
}

function getTicketContatoLegadoSelect() {
    return document.getElementById('ticketContatoLegadoSelect');
}

function ensureTicketContatoLegadoPanel() {
    let panel = getTicketContatoLegadoPanel();
    if (panel || !ticketContatoGroup) {
        return panel;
    }
    panel = document.createElement('details');
    panel.id = 'ticketContatoLegadoPanel';
    panel.className = 'hidden';
    const summary = document.createElement('summary');
    summary.textContent = ABIR_TICKET_LEGADO_PANEL_SUMMARY;
    const select = document.createElement('select');
    select.id = 'ticketContatoLegadoSelect';
    select.innerHTML = '<option value="">Nenhum</option>';
    panel.appendChild(summary);
    panel.appendChild(select);
    ticketContatoGroup.appendChild(panel);
    select.addEventListener('change', () => {
        if (select.value && ticketContatoSolicitante) {
            ticketContatoSolicitante.value = '';
        }
    });
    if (!legadoPanelToggleBound) {
        legadoPanelToggleBound = true;
        panel.addEventListener('toggle', () => {
            if (panel.open && clienteSelecionado?.id) {
                void loadLegadoSelectOptions(clienteSelecionado.id);
            }
        });
    }
    return panel;
}

function hideTicketContatoLegadoPanel() {
    const panel = getTicketContatoLegadoPanel();
    if (!panel) {
        return;
    }
    panel.open = false;
    panel.classList.add('hidden');
    const select = getTicketContatoLegadoSelect();
    if (select) {
        select.innerHTML = '<option value="">Nenhum</option>';
    }
    legadoOptionsLoadedForClienteId = null;
}

function showTicketContatoLegadoPanel() {
    const panel = ensureTicketContatoLegadoPanel();
    if (panel) {
        panel.classList.remove('hidden');
        panel.open = false;
    }
}

async function loadLegadoSelectOptions(clienteId) {
    if (legadoOptionsLoadedForClienteId === clienteId) {
        return;
    }
    const select = getTicketContatoLegadoSelect();
    if (!select) {
        return;
    }
    select.innerHTML = '<option value="">Nenhum</option>';
    const contatos = await clienteService.listContatosAtivos(clienteId);
    if (!contatos?.length) {
        return;
    }
    contatos.forEach(contato => {
        const option = document.createElement('option');
        option.value = String(contato.id);
        option.textContent = formatContatoClienteLegadoLabel(contato);
        select.appendChild(option);
    });
    legadoOptionsLoadedForClienteId = clienteId;
}

function bindMainContatoSelectClearLegado() {
    if (!ticketContatoSolicitante || ticketContatoSolicitante.dataset.f4LegadoClearBound) {
        return;
    }
    ticketContatoSolicitante.dataset.f4LegadoClearBound = '1';
    ticketContatoSolicitante.addEventListener('change', () => {
        const selected = ticketContatoSolicitante.selectedOptions?.[0];
        if (selected?.value && selected.dataset.source === CONTATO_SOURCE_WHATSAPP) {
            const legado = getTicketContatoLegadoSelect();
            if (legado) {
                legado.value = '';
            }
        }
    });
}

function showAlert(message, box, type) {
    showAlertFn(message, box, type);
}

function resetContatoSelect() {
    if (!ticketContatoSolicitante) {
        return;
    }
    ticketContatoSolicitante.innerHTML = '<option value="">Nenhum</option>';
}

function appendWhatsappContatoOptions(contatos) {
    const og = document.createElement('optgroup');
    og.label = 'Contato atendido (WhatsApp)';
    contatos.forEach(contato => {
        const option = document.createElement('option');
        option.value = String(contato.id);
        option.dataset.source = CONTATO_SOURCE_WHATSAPP;
        if (contato.whatsapp) {
            option.dataset.whatsapp = contato.whatsapp;
        }
        const nome = contato.nome && String(contato.nome).trim() ? contato.nome.trim() : 'Contato';
        option.dataset.nome = nome;
        option.textContent = formatContatoWhatsappLabel(contato);
        og.appendChild(option);
    });
    ticketContatoSolicitante.appendChild(og);
}

async function appendContatoClienteLegadoOptions(clienteId) {
    const contatos = await clienteService.listContatosAtivos(clienteId);
    if (!contatos?.length) {
        return false;
    }
    const og = document.createElement('optgroup');
    og.label = 'Solicitante cadastro (legado)';
    contatos.forEach(contato => {
        const option = document.createElement('option');
        option.value = String(contato.id);
        option.dataset.source = CONTATO_SOURCE_SOLICITANTE_LEGADO;
        option.textContent = formatContatoClienteLegadoLabel(contato);
        og.appendChild(option);
    });
    ticketContatoSolicitante.appendChild(og);
    return true;
}

async function loadContatosTicket(clienteId) {
    if (!ticketContatoSolicitante || !ticketContatoGroup) {
        return;
    }
    resetContatoSelect();
    hideTicketContatoLegadoPanel();
    bindMainContatoSelectClearLegado();
    if (!clienteId) {
        ticketContatoGroup.classList.add('hidden');
        if (ticketContatoLabel) {
            ticketContatoLabel.textContent = 'Contato (opcional)';
        }
        return;
    }
    try {
        let contatosReais = [];
        try {
            const lista = await contatoService.listarPorCliente(clienteId);
            contatosReais = filtrarContatosWhatsappAtivos(lista);
        } catch {
            contatosReais = [];
        }

        const uiMode = resolveAbrirTicketContatoUiMode(contatosReais.length);
        if (ticketContatoLabel) {
            ticketContatoLabel.textContent = getAbrirTicketContatoLabel(uiMode);
        }

        let temOpcao = false;
        if (contatosReais.length > 0) {
            appendWhatsappContatoOptions(contatosReais);
            temOpcao = true;
            showTicketContatoLegadoPanel();
        } else if (shouldInlineLegadoContatoClienteOptions(uiMode)) {
            temOpcao = await appendContatoClienteLegadoOptions(clienteId);
        }

        if (!temOpcao) {
            ticketContatoGroup.classList.add('hidden');
            return;
        }
        ticketContatoGroup.classList.remove('hidden');
    } catch {
        ticketContatoGroup.classList.add('hidden');
    }
}

function renderClienteResultados(clientes) {
    if (!clienteResultados) return;
    clienteResultados.innerHTML = '';
    if (!clientes.length) {
        clienteResultados.innerHTML = '<div class="cliente-search-item"><span>Nenhum cliente encontrado.</span></div>';
        clienteResultados.classList.remove('hidden');
        return;
    }

    clientes.forEach(cliente => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'cliente-search-item';
        button.innerHTML = `
            <strong>${cliente.nome || '-'}</strong>
            <span>${cliente.telefone || '-'} - ${cliente.empresa || 'Sem empresa'} - ${cliente.cnpj || 'Sem CNPJ'}</span>
        `;
        button.addEventListener('click', () => selecionarCliente(cliente));
        clienteResultados.appendChild(button);
    });
    clienteResultados.classList.remove('hidden');
}

async function pesquisarClientes() {
    if (!clienteBusca) return;
    const termo = clienteBusca.value.trim();
    if (termo.length < 2) {
        clienteResultados?.classList.add('hidden');
        if (clienteResultados) clienteResultados.innerHTML = '';
        return;
    }
    try {
        const clientes = await clienteService.listOrSearch(termo, false);
        renderClienteResultados(Array.isArray(clientes) ? clientes : []);
    } catch (error) {
        showAlert(error.message, alertBoxTicket);
    }
}

function selecionarCliente(cliente) {
    clienteSelecionado = cliente;
    loadContatosTicket(cliente.id);
    const clienteField = document.getElementById('cliente');
    const telefoneField = document.getElementById('telefone');
    const conexaoField = document.getElementById('conexao');
    if (clienteField) clienteField.value = cliente.nome || '';
    if (telefoneField) telefoneField.value = cliente.telefone || '';
    if (conexaoField) conexaoField.value = cliente.carteira || '';
    const nomeEl = document.getElementById('clienteSelecionadoNome');
    const telEl = document.getElementById('clienteSelecionadoTelefone');
    const telContatoEl = document.getElementById('clienteSelecionadoTelefoneContato');
    const emailEl = document.getElementById('clienteSelecionadoEmail');
    const empresaEl = document.getElementById('clienteSelecionadoEmpresa');
    const statusEl = document.getElementById('clienteSelecionadoStatus');
    if (nomeEl) nomeEl.value = cliente.nome || '';
    if (telEl) telEl.value = cliente.telefone || '';
    if (telContatoEl) telContatoEl.value = cliente.telefoneContato || '';
    if (emailEl) emailEl.value = cliente.email || '';
    if (empresaEl) empresaEl.value = cliente.empresa || '';
    if (statusEl) statusEl.value = cliente.status || '';
    if (clienteBusca) clienteBusca.value = cliente.nome || '';
    clienteSelecionadoBox?.classList.remove('hidden');
    clienteResultados?.classList.add('hidden');
}

export function limparClienteSelecionado() {
    clienteSelecionado = null;
    loadContatosTicket(null);
    if (clienteBusca) clienteBusca.value = '';
    if (clienteResultados) {
        clienteResultados.innerHTML = '';
        clienteResultados.classList.add('hidden');
    }
    clienteSelecionadoBox?.classList.add('hidden');
    const clienteField = document.getElementById('cliente');
    const telefoneField = document.getElementById('telefone');
    const conexaoField = document.getElementById('conexao');
    if (clienteField) clienteField.value = '';
    if (telefoneField) telefoneField.value = '';
    if (conexaoField) conexaoField.value = '';
}

function readContatoSelectionFromSelect() {
    const selected = ticketContatoSolicitante?.selectedOptions?.[0];
    if (selected?.value && selected.dataset.source === CONTATO_SOURCE_WHATSAPP) {
        return {
            source: CONTATO_SOURCE_WHATSAPP,
            id: selected.value,
            whatsapp: selected.dataset.whatsapp || '',
            nome: selected.dataset.nome || ''
        };
    }
    if (selected?.value && selected.dataset.source === CONTATO_SOURCE_SOLICITANTE_LEGADO) {
        return {
            source: CONTATO_SOURCE_SOLICITANTE_LEGADO,
            id: selected.value
        };
    }
    const legadoSelect = getTicketContatoLegadoSelect();
    if (legadoSelect?.value) {
        return {
            source: CONTATO_SOURCE_SOLICITANTE_LEGADO,
            id: legadoSelect.value
        };
    }
    return null;
}

async function submitTicket(event) {
    event.preventDefault();
    if (!clienteSelecionado) {
        showAlert('Selecione um cliente cadastrado antes de abrir o ticket.', alertBoxTicket);
        return;
    }
    const payload = buildAbrirTicketPayloadFromForm(
        clienteSelecionado,
        {
            canal: document.getElementById('canal')?.value.trim() || '',
            conexao: document.getElementById('conexao')?.value.trim() || '',
            mensagem: document.getElementById('mensagem')?.value.trim() || '',
            prioridade: ticketPrioridade?.value || 'MEDIA'
        },
        readContatoSelectionFromSelect()
    );

    try {
        const createdTicket = await ticketService.createTicket(payload);
        ticketForm?.reset();
        if (ticketPrioridade) ticketPrioridade.value = 'MEDIA';
        limparClienteSelecionado();
        showAlert('Ticket criado com sucesso!', alertBoxTicket, 'success');
        await onTicketCreatedSuccessFn(createdTicket);
    } catch (error) {
        showAlert(error.message, alertBoxTicket);
    }
}

export function loadAbrirTicketPage() {
    clearAlertFn(alertBoxTicket);
}

export function initAbrirTicketPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.onTicketCreatedSuccess) onTicketCreatedSuccessFn = deps.onTicketCreatedSuccess;
    if (listenersBound) return;
    listenersBound = true;

    ticketForm?.addEventListener('submit', submitTicket);
    ticketForm?.addEventListener('reset', limparClienteSelecionado);
    clienteBusca?.addEventListener('input', pesquisarClientes);
}
