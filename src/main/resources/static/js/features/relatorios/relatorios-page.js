import {
    buildRelatorioBuscaParams,
    buildSatisfacaoResumoParams,
    formatDateIsoToBr
} from '@shared/ui/query-params.js';
import { closeOtherSidebarGroups } from '@shared/ui/sidebar-groups.js';
import * as ticketService from '@features/tickets/ticket-service.js';
import * as satisfacaoService from '@features/satisfacao/satisfacao-service.js';
import * as categoriaService from '@features/configuracoes/categoria-service.js';
import * as clienteService from '@features/clientes/cliente-service.js';
import { filterClientesAtivosIndicadores } from '@features/satisfacao/encerramento-avaliacao-view.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatDateTimeFn = () => '-';
let mensagemErroSessaoApiFn = (_r, _d, msg) => msg;
let sortTicketsByPriorityFn = (tickets) => (Array.isArray(tickets) ? tickets : []);
let getTicketPriorityRowClassFn = () => '';
let getStatusClassFn = () => 'status-badge';
let formatPriorityBadgeHtmlFn = () => '-';
let formatSlaBadgeHtmlFn = () => '-';
let formatSlaResolucaoCellHtmlFn = () => '-';

const relatorioFiltroDataInicio = document.getElementById('relatorioFiltroDataInicio');
const relatorioFiltroDataFim = document.getElementById('relatorioFiltroDataFim');
const relatorioFiltroOrigem = document.getElementById('relatorioFiltroOrigem');
const relatorioFiltroStatus = document.getElementById('relatorioFiltroStatus');
const relatorioFiltroCliente = document.getElementById('relatorioFiltroCliente');
const relatorioFiltroAnalistaId = document.getElementById('relatorioFiltroAnalistaId');
const relatorioFiltroGrupo = document.getElementById('relatorioFiltroGrupo');
const relatorioFiltroSubgrupo = document.getElementById('relatorioFiltroSubgrupo');
const relatorioFiltroPrioridade = document.getElementById('relatorioFiltroPrioridade');
const relatorioFiltroSlaPrimeiro = document.getElementById('relatorioFiltroSlaPrimeiro');
const relatorioFiltroSlaResolucao = document.getElementById('relatorioFiltroSlaResolucao');
const relatorioFiltroEscalonado = document.getElementById('relatorioFiltroEscalonado');
const relatorioFiltroMotivo = document.getElementById('relatorioFiltroMotivo');
const relatorioFiltroStatusPesquisa = document.getElementById('relatorioFiltroStatusPesquisa');
const relatorioFiltroNotaAvaliacao = document.getElementById('relatorioFiltroNotaAvaliacao');
const relatorioFiltroEnvioStatus = document.getElementById('relatorioFiltroEnvioStatus');
const relatorioGerarBtn = document.getElementById('relatorioGerarBtn');
const relatorioExportarCsvBtn = document.getElementById('relatorioExportarCsvBtn');
const relatorioLimparBtn = document.getElementById('relatorioLimparBtn');
const relatorioTotal = document.getElementById('relatorioTotal');
const relatorioAbertos = document.getElementById('relatorioAbertos');
const relatorioEmAtendimento = document.getElementById('relatorioEmAtendimento');
const relatorioAguardandoCliente = document.getElementById('relatorioAguardandoCliente');
const relatorioResolvidos = document.getElementById('relatorioResolvidos');
const relatorioCancelados = document.getElementById('relatorioCancelados');
const relatorioCritica = document.getElementById('relatorioCritica');
const relatorioAlta = document.getElementById('relatorioAlta');
const relatorioMedia = document.getElementById('relatorioMedia');
const relatorioBaixa = document.getElementById('relatorioBaixa');
const relatorioTicketsBody = document.getElementById('relatorioTicketsBody');
const relatorioEmptyMessage = document.getElementById('relatorioEmptyMessage');
const relatorioInitialMessage = document.getElementById('relatorioInitialMessage');
const satisfacaoResumoVazio = document.getElementById('satisfacaoResumoVazio');
const satisfacaoResumoConteudo = document.getElementById('satisfacaoResumoConteudo');
const satisfacaoResumoMedia = document.getElementById('satisfacaoResumoMedia');
const satisfacaoResumoTotal = document.getElementById('satisfacaoResumoTotal');
const satisfacaoResumoPositivas = document.getElementById('satisfacaoResumoPositivas');
const satisfacaoResumoNegativas = document.getElementById('satisfacaoResumoNegativas');
const satisfacaoResumoDistribuicaoBody = document.getElementById('satisfacaoResumoDistribuicaoBody');
const satisfacaoEvolucaoVazio = document.getElementById('satisfacaoEvolucaoVazio');
const satisfacaoEvolucaoConteudo = document.getElementById('satisfacaoEvolucaoConteudo');
const satisfacaoEvolucaoBody = document.getElementById('satisfacaoEvolucaoBody');
const alertBoxSatisfacaoResumo = document.getElementById('alertBoxSatisfacaoResumo');
const satisfacaoFiltroNota = document.getElementById('satisfacaoFiltroNota');
const satisfacaoFiltroStatus = document.getElementById('satisfacaoFiltroStatus');
const satisfacaoAtualizarBtn = document.getElementById('satisfacaoAtualizarBtn');
const satisfacaoExportarCsvBtn = document.getElementById('satisfacaoExportarCsvBtn');
const alertBoxRelatorios = document.getElementById('alertBoxRelatorios');

