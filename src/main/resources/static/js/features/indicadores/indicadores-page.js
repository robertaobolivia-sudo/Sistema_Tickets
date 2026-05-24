import { buildIndicadoresChamadosParams, formatDateIsoToBr } from '@shared/ui/query-params.js';
import { closeOtherSidebarGroups } from '@shared/ui/sidebar-groups.js';
import {
    getIndicadoresSubpageMeta,
    INDICADORES_SUBPAGE_DEFAULT,
    isIndicadoresSubpageKey
} from '@features/indicadores/indicadores-subpages.js';
import {
    buildIndicadoresSatisfacaoParams,
    formatSatisfacaoPercent,
    hasSatisfacaoResumoData
} from '@features/satisfacao/satisfacao-view.js';
import {
    buildIndicadoresEncerramentoAvaliacaoParams,
    filterClientesAtivosIndicadores,
    formatMediaNota,
    hasEncerramentoAvaliacaoData
} from '@features/satisfacao/encerramento-avaliacao-view.js';
import * as indicadoresService from '@features/indicadores/indicadores-service.js';
import * as satisfacaoService from '@features/satisfacao/satisfacao-service.js';
import * as categoriaService from '@features/configuracoes/categoria-service.js';
import * as clienteService from '@features/clientes/cliente-service.js';
import { formatIndicadorStatusRotulo } from '@features/indicadores/indicadores-gerencial-view.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = v => (v == null ? '-' : String(v));

const indicadoresPageTitle = document.getElementById('indicadoresPageTitle');
const indicadoresPageSubtitle = document.getElementById('indicadoresPageSubtitle');
const indicadoresSubpanels = document.querySelectorAll('[data-indicadores-subpanel]');

const indicadoresFiltroDataInicio = document.getElementById('indicadoresFiltroDataInicio');
const indicadoresFiltroDataFim = document.getElementById('indicadoresFiltroDataFim');
const indicadoresAtualizarBtn = document.getElementById('indicadoresAtualizarBtn');
const indicadoresVazio = document.getElementById('indicadoresVazio');
const indicadoresConteudo = document.getElementById('indicadoresConteudo');
const indicadoresTotalChamados = document.getElementById('indicadoresTotalChamados');
const indicadoresTotalClientes = document.getElementById('indicadoresTotalClientes');
const indicadoresTotalNaoAtendimento = document.getElementById('indicadoresTotalNaoAtendimento');
const indicadoresRankingAtendenteBody = document.getElementById('indicadoresRankingAtendenteBody');
const indicadoresTopGruposBody = document.getElementById('indicadoresTopGruposBody');
const indicadoresTopSubgruposBody = document.getElementById('indicadoresTopSubgruposBody');
const indicadoresPrioridadeBody = document.getElementById('indicadoresPrioridadeBody');
const indicadoresStatusBody = document.getElementById('indicadoresStatusBody');

const indicSatFiltroDataInicio = document.getElementById('indicSatFiltroDataInicio');
const indicSatFiltroDataFim = document.getElementById('indicSatFiltroDataFim');
const indicSatFiltroNota = document.getElementById('indicSatFiltroNota');
const indicSatFiltroStatus = document.getElementById('indicSatFiltroStatus');
const indicSatFiltroCliente = document.getElementById('indicSatFiltroCliente');
const indicSatAtualizarBtn = document.getElementById('indicSatAtualizarBtn');
const indicSatResumoVazio = document.getElementById('indicSatResumoVazio');
const indicSatResumoConteudo = document.getElementById('indicSatResumoConteudo');
const indicSatResumoMedia = document.getElementById('indicSatResumoMedia');
const indicSatResumoTotal = document.getElementById('indicSatResumoTotal');
const indicSatResumoPositivas = document.getElementById('indicSatResumoPositivas');
const indicSatResumoNegativas = document.getElementById('indicSatResumoNegativas');
const indicSatDistribuicaoBody = document.getElementById('indicSatDistribuicaoBody');
const indicSatEvolucaoVazio = document.getElementById('indicSatEvolucaoVazio');
const indicSatEvolucaoConteudo = document.getElementById('indicSatEvolucaoConteudo');
const indicSatEvolucaoBody = document.getElementById('indicSatEvolucaoBody');

