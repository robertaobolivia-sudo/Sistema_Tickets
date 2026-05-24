import * as dashboardService from '@features/dashboard/dashboard-service.js';
import * as clienteService from '@features/clientes/cliente-service.js';
import * as notificacaoService from '@features/perfil/notificacao-service.js';
import { filterClientesAtivosIndicadores } from '@features/satisfacao/encerramento-avaliacao-view.js';
import { mensagemParaExibirUsuario } from '@shared/ui/messages.js';
import {
    buildDashboardEncerramentoQueryParams,
    normalizeDashboardEncerramentoResumo
} from '@features/dashboard/dashboard-encerramento-view.js';
import { renderOperacaoAgoraCard } from '@features/dashboard/dashboard-operacao-agora-view.js';
import {
    buildAnalistaOnlineCardElement,
    flattenOperadoresDashboard
} from '@features/dashboard/dashboard-analistas-online-view.js';
import { formatChamadoStatusLabel } from '@features/dashboard/dashboard-operacao-cliente-b2b-view.js';
import { extractNumeroTicketAcompanhar } from '@features/dashboard/dashboard-acompanhamento-view.js';
import { openDashboardAcompanhamento } from '@features/dashboard/dashboard-acompanhamento-page.js';
import {
    buildRecorrenciasHtml,
    formatTicketCriticoLinha,
    formatMediaAtual,
    normalizeAvaliacaoTempoReal,
    normalizeEncerramentosDia,
    normalizeSlaVivoFromApi
} from '@features/dashboard/dashboard-operacional-view.js';

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

const dashboardOpAtendimentoQtd = document.getElementById('dashboardOpAtendimentoQtd');
const dashboardOpAtendimentoRotulo = document.getElementById('dashboardOpAtendimentoRotulo');
const dashboardOpAtendimentoTempo = document.getElementById('dashboardOpAtendimentoTempo');
const dashboardOpFilaQtd = document.getElementById('dashboardOpFilaQtd');
const dashboardOpFilaRotulo = document.getElementById('dashboardOpFilaRotulo');
const dashboardOpFilaTempo = document.getElementById('dashboardOpFilaTempo');

const dashboardStatusOperadorGrid = document.getElementById('dashboardStatusOperadorGrid');

const dashboardOperacaoB2bEmpty = document.getElementById('dashboardOperacaoB2bEmpty');
const dashboardOperacaoB2bGrid = document.getElementById('dashboardOperacaoB2bGrid');

const dashboardSlaVivoDentro = document.getElementById('dashboardSlaVivoDentro');
const dashboardSlaVivoProximo = document.getElementById('dashboardSlaVivoProximo');
const dashboardSlaVivoVencido = document.getElementById('dashboardSlaVivoVencido');
const dashboardSlaVivoCritico = document.getElementById('dashboardSlaVivoCritico');
const dashboardSlaVivoCriticoEmpty = document.getElementById('dashboardSlaVivoCriticoEmpty');
const dashboardEncDiaFinalizados = document.getElementById('dashboardEncDiaFinalizados');
const dashboardEncDiaNaoResolvidos = document.getElementById('dashboardEncDiaNaoResolvidos');
const dashboardEncDiaEscalonados = document.getElementById('dashboardEncDiaEscalonados');
const dashboardEncDiaAbandonados = document.getElementById('dashboardEncDiaAbandonados');
const dashboardEncDiaRecorrencias = document.getElementById('dashboardEncDiaRecorrencias');
const dashboardAvalMedia = document.getElementById('dashboardAvalMedia');
const dashboardAvalRuins = document.getElementById('dashboardAvalRuins');
const dashboardAvalRespondidas = document.getElementById('dashboardAvalRespondidas');
const dashboardAvalPendentes = document.getElementById('dashboardAvalPendentes');
const dashboardAvalExpiradas = document.getElementById('dashboardAvalExpiradas');

const totalTickets = document.getElementById('totalTickets');
const openTickets = document.getElementById('openTickets');
const inProgressTickets = document.getElementById('inProgressTickets');
const resolvedTickets = document.getElementById('resolvedTickets');
const cancelledTickets = document.getElementById('cancelledTickets');
const naoAtendimentoTickets = document.getElementById('naoAtendimentoTickets');
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
    setDashboardMetric(naoAtendimentoTickets, resumo?.ticketsNaoAtendimento ?? 0);
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
    renderDashboardGroupList(dashboardByConnection, resumo?.ticketsPorCliente, 'Nenhum ticket por cliente.');
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

