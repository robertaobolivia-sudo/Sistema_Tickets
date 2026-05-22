import * as ticketService from '../services/ticketService.js';
import * as clienteService from '../services/clienteService.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let onTicketCreatedSuccessFn = async () => {};

const ticketForm = document.getElementById('ticketForm');
const ticketContatoGroup = document.getElementById('ticketContatoGroup');
const ticketContatoSolicitante = document.getElementById('ticketContatoSolicitante');
const ticketPrioridade = document.getElementById('ticketPrioridade');
const clienteBusca = document.getElementById('clienteBusca');
const clienteResultados = document.getElementById('clienteResultados');
const clienteSelecionadoBox = document.getElementById('clienteSelecionadoBox');
const alertBoxTicket = document.getElementById('alertBoxTicket');

let clienteSelecionado = null;
let listenersBound = false;

function showAlert(message, box, type) {
    showAlertFn(message, box, type);
}

function formatContatoTelefoneCelular(contato) {
    const telefone = contato?.telefone && contato.telefone !== '-' ? contato.telefone : '';
    const celular = contato?.celular && contato.celular !== '-' ? contato.celular : '';
    if (telefone && celular) {
        return `${telefone} / ${celular}`;
    }
    return telefone || celular || '-';
}

async function loadContatosTicket(clienteId) {
    if (!ticketContatoSolicitante || !ticketContatoGroup) {
        return;
    }
    ticketContatoSolicitante.innerHTML = '<option value="">Nenhum</option>';
    if (!clienteId) {
        ticketContatoGroup.classList.add('hidden');
        return;
    }
    try {
        const contatos = await clienteService.listContatosAtivos(clienteId);
        if (!contatos?.length) {
            ticketContatoGroup.classList.add('hidden');
            return;
        }
        contatos.forEach(contato => {
            const option = document.createElement('option');
            option.value = contato.id;
            option.textContent = `${contato.nome} - ${formatContatoTelefoneCelular(contato)}`;
            ticketContatoSolicitante.appendChild(option);
        });
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

async function submitTicket(event) {
    event.preventDefault();
    if (!clienteSelecionado) {
        showAlert('Selecione um cliente cadastrado antes de abrir o ticket.', alertBoxTicket);
        return;
    }
    const payload = {
        cliente: clienteSelecionado.nome,
        telefone: clienteSelecionado.telefone,
        canal: document.getElementById('canal')?.value.trim() || '',
        conexao: clienteSelecionado.carteira || document.getElementById('conexao')?.value.trim() || '',
        mensagem: document.getElementById('mensagem')?.value.trim() || '',
        prioridade: ticketPrioridade?.value || 'MEDIA'
    };
    const contatoId = ticketContatoSolicitante?.value;
    if (contatoId) {
        payload.contatoSolicitanteId = Number(contatoId);
    }

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