const indicEncDataInicio = document.getElementById('indicEncDataInicio');
const indicEncDataFim = document.getElementById('indicEncDataFim');
const indicEncFiltroCliente = document.getElementById('indicEncFiltroCliente');
const indicEncFiltroMotivo = document.getElementById('indicEncFiltroMotivo');
const indicEncFiltroStatusPesquisa = document.getElementById('indicEncFiltroStatusPesquisa');
const indicEncFiltroNota = document.getElementById('indicEncFiltroNota');
const indicEncAtualizarBtn = document.getElementById('indicEncAtualizarBtn');
const indicEncVazio = document.getElementById('indicEncVazio');
const indicEncConteudo = document.getElementById('indicEncConteudo');
const indicEncTopMotivosBody = document.getElementById('indicEncTopMotivosBody');
const indicEncTotalPesquisas = document.getElementById('indicEncTotalPesquisas');
const indicEncMediaNota = document.getElementById('indicEncMediaNota');
const indicEncPendentes = document.getElementById('indicEncPendentes');
const indicEncRespondidas = document.getElementById('indicEncRespondidas');
const indicEncExpiradas = document.getElementById('indicEncExpiradas');
const indicEncNaoEnviadas = document.getElementById('indicEncNaoEnviadas');
const indicEncNotasBody = document.getElementById('indicEncNotasBody');
const indicEncEnvioSimuladas = document.getElementById('indicEncEnvioSimuladas');
const indicEncEnvioFalhas = document.getElementById('indicEncEnvioFalhas');
const indicEncEnvioSemTentativa = document.getElementById('indicEncEnvioSemTentativa');

const alertBoxIndicadores = document.getElementById('alertBoxIndicadores');

let indicEncMotivosCarregados = false;
let indicEncClientesCarregados = false;

let activeSubpage = INDICADORES_SUBPAGE_DEFAULT;
let listenersBound = false;

export function getActiveIndicadoresSubpage() {
    return activeSubpage;
}

export function setActiveIndicadoresSubpage(subKey) {
    if (isIndicadoresSubpageKey(subKey)) {
        activeSubpage = subKey;
    }
}

export function initIndicadoresPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (listenersBound) return;
    listenersBound = true;
    indicadoresAtualizarBtn?.addEventListener('click', () => loadIndicadoresChamados());
    indicSatAtualizarBtn?.addEventListener('click', () => loadIndicadoresSatisfacao());
    indicEncAtualizarBtn?.addEventListener('click', () => loadIndicadoresEncerramentoSatisfacao());
    document.getElementById('indicClientesAtualizarBtn')?.addEventListener('click', () => loadIndicadoresClientes());
    document.getElementById('indicAtendAtualizarBtn')?.addEventListener('click', () => loadIndicadoresAtendentes());
    document.getElementById('indicSlaAtualizarBtn')?.addEventListener('click', () => loadIndicadoresSla());
}

export async function loadIndicadoresPage() {
    showIndicadoresSubpage(activeSubpage);
}