function renderDashboardSlaVivo(slaPayload) {
    const vivo = normalizeSlaVivoFromApi(slaPayload ?? {});
    setDashboardMetric(dashboardSlaVivoDentro, vivo.dentroDoPrazo);
    setDashboardMetric(dashboardSlaVivoProximo, vivo.proximosDoLimite);
    setDashboardMetric(dashboardSlaVivoVencido, vivo.vencidos);

    const linha = formatTicketCriticoLinha(vivo.ticketMaisCritico, displayValue);
    if (linha && dashboardSlaVivoCritico) {
        dashboardSlaVivoCritico.textContent = `Mais crítico: ${linha}`;
        dashboardSlaVivoCritico.dataset.ticket = vivo.ticketMaisCritico?.numeroTicket ?? '';
        dashboardSlaVivoCritico.classList.remove('hidden');
        dashboardSlaVivoCriticoEmpty?.classList.add('hidden');
    } else {
        dashboardSlaVivoCritico?.classList.add('hidden');
        dashboardSlaVivoCriticoEmpty?.classList.remove('hidden');
        if (dashboardSlaVivoCritico) {
            dashboardSlaVivoCritico.dataset.ticket = '';
        }
    }
}

function renderDashboardAvaliacaoTempoReal(payload) {
    const norm = normalizeAvaliacaoTempoReal(payload ?? {});
    if (dashboardAvalMedia) {
        dashboardAvalMedia.textContent = formatMediaAtual(norm.mediaAtual);
    }
    setDashboardMetric(dashboardAvalRuins, norm.avaliacoesRuins);
    setDashboardMetric(dashboardAvalRespondidas, norm.pesquisasRespondidas);
    setDashboardMetric(dashboardAvalPendentes, norm.pesquisasPendentes);
    setDashboardMetric(dashboardAvalExpiradas, norm.pesquisasExpiradas);
}

function renderDashboardEncerramentosDia(payload) {
    const norm = normalizeEncerramentosDia(payload ?? {});
    setDashboardMetric(dashboardEncDiaFinalizados, norm.finalizados);
    setDashboardMetric(dashboardEncDiaNaoResolvidos, norm.naoResolvidos);
    setDashboardMetric(dashboardEncDiaEscalonados, norm.escalonados);
    setDashboardMetric(dashboardEncDiaAbandonados, norm.abandonados);
    if (dashboardEncDiaRecorrencias) {
        dashboardEncDiaRecorrencias.innerHTML = buildRecorrenciasHtml(norm.recorrencias, escapeHtmlB2b);
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
        renderDashboardSlaVivo(data);
    } catch (error) {
        renderDashboardSlaResumo({});
        renderDashboardTicketsCriticosSla([]);
        renderDashboardSlaVivo(null);
        showAlert(error.message, alertBoxDashboard);
    }
}

export async function loadDashboardEncerramentosDia() {
    try {
        const data = await dashboardService.getEncerramentosDia();
        renderDashboardEncerramentosDia(data);
    } catch (error) {
        renderDashboardEncerramentosDia(null);
        showAlert(error?.message || 'Falha ao carregar encerramentos do dia.', alertBoxDashboard);
    }
}

export async function loadDashboardAvaliacaoTempoReal() {
    try {
        const data = await dashboardService.getAvaliacaoTempoReal();
        renderDashboardAvaliacaoTempoReal(data);
    } catch (error) {
        renderDashboardAvaliacaoTempoReal(null);
        showAlert(error?.message || 'Falha ao carregar avaliação em tempo real.', alertBoxDashboard);
    }
}

function updateDashboardOperacaoAgora(data) {
    renderOperacaoAgoraCard(
        {
            quantidadeEl: dashboardOpAtendimentoQtd,
            rotuloEl: dashboardOpAtendimentoRotulo,
            tempoEl: dashboardOpAtendimentoTempo
        },
        data?.emAtendimento
    );
    renderOperacaoAgoraCard(
        {
            quantidadeEl: dashboardOpFilaQtd,
            rotuloEl: dashboardOpFilaRotulo,
            tempoEl: dashboardOpFilaTempo
        },
        data?.emFila
    );
}

