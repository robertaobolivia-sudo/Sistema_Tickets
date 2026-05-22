import * as dashboardService from '../services/dashboardService.js';
import * as clienteService from '../services/clienteService.js';
import * as notificacaoService from '../services/notificacaoService.js';
import { filterClientesAtivosIndicadores } from '../core/encerramentoAvaliacaoView.js';
import { mensagemParaExibirUsuario } from '../core/messages.js';
import {
    buildDashboardEncerramentoQueryParams,
    normalizeDashboardEncerramentoResumo
} from '../core/dashboardEncerramentoView.js';

let showAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatDateTimeFn = () => '-';
let sortTicketsByPriorityFn = (tickets) => (Array.isArray(tickets) ? tickets : []);
let getTicketPriorityRowClassFn = () => '';
let getStatusClassFn = () => 'status-badge';
let formatPriorityBadgeHtmlFn = () => '-';
let formatSlaBadgeHtmlFn = () => '-';
let formatSlaResolucaoCellHtmlFn = () => '-';
let openDetailsFn = () => {};
let escapeAttrFn = (v) => String(v ?? '');
let initTicketAlertBaselineFn = () => {};
let checkTicketAlertVisualFn = () => {};
let refreshNotificacoesUiFn = async () => {};

const totalTickets = document.getElementById('totalTickets');
const openTickets = document.getElementById('openTickets');
const inProgressTickets = document.getElementById('inProgressTickets');
const resolvedTickets = document.getElementById('resolvedTickets');
const cancelledTickets = document.getElementById('cancelledTickets');
const unassignedTickets = document.getElementById('unassignedTickets');
const openedTodayTickets = document.getElementById('openedTodayTickets');
const resolvedTodayTickets = document.getElementById('resolvedTodayTickets');
const activeClients = document.getElementById('activeClients');
const analystsOnline = document.getElementById('analystsOnline');
const analystsAway = document.getElementById('analystsAway');
const analystsOffline = document.getElementById('analystsOffline');
const avgFirstResponse = document.getElementById('avgFirstResponse');
const avgResolution = document.getElementById('avgResolution');
const dashboardByStatus = document.getElementById('dashboardByStatus');
const dashboardByConnection = document.getElementById('dashboardByConnection');
const dashboardByAnalyst = document.getElementById('dashboardByAnalyst');
const dashboardPrioridadeCritica = document.getElementById('dashboardPrioridadeCritica');
const dashboardPrioridadeAlta = document.getElementById('dashboardPrioridadeAlta');
const dashboardPrioridadeMedia = document.getElementById('dashboardPrioridadeMedia');
const dashboardPrioridadeBaixa = document.getElementById('dashboardPrioridadeBaixa');
const dashboardPrioridadeSem = document.getElementById('dashboardPrioridadeSem');
const dashboardTopGrupos = document.getElementById('dashboardTopGrupos');
const dashboardTopSubgrupos = document.getElementById('dashboardTopSubgrupos');
const dashboardCriticosBody = document.getElementById('dashboardCriticosBody');
const dashboardCriticosEmpty = document.getElementById('dashboardCriticosEmpty');
const alertBoxDashboard = document.getElementById('alertBox');
const conexoesPendencias = document.getElementById('conexoesPendencias');
const refreshButton = document.getElementById('refreshButton');
const dashboardSlaPrimeiroDentro = document.getElementById('dashboardSlaPrimeiroDentro');
const dashboardSlaPrimeiroVencido = document.getElementById('dashboardSlaPrimeiroVencido');
const dashboardSlaPrimeiroCumprido = document.getElementById('dashboardSlaPrimeiroCumprido');
const dashboardSlaPrimeiroViolado = document.getElementById('dashboardSlaPrimeiroViolado');
const dashboardSlaPrimeiroNaoCalc = document.getElementById('dashboardSlaPrimeiroNaoCalc');
const dashboardSlaPrimeiroProximo = document.getElementById('dashboardSlaPrimeiroProximo');
const dashboardSlaResolucaoDentro = document.getElementById('dashboardSlaResolucaoDentro');
const dashboardSlaResolucaoProximo = document.getElementById('dashboardSlaResolucaoProximo');
const dashboardSlaResolucaoPausado = document.getElementById('dashboardSlaResolucaoPausado');
const dashboardSlaResolucaoVencido = document.getElementById('dashboardSlaResolucaoVencido');
const dashboardSlaResolucaoCumprido = document.getElementById('dashboardSlaResolucaoCumprido');
const dashboardSlaResolucaoViolado = document.getElementById('dashboardSlaResolucaoViolado');
const dashboardSlaResolucaoNaoCalc = document.getElementById('dashboardSlaResolucaoNaoCalc');
const dashboardSlaPorPrioridadeBody = document.getElementById('dashboardSlaPorPrioridadeBody');
const dashboardSlaCriticosBody = document.getElementById('dashboardSlaCriticosBody');
const dashboardSlaCriticosEmpty = document.getElementById('dashboardSlaCriticosEmpty');
const dashboardSlaTicketsEscalonados = document.getElementById('dashboardSlaTicketsEscalonados');
const dashboardSlaVerificarBtn = document.getElementById('dashboardSlaVerificarBtn');
const dashboardEncPeriodoHint = document.getElementById('dashboardEncPeriodoHint');
const dashboardEncFiltroDias = document.getElementById('dashboardEncFiltroDias');
const dashboardEncFiltroCliente = document.getElementById('dashboardEncFiltroCliente');
let dashboardEncClientesCarregados = false;
const dashboardEncMotivo = document.getElementById('dashboardEncMotivo');
const dashboardEncMedia = document.getElementById('dashboardEncMedia');
const dashboardEncRespondidas = document.getElementById('dashboardEncRespondidas');
const dashboardEncPendentes = document.getElementById('dashboardEncPendentes');
const dashboardEncExpiradas = document.getElementById('dashboardEncExpiradas');
const dashboardEncFalhasEnvio = document.getElementById('dashboardEncFalhasEnvio');