export function showIndicadoresSubpage(subKey) {
    const key = isIndicadoresSubpageKey(subKey) ? subKey : INDICADORES_SUBPAGE_DEFAULT;
    activeSubpage = key;
    clearAlertFn(alertBoxIndicadores);

    const meta = getIndicadoresSubpageMeta(key);
    if (indicadoresPageTitle) {
        indicadoresPageTitle.textContent = meta.titulo;
    }
    if (indicadoresPageSubtitle) {
        indicadoresPageSubtitle.textContent = meta.subtitulo;
    }

    indicadoresSubpanels?.forEach(panel => {
        const panelKey = panel.getAttribute('data-indicadores-subpanel');
        const ativo = panelKey === key;
        panel.classList.toggle('hidden', !ativo);
        panel.classList.toggle('active', ativo);
    });

    syncIndicadoresNavActive(key);

    if (key === 'indicadores-chamados') {
        loadIndicadoresChamados();
    } else if (key === 'indicadores-satisfacao') {
        loadIndicadoresSatisfacao();
    } else if (key === 'indicadores-encerramento-satisfacao') {
        loadIndicadoresEncerramentoSatisfacao();
    } else if (key === 'indicadores-clientes') {
        loadIndicadoresClientes();
    } else if (key === 'indicadores-atendentes') {
        loadIndicadoresAtendentes();
    } else if (key === 'indicadores-sla') {
        loadIndicadoresSla();
    }
}

function syncIndicadoresNavActive(subKey) {
    document.querySelectorAll('[data-indicadores-sub]').forEach(btn => {
        const match = btn.getAttribute('data-indicadores-sub') === subKey;
        btn.classList.remove('active');
        btn.classList.toggle('is-active-page', match);
    });
    const parent = document.getElementById('navIndicadoresToggle');
    const group = document.getElementById('navIndicadoresGroup');
    if (parent) {
        parent.classList.remove('active');
        parent.classList.add('is-active-parent');
        parent.setAttribute('aria-expanded', group?.classList.contains('is-open') ? 'true' : 'false');
    }
}

function montarQueryParamsIndicadoresSatisfacao() {
    return buildIndicadoresSatisfacaoParams({
        dataInicio: indicSatFiltroDataInicio?.value,
        dataFim: indicSatFiltroDataFim?.value,
        nota: indicSatFiltroNota?.value,
        statusTicket: indicSatFiltroStatus?.value,
        termoCliente: indicSatFiltroCliente?.value
    });
}

