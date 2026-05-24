import { buildAuditoriaFilterParams } from '@shared/ui/query-params.js';
import { canAccessAuditoria } from '@shared/permissions/permissions.js';
import * as auditoriaService from '@features/auditoria/auditoria-service.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatDateTimeFn = () => '-';
let mensagemErroSessaoApiFn = (_r, _d, msg) => msg;

const alertBoxAuditoria = document.getElementById('alertBoxAuditoria');
const auditoriaFiltrosForm = document.getElementById('auditoriaFiltrosForm');
const auditoriaFiltroDataInicio = document.getElementById('auditoriaFiltroDataInicio');
const auditoriaFiltroDataFim = document.getElementById('auditoriaFiltroDataFim');
const auditoriaFiltroAnalistaId = document.getElementById('auditoriaFiltroAnalistaId');
const auditoriaFiltroAcao = document.getElementById('auditoriaFiltroAcao');
const auditoriaFiltroEntidade = document.getElementById('auditoriaFiltroEntidade');
const auditoriaFiltroEntidadeId = document.getElementById('auditoriaFiltroEntidadeId');
const auditoriaFiltroLimite = document.getElementById('auditoriaFiltroLimite');
const auditoriaLimparFiltrosBtn = document.getElementById('auditoriaLimparFiltrosBtn');
const auditoriaExportarCsvBtn = document.getElementById('auditoriaExportarCsvBtn');
const auditoriaTableBody = document.getElementById('auditoriaTableBody');
const auditoriaEmptyMessage = document.getElementById('auditoriaEmptyMessage');
const auditoriaPaginacaoInfo = document.getElementById('auditoriaPaginacaoInfo');
const auditoriaPaginaAnteriorBtn = document.getElementById('auditoriaPaginaAnteriorBtn');
const auditoriaPaginaProximaBtn = document.getElementById('auditoriaPaginaProximaBtn');
const auditoriaRetencaoAntesDe = document.getElementById('auditoriaRetencaoAntesDe');
const auditoriaRetencaoContarBtn = document.getElementById('auditoriaRetencaoContarBtn');
const auditoriaRetencaoExcluirBtn = document.getElementById('auditoriaRetencaoExcluirBtn');
const auditoriaRetencaoResultado = document.getElementById('auditoriaRetencaoResultado');

let auditoriaPaginaAtual = 0;
let listenersBound = false;

export function initAuditoriaPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDateTime) formatDateTimeFn = deps.formatDateTime;
    if (deps.mensagemErroSessaoApi) mensagemErroSessaoApiFn = deps.mensagemErroSessaoApi;
    if (listenersBound) return;
    listenersBound = true;

    auditoriaFiltrosForm?.addEventListener('submit', event => {
        event.preventDefault();
        auditoriaPaginaAtual = 0;
        buscarAuditoriaEventos(0);
    });
    auditoriaLimparFiltrosBtn?.addEventListener('click', () => {
        limparFiltrosAuditoria();
        buscarAuditoriaEventos(0);
    });
    auditoriaExportarCsvBtn?.addEventListener('click', exportarAuditoriaCsv);
    auditoriaRetencaoContarBtn?.addEventListener('click', contarAuditoriaEventosAntigos);
    auditoriaRetencaoExcluirBtn?.addEventListener('click', excluirAuditoriaEventosAntigos);
    auditoriaPaginaAnteriorBtn?.addEventListener('click', () => {
        if (auditoriaPaginaAtual > 0) {
            buscarAuditoriaEventos(auditoriaPaginaAtual - 1);
        }
    });
    auditoriaPaginaProximaBtn?.addEventListener('click', () => {
        buscarAuditoriaEventos(auditoriaPaginaAtual + 1);
    });
}

