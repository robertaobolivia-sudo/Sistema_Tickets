import { createListPaginationController } from '../components/listPaginationBar.js';
import { buildTicketBuscaParams } from '../core/queryParams.js';
import { slicePageItems } from '../core/listPagination.js';
import * as ticketService from '../services/ticketService.js';

let showAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatDateTimeFn = () => '-';
let sortTicketsByPriorityFn = (t) => t;
let getTicketPriorityRowClassFn = () => '';
let getStatusClassFn = () => 'status-badge';
let formatPriorityBadgeHtmlFn = () => '';
let formatSlaBadgeHtmlFn = () => '-';
let formatSlaResolucaoCellHtmlFn = () => '-';
let openDetailsFn = () => {};
let changeTicketStatusFn = () => {};

const statusFilterTickets = document.getElementById('statusFilter2');
const ticketsBodyList = document.getElementById('ticketsBody2');
const emptyMessageList = document.getElementById('emptyMessage2');
const ticketFiltroTexto = document.getElementById('ticketFiltroTexto');
const ticketFiltroStatus = document.getElementById('ticketFiltroStatus');
const ticketFiltroConexao = document.getElementById('ticketFiltroConexao');
const ticketFiltroCliente = document.getElementById('ticketFiltroCliente');
const ticketFiltroAnalistaId = document.getElementById('ticketFiltroAnalistaId');
const ticketFiltroDataInicio = document.getElementById('ticketFiltroDataInicio');
const ticketFiltroDataFim = document.getElementById('ticketFiltroDataFim');
const ticketFiltroPrioridade = document.getElementById('ticketFiltroPrioridade');
const ticketBuscaBtn = document.getElementById('ticketBuscaBtn');
const ticketLimparFiltrosBtn = document.getElementById('ticketLimparFiltrosBtn');
const alertBoxTicketsPage = document.getElementById('alertBoxTickets');
const ticketsListaPagination = document.getElementById('ticketsListaPagination');

let listenersBound = false;
/** Evita que listagem padrão (async) sobrescreva resultado de busca avançada mais recente. */
let ticketTableLoadSeq = 0;
let cachedTicketsList = [];
let ticketsListPage = 1;

const ticketsListPagination = createListPaginationController(ticketsListaPagination, {
    ariaLabel: 'Paginação da lista de tickets',
    onPageChange(page) {
        ticketsListPage = page;
        renderTable(cachedTicketsList, ticketsBodyList, emptyMessageList);
    }
});

function canAttend(ticket) {
    return ticket?.status === 'ABERTO';
}

export function initTicketsPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDateTime) formatDateTimeFn = deps.formatDateTime;
    if (deps.sortTicketsByPriority) sortTicketsByPriorityFn = deps.sortTicketsByPriority;
    if (deps.getTicketPriorityRowClass) getTicketPriorityRowClassFn = deps.getTicketPriorityRowClass;
    if (deps.getStatusClass) getStatusClassFn = deps.getStatusClass;
    if (deps.formatPriorityBadgeHtml) formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml;
    if (deps.formatSlaBadgeHtml) formatSlaBadgeHtmlFn = deps.formatSlaBadgeHtml;
    if (deps.formatSlaResolucaoCellHtml) formatSlaResolucaoCellHtmlFn = deps.formatSlaResolucaoCellHtml;
    if (deps.openDetails) openDetailsFn = deps.openDetails;
    if (deps.changeTicketStatus) changeTicketStatusFn = deps.changeTicketStatus;
    if (listenersBound) return;
    listenersBound = true;

    ticketBuscaBtn?.addEventListener('click', () => {
        ticketsListPage = 1;
        searchTicketsAdvanced();
    });
    ticketLimparFiltrosBtn?.addEventListener('click', async () => {
        clearTicketSearchFilters();
        ticketsListPage = 1;
        await loadTicketTableDefault();
    });
    statusFilterTickets?.addEventListener('change', loadTicketTable);
    ticketsBodyList?.addEventListener('click', event => {
        const button = event.target.closest('button[data-action]');
        if (!button) return;
        const action = button.dataset.action;
        const ticketNumber = button.dataset.ticket;
        if (action === 'details') return openDetailsFn(ticketNumber);
        changeTicketStatusFn(ticketNumber, action);
    });
}

export async function loadTicketTable() {
    ticketsListPage = 1;
    if (hasActiveTicketSearchFilters()) {
        await searchTicketsAdvanced();
        return;
    }
    await loadTicketTableDefault();
}

function buildTicketBuscaQueryString() {
    return buildTicketBuscaParams({
        textoLivre: ticketFiltroTexto?.value,
        status: ticketFiltroStatus?.value,
        conexao: ticketFiltroConexao?.value,
        cliente: ticketFiltroCliente?.value,
        analistaId: ticketFiltroAnalistaId?.value,
        dataInicio: ticketFiltroDataInicio?.value,
        dataFim: ticketFiltroDataFim?.value,
        prioridade: ticketFiltroPrioridade?.value
    }).toString();
}