let waitTimerInterval = null;

function displayValue(v) {
    return displayValueFn(v);
}

function formatDateTime(v) {
    return formatDateTimeFn(v);
}

function showAlert(message, box, type) {
    showAlertFn(message, box, type);
}

export function parseDashboardMetricNumber(element) {
    if (!element) return 0;
    const value = parseInt(String(element.textContent || '').replace(/\D/g, ''), 10);
    return Number.isFinite(value) ? value : 0;
}

export function getDashboardAlertMetricElements() {
    return { totalTickets, openedTodayTickets };
}

function setDashboardMetric(element, value) {
    if (!element) return;
    element.textContent = value === undefined || value === null || value === '' ? '-' : value;
}

function renderDashboardGroupList(container, items, emptyMessage) {
    if (!container) return;
    container.innerHTML = '';
    if (!items || !items.length) {
        container.innerHTML = `<li class="empty-state">${emptyMessage}</li>`;
        return;
    }
    items.forEach(item => {
        const li = document.createElement('li');
        li.innerHTML = `<span>${displayValue(item.nome)}</span><strong>${item.total ?? 0}</strong>`;
        container.appendChild(li);
    });
}

function renderDashboardAnalystList(container, items) {
    if (!container) return;
    container.innerHTML = '';
    if (!items || !items.length) {
        container.innerHTML = '<li class="empty-state">Nenhum ticket por analista.</li>';
        return;
    }
    items.forEach(item => {
        const li = document.createElement('li');
        const status = item.statusOperador ? ` (${item.statusOperador})` : '';
        li.innerHTML = `<span>${item.nome || '-'}${status}</span><strong>${item.total ?? 0}</strong>`;
        container.appendChild(li);
    });
}