async function loadIndicadoresSatisfacao() {
    clearAlertFn(alertBoxIndicadores);
    try {
        const query = montarQueryParamsIndicadoresSatisfacao().toString();
        const [resumo, evolucao] = await Promise.all([
            satisfacaoService.getResumo(query),
            satisfacaoService.getEvolucao(query)
        ]);
        renderIndicadoresSatisfacaoResumo(resumo);
        renderIndicadoresSatisfacaoEvolucao(evolucao);
    } catch (error) {
        indicSatResumoConteudo?.classList.add('hidden');
        indicSatResumoVazio?.classList.add('hidden');
        indicSatEvolucaoConteudo?.classList.add('hidden');
        indicSatEvolucaoVazio?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresSatisfacaoResumo(resumo) {
    if (!hasSatisfacaoResumoData(resumo)) {
        indicSatResumoConteudo?.classList.add('hidden');
        indicSatResumoVazio?.classList.remove('hidden');
        return;
    }
    indicSatResumoVazio?.classList.add('hidden');
    indicSatResumoConteudo?.classList.remove('hidden');

    if (indicSatResumoMedia) {
        indicSatResumoMedia.textContent =
            resumo.mediaGeral != null ? String(resumo.mediaGeral).replace('.', ',') : '—';
    }
    if (indicSatResumoTotal) {
        indicSatResumoTotal.textContent = String(resumo.totalAvaliacoes);
    }
    if (indicSatResumoPositivas) {
        indicSatResumoPositivas.textContent = `${formatSatisfacaoPercent(resumo.percentualPositivas)}%`;
    }
    if (indicSatResumoNegativas) {
        indicSatResumoNegativas.textContent = `${formatSatisfacaoPercent(resumo.percentualNegativas)}%`;
    }
    if (indicSatDistribuicaoBody) {
        indicSatDistribuicaoBody.innerHTML = '';
        for (let nota = 1; nota <= 5; nota++) {
            const qtd = resumo[`quantidadeNota${nota}`] ?? 0;
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>Nota ${nota}</td><td>${qtd}</td>`;
            indicSatDistribuicaoBody.appendChild(tr);
        }
    }
}

function renderIndicadoresSatisfacaoEvolucao(dias) {
    const lista = Array.isArray(dias) ? dias : [];
    if (!lista.length) {
        indicSatEvolucaoConteudo?.classList.add('hidden');
        indicSatEvolucaoVazio?.classList.remove('hidden');
        if (indicSatEvolucaoBody) {
            indicSatEvolucaoBody.innerHTML = '';
        }
        return;
    }
    indicSatEvolucaoVazio?.classList.add('hidden');
    indicSatEvolucaoConteudo?.classList.remove('hidden');
    if (!indicSatEvolucaoBody) {
        return;
    }
    indicSatEvolucaoBody.innerHTML = '';
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
        indicSatEvolucaoBody.appendChild(tr);
    });
}

async function ensureIndicEncClientesSelect() {
    if (indicEncClientesCarregados || !indicEncFiltroCliente) {
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
            const nome = c.nome && String(c.nome).trim() !== '' ? String(c.nome).trim() : `Cliente ${c.id}`;
            option.textContent = nome;
            indicEncFiltroCliente.appendChild(option);
        });
        indicEncClientesCarregados = true;
    } catch {
        /* mantém apenas "Todos os clientes" */
    }
}

async function ensureIndicEncMotivosSelect() {
    if (indicEncMotivosCarregados || !indicEncFiltroMotivo) {
        return;
    }
    try {
        const motivos = await categoriaService.listMotivos();
        const lista = Array.isArray(motivos) ? motivos : [];
        lista.forEach(m => {
            const option = document.createElement('option');
            option.value = String(m.id);
            option.textContent = m.nome ?? `Motivo ${m.id}`;
            indicEncFiltroMotivo.appendChild(option);
        });
        indicEncMotivosCarregados = true;
    } catch {
        /* filtro de motivo permanece só com "Todos" */
    }
}

async function loadIndicadoresEncerramentoSatisfacao() {
    clearAlertFn(alertBoxIndicadores);
    await Promise.all([ensureIndicEncClientesSelect(), ensureIndicEncMotivosSelect()]);
    try {
        const params = buildIndicadoresEncerramentoAvaliacaoParams({
            dataInicio: indicEncDataInicio?.value,
            dataFim: indicEncDataFim?.value,
            clienteId: indicEncFiltroCliente?.value,
            motivoId: indicEncFiltroMotivo?.value,
            statusPesquisa: indicEncFiltroStatusPesquisa?.value,
            notaAvaliacao: indicEncFiltroNota?.value
        });
        const dados = await indicadoresService.getEncerramentoAvaliacao(params.toString());
        renderIndicadoresEncerramentoSatisfacao(dados);
    } catch (error) {
        indicEncConteudo?.classList.add('hidden');
        indicEncVazio?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresEncerramentoSatisfacao(dados) {
    if (!hasEncerramentoAvaliacaoData(dados)) {
        indicEncConteudo?.classList.add('hidden');
        indicEncVazio?.classList.remove('hidden');
        return;
    }
    indicEncVazio?.classList.add('hidden');
    indicEncConteudo?.classList.remove('hidden');

    if (indicEncTopMotivosBody) {
        indicEncTopMotivosBody.innerHTML = '';
        const top = Array.isArray(dados.topMotivos) ? dados.topMotivos : [];
        if (!top.length) {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td colspan="3" class="empty-state">—</td>';
            indicEncTopMotivosBody.appendChild(tr);
        } else {
            top.forEach(item => {
                const tr = document.createElement('tr');
                const cat = [item.categoriaNome, item.subcategoriaNome].filter(Boolean).join(' / ');
                tr.innerHTML = `
                    <td>${escapeHtml(item.motivoNome ?? '—')}</td>
                    <td>${escapeHtml(cat || '—')}</td>
                    <td>${item.totalTickets ?? 0}</td>
                `;
                indicEncTopMotivosBody.appendChild(tr);
            });
        }
    }

    const p = dados.pesquisa ?? {};
    if (indicEncTotalPesquisas) indicEncTotalPesquisas.textContent = String(p.totalPesquisas ?? 0);
    if (indicEncMediaNota) indicEncMediaNota.textContent = formatMediaNota(p.mediaNota);
    if (indicEncPendentes) indicEncPendentes.textContent = String(p.pendentes ?? 0);
    if (indicEncRespondidas) indicEncRespondidas.textContent = String(p.respondidas ?? 0);
    if (indicEncExpiradas) indicEncExpiradas.textContent = String(p.expiradas ?? 0);
    if (indicEncNaoEnviadas) indicEncNaoEnviadas.textContent = String(p.naoEnviadas ?? 0);

    if (indicEncNotasBody) {
        indicEncNotasBody.innerHTML = '';
        for (let nota = 1; nota <= 5; nota++) {
            const qtd = p[`quantidadeNota${nota}`] ?? 0;
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>Nota ${nota}</td><td>${qtd}</td>`;
            indicEncNotasBody.appendChild(tr);
        }
    }

    const e = dados.envio ?? {};
    if (indicEncEnvioSimuladas) indicEncEnvioSimuladas.textContent = String(e.simuladas ?? 0);
    if (indicEncEnvioFalhas) indicEncEnvioFalhas.textContent = String(e.falhas ?? 0);
    if (indicEncEnvioSemTentativa) indicEncEnvioSemTentativa.textContent = String(e.semTentativa ?? 0);
}

async function loadIndicadoresChamados() {
    clearAlertFn(alertBoxIndicadores);
    try {
        const params = buildIndicadoresChamadosParams({
            dataInicio: indicadoresFiltroDataInicio?.value,
            dataFim: indicadoresFiltroDataFim?.value
        });
        const dados = await indicadoresService.getChamados(params.toString());
        renderIndicadoresChamados(dados);
    } catch (error) {
        indicadoresConteudo?.classList.add('hidden');
        indicadoresVazio?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresChamados(dados) {
    const total = dados?.totalChamados ?? 0;
    const naoAtend = dados?.totalNaoAtendimento ?? 0;
    if (!total && !naoAtend) {
        indicadoresConteudo?.classList.add('hidden');
        indicadoresVazio?.classList.remove('hidden');
        return;
    }
    indicadoresVazio?.classList.add('hidden');
    indicadoresConteudo?.classList.remove('hidden');

    if (indicadoresTotalChamados) {
        indicadoresTotalChamados.textContent = String(total);
    }
    if (indicadoresTotalClientes) {
        indicadoresTotalClientes.textContent = String(dados.totalClientes ?? 0);
    }
    if (indicadoresTotalNaoAtendimento) {
        indicadoresTotalNaoAtendimento.textContent = String(naoAtend);
    }

    preencherTabela(indicadoresRankingAtendenteBody, dados.chamadosPorAtendente);
    preencherTabela(indicadoresTopGruposBody, dados.chamadosPorGrupo);
    preencherTabela(indicadoresTopSubgruposBody, dados.chamadosPorSubgrupo);
    preencherTabela(indicadoresPrioridadeBody, dados.chamadosPorPrioridade);
    preencherTabela(indicadoresStatusBody, dados.chamadosPorStatus, true);
}

function preencherTabela(tbody, itens, formatStatusRotulo = false) {
    if (!tbody) return;
    tbody.innerHTML = '';
    const lista = Array.isArray(itens) ? itens : [];
    if (!lista.length) {
        const tr = document.createElement('tr');
        tr.innerHTML = '<td colspan="2" class="empty-state">—</td>';
        tbody.appendChild(tr);
        return;
    }
    lista.forEach(item => {
        const tr = document.createElement('tr');
        const rotulo = formatStatusRotulo
            ? formatIndicadorStatusRotulo(item.rotulo)
            : (item.rotulo ?? '-');
        tr.innerHTML = `<td>${escapeHtml(rotulo)}</td><td>${item.total ?? 0}</td>`;
        tbody.appendChild(tr);
    });
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// ===== CLIENTES =====
async function loadIndicadoresClientes() {
    clearAlertFn(alertBoxIndicadores);
    const params = new URLSearchParams();
    const inicio = document.getElementById('indicClientesFiltroDataInicio')?.value;
    const fim = document.getElementById('indicClientesFiltroDataFim')?.value;
    if (inicio) params.set('dataInicio', inicio);
    if (fim) params.set('dataFim', fim);
    try {
        const dados = await indicadoresService.getClientes(params.toString());
        renderIndicadoresClientes(dados);
    } catch (error) {
        document.getElementById('indicClientesConteudo')?.classList.add('hidden');
        document.getElementById('indicClientesVazio')?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresClientes(dados) {
    const vazio = document.getElementById('indicClientesVazio');
    const conteudo = document.getElementById('indicClientesConteudo');
    const lista = Array.isArray(dados?.rankingClientes) ? dados.rankingClientes : [];
    if (!lista.length) {
        conteudo?.classList.add('hidden');
        vazio?.classList.remove('hidden');
        return;
    }
    vazio?.classList.add('hidden');
    conteudo?.classList.remove('hidden');
    const totalEl = document.getElementById('indicClientesTotalTickets');
    const clientesEl = document.getElementById('indicClientesTotalClientes');
    if (totalEl) totalEl.textContent = String(dados.totalTickets ?? 0);
    if (clientesEl) clientesEl.textContent = String(dados.totalClientes ?? 0);
    const tbody = document.getElementById('indicClientesRankingBody');
    if (tbody) {
        tbody.innerHTML = '';
        lista.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(item.clienteNome ?? '—')}</td>
                <td>${item.totalTickets ?? 0}</td>
                <td>${item.tmeMinutosUteis != null ? item.tmeMinutosUteis : '—'}</td>
                <td>${item.tmaMinutosUteis != null ? item.tmaMinutosUteis : '—'}</td>
                <td>${item.percentualSlaCumprido != null ? item.percentualSlaCumprido + '%' : '—'}</td>
            `;
            tbody.appendChild(tr);
        });
    }
}

// ===== ATENDENTES =====
async function loadIndicadoresAtendentes() {
    clearAlertFn(alertBoxIndicadores);
    const params = new URLSearchParams();
    const inicio = document.getElementById('indicAtendFiltroDataInicio')?.value;
    const fim = document.getElementById('indicAtendFiltroDataFim')?.value;
    if (inicio) params.set('dataInicio', inicio);
    if (fim) params.set('dataFim', fim);
    try {
        const dados = await indicadoresService.getAtendentes(params.toString());
        renderIndicadoresAtendentes(dados);
    } catch (error) {
        document.getElementById('indicAtendConteudo')?.classList.add('hidden');
        document.getElementById('indicAtendVazio')?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresAtendentes(dados) {
    const vazio = document.getElementById('indicAtendVazio');
    const conteudo = document.getElementById('indicAtendConteudo');
    const lista = Array.isArray(dados?.atendentes) ? dados.atendentes : [];
    if (!lista.length) {
        conteudo?.classList.add('hidden');
        vazio?.classList.remove('hidden');
        return;
    }
    vazio?.classList.add('hidden');
    conteudo?.classList.remove('hidden');
    const totalEl = document.getElementById('indicAtendTotalTickets');
    if (totalEl) totalEl.textContent = String(dados.totalTickets ?? 0);
    const tbody = document.getElementById('indicAtendDesempenhoBody');
    if (tbody) {
        tbody.innerHTML = '';
        lista.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(item.analistaNome ?? '—')}</td>
                <td>${item.totalTickets ?? 0}</td>
                <td>${item.tmaMinutosUteis != null ? item.tmaMinutosUteis : '—'}</td>
                <td>${item.percentualSlaCumprido != null ? item.percentualSlaCumprido + '%' : '—'}</td>
                <td>${item.mediaAvaliacao != null ? String(item.mediaAvaliacao).replace('.', ',') : '—'}</td>
                <td>${item.totalAvaliacoes ?? 0}</td>
            `;
            tbody.appendChild(tr);
        });
    }
}

// ===== SLA =====
async function loadIndicadoresSla() {
    clearAlertFn(alertBoxIndicadores);
    const params = new URLSearchParams();
    const inicio = document.getElementById('indicSlaFiltroDataInicio')?.value;
    const fim = document.getElementById('indicSlaFiltroDataFim')?.value;
    if (inicio) params.set('dataInicio', inicio);
    if (fim) params.set('dataFim', fim);
    try {
        const dados = await indicadoresService.getSla(params.toString());
        renderIndicadoresSla(dados);
    } catch (error) {
        document.getElementById('indicSlaConteudo')?.classList.add('hidden');
        document.getElementById('indicSlaVazio')?.classList.add('hidden');
        showAlertFn(error.message, alertBoxIndicadores);
    }
}

function renderIndicadoresSla(dados) {
    const vazio = document.getElementById('indicSlaVazio');
    const conteudo = document.getElementById('indicSlaConteudo');
    const lista = Array.isArray(dados?.porPrioridade) ? dados.porPrioridade : [];
    if (!dados?.totalTicketsEncerrados) {
        conteudo?.classList.add('hidden');
        vazio?.classList.remove('hidden');
        return;
    }
    vazio?.classList.add('hidden');
    conteudo?.classList.remove('hidden');
    const set = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v; };
    set('indicSlaTotalEncerrados', String(dados.totalTicketsEncerrados ?? 0));
    set('indicSlaTotalCumpridos', String(dados.totalCumpridos ?? 0));
    set('indicSlaPctGeral', dados.percentualGeralCumprimento != null ? dados.percentualGeralCumprimento + '%' : '—');
    set('indicSlaMetaMinutos', String(dados.metaMinutos ?? 60));
    const tbody = document.getElementById('indicSlaPrioridadeBody');
    if (tbody) {
        tbody.innerHTML = '';
        lista.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(item.prioridade ?? '—')}</td>
                <td>${item.totalTickets ?? 0}</td>
                <td>${item.cumpridos ?? 0}</td>
                <td>${item.naoAvaliados ?? 0}</td>
                <td>${item.percentualCumprimento != null ? item.percentualCumprimento + '%' : '—'}</td>
                <td>${item.tempoMedioMinutos != null ? item.tempoMedioMinutos : '—'}</td>
            `;
            tbody.appendChild(tr);
        });
    }
}

export function initIndicadoresSidebarNav({ showPageFn, canAccessIndicadoresFn }) {
    const group = document.getElementById('navIndicadoresGroup');
    const toggle = document.getElementById('navIndicadoresToggle');
    const submenu = document.getElementById('navIndicadoresSubmenu');

    if (!group || !toggle || !submenu) {
        return;
    }

    const setOpen = open => {
        group.classList.toggle('is-open', open);
        toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
        submenu.classList.toggle('hidden', !open);
    };

    toggle.addEventListener('click', () => {
        if (canAccessIndicadoresFn && !canAccessIndicadoresFn()) {
            return;
        }
        const willOpen = !group.classList.contains('is-open');
        if (willOpen) closeOtherSidebarGroups('navIndicadoresGroup');
        setOpen(willOpen);
    });

    document.querySelectorAll('[data-indicadores-sub]').forEach(btn => {
        btn.addEventListener('click', () => {
            const sub = btn.getAttribute('data-indicadores-sub');
            if (!isIndicadoresSubpageKey(sub)) {
                return;
            }
            setActiveIndicadoresSubpage(sub);
            setOpen(true);
            showPageFn('indicadores');
            btn.classList.add('is-active-page');
        });
    });
}