function resetDashboardOperacaoAgora() {
    updateDashboardOperacaoAgora({
        emAtendimento: { quantidade: 0, tempoMedioFormatado: '-', tempoMedioRotulo: 'TMA' },
        emFila: { quantidade: 0, tempoMedioFormatado: '-', tempoMedioRotulo: 'TME' }
    });
}

export async function loadDashboardOperacaoAgora() {
    try {
        const data = await dashboardService.getOperacaoAgora();
        updateDashboardOperacaoAgora(data);
    } catch (error) {
        resetDashboardOperacaoAgora();
        showAlert(error.message, alertBoxDashboard);
    }
}

function renderDashboardStatusOperador(data) {
    if (!dashboardStatusOperadorGrid) return;
    dashboardStatusOperadorGrid.innerHTML = '';
    const operadores = flattenOperadoresDashboard(data);
    operadores.forEach(op => {
        dashboardStatusOperadorGrid.appendChild(buildAnalistaOnlineCardElement(op));
    });
}

function resetDashboardStatusOperador() {
    if (dashboardStatusOperadorGrid) {
        dashboardStatusOperadorGrid.innerHTML = '';
    }
}

export async function loadDashboardAnalistasOnline() {
    try {
        const data = await dashboardService.getAnalistasOnline();
        renderDashboardStatusOperador(data);
    } catch (error) {
        resetDashboardStatusOperador();
        showAlert(error.message, alertBoxDashboard);
    }
}

/** Recarrega grid Status Operador (ex.: após troca no menu do avatar). */
export function refreshDashboardStatusOperador() {
    return loadDashboardAnalistasOnline();
}