export function updateDashboardFromResumo(resumo) {
    initTicketAlertBaselineFn(resumo);
    setDashboardMetric(totalTickets, resumo?.totalTickets ?? 0);
    setDashboardMetric(openTickets, resumo?.ticketsAbertos ?? 0);
    setDashboardMetric(inProgressTickets, resumo?.ticketsEmAtendimento ?? 0);
    setDashboardMetric(resolvedTickets, resumo?.ticketsResolvidos ?? 0);
    setDashboardMetric(cancelledTickets, resumo?.ticketsCancelados ?? 0);
    setDashboardMetric(unassignedTickets, resumo?.ticketsSemAnalista ?? 0);
    setDashboardMetric(openedTodayTickets, resumo?.ticketsAbertosHoje ?? 0);
    setDashboardMetric(resolvedTodayTickets, resumo?.ticketsResolvidosHoje ?? 0);
    setDashboardMetric(activeClients, resumo?.clientesAtivos ?? 0);
    setDashboardMetric(analystsOnline, resumo?.analistasOnline ?? 0);
    setDashboardMetric(analystsAway, resumo?.analistasAusentes ?? 0);
    setDashboardMetric(analystsOffline, resumo?.analistasOffline ?? 0);
    setDashboardMetric(avgFirstResponse, resumo?.tempoMedioPrimeiroAtendimento ?? '-');
    setDashboardMetric(avgResolution, resumo?.tempoMedioResolucao ?? '-');
    renderDashboardGroupList(dashboardByStatus, resumo?.ticketsPorStatus, 'Nenhum ticket por status.');
    renderDashboardGroupList(dashboardByConnection, resumo?.ticketsPorConexao, 'Nenhum ticket por conexão.');
    renderDashboardAnalystList(dashboardByAnalyst, resumo?.ticketsPorAnalista);
    checkTicketAlertVisualFn(resumo);
}

function renderDashboardPrioridades(totais) {
    const dados = totais || {};
    setDashboardMetric(dashboardPrioridadeCritica, dados.critica ?? 0);
    setDashboardMetric(dashboardPrioridadeAlta, dados.alta ?? 0);
    setDashboardMetric(dashboardPrioridadeMedia, dados.media ?? 0);
    setDashboardMetric(dashboardPrioridadeBaixa, dados.baixa ?? 0);
    setDashboardMetric(dashboardPrioridadeSem, dados.semPrioridade ?? 0);
}

function renderDashboardTopGrupos(grupos) {
    if (!dashboardTopGrupos) return;
    renderDashboardGroupList(dashboardTopGrupos, grupos, 'Nenhum grupo categorizado encontrado.');
}

function renderDashboardTopSubgrupos(subgrupos) {
    if (!dashboardTopSubgrupos) return;
    dashboardTopSubgrupos.innerHTML = '';
    const items = Array.isArray(subgrupos) ? subgrupos : [];
    if (!items.length) {
        dashboardTopSubgrupos.innerHTML = '<li class="empty-state">Nenhum subgrupo categorizado encontrado.</li>';
        return;
    }
    items.forEach(item => {
        const li = document.createElement('li');
        const grupo = item.grupoNome && item.grupoNome !== '-' ? ` (${item.grupoNome})` : '';
        li.innerHTML = `<span>${displayValue(item.nome)}${grupo}</span><strong>${item.total ?? 0}</strong>`;
        dashboardTopSubgrupos.appendChild(li);
    });
}

function renderDashboardTicketsCriticos(tickets) {
    if (!dashboardCriticosBody) return;
    dashboardCriticosBody.innerHTML = '';
    const lista = sortTicketsByPriorityFn(
        (Array.isArray(tickets) ? tickets : []).filter(
            ticket => ticket.prioridade === 'CRITICA' || ticket.prioridade === 'ALTA'
        )
    );

    if (!lista.length) {
        dashboardCriticosEmpty?.classList.remove('hidden');
        if (dashboardCriticosEmpty) {
            dashboardCriticosEmpty.textContent = 'Nenhum ticket crítico ou alto no momento.';
        }
        return;
    }
    dashboardCriticosEmpty?.classList.add('hidden');

    lista.forEach(ticket => {
        const row = document.createElement('tr');
        const rowClass = getTicketPriorityRowClassFn(ticket.prioridade);
        if (rowClass) {
            row.className = rowClass;
        }
        const numeroTicketAttr = ticket.numeroTicket ? String(ticket.numeroTicket) : '';
        row.innerHTML = `
            <td>${displayValue(ticket.numeroTicket)}</td>
            <td>${displayValue(ticket.cliente)}</td>
            <td>${formatPriorityBadgeHtmlFn(ticket.prioridade)}</td>
            <td><span class="${getStatusClassFn(ticket.status)}">${displayValue(ticket.status)}</span></td>
            <td>${formatDateTime(ticket.dataAbertura)}</td>
            <td>
                <button type="button" class="button button-secondary" data-action="details" data-ticket="${numeroTicketAttr}">Ver detalhes</button>
            </td>
        `;
        dashboardCriticosBody.appendChild(row);
    });
}