let relatorioGruposCarregados = false;
let relatorioMotivosCarregados = false;
let relatorioClientesCarregados = false;
let listenersBound = false;

export function initRelatoriosPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDateTime) formatDateTimeFn = deps.formatDateTime;
    if (deps.mensagemErroSessaoApi) mensagemErroSessaoApiFn = deps.mensagemErroSessaoApi;
    if (deps.sortTicketsByPriority) sortTicketsByPriorityFn = deps.sortTicketsByPriority;
    if (deps.getTicketPriorityRowClass) getTicketPriorityRowClassFn = deps.getTicketPriorityRowClass;
    if (deps.getStatusClass) getStatusClassFn = deps.getStatusClass;
    if (deps.formatPriorityBadgeHtml) formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml;
    if (deps.formatSlaBadgeHtml) formatSlaBadgeHtmlFn = deps.formatSlaBadgeHtml;
    if (deps.formatSlaResolucaoCellHtml) formatSlaResolucaoCellHtmlFn = deps.formatSlaResolucaoCellHtml;
    if (listenersBound) return;
    listenersBound = true;

    relatorioFiltroGrupo?.addEventListener('change', carregarSubgruposRelatorio);
    relatorioGerarBtn?.addEventListener('click', gerarRelatorioTickets);
    relatorioExportarCsvBtn?.addEventListener('click', exportarRelatorioCsv);
    satisfacaoAtualizarBtn?.addEventListener('click', loadSatisfacaoResumoRelatorio);
    satisfacaoExportarCsvBtn?.addEventListener('click', exportarSatisfacaoCsv);
    relatorioLimparBtn?.addEventListener('click', limparFiltrosRelatorio);
}

export function initRelatoriosSidebarNav({ showPageFn }) {
    const group = document.getElementById('navRelatoriosGroup');
    const toggle = document.getElementById('navRelatoriosToggle');
    const submenu = document.getElementById('navRelatoriosSubmenu');
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
        if (willOpen) closeOtherSidebarGroups('navRelatoriosGroup');
        setOpen(willOpen);
    });

    document.querySelectorAll('[data-relatorios-sub]').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = btn.getAttribute('data-page');
            setOpen(true);
            showPageFn(page || 'relatorios');
        });
    });
}

export async function loadRelatoriosPage() {
    clearAlertFn(alertBoxRelatorios);
    if (!relatorioGruposCarregados) {
        await carregarGruposRelatorio();
    }
    if (!relatorioMotivosCarregados) {
        await carregarMotivosRelatorio();
    }
    await carregarClientesRelatorio();
    await loadSatisfacaoResumoRelatorio();
}

async function carregarClientesRelatorio() {
    if (relatorioClientesCarregados || !relatorioFiltroCliente) {
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
            relatorioFiltroCliente.appendChild(option);
        });
        relatorioClientesCarregados = true;
    } catch {
        /* mantém apenas "Todos os clientes" */
    }
}