export function loadAuditoriaPage() {
    clearAlertFn(alertBoxAuditoria);
    if (!canAccessAuditoria()) {
        showAlertFn('Acesso restrito a administradores.', alertBoxAuditoria);
        return;
    }
    if (auditoriaRetencaoAntesDe && !auditoriaRetencaoAntesDe.value) {
        const sugerida = new Date();
        sugerida.setDate(sugerida.getDate() - 31);
        auditoriaRetencaoAntesDe.value = sugerida.toISOString().slice(0, 10);
    }
    if (auditoriaRetencaoResultado) {
        auditoriaRetencaoResultado.textContent = '—';
    }
    auditoriaPaginaAtual = 0;
    buscarAuditoriaEventos(0);
}

function obterAuditoriaRetencaoAntesDe() {
    const valor = auditoriaRetencaoAntesDe?.value?.trim();
    if (!valor) {
        throw new Error('Informe a data limite para retenção.');
    }
    return valor;
}

async function contarAuditoriaEventosAntigos() {
    clearAlertFn(alertBoxAuditoria);
    if (!canAccessAuditoria()) {
        showAlertFn('Acesso restrito a administradores.', alertBoxAuditoria);
        return;
    }
    try {
        const antesDe = obterAuditoriaRetencaoAntesDe();
        const params = new URLSearchParams({ antesDe });
        const data = await auditoriaService.contarEventosAntigos(params.toString());
        const qtd = data?.quantidade ?? 0;
        const msg = `${qtd} evento(s) com data anterior a ${data?.antesDe ?? antesDe}.`;
        if (auditoriaRetencaoResultado) {
            auditoriaRetencaoResultado.textContent = msg;
        }
        showAlertFn(msg, alertBoxAuditoria, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxAuditoria);
    }
}

async function excluirAuditoriaEventosAntigos() {
    clearAlertFn(alertBoxAuditoria);
    if (!canAccessAuditoria()) {
        showAlertFn('Acesso restrito a administradores.', alertBoxAuditoria);
        return;
    }
    try {
        const antesDe = obterAuditoriaRetencaoAntesDe();
        const ok = window.confirm(
            `Excluir permanentemente os eventos de auditoria anteriores a ${antesDe}? Esta ação não pode ser desfeita.`
        );
        if (!ok) {
            return;
        }
        const params = new URLSearchParams({ antesDe, confirmar: 'true' });
        const data = await auditoriaService.excluirEventosAntigos(params.toString());
        const excluidos = data?.excluidos ?? 0;
        const msg = `${excluidos} evento(s) excluído(s) (anteriores a ${data?.antesDe ?? antesDe}).`;
        if (auditoriaRetencaoResultado) {
            auditoriaRetencaoResultado.textContent = msg;
        }
        showAlertFn(msg, alertBoxAuditoria, 'success');
        buscarAuditoriaEventos(auditoriaPaginaAtual);
    } catch (error) {
        showAlertFn(error.message, alertBoxAuditoria);
    }
}

function montarQueryParamsAuditoriaFiltros() {
    return buildAuditoriaFilterParams({
        dataInicio: auditoriaFiltroDataInicio?.value,
        dataFim: auditoriaFiltroDataFim?.value,
        analistaId: auditoriaFiltroAnalistaId?.value,
        acao: auditoriaFiltroAcao?.value,
        entidade: auditoriaFiltroEntidade?.value,
        entidadeId: auditoriaFiltroEntidadeId?.value
    });
}

function montarParametrosAuditoria(pagina) {
    const params = montarQueryParamsAuditoriaFiltros();
    params.set('pagina', String(Math.max(0, pagina)));
    const limite = Number(auditoriaFiltroLimite?.value) || 50;
    params.set('limite', String(limite));
    return params;
}

async function exportarAuditoriaCsv() {
    clearAlertFn(alertBoxAuditoria);
    if (!canAccessAuditoria()) {
        showAlertFn('Acesso restrito a administradores.', alertBoxAuditoria);
        return;
    }
    try {
        const query = montarQueryParamsAuditoriaFiltros().toString();
        const response = await auditoriaService.fetchEventosCsv(query);
        if (!response.ok) {
            const data = await response.json().catch(() => null);
            throw new Error(mensagemErroSessaoApiFn(response, data, 'Não foi possível exportar a auditoria em CSV.'));
        }
        const blob = await response.blob();
        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = 'auditoria-eventos.csv';
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(objectUrl);
        showAlertFn('Exportação CSV concluída.', alertBoxAuditoria, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxAuditoria);
    }
}