function updateDashboardGerencial(gerencial) {
    renderDashboardPrioridades(gerencial?.totaisPorPrioridade);
    renderDashboardTopGrupos(gerencial?.topGrupos);
    renderDashboardTopSubgrupos(gerencial?.topSubgrupos);
    renderDashboardTicketsCriticos(gerencial?.ticketsCriticosAltos);
}

function resetDashboardGerencialOnError() {
    [
        dashboardPrioridadeCritica,
        dashboardPrioridadeAlta,
        dashboardPrioridadeMedia,
        dashboardPrioridadeBaixa,
        dashboardPrioridadeSem
    ].forEach(element => setDashboardMetric(element, '-'));
    renderDashboardGroupList(dashboardTopGrupos, [], 'Não foi possível carregar grupos.');
    renderDashboardTopSubgrupos([]);
    if (dashboardTopSubgrupos) {
        dashboardTopSubgrupos.innerHTML = '<li class="empty-state">Não foi possível carregar subgrupos.</li>';
    }
    if (dashboardCriticosBody) dashboardCriticosBody.innerHTML = '';
    dashboardCriticosEmpty?.classList.remove('hidden');
    if (dashboardCriticosEmpty) {
        dashboardCriticosEmpty.textContent = 'Não foi possível carregar tickets críticos e altos.';
    }
}

async function loadDashboardGerencial() {
    try {
        const gerencial = await dashboardService.getGerencial();
        updateDashboardGerencial(gerencial);
    } catch (error) {
        resetDashboardGerencialOnError();
        showAlert(error.message, alertBoxDashboard);
    }
}

function resetDashboardResumo() {
    const dash = '-';
    [
        totalTickets, openTickets, inProgressTickets, resolvedTickets, cancelledTickets,
        unassignedTickets, openedTodayTickets, resolvedTodayTickets, activeClients,
        analystsOnline, analystsAway, analystsOffline
    ].forEach(el => setDashboardMetric(el, dash));
    setDashboardMetric(avgFirstResponse, dash);
    setDashboardMetric(avgResolution, dash);
    renderDashboardGroupList(dashboardByStatus, [], 'Não foi possível carregar.');
    renderDashboardGroupList(dashboardByConnection, [], 'Não foi possível carregar.');
    renderDashboardAnalystList(dashboardByAnalyst, []);
}