const ORIGEM_TICKET_LABELS = {
    RECEPTIVO_WHATSAPP: 'Receptivo WhatsApp',
    ATIVO_MANUAL: 'Ativo manual'
};

function formatOrigemTicketRelatorio(origem) {
    if (origem == null || origem === '') {
        return '-';
    }
    return ORIGEM_TICKET_LABELS[origem] ?? String(origem);
}

function getRelatorioSelectNome(selectElement) {
    const option = selectElement?.selectedOptions?.[0];
    if (!selectElement?.value || !option) {
        return '';
    }
    return option.dataset.nome || option.textContent?.trim() || '';
}

function buildRelatorioBuscaQueryString() {
    return buildRelatorioBuscaParams({
        dataInicio: relatorioFiltroDataInicio?.value,
        dataFim: relatorioFiltroDataFim?.value,
        status: relatorioFiltroStatus?.value,
        origemTicket: relatorioFiltroOrigem?.value,
        clienteId: relatorioFiltroCliente?.value,
        analistaId: relatorioFiltroAnalistaId?.value,
        grupo: getRelatorioSelectNome(relatorioFiltroGrupo),
        subgrupo: getRelatorioSelectNome(relatorioFiltroSubgrupo),
        prioridade: relatorioFiltroPrioridade?.value,
        slaPrimeiroAtendimentoStatus: relatorioFiltroSlaPrimeiro?.value,
        slaResolucaoStatus: relatorioFiltroSlaResolucao?.value,
        escalonado: relatorioFiltroEscalonado?.value,
        motivoId: relatorioFiltroMotivo?.value,
        statusPesquisa: relatorioFiltroStatusPesquisa?.value,
        notaAvaliacao: relatorioFiltroNotaAvaliacao?.value,
        envioStatus: relatorioFiltroEnvioStatus?.value
    }).toString();
}

async function fetchRelatorioTickets() {
    return ticketService.searchTicketsRelatorio(buildRelatorioBuscaQueryString());
}

function renderResumoRelatorio(tickets) {
    const lista = Array.isArray(tickets) ? tickets : [];
    const contagem = {
        ABERTO: 0,
        EM_ATENDIMENTO: 0,
        AGUARDANDO_CLIENTE: 0,
        RESOLVIDO: 0,
        CANCELADO: 0
    };
    lista.forEach(ticket => {
        if (contagem[ticket.status] !== undefined) {
            contagem[ticket.status] += 1;
        }
    });
    if (relatorioTotal) relatorioTotal.textContent = String(lista.length);
    if (relatorioAbertos) relatorioAbertos.textContent = String(contagem.ABERTO);
    if (relatorioEmAtendimento) relatorioEmAtendimento.textContent = String(contagem.EM_ATENDIMENTO);
    if (relatorioAguardandoCliente) relatorioAguardandoCliente.textContent = String(contagem.AGUARDANDO_CLIENTE);
    if (relatorioResolvidos) relatorioResolvidos.textContent = String(contagem.RESOLVIDO);
    if (relatorioCancelados) relatorioCancelados.textContent = String(contagem.CANCELADO);

    const contagemPrioridade = {
        CRITICA: 0,
        ALTA: 0,
        MEDIA: 0,
        BAIXA: 0
    };
    lista.forEach(ticket => {
        if (contagemPrioridade[ticket.prioridade] !== undefined) {
            contagemPrioridade[ticket.prioridade] += 1;
        }
    });
    if (relatorioCritica) relatorioCritica.textContent = String(contagemPrioridade.CRITICA);
    if (relatorioAlta) relatorioAlta.textContent = String(contagemPrioridade.ALTA);
    if (relatorioMedia) relatorioMedia.textContent = String(contagemPrioridade.MEDIA);
    if (relatorioBaixa) relatorioBaixa.textContent = String(contagemPrioridade.BAIXA);
}