async function fetchAuditoriaEventos(pagina) {
    return auditoriaService.listEventos(montarParametrosAuditoria(pagina).toString());
}

function renderAuditoriaTabela(page) {
    if (!auditoriaTableBody) {
        return;
    }
    auditoriaTableBody.innerHTML = '';
    const itens = Array.isArray(page?.itens) ? page.itens : [];
    if (!itens.length) {
        auditoriaEmptyMessage?.classList.remove('hidden');
        if (auditoriaPaginacaoInfo) {
            auditoriaPaginacaoInfo.textContent = 'Nenhum registro';
        }
        atualizarBotoesPaginacaoAuditoria(page, false, false);
        return;
    }
    auditoriaEmptyMessage?.classList.add('hidden');
    itens.forEach(evento => {
        const tr = document.createElement('tr');
        const nomeAnalista = displayValueFn(
            evento.analistaNome || (evento.analistaId != null ? `ID ${evento.analistaId}` : '-')
        );
        tr.innerHTML = `
            <td>${formatDateTimeFn(evento.dataHora)}</td>
            <td title="${nomeAnalista}">${nomeAnalista}</td>
            <td>${displayValueFn(evento.perfilAcesso)}</td>
            <td>${displayValueFn(evento.acao)}</td>
            <td>${displayValueFn(evento.entidade)}</td>
            <td>${displayValueFn(evento.entidadeId)}</td>
            <td class="auditoria-desc-cell" title="${displayValueFn(evento.descricao)}">${displayValueFn(evento.descricao)}</td>
            <td>${displayValueFn(evento.ipOrigem)}</td>
        `;
        auditoriaTableBody.appendChild(tr);
    });

    const total = page?.total ?? itens.length;
    const pagina = page?.pagina ?? 0;
    const limite = page?.limite ?? itens.length;
    const inicio = total === 0 ? 0 : pagina * limite + 1;
    const fim = Math.min(total, (pagina + 1) * limite);
    if (auditoriaPaginacaoInfo) {
        auditoriaPaginacaoInfo.textContent = `Exibindo ${inicio}–${fim} de ${total} (página ${pagina + 1})`;
    }
    const temAnterior = pagina > 0;
    const temProxima = fim < total;
    atualizarBotoesPaginacaoAuditoria(page, temAnterior, temProxima);
}

function atualizarBotoesPaginacaoAuditoria(page, temAnterior, temProxima) {
    if (auditoriaPaginaAnteriorBtn) {
        auditoriaPaginaAnteriorBtn.disabled = !temAnterior;
    }
    if (auditoriaPaginaProximaBtn) {
        auditoriaPaginaProximaBtn.disabled = !temProxima;
    }
    auditoriaPaginaAtual = page?.pagina ?? 0;
}

async function buscarAuditoriaEventos(pagina) {
    clearAlertFn(alertBoxAuditoria);
    if (!canAccessAuditoria()) {
        showAlertFn('Acesso restrito a administradores.', alertBoxAuditoria);
        return;
    }
    try {
        const page = await fetchAuditoriaEventos(pagina);
        renderAuditoriaTabela(page);
    } catch (error) {
        if (auditoriaTableBody) {
            auditoriaTableBody.innerHTML = '';
        }
        auditoriaEmptyMessage?.classList.add('hidden');
        showAlertFn(error.message, alertBoxAuditoria);
    }
}

function limparFiltrosAuditoria() {
    auditoriaFiltrosForm?.reset();
    if (auditoriaFiltroLimite) {
        auditoriaFiltroLimite.value = '50';
    }
    auditoriaPaginaAtual = 0;
}