function hasActiveTicketSearchFilters() {
    return Boolean(
        ticketFiltroTexto?.value?.trim()
        || ticketFiltroStatus?.value?.trim()
        || ticketFiltroConexao?.value?.trim()
        || ticketFiltroCliente?.value?.trim()
        || ticketFiltroAnalistaId?.value?.trim()
        || ticketFiltroDataInicio?.value
        || ticketFiltroDataFim?.value
        || ticketFiltroPrioridade?.value?.trim()
    );
}

function clearTicketSearchFilters() {
    if (ticketFiltroTexto) ticketFiltroTexto.value = '';
    if (ticketFiltroStatus) ticketFiltroStatus.value = '';
    if (ticketFiltroConexao) ticketFiltroConexao.value = '';
    if (ticketFiltroCliente) ticketFiltroCliente.value = '';
    if (ticketFiltroAnalistaId) ticketFiltroAnalistaId.value = '';
    if (ticketFiltroDataInicio) ticketFiltroDataInicio.value = '';
    if (ticketFiltroDataFim) ticketFiltroDataFim.value = '';
    if (ticketFiltroPrioridade) ticketFiltroPrioridade.value = '';
    if (statusFilterTickets) statusFilterTickets.value = 'ALL';
}

async function searchTicketsAdvanced() {
    const seq = ++ticketTableLoadSeq;
    try {
        const tickets = await ticketService.searchTickets(buildTicketBuscaQueryString());
        if (seq !== ticketTableLoadSeq) {
            return;
        }
        cachedTicketsList = Array.isArray(tickets) ? tickets : [];
        ticketsListPage = 1;
        renderTable(cachedTicketsList, ticketsBodyList, emptyMessageList);
        if (!tickets.length) {
            emptyMessageList?.classList.remove('hidden');
            if (emptyMessageList) emptyMessageList.textContent = 'Nenhum ticket encontrado.';
        }
    } catch (error) {
        if (seq !== ticketTableLoadSeq) {
            return;
        }
        cachedTicketsList = [];
        ticketsListPagination.setState({ page: 1, total: 0 });
        renderTable([], ticketsBodyList, emptyMessageList);
        if (emptyMessageList) {
            emptyMessageList.textContent = 'Não foi possível buscar os tickets.';
            emptyMessageList.classList.remove('hidden');
        }
        showAlertFn(error.message, alertBoxTicketsPage);
    }
}

async function loadTicketTableDefault() {
    const seq = ++ticketTableLoadSeq;
    try {
        const tickets = await ticketService.listTickets('ALL');
        if (seq !== ticketTableLoadSeq) {
            return;
        }
        cachedTicketsList = Array.isArray(tickets) ? tickets : [];
        ticketsListPage = 1;
        renderTable(cachedTicketsList, ticketsBodyList, emptyMessageList);
    } catch (error) {
        if (seq !== ticketTableLoadSeq) {
            return;
        }
        renderTable([], ticketsBodyList, emptyMessageList);
        showAlertFn(error.message, alertBoxTicketsPage);
    }
}

function renderTable(tickets, bodyElement, emptyMessageElement) {
    if (!bodyElement) return;
    bodyElement.innerHTML = '';
    const sortedTickets = sortTicketsByPriorityFn(tickets);
    if (!sortedTickets.length) {
        ticketsListPagination.setState({ page: 1, total: 0 });
        emptyMessageElement?.classList.remove('hidden');
        return;
    }
    emptyMessageElement?.classList.add('hidden');

    ticketsListPagination.setState({ page: ticketsListPage, total: sortedTickets.length });
    ticketsListPage = ticketsListPagination.getPage();
    const pageTickets = slicePageItems(sortedTickets, ticketsListPage);

    pageTickets.forEach(ticket => {
        const row = document.createElement('tr');
        const rowClass = getTicketPriorityRowClassFn(ticket.prioridade);
        if (rowClass) {
            row.className = rowClass;
        }
        const attendDisabled = canAttend(ticket) ? '' : 'disabled';
        const numeroTicketAttr = ticket.numeroTicket ? String(ticket.numeroTicket) : '';
        row.innerHTML = `
            <td>${displayValueFn(ticket.numeroTicket)}</td>
            <td>${displayValueFn(ticket.cliente)}</td>
            <td>${displayValueFn(ticket.telefone)}</td>
            <td>${displayValueFn(ticket.canal)}</td>
            <td><span class="${getStatusClassFn(ticket.status)}">${displayValueFn(ticket.status)}</span></td>
            <td>${formatPriorityBadgeHtmlFn(ticket.prioridade)}</td>
            <td>${formatSlaBadgeHtmlFn(ticket.slaPrimeiroAtendimentoStatus)}</td>
            <td>${formatSlaResolucaoCellHtmlFn(ticket)}</td>
            <td>${formatDateTimeFn(ticket.dataAbertura)}</td>
            <td>
                <div class="action-group">
                    <button class="button button-secondary" data-action="details" data-ticket="${numeroTicketAttr}">Ver detalhes</button>
                    <button class="button button-primary" data-action="attend" data-ticket="${numeroTicketAttr}" ${attendDisabled}>Atender</button>
                </div>
            </td>
        `;

        bodyElement.appendChild(row);
    });
}