function renderDashboardSlaResumo(data) {
    const primeiro = data?.primeiroAtendimento || {};
    const resolucao = data?.resolucao || {};
    setDashboardMetric(dashboardSlaPrimeiroDentro, primeiro.dentroDoPrazo ?? 0);
    setDashboardMetric(dashboardSlaPrimeiroProximo, primeiro.proximoVencimento ?? 0);
    setDashboardMetric(dashboardSlaPrimeiroVencido, primeiro.vencido ?? 0);
    setDashboardMetric(dashboardSlaPrimeiroCumprido, primeiro.cumprido ?? 0);
    setDashboardMetric(dashboardSlaPrimeiroViolado, primeiro.violado ?? 0);
    setDashboardMetric(dashboardSlaPrimeiroNaoCalc, primeiro.naoCalculado ?? 0);
    setDashboardMetric(dashboardSlaResolucaoDentro, resolucao.dentroDoPrazo ?? 0);
    setDashboardMetric(dashboardSlaResolucaoProximo, resolucao.proximoVencimento ?? 0);
    setDashboardMetric(dashboardSlaResolucaoPausado, resolucao.pausado ?? 0);
    setDashboardMetric(dashboardSlaResolucaoVencido, resolucao.vencido ?? 0);
    setDashboardMetric(dashboardSlaResolucaoCumprido, resolucao.cumprido ?? 0);
    setDashboardMetric(dashboardSlaResolucaoViolado, resolucao.violado ?? 0);
    setDashboardMetric(dashboardSlaResolucaoNaoCalc, resolucao.naoCalculado ?? 0);
    setDashboardMetric(dashboardSlaTicketsEscalonados, data?.ticketsEscalonados ?? 0);

    if (dashboardSlaPorPrioridadeBody) {
        dashboardSlaPorPrioridadeBody.innerHTML = '';
        const lista = Array.isArray(data?.porPrioridade) ? data.porPrioridade : [];
        if (!lista.length) {
            dashboardSlaPorPrioridadeBody.innerHTML = '<tr><td colspan="5" class="empty-state">-</td></tr>';
        } else {
            lista.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${formatPriorityBadgeHtmlFn(item.prioridade)}</td>
                    <td>${item.total ?? 0}</td>
                    <td>${item.slaPrimeiroViolado ?? 0}</td>
                    <td>${item.slaResolucaoViolado ?? 0}</td>
                    <td>${item.slaResolucaoVencido ?? 0}</td>
                `;
                dashboardSlaPorPrioridadeBody.appendChild(row);
            });
        }
    }
}

function renderDashboardTicketsCriticosSla(tickets) {
    if (!dashboardSlaCriticosBody) return;
    dashboardSlaCriticosBody.innerHTML = '';
    const lista = Array.isArray(tickets) ? tickets : [];
    if (!lista.length) {
        dashboardSlaCriticosEmpty?.classList.remove('hidden');
        return;
    }
    dashboardSlaCriticosEmpty?.classList.add('hidden');
    lista.forEach(ticket => {
        const row = document.createElement('tr');
        const numero = ticket.numeroTicket || ticket.numero || '';
        const numeroAttr = numero ? String(numero) : '';
        row.innerHTML = `
            <td>${displayValue(numeroAttr)}</td>
            <td>${displayValue(ticket.cliente)}</td>
            <td>${formatPriorityBadgeHtmlFn(ticket.prioridade)}</td>
            <td><span class="${getStatusClassFn(ticket.status)}">${displayValue(ticket.status)}</span></td>
            <td>${formatSlaBadgeHtmlFn(ticket.slaPrimeiroAtendimentoStatus)}</td>
            <td>${formatSlaResolucaoCellHtmlFn(ticket)}</td>
            <td>${formatDateTime(ticket.vencimentoMaisCritico)}</td>
            <td class="table-actions">
                <button type="button" class="button button-secondary button-small" data-action="details" data-ticket="${numeroAttr}">Ver detalhes</button>
            </td>
        `;
        dashboardSlaCriticosBody.appendChild(row);
    });
}

export function renderDashboardEncerramentoSatisfacao(resumo) {
    const norm = normalizeDashboardEncerramentoResumo(resumo);
    if (dashboardEncPeriodoHint) {
        dashboardEncPeriodoHint.textContent = norm.periodoHint;
    }
    setDashboardMetric(dashboardEncMotivo, norm.motivoLabel);
    setDashboardMetric(dashboardEncMedia, norm.mediaLabel);
    setDashboardMetric(dashboardEncRespondidas, norm.respondidas);
    setDashboardMetric(dashboardEncPendentes, norm.pendentes);
    setDashboardMetric(dashboardEncExpiradas, norm.expiradas);
    setDashboardMetric(dashboardEncFalhasEnvio, norm.falhasEnvio);
}

function resetDashboardEncerramentoOnError() {
    renderDashboardEncerramentoSatisfacao(null);
}

async function ensureDashboardEncClientesSelect() {
    if (dashboardEncClientesCarregados || !dashboardEncFiltroCliente) {
        return;
    }
    try {
        const clientes = await clienteService.listOrSearch('', true);
        const ativos = filterClientesAtivosIndicadores(clientes);
        ativos.sort((a, b) =>
            String(a.nome ?? '').localeCompare(String(b.nome ?? ''), 'pt-BR', {
                sensitivity: 'base'
            })
        );
        ativos.forEach(c => {
            const option = document.createElement('option');
            option.value = String(c.id);
            const nome =
                c.nome && String(c.nome).trim() !== '' ? String(c.nome).trim() : `Cliente ${c.id}`;
            option.textContent = nome;
            dashboardEncFiltroCliente.appendChild(option);
        });
        dashboardEncClientesCarregados = true;
    } catch {
        /* mantém apenas "Todos os clientes" */
    }
}

export async function loadDashboardEncerramentoSatisfacao() {
    try {
        await ensureDashboardEncClientesSelect();
        const params = buildDashboardEncerramentoQueryParams({
            dias: dashboardEncFiltroDias?.value,
            clienteId: dashboardEncFiltroCliente?.value
        });
        const data = await dashboardService.getEncerramentoSatisfacao(params.toString());
        renderDashboardEncerramentoSatisfacao(data);
    } catch (error) {
        resetDashboardEncerramentoOnError();
        showAlert(error.message, alertBoxDashboard);
    }
}

export async function loadDashboardSla() {
    try {
        const data = await dashboardService.getSla();
        renderDashboardSlaResumo(data);
        renderDashboardTicketsCriticosSla(data?.ticketsCriticosSla);
    } catch (error) {
        renderDashboardSlaResumo({});
        renderDashboardTicketsCriticosSla([]);
        showAlert(error.message, alertBoxDashboard);
    }
}

export async function loadDashboard() {
    try {
        const resumo = await dashboardService.getResumo();
        updateDashboardFromResumo(resumo);
    } catch (error) {
        resetDashboardResumo();
        showAlert(error.message, alertBoxDashboard);
    }

    await loadDashboardGerencial();
    await loadDashboardEncerramentoSatisfacao();
    await loadDashboardSla();
    await loadConexoesPendencias();
}

function formatWaitingTime(startValue) {
    if (!startValue) return '00:00:00';
    const start = new Date(startValue).getTime();
    if (Number.isNaN(start)) return '00:00:00';

    const diffSeconds = Math.max(0, Math.floor((Date.now() - start) / 1000));
    const hours = String(Math.floor(diffSeconds / 3600)).padStart(2, '0');
    const minutes = String(Math.floor((diffSeconds % 3600) / 60)).padStart(2, '0');
    const seconds = String(diffSeconds % 60).padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
}

function getWaitingSeconds(startValue) {
    if (!startValue) return 0;
    const start = new Date(startValue).getTime();
    if (Number.isNaN(start)) return 0;
    return Math.max(0, Math.floor((Date.now() - start) / 1000));
}

function getWaitingTimeClass(seconds) {
    if (seconds >= 300) return 'waiting-time-danger';
    if (seconds >= 180) return 'waiting-time-warning';
    return 'waiting-time-ok';
}

function updateWaitingTimers() {
    document.querySelectorAll('[data-wait-start]').forEach(element => {
        const seconds = getWaitingSeconds(element.dataset.waitStart);
        element.textContent = formatWaitingTime(element.dataset.waitStart);
        element.classList.remove('waiting-time-ok', 'waiting-time-warning', 'waiting-time-danger');
        element.classList.add(getWaitingTimeClass(seconds));
        const alert = element.parentElement?.querySelector('.waiting-alert');
        alert?.classList.toggle('hidden', seconds < 300);
    });
}

function restartWaitingTimers() {
    if (waitTimerInterval) {
        clearInterval(waitTimerInterval);
    }
    updateWaitingTimers();
    waitTimerInterval = setInterval(updateWaitingTimers, 1000);
}

async function loadConexoesPendencias() {
    if (!conexoesPendencias) return;
    try {
        const conexoes = await dashboardService.getConexoesPendencias();
        renderConexoesPendencias(conexoes);
    } catch (error) {
        conexoesPendencias.innerHTML = `<p class="empty-state">${escapeAttrFn(mensagemParaExibirUsuario(error.message))}</p>`;
    }
}

function renderConexoesPendencias(conexoes) {
    conexoesPendencias.innerHTML = '';
    if (!conexoes.length) {
        conexoesPendencias.innerHTML = '<p class="empty-state">Nenhuma pendência por conexão.</p>';
        restartWaitingTimers();
        return;
    }

    conexoes.forEach(conexao => {
        const card = document.createElement('div');
        card.className = 'connection-card';
        const tickets = conexao.tickets || [];
        card.innerHTML = `
            <div class="connection-card-header">
                <div>
                    <strong>${conexao.conexao || 'Sem conexão'}</strong>
                    <span>${conexao.quantidadePendencias || 0} pendência(s)</span>
                </div>
            </div>
            <div class="connection-ticket-list"></div>
        `;

        const list = card.querySelector('.connection-ticket-list');
        if (!tickets.length) {
            list.innerHTML = '<p class="empty-state">Nenhuma pendência.</p>';
        } else {
            tickets.forEach(ticket => {
                const item = document.createElement('div');
                item.className = 'connection-ticket-card';
                item.innerHTML = `
                    <strong>${ticket.cliente || '-'}</strong>
                    <p>${ticket.assunto || '-'}</p>
                    <span>Tempo de espera: <strong class="waiting-time" data-wait-start="${ticket.dataAbertura || ''}">${formatWaitingTime(ticket.dataAbertura)}</strong> <span class="waiting-alert hidden">!</span></span>
                `;
                list.appendChild(item);
            });
        }

        conexoesPendencias.appendChild(card);
    });

    restartWaitingTimers();
}

export function initDashboardPage(deps) {
    showAlertFn = deps.showAlert || showAlertFn;
    displayValueFn = deps.displayValue || displayValueFn;
    formatDateTimeFn = deps.formatDateTime || formatDateTimeFn;
    sortTicketsByPriorityFn = deps.sortTicketsByPriority || sortTicketsByPriorityFn;
    getTicketPriorityRowClassFn = deps.getTicketPriorityRowClass || getTicketPriorityRowClassFn;
    getStatusClassFn = deps.getStatusClass || getStatusClassFn;
    formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml || formatPriorityBadgeHtmlFn;
    formatSlaBadgeHtmlFn = deps.formatSlaBadgeHtml || formatSlaBadgeHtmlFn;
    formatSlaResolucaoCellHtmlFn = deps.formatSlaResolucaoCellHtml || formatSlaResolucaoCellHtmlFn;
    openDetailsFn = deps.openDetails || openDetailsFn;
    escapeAttrFn = deps.escapeAttr || escapeAttrFn;
    initTicketAlertBaselineFn = deps.initTicketAlertBaseline || initTicketAlertBaselineFn;
    checkTicketAlertVisualFn = deps.checkTicketAlertVisual || checkTicketAlertVisualFn;
    refreshNotificacoesUiFn = deps.refreshNotificacoesUi || refreshNotificacoesUiFn;

    refreshButton?.addEventListener('click', loadDashboard);

    const onDashboardEncFiltroChange = () => loadDashboardEncerramentoSatisfacao();
    dashboardEncFiltroDias?.addEventListener('change', onDashboardEncFiltroChange);
    dashboardEncFiltroCliente?.addEventListener('change', onDashboardEncFiltroChange);

    dashboardSlaCriticosBody?.addEventListener('click', event => {
        const button = event.target.closest('button[data-action="details"]');
        if (!button) return;
        const ticketNumber = button.dataset.ticket;
        if (ticketNumber) openDetailsFn(ticketNumber);
    });

    dashboardCriticosBody?.addEventListener('click', event => {
        const button = event.target.closest('button[data-action="details"]');
        if (!button) return;
        openDetailsFn(button.dataset.ticket);
    });

    dashboardSlaVerificarBtn?.addEventListener('click', async () => {
        if (!dashboardSlaVerificarBtn) return;
        dashboardSlaVerificarBtn.disabled = true;
        try {
            const resultado = await notificacaoService.verificarSlaCritico();
            const criadas = Number(resultado?.notificacoesCriadas ?? 0);
            await refreshNotificacoesUiFn();
            showAlert(`Verificação concluída: ${criadas} notificação(ões) criada(s).`, alertBoxDashboard, 'success');
        } catch (error) {
            showAlert(error.message, alertBoxDashboard);
        } finally {
            dashboardSlaVerificarBtn.disabled = false;
        }
    });
}
