import * as ticketService from '@features/tickets/ticket-service.js';
import * as clienteService from '@features/clientes/cliente-service.js';
import * as contatoService from '@features/contatos/contato-service.js';
import {
    buildAbrirTicketPayloadFromForm,
    filtrarContatosWhatsappAtivos,
    formatContatoWhatsappLabel,
    getAbrirTicketContatoLabel,
    getAbrirTicketSemContatosOrientacao,
    shouldShowCadastrarContatoLink,
    validateAbrirTicketSubmit
} from '@features/tickets/abrir-ticket-payload-view.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let onTicketCreatedSuccessFn = async () => {};
let onCadastrarContatoFn = async () => {};

const ticketForm = document.getElementById('ticketForm');
const ticketContatoGroup = document.getElementById('ticketContatoGroup');
const ticketContatoWhatsapp = document.getElementById('ticketContatoWhatsapp');
const ticketContatoLabel = document.querySelector('label[for="ticketContatoWhatsapp"]');
const ticketPrioridade = document.getElementById('ticketPrioridade');
const clienteBusca = document.getElementById('clienteBusca');
const clienteResultados = document.getElementById('clienteResultados');
const clienteSelecionadoBox = document.getElementById('clienteSelecionadoBox');
const alertBoxTicket = document.getElementById('alertBoxTicket');

let clienteSelecionado = null;
let contatosReaisAtivosCount = 0;
let listenersBound = false;

function ensureTicketContatoSemContatosHint() {
    let hint = document.getElementById('ticketContatoSemContatosHint');
    if (!hint && ticketContatoGroup) {
        hint = document.createElement('p');
        hint.id = 'ticketContatoSemContatosHint';
        hint.className = 'form-hint hidden';
        hint.setAttribute('role', 'status');
        ticketContatoGroup.appendChild(hint);
    }
    return hint;
}

function ensureTicketContatoCadastrarBtn() {
    let wrap = document.getElementById('ticketContatoSemContatosActions');
    if (!wrap && ticketContatoGroup) {
        wrap = document.createElement('div');
        wrap.id = 'ticketContatoSemContatosActions';
        wrap.className = 'form-hint-actions hidden';
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.id = 'ticketContatoCadastrarBtn';
        btn.className = 'btn btn-secondary btn-sm';
        btn.textContent = 'Cadastrar Contato';
        btn.addEventListener('click', () => {
            if (clienteSelecionado?.id) {
                void onCadastrarContatoFn(clienteSelecionado.id);
            }
        });
        wrap.appendChild(btn);
        ticketContatoGroup.appendChild(wrap);
    }
    return wrap;
}

function updateSemContatosUi() {
    const hint = ensureTicketContatoSemContatosHint();
    const actions = ensureTicketContatoCadastrarBtn();
    const semContatos = contatosReaisAtivosCount <= 0;
    const showOrientacao =
        semContatos && shouldShowCadastrarContatoLink(contatosReaisAtivosCount, clienteSelecionado?.id);
    if (ticketContatoWhatsapp) {
        ticketContatoWhatsapp.disabled = semContatos;
    }
    if (hint) {
        if (showOrientacao) {
            hint.textContent = getAbrirTicketSemContatosOrientacao();
            hint.classList.remove('hidden');
        } else {
            hint.textContent = '';
            hint.classList.add('hidden');
        }
    }
    if (actions) {
        actions.classList.toggle('hidden', !showOrientacao);
    }
}

function showAlert(message, box, type) {
    showAlertFn(message, box, type);
}

function resetContatoSelect() {
    if (!ticketContatoWhatsapp) {
        return;
    }
    ticketContatoWhatsapp.innerHTML = '<option value="">Selecione o Contato</option>';
}

function appendWhatsappContatoOptions(contatos) {
    contatos.forEach(contato => {
        const option = document.createElement('option');
        option.value = String(contato.id);
        if (contato.whatsapp) {
            option.dataset.whatsapp = contato.whatsapp;
        }
        const nome = contato.nome && String(contato.nome).trim() ? contato.nome.trim() : 'Contato';
        option.dataset.nome = nome;
        option.textContent = formatContatoWhatsappLabel(contato);
        ticketContatoWhatsapp.appendChild(option);
    });
}

async function loadContatosTicket(clienteId) {
    if (!ticketContatoWhatsapp || !ticketContatoGroup) {
        return;
    }
    resetContatoSelect();
    contatosReaisAtivosCount = 0;
    if (!clienteId) {
        ticketContatoGroup.classList.add('hidden');
        if (ticketContatoLabel) {
            ticketContatoLabel.textContent = 'Contato atendido (WhatsApp)';
        }
        updateSemContatosUi();
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

        contatosReaisAtivosCount = contatosReais.length;
        if (ticketContatoLabel) {
            ticketContatoLabel.textContent = getAbrirTicketContatoLabel(contatosReaisAtivosCount);
        }
        if (contatosReais.length > 0) {
            appendWhatsappContatoOptions(contatosReais);
        }
        ticketContatoGroup.classList.remove('hidden');
        updateSemContatosUi();
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
    if (clienteField) clienteField.value = cliente.nome || '';
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
    if (clienteField) clienteField.value = '';
    if (telefoneField) telefoneField.value = '';
}

function readContatoWhatsappSelection() {
    const selected = ticketContatoWhatsapp?.selectedOptions?.[0];
    if (!selected?.value) {
        return null;
    }
    return {
        id: selected.value,
        whatsapp: selected.dataset.whatsapp || '',
        nome: selected.dataset.nome || ''
    };
}

async function submitTicket(event) {
    event.preventDefault();
    if (!clienteSelecionado) {
        showAlert('Selecione um cliente cadastrado antes de abrir o ticket.', alertBoxTicket);
        return;
    }
    const contato = readContatoWhatsappSelection();
    const validationError = validateAbrirTicketSubmit(contatosReaisAtivosCount, contato?.id);
    if (validationError) {
        showAlert(validationError, alertBoxTicket);
        return;
    }
    const payload = buildAbrirTicketPayloadFromForm(
        clienteSelecionado,
        {
            canal: document.getElementById('canal')?.value.trim() || '',
            mensagem: document.getElementById('mensagem')?.value.trim() || '',
            prioridade: ticketPrioridade?.value || 'MEDIA'
        },
        contato
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
    if (deps.onCadastrarContato) onCadastrarContatoFn = deps.onCadastrarContato;
    if (listenersBound) return;
    listenersBound = true;

    loadAbrirTicketPage();
    ticketForm?.addEventListener('submit', submitTicket);
    ticketForm?.addEventListener('reset', limparClienteSelecionado);
    clienteBusca?.addEventListener('input', pesquisarClientes);
}