function renderDashboardOperacaoClienteB2b(data) {
    const clientes = Array.isArray(data?.clientes) ? data.clientes : [];
    if (!dashboardOperacaoB2bGrid) {
        return;
    }
    dashboardOperacaoB2bGrid.innerHTML = '';
    if (!clientes.length) {
        dashboardOperacaoB2bGrid.classList.add('hidden');
        dashboardOperacaoB2bEmpty?.classList.remove('hidden');
        return;
    }
    dashboardOperacaoB2bEmpty?.classList.add('hidden');
    dashboardOperacaoB2bGrid.classList.remove('hidden');

    clientes.forEach(cliente => {
        const card = document.createElement('article');
        card.className = 'dashboard-b2b-client-card';
        card.dataset.clienteId = String(cliente.clienteId ?? '');

        const title = document.createElement('h3');
        title.className = 'dashboard-b2b-client-title';
        title.textContent = cliente.clienteNome || 'Cliente';
        card.appendChild(title);

        const chamados = Array.isArray(cliente.chamados) ? cliente.chamados : [];
        if (!chamados.length) {
            const empty = document.createElement('p');
            empty.className = 'dashboard-b2b-client-empty';
            empty.textContent = 'Nenhum chamado ativo neste momento.';
            card.appendChild(empty);
        } else {
            const table = document.createElement('table');
            table.className = 'dashboard-b2b-chamados-table';
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Protocolo</th>
                        <th>Contato</th>
                        <th>Status</th>
                        <th>Analista</th>
                        <th>TME</th>
                        <th>TMA</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody></tbody>
            `;
            const tbody = table.querySelector('tbody');
            chamados.forEach(ch => {
                const tr = document.createElement('tr');
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'button button-secondary dashboard-b2b-acompanhar-btn';
                btn.textContent = 'Acompanhar';
                btn.dataset.acompanharClienteId = String(cliente.clienteId ?? '');
                btn.dataset.acompanharClienteNome = cliente.clienteNome || '';
                btn.addEventListener('click', () => {
                    const numero = extractNumeroTicketAcompanhar(ch);
                    if (numero) {
                        openDashboardAcompanhamento(numero, cliente.clienteNome);
                        return;
                    }
                    showAlertFn(
                        'Este item é uma pendência pós-encerramento. Acompanhe o atendimento ativo pelo protocolo TK- no Chats.',
                        alertBoxDashboard,
                        'info'
                    );
                });
                tr.innerHTML = `
                    <td>${escapeHtmlB2b(ch.numeroTicket)}</td>
                    <td>${escapeHtmlB2b(ch.contato)}</td>
                    <td>${escapeHtmlB2b(formatChamadoStatusLabel(ch.status))}</td>
                    <td>${escapeHtmlB2b(ch.analista)}</td>
                    <td class="dashboard-b2b-tempo-cell"><span class="dashboard-b2b-tempo-label">TME</span>${escapeHtmlB2b(ch.tmeFormatado)}</td>
                    <td class="dashboard-b2b-tempo-cell"><span class="dashboard-b2b-tempo-label">TMA</span>${escapeHtmlB2b(ch.tmaFormatado)}</td>
                    <td></td>
                `;
                tr.querySelector('td:last-child')?.appendChild(btn);
                tbody?.appendChild(tr);
            });
            card.appendChild(table);
        }
        dashboardOperacaoB2bGrid.appendChild(card);
    });
}

function escapeHtmlB2b(text) {
    return String(text ?? '—')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function resetDashboardOperacaoClienteB2b() {
    if (dashboardOperacaoB2bGrid) {
        dashboardOperacaoB2bGrid.innerHTML = '';
        dashboardOperacaoB2bGrid.classList.add('hidden');
    }
    dashboardOperacaoB2bEmpty?.classList.remove('hidden');
}

export async function loadDashboardOperacaoClienteB2b() {
    try {
        const data = await dashboardService.getOperacaoClienteB2b();
        renderDashboardOperacaoClienteB2b(data);
    } catch (error) {
        resetDashboardOperacaoClienteB2b();
        showAlert(error.message, alertBoxDashboard);
    }
}

const dashboardUltimaAtualizacao = document.getElementById('dashboardUltimaAtualizacao');

function atualizarDashboardUltimaAtualizacao() {
    if (!dashboardUltimaAtualizacao) {
        return;
    }
    const texto = formatDateTimeFn(new Date());
    dashboardUltimaAtualizacao.textContent = `Última atualização: ${texto}`;
}

export async function loadDashboard() {
    await loadDashboardOperacaoAgora();
    await loadDashboardAnalistasOnline();
    await loadClientesPendencias();
    await loadDashboardSla();
    await loadDashboardEncerramentosDia();
    await loadDashboardAvaliacaoTempoReal();
    atualizarDashboardUltimaAtualizacao();
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

async function loadClientesPendencias() {
    if (!conexoesPendencias) return;
    try {
        const grupos = await dashboardService.getClientesPendencias();
        renderClientesPendencias(grupos);
    } catch (error) {
        conexoesPendencias.innerHTML = `<p class="empty-state">${escapeAttrFn(mensagemParaExibirUsuario(error.message))}</p>`;
    }
}

function renderClientePendenciasCard(grupo) {
    const tickets = Array.isArray(grupo.tickets) ? grupo.tickets : [];
    const card = document.createElement('article');
    card.className = 'dashboard-cliente-card';

    const header = document.createElement('div');
    header.className = 'dashboard-cliente-card-header';
    const nomeEl = document.createElement('strong');
    nomeEl.className = 'dashboard-cliente-card-nome';
    nomeEl.textContent = grupo.cliente || 'Sem cliente';
    header.appendChild(nomeEl);
    if (tickets.length > 0) {
        const totalEl = document.createElement('span');
        totalEl.className = 'dashboard-cliente-card-total';
        totalEl.textContent = `Total: ${tickets.length}`;
        header.appendChild(totalEl);
    }
    card.appendChild(header);

    if (!tickets.length) {
        const empty = document.createElement('p');
        empty.className = 'empty-state';
        empty.textContent = 'Nenhum atendimento ativo.';
        card.appendChild(empty);
        return card;
    }

    let idx = 0;
    const carouselBody = document.createElement('div');
    carouselBody.className = 'dashboard-cliente-carousel-body';

    const renderSlide = () => {
        carouselBody.innerHTML = '';
        const t = tickets[idx];
        const isAtendendo = t.tipoStatus === 'ATENDENDO';

        const slide = document.createElement('div');
        slide.className = 'dashboard-cliente-slide';

        const top = document.createElement('div');
        top.className = 'dashboard-cliente-slide-top';

        const badge = document.createElement('span');
        badge.className = `dashboard-cliente-status-badge dashboard-cliente-status-badge--${isAtendendo ? 'atendendo' : 'espera'}`;
        badge.textContent = isAtendendo ? 'ATENDENDO' : 'EM ESPERA';
        top.appendChild(badge);

        const tempo = document.createElement('span');
        tempo.className = 'dashboard-cliente-tempo';
        const tempoLabel = document.createElement('span');
        tempoLabel.className = 'dashboard-cliente-tempo-label';
        tempo.appendChild(tempoLabel);
        if (isAtendendo) {
            tempoLabel.textContent = 'TMA';
            tempo.appendChild(document.createTextNode(t.tmaFormatado || '—'));
        } else {
            tempoLabel.textContent = 'TME';
            const tempoVal = document.createElement('span');
            tempoVal.className = 'waiting-time';
            if (t.dataAbertura) {
                tempoVal.dataset.waitStart = t.dataAbertura;
            }
            tempoVal.textContent = formatWaitingTime(t.dataAbertura);
            tempo.appendChild(tempoVal);
        }
        top.appendChild(tempo);
        slide.appendChild(top);

        const info = document.createElement('div');
        info.className = 'dashboard-cliente-slide-info';

        const contatoEl = document.createElement('span');
        contatoEl.className = 'dashboard-cliente-contato';
        contatoEl.textContent = t.contato || '-';
        info.appendChild(contatoEl);

        const analistaEl = document.createElement('span');
        analistaEl.className = 'dashboard-cliente-analista';
        analistaEl.textContent = isAtendendo ? (t.analista || '—') : 'Não atribuído';
        info.appendChild(analistaEl);

        slide.appendChild(info);

        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'button button-secondary dashboard-cliente-acompanhar-btn';
        btn.textContent = 'Acompanhar';
        btn.addEventListener('click', () => {
            const numero = t.numeroTicket;
            if (numero) {
                openDashboardAcompanhamento(numero, grupo.cliente);
            } else {
                showAlertFn(
                    'Este item não possui protocolo de atendimento ativo para acompanhar.',
                    alertBoxDashboard,
                    'info'
                );
            }
        });
        slide.appendChild(btn);

        carouselBody.appendChild(slide);
    };

    renderSlide();
    card.appendChild(carouselBody);

    if (tickets.length > 1) {
        const nav = document.createElement('div');
        nav.className = 'dashboard-cliente-carousel-nav';

        const dotsEl = document.createElement('div');
        dotsEl.className = 'dashboard-cliente-carousel-dots';

        const renderDots = () => {
            dotsEl.innerHTML = '';
            tickets.forEach((_, i) => {
                const dot = document.createElement('span');
                dot.className = `dashboard-cliente-dot${i === idx ? ' dashboard-cliente-dot--active' : ''}`;
                dotsEl.appendChild(dot);
            });
        };
        renderDots();

        const prevBtn = document.createElement('button');
        prevBtn.type = 'button';
        prevBtn.className = 'dashboard-cliente-nav-btn';
        prevBtn.setAttribute('aria-label', 'Anterior');
        prevBtn.textContent = '‹';
        prevBtn.addEventListener('click', () => {
            idx = (idx - 1 + tickets.length) % tickets.length;
            renderSlide();
            renderDots();
            restartWaitingTimers();
        });

        const nextBtn = document.createElement('button');
        nextBtn.type = 'button';
        nextBtn.className = 'dashboard-cliente-nav-btn';
        nextBtn.setAttribute('aria-label', 'Próximo');
        nextBtn.textContent = '›';
        nextBtn.addEventListener('click', () => {
            idx = (idx + 1) % tickets.length;
            renderSlide();
            renderDots();
            restartWaitingTimers();
        });

        nav.appendChild(prevBtn);
        nav.appendChild(dotsEl);
        nav.appendChild(nextBtn);
        card.appendChild(nav);
    }

    return card;
}

function renderClientesPendencias(grupos) {
    conexoesPendencias.innerHTML = '';
    const lista = Array.isArray(grupos) ? grupos : [];
    if (!lista.length) {
        conexoesPendencias.innerHTML = '<p class="empty-state">Nenhum atendimento ativo por cliente.</p>';
        restartWaitingTimers();
        return;
    }
    lista.forEach(grupo => {
        conexoesPendencias.appendChild(renderClientePendenciasCard(grupo));
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

    dashboardSlaVivoCritico?.addEventListener('click', () => {
        const numero = dashboardSlaVivoCritico.dataset.ticket;
        if (numero) {
            openDetailsFn(numero);
        }
    });

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