function renderTabelaRelatorio(tickets) {
    if (!relatorioTicketsBody) return;
    relatorioTicketsBody.innerHTML = '';
    const lista = sortTicketsByPriorityFn(Array.isArray(tickets) ? tickets : []);
    relatorioInitialMessage?.classList.add('hidden');

    if (!lista.length) {
        relatorioEmptyMessage?.classList.remove('hidden');
        return;
    }
    relatorioEmptyMessage?.classList.add('hidden');

    lista.forEach(ticket => {
        const row = document.createElement('tr');
        const rowClass = getTicketPriorityRowClassFn(ticket.prioridade);
        if (rowClass) {
            row.className = rowClass;
        }
        row.innerHTML = `
            <td>${displayValueFn(ticket.numeroTicket)}</td>
            <td>${displayValueFn(ticket.cliente)}</td>
            <td>${formatOrigemTicketRelatorio(ticket.origemTicket)}</td>
            <td><span class="${getStatusClassFn(ticket.status)}">${displayValueFn(ticket.status)}</span></td>
            <td>${formatPriorityBadgeHtmlFn(ticket.prioridade)}</td>
            <td>${displayValueFn(ticket.canal)}</td>
            <td>${displayValueFn(ticket.analistaResponsavelNome)}</td>
            <td>${displayValueFn(ticket.grupoCategoriaNome)}</td>
            <td>${displayValueFn(ticket.subgrupoCategoriaNome)}</td>
            <td>${formatDateTimeFn(ticket.dataAbertura)}</td>
            <td>${formatDateTimeFn(ticket.dataEncerramento)}</td>
            <td>${formatSlaBadgeHtmlFn(ticket.slaPrimeiroAtendimentoStatus)}</td>
            <td>${formatSlaResolucaoCellHtmlFn(ticket)}</td>
        `;
        relatorioTicketsBody.appendChild(row);
    });
}

function resetRelatorioResultadoUi() {
    renderResumoRelatorio([]);
    if (relatorioTicketsBody) relatorioTicketsBody.innerHTML = '';
    relatorioEmptyMessage?.classList.add('hidden');
    relatorioInitialMessage?.classList.remove('hidden');
}

async function carregarMotivosRelatorio() {
    if (!relatorioFiltroMotivo) {
        return;
    }
    relatorioFiltroMotivo.innerHTML = '<option value="">Todos</option>';
    try {
        const motivos = await categoriaService.listMotivos();
        const lista = Array.isArray(motivos) ? motivos : [];
        lista.forEach(m => {
            const option = document.createElement('option');
            option.value = String(m.id);
            option.textContent = m.nome || `Motivo ${m.id}`;
            relatorioFiltroMotivo.appendChild(option);
        });
        relatorioMotivosCarregados = true;
    } catch (error) {
        showAlertFn(error.message, alertBoxRelatorios);
    }
}

async function carregarGruposRelatorio() {
    if (!relatorioFiltroGrupo) return;
    relatorioFiltroGrupo.innerHTML = '<option value="">Todos</option>';
    try {
        const grupos = await categoriaService.listGrupos();
        grupos.forEach(grupo => {
            const option = document.createElement('option');
            option.value = String(grupo.id);
            option.dataset.nome = grupo.nome || '';
            option.textContent = grupo.nome || `Grupo ${grupo.id}`;
            relatorioFiltroGrupo.appendChild(option);
        });
        relatorioGruposCarregados = true;
    } catch (error) {
        showAlertFn(error.message, alertBoxRelatorios);
    }
}

async function carregarSubgruposRelatorio() {
    if (!relatorioFiltroSubgrupo || !relatorioFiltroGrupo) return;
    const grupoId = relatorioFiltroGrupo.value;
    relatorioFiltroSubgrupo.innerHTML = '<option value="">Todos</option>';
    if (!grupoId) {
        relatorioFiltroSubgrupo.disabled = true;
        return;
    }
    relatorioFiltroSubgrupo.disabled = false;
    try {
        const subgrupos = await categoriaService.listSubgruposByGrupo(grupoId);
        subgrupos.forEach(subgrupo => {
            const option = document.createElement('option');
            option.value = String(subgrupo.id);
            option.dataset.nome = subgrupo.nome || '';
            option.textContent = subgrupo.nome || `Subgrupo ${subgrupo.id}`;
            relatorioFiltroSubgrupo.appendChild(option);
        });
    } catch (error) {
        showAlertFn(error.message, alertBoxRelatorios);
        relatorioFiltroSubgrupo.disabled = true;
    }
}

function montarQueryParamsSatisfacaoResumo() {
    return buildSatisfacaoResumoParams({
        dataInicio: relatorioFiltroDataInicio?.value,
        dataFim: relatorioFiltroDataFim?.value,
        nota: satisfacaoFiltroNota?.value,
        statusTicket: satisfacaoFiltroStatus?.value,
        clienteId: relatorioFiltroCliente?.value
    });
}

function formatPercent(value) {
    const n = Number(value);
    if (!Number.isFinite(n)) {
        return '0';
    }
    return String(n).replace('.', ',');
}

function renderSatisfacaoResumo(resumo) {
    clearAlertFn(alertBoxSatisfacaoResumo);
    const total = resumo?.totalAvaliacoes ?? 0;
    if (!total) {
        satisfacaoResumoConteudo?.classList.add('hidden');
        satisfacaoResumoVazio?.classList.remove('hidden');
        return;
    }
    satisfacaoResumoVazio?.classList.add('hidden');
    satisfacaoResumoConteudo?.classList.remove('hidden');
    if (satisfacaoResumoMedia) {
        satisfacaoResumoMedia.textContent =
            resumo.mediaGeral != null ? String(resumo.mediaGeral).replace('.', ',') : '—';
    }
    if (satisfacaoResumoTotal) {
        satisfacaoResumoTotal.textContent = String(total);
    }
    if (satisfacaoResumoPositivas) {
        satisfacaoResumoPositivas.textContent = `${formatPercent(resumo.percentualPositivas)}%`;
    }
    if (satisfacaoResumoNegativas) {
        satisfacaoResumoNegativas.textContent = `${formatPercent(resumo.percentualNegativas)}%`;
    }
    if (satisfacaoResumoDistribuicaoBody) {
        satisfacaoResumoDistribuicaoBody.innerHTML = '';
        for (let nota = 1; nota <= 5; nota++) {
            const qtd = resumo[`quantidadeNota${nota}`] ?? 0;
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>Nota ${nota}</td><td>${qtd}</td>`;
            satisfacaoResumoDistribuicaoBody.appendChild(tr);
        }
    }
}

function renderSatisfacaoEvolucao(dias) {
    const lista = Array.isArray(dias) ? dias : [];
    if (!lista.length) {
        satisfacaoEvolucaoConteudo?.classList.add('hidden');
        satisfacaoEvolucaoVazio?.classList.remove('hidden');
        if (satisfacaoEvolucaoBody) {
            satisfacaoEvolucaoBody.innerHTML = '';
        }
        return;
    }
    satisfacaoEvolucaoVazio?.classList.add('hidden');
    satisfacaoEvolucaoConteudo?.classList.remove('hidden');
    if (!satisfacaoEvolucaoBody) {
        return;
    }
    satisfacaoEvolucaoBody.innerHTML = '';
    lista.forEach(dia => {
        const tr = document.createElement('tr');
        const media =
            dia.mediaNota != null ? String(dia.mediaNota).replace('.', ',') : '—';
        tr.innerHTML = `
            <td>${formatDateIsoToBr(dia.data)}</td>
            <td>${displayValueFn(dia.totalAvaliacoes)}</td>
            <td>${media}</td>
            <td>${displayValueFn(dia.positivas)}</td>
            <td>${displayValueFn(dia.negativas)}</td>
        `;
        satisfacaoEvolucaoBody.appendChild(tr);
    });
}

async function loadSatisfacaoResumoRelatorio() {
    try {
        const query = montarQueryParamsSatisfacaoResumo().toString();
        const [resumo, evolucao] = await Promise.all([
            satisfacaoService.getResumo(query),
            satisfacaoService.getEvolucao(query)
        ]);
        renderSatisfacaoResumo(resumo);
        renderSatisfacaoEvolucao(evolucao);
    } catch (error) {
        satisfacaoResumoConteudo?.classList.add('hidden');
        satisfacaoResumoVazio?.classList.add('hidden');
        satisfacaoEvolucaoConteudo?.classList.add('hidden');
        satisfacaoEvolucaoVazio?.classList.add('hidden');
        showAlertFn(error.message, alertBoxSatisfacaoResumo);
    }
}

async function exportarSatisfacaoCsv() {
    clearAlertFn(alertBoxSatisfacaoResumo);
    try {
        const query = montarQueryParamsSatisfacaoResumo().toString();
        const resumo = await satisfacaoService.getResumo(query);
        const response = await satisfacaoService.fetchCsv(query);
        if (!response.ok) {
            const data = await response.json().catch(() => null);
            throw new Error(mensagemErroSessaoApiFn(response, data, 'Não foi possível exportar satisfação em CSV.'));
        }
        const blob = await response.blob();
        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = 'satisfacao-tickets.csv';
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(objectUrl);
        const msg =
            (resumo?.totalAvaliacoes ?? 0) === 0
                ? 'CSV exportado. Nenhuma avaliação encontrada com os filtros selecionados.'
                : 'Exportação CSV de satisfação concluída.';
        showAlertFn(msg, alertBoxSatisfacaoResumo, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxSatisfacaoResumo);
    }
}

async function gerarRelatorioTickets() {
    clearAlertFn(alertBoxRelatorios);
    try {
        const tickets = await fetchRelatorioTickets();
        renderResumoRelatorio(tickets);
        renderTabelaRelatorio(tickets);
        await loadSatisfacaoResumoRelatorio();
    } catch (error) {
        resetRelatorioResultadoUi();
        showAlertFn(error.message, alertBoxRelatorios);
    }
}

async function exportarRelatorioCsv() {
    clearAlertFn(alertBoxRelatorios);
    try {
        const query = buildRelatorioBuscaQueryString();
        const response = await ticketService.fetchRelatorioCsv(query);
        if (!response.ok) {
            const data = await response.json().catch(() => null);
            throw new Error(mensagemErroSessaoApiFn(response, data, 'Não foi possível exportar o relatório em CSV.'));
        }
        const blob = await response.blob();
        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = 'relatorio-tickets.csv';
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(objectUrl);
    } catch (error) {
        showAlertFn(error.message, alertBoxRelatorios);
    }
}

function limparFiltrosRelatorio() {
    if (relatorioFiltroDataInicio) relatorioFiltroDataInicio.value = '';
    if (relatorioFiltroDataFim) relatorioFiltroDataFim.value = '';
    if (relatorioFiltroOrigem) relatorioFiltroOrigem.value = '';
    if (relatorioFiltroStatus) relatorioFiltroStatus.value = '';
    if (relatorioFiltroCliente) relatorioFiltroCliente.value = '';
    if (relatorioFiltroAnalistaId) relatorioFiltroAnalistaId.value = '';
    if (relatorioFiltroGrupo) relatorioFiltroGrupo.value = '';
    if (relatorioFiltroPrioridade) relatorioFiltroPrioridade.value = '';
    if (relatorioFiltroSlaPrimeiro) relatorioFiltroSlaPrimeiro.value = '';
    if (relatorioFiltroSlaResolucao) relatorioFiltroSlaResolucao.value = '';
    if (relatorioFiltroEscalonado) relatorioFiltroEscalonado.value = '';
    if (relatorioFiltroMotivo) relatorioFiltroMotivo.value = '';
    if (relatorioFiltroStatusPesquisa) relatorioFiltroStatusPesquisa.value = '';
    if (relatorioFiltroNotaAvaliacao) relatorioFiltroNotaAvaliacao.value = '';
    if (relatorioFiltroEnvioStatus) relatorioFiltroEnvioStatus.value = '';
    if (relatorioFiltroSubgrupo) {
        relatorioFiltroSubgrupo.innerHTML = '<option value="">Todos</option>';
        relatorioFiltroSubgrupo.disabled = true;
    }
    if (satisfacaoFiltroNota) satisfacaoFiltroNota.value = '';
    if (satisfacaoFiltroStatus) satisfacaoFiltroStatus.value = '';
    clearAlertFn(alertBoxRelatorios);
    clearAlertFn(alertBoxSatisfacaoResumo);
    resetRelatorioResultadoUi();
    loadSatisfacaoResumoRelatorio();
}
