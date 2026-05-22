import * as ticketService from '../services/ticketService.js';
import { canRegisterSatisfacao, isTicketFinalizado } from '../rules/ticketViewRules.js';
import * as satisfacaoService from '../services/satisfacaoService.js';
import * as categoriaService from '../services/categoriaService.js';
import {
    deveDesabilitarPesquisaSimEncerramento,
    validateEncerramentoPayload
} from '../core/encerramentoView.js';
import { formatSatisfacaoNotaExibicao, formatSatisfacaoStatusLabel } from '../core/satisfacaoView.js';
import { mensagemErroSessaoApi } from '../core/messages.js';

const detailModal = document.getElementById('modalDetalhes');
const closeModal = document.getElementById('fecharModal');
const detailNumero = document.getElementById('detailNumero');
const detailCliente = document.getElementById('detailCliente');
const detailTelefone = document.getElementById('detailTelefone');
const detailTelefoneContato = document.getElementById('detailTelefoneContato');
const detailEmail = document.getElementById('detailEmail');
const detailEmpresa = document.getElementById('detailEmpresa');
const detailCnpj = document.getElementById('detailCnpj');
const detailCidade = document.getElementById('detailCidade');
const detailUf = document.getElementById('detailUf');
const detailCarteira = document.getElementById('detailCarteira');
const detailAnalista = document.getElementById('detailAnalista');
const detailCanal = document.getElementById('detailCanal');
const detailConexao = document.getElementById('detailConexao');
const detailMensagem = document.getElementById('detailMensagem');
const detailStatus = document.getElementById('detailStatus');
const detailPrioridade = document.getElementById('detailPrioridade');
const detailAbertura = document.getElementById('detailAbertura');
const detailPrimeiro = document.getElementById('detailPrimeiro');
const detailSlaVencimento = document.getElementById('detailSlaVencimento');
const detailSlaStatus = document.getElementById('detailSlaStatus');
const detailSlaResolucaoVencimento = document.getElementById('detailSlaResolucaoVencimento');
const detailSlaResolucaoStatus = document.getElementById('detailSlaResolucaoStatus');
const detailSlaPausado = document.getElementById('detailSlaPausado');
const detailSlaPausaInicio = document.getElementById('detailSlaPausaInicio');
const detailSlaMinutosPausados = document.getElementById('detailSlaMinutosPausados');
const detailEncerramento = document.getElementById('detailEncerramento');
const detailEncerramentoResumo = document.getElementById('detailEncerramentoResumo');
const detailGrupoCategoria = document.getElementById('detailGrupoCategoria');
const detailSubgrupoCategoria = document.getElementById('detailSubgrupoCategoria');
const detailMotivoEncerramento = document.getElementById('detailMotivoEncerramento');
const detailComentarioEncerramento = document.getElementById('detailComentarioEncerramento');
const detailSatisfacaoExibicao = document.getElementById('detailSatisfacaoExibicao');
const detailSatisfacaoVazio = document.getElementById('detailSatisfacaoVazio');
const detailSatisfacaoStatus = document.getElementById('detailSatisfacaoStatus');
const detailSatisfacaoNota = document.getElementById('detailSatisfacaoNota');
const detailSatisfacaoComentario = document.getElementById('detailSatisfacaoComentario');
const detailSatisfacaoCriadoEm = document.getElementById('detailSatisfacaoCriadoEm');
const detailSatisfacaoEnvioRow = document.getElementById('detailSatisfacaoEnvioRow');
const detailSatisfacaoEnvio = document.getElementById('detailSatisfacaoEnvio');
const detailSatisfacaoLinkRow = document.getElementById('detailSatisfacaoLinkRow');
const detailSatisfacaoLink = document.getElementById('detailSatisfacaoLink');
const ticketSatisfacaoForm = document.getElementById('ticketSatisfacaoForm');
const satisfacaoNota = document.getElementById('satisfacaoNota');
const satisfacaoComentario = document.getElementById('satisfacaoComentario');
const alertBoxSatisfacao = document.getElementById('alertBoxSatisfacao');
const ticketInteracoesTimeline = document.getElementById('ticketInteracoesTimeline');
const ticketInteracaoForm = document.getElementById('ticketInteracaoForm');
const interacaoTipo = document.getElementById('interacaoTipo');
const interacaoVisibilidade = document.getElementById('interacaoVisibilidade');
const interacaoMensagem = document.getElementById('interacaoMensagem');
const alertBoxInteracoes = document.getElementById('alertBoxInteracoes');
const atualizarHistoricoBtn = document.getElementById('atualizarHistoricoBtn');
const salvarInteracaoBtn = document.getElementById('salvarInteracaoBtn');
const ticketFinalizadoMensagem = document.getElementById('ticketFinalizadoMensagem');
const detailEncerrarTicketBtn = document.getElementById('detailEncerrarTicketBtn');
const detailReabrirTicketBtn = document.getElementById('detailReabrirTicketBtn');
const detailGerarPdfBtn = document.getElementById('detailGerarPdfBtn');
const detailContatoSolicitante = document.getElementById('detailContatoSolicitante');
const detailContatoSolicitanteTelefone = document.getElementById('detailContatoSolicitanteTelefone');
const detailContatoSolicitanteEmail = document.getElementById('detailContatoSolicitanteEmail');
const detailEscalonado = document.getElementById('detailEscalonado');
const detailEscalonadoEm = document.getElementById('detailEscalonadoEm');
const detailEscalonamentoObservacao = document.getElementById('detailEscalonamentoObservacao');
const detailEscalonarTicketBtn = document.getElementById('detailEscalonarTicketBtn');
const detailRemoverEscalonamentoBtn = document.getElementById('detailRemoverEscalonamentoBtn');
const modalEscalonamento = document.getElementById('modalEscalonamento');
const fecharModalEscalonamento = document.getElementById('fecharModalEscalonamento');
const cancelarEscalonamentoBtn = document.getElementById('cancelarEscalonamentoBtn');
const confirmarEscalonamentoBtn = document.getElementById('confirmarEscalonamentoBtn');
const escalonamentoObservacao = document.getElementById('escalonamentoObservacao');
const escalonamentoTicketNumero = document.getElementById('escalonamentoTicketNumero');
const alertBoxEscalonamento = document.getElementById('alertBoxEscalonamento');
const closeTicketModal = document.getElementById('modalEncerramento');
const closeTicketForm = document.getElementById('encerrarTicketForm');
const closeTicketNumber = document.getElementById('encerrarTicketNumero');
const closeTicketModalButton = document.getElementById('fecharModalEncerramento');
const closeTicketCancelButton = document.getElementById('cancelarEncerramento');
const grupoCategoriaSelect = document.getElementById('grupoCategoriaSelect');
const subgrupoCategoriaSelect = document.getElementById('subgrupoCategoriaSelect');
const motivoEncerramentoSelect = document.getElementById('motivoEncerramentoSelect');
const motivoEncerramentoEmptyHint = document.getElementById('motivoEncerramentoEmptyHint');
const comentarioEncerramento = document.getElementById('comentarioEncerramento');
const alertBoxEncerramento = document.getElementById('alertBoxEncerramento');
const encerrarPesquisaNao = document.getElementById('encerrarPesquisaNao');
const encerrarPesquisaSim = document.getElementById('encerrarPesquisaSim');
const encerramentoChoiceSimLabel = document.querySelector('label.encerramento-choice--sim');
const encerramentoSemContatoAviso = document.getElementById('encerramentoSemContatoAviso');

let currentDetailTicketNumber = null;
let encerramentoContatoId = null;
let currentDetailTicketStatus = null;
let listenersBound = false;

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => String(v ?? '-');
let resolveTicketNumberFn = () => '';
let formatDateTimeFn = () => '-';
let formatPriorityFn = () => '-';
let formatSlaPrimeiroAtendimentoLabelFn = () => '-';
let formatSlaBadgeHtmlFn = () => '-';
let formatSlaResolucaoBadgeFn = () => '-';
let formatSlaPausadoSimNaoFn = () => '-';
let formatSlaMinutosPausadosFn = () => '-';
let formatEscalonamentoBadgeHtmlFn = () => '';
let getLoggedAnalystFn = () => null;
let alertBoxTicketsEl = null;
let pagesRef = {};
let loadDashboardFn = () => {};
let loadTicketTableFn = () => {};
let loadAnalistasKanbanFn = async () => {};
let loadDashboardSlaFn = () => {};
let refreshNotificacoesUiFn = async () => {};

function showAlert(message, container, type) {
    showAlertFn(message, container, type);
}

function clearAlert(container) {
    clearAlertFn(container);
}

function displayValue(value) {
    return displayValueFn(value);
}

function resolveTicketNumber(value, fallbackSource) {
    return resolveTicketNumberFn(value, fallbackSource);
}

function formatDateTime(value) {
    return formatDateTimeFn(value);
}

function formatPriority(value) {
    return formatPriorityFn(value);
}

function formatSatisfacaoNota(nota) {
    const n = Number(nota);
    if (!Number.isFinite(n) || n < 1 || n > 5) {
        return '-';
    }
    return `${n} / 5`;
}

async function fetchTicketSatisfacao(numeroTicket) {
    return satisfacaoService.getSatisfacaoByTicket(numeroTicket);
}

function renderTicketSatisfacaoBloco(satisfacao, ticketStatus) {
    clearAlert(alertBoxSatisfacao);
    const finalizado = isTicketFinalizado(ticketStatus);
    const podeRegistrar = canRegisterSatisfacao(ticketStatus, Boolean(satisfacao));
    if (satisfacao) {
        detailSatisfacaoExibicao?.classList.remove('hidden');
        detailSatisfacaoVazio?.classList.add('hidden');
        ticketSatisfacaoForm?.classList.add('hidden');
        if (detailSatisfacaoStatus) {
            detailSatisfacaoStatus.textContent = formatSatisfacaoStatusLabel(satisfacao.status);
        }
        if (detailSatisfacaoNota) {
            detailSatisfacaoNota.textContent = formatSatisfacaoNotaExibicao(
                satisfacao.nota,
                satisfacao.status
            );
        }
        if (detailSatisfacaoComentario) {
            detailSatisfacaoComentario.textContent = displayValue(satisfacao.comentario) === '-'
                ? '—'
                : displayValue(satisfacao.comentario);
        }
        if (detailSatisfacaoCriadoEm) {
            detailSatisfacaoCriadoEm.textContent = formatDateTime(satisfacao.criadoEm);
        }
        const envio = satisfacao.envioStatus;
        if (detailSatisfacaoEnvioRow && detailSatisfacaoEnvio) {
            const showEnvio = envio != null && String(envio).trim() !== '';
            detailSatisfacaoEnvioRow.classList.toggle('hidden', !showEnvio);
            detailSatisfacaoEnvio.textContent = showEnvio ? String(envio) : '';
        }
        const linkPub = satisfacao.linkAvaliacaoPublico;
        if (detailSatisfacaoLinkRow && detailSatisfacaoLink) {
            const showLink = linkPub != null && String(linkPub).trim() !== '';
            detailSatisfacaoLinkRow.classList.toggle('hidden', !showLink);
            if (showLink) {
                detailSatisfacaoLink.href = linkPub;
                detailSatisfacaoLink.textContent = linkPub;
            }
        }
        return;
    }
    detailSatisfacaoEnvioRow?.classList.add('hidden');
    detailSatisfacaoLinkRow?.classList.add('hidden');
    detailSatisfacaoExibicao?.classList.add('hidden');
    if (podeRegistrar) {
        detailSatisfacaoVazio?.classList.remove('hidden');
        ticketSatisfacaoForm?.classList.remove('hidden');
    } else {
        detailSatisfacaoVazio?.classList.remove('hidden');
        detailSatisfacaoVazio.textContent = 'Sem avaliação registrada. Disponível após encerramento do ticket.';
        ticketSatisfacaoForm?.classList.add('hidden');
    }
    if (finalizado && detailSatisfacaoVazio && !satisfacao) {
        detailSatisfacaoVazio.textContent = 'Sem avaliação registrada.';
    }
    if (satisfacaoNota) {
        satisfacaoNota.value = '';
    }
    if (satisfacaoComentario) {
        satisfacaoComentario.value = '';
    }
}

async function loadTicketSatisfacao(numeroTicket, ticketStatus) {
    try {
        const satisfacao = await fetchTicketSatisfacao(numeroTicket);
        renderTicketSatisfacaoBloco(satisfacao, ticketStatus);
    } catch (error) {
        renderTicketSatisfacaoBloco(null, ticketStatus);
        showAlert(error.message, alertBoxSatisfacao);
    }
}

async function submitTicketSatisfacao(event) {
    event.preventDefault();
    if (!currentDetailTicketNumber) {
        return;
    }
    const nota = Number(satisfacaoNota?.value);
    if (!Number.isFinite(nota) || nota < 1 || nota > 5) {
        showAlert('Selecione uma nota de 1 a 5.', alertBoxSatisfacao);
        return;
    }
    const payload = { nota };
    const comentario = satisfacaoComentario?.value?.trim();
    if (comentario) {
        payload.comentario = comentario;
    }
    try {
        const data = await ticketService.registrarSatisfacao(currentDetailTicketNumber, payload);
        showAlert('Satisfação registrada com sucesso.', alertBoxSatisfacao, 'success');
        renderTicketSatisfacaoBloco(data, currentDetailTicketStatus);
    } catch (error) {
        showAlert(error.message, alertBoxSatisfacao);
    }
}

function updateDetailActions(status) {
    const finalizado = isTicketFinalizado(status);
    detailEncerrarTicketBtn?.classList.toggle('hidden', finalizado);
    detailReabrirTicketBtn?.classList.toggle('hidden', !finalizado);
}

function updateDetailEscalonamentoActions(ticket) {
    const escalonado = Boolean(ticket?.escalonado);
    detailEscalonarTicketBtn?.classList.toggle('hidden', escalonado);
    detailRemoverEscalonamentoBtn?.classList.toggle('hidden', !escalonado);
}

function setInteracaoFormEnabled(enabled) {
    ticketInteracaoForm?.classList.toggle('hidden', !enabled);
    ticketFinalizadoMensagem?.classList.toggle('hidden', enabled);
    [interacaoTipo, interacaoVisibilidade, interacaoMensagem]
        .forEach(field => {
            if (field) field.disabled = !enabled;
        });
    if (salvarInteracaoBtn) salvarInteracaoBtn.disabled = !enabled;
}

function fecharModalEscalonamentoUi() {
    modalEscalonamento?.classList.remove('ativo');
    clearAlert(alertBoxEscalonamento);
    if (escalonamentoObservacao) {
        escalonamentoObservacao.value = '';
    }
}

function abrirModalEscalonamento() {
    if (!currentDetailTicketNumber || !modalEscalonamento) return;
    if (escalonamentoTicketNumero) {
        escalonamentoTicketNumero.textContent = `Ticket ${currentDetailTicketNumber}`;
    }
    if (escalonamentoObservacao) {
        escalonamentoObservacao.value = '';
    }
    clearAlert(alertBoxEscalonamento);
    modalEscalonamento.classList.add('ativo');
    escalonamentoObservacao?.focus();
}

async function fetchGruposCategoria() {
    return categoriaService.listGrupos();
}

async function fetchSubgruposCategoria(grupoId) {
    return categoriaService.listSubgruposByGrupo(grupoId);
}

async function fetchTicketInteracoes(ticketNumber) {
    return ticketService.listInteracoes(ticketNumber);
}

function refreshViewsAfterTicketChange() {
    loadDashboardFn();
    if (pagesRef.atendentes?.classList.contains('active')) {
        loadAnalistasKanbanFn();
    }
    if (pagesRef.tickets?.classList.contains('active')) {
        loadTicketTableFn();
    }
}

export async function openDetails(ticketNumber, fallbackSource) {
    const numero = resolveTicketNumber(ticketNumber, fallbackSource);
    if (!numero) {
        showAlert('Número do ticket não informado.', alertBoxTicketsEl);
        return;
    }
    try {
        const ticket = await ticketService.getTicketByNumero(numero);
        currentDetailTicketNumber = ticket.numeroTicket;
        currentDetailTicketStatus = ticket.status;
        detailNumero.textContent = displayValue(ticket.numeroTicket);
        detailStatus.textContent = displayValue(ticket.status);
        if (detailPrioridade) {
            const prioridadeExibicao = ticket.prioridade && displayValue(ticket.prioridade) !== '-'
                ? ticket.prioridade
                : null;
            detailPrioridade.textContent = formatPriority(prioridadeExibicao);
        }
        detailCanal.textContent = displayValue(ticket.canal);
        detailConexao.textContent = displayValue(ticket.conexao);
        detailCarteira.textContent = displayValue(ticket.carteira);
        detailAnalista.textContent = displayValue(ticket.analistaResponsavelNome);
        detailMensagem.textContent = displayValue(ticket.mensagemInicial);
        detailAbertura.textContent = formatDateTime(ticket.dataAbertura);
        detailPrimeiro.textContent = formatDateTime(ticket.dataPrimeiroAtendimento);
        if (detailSlaVencimento) {
            detailSlaVencimento.textContent = formatDateTime(ticket.slaPrimeiroAtendimentoVencimento);
        }
        if (detailSlaStatus) {
            const slaLabel = formatSlaPrimeiroAtendimentoLabelFn(ticket.slaPrimeiroAtendimentoStatus);
            detailSlaStatus.innerHTML = slaLabel === '-'
                ? '-'
                : formatSlaBadgeHtmlFn(ticket.slaPrimeiroAtendimentoStatus);
        }
        if (detailSlaResolucaoVencimento) {
            detailSlaResolucaoVencimento.textContent = formatDateTime(ticket.slaResolucaoVencimento);
        }
        if (detailSlaResolucaoStatus) {
            detailSlaResolucaoStatus.innerHTML = formatSlaResolucaoBadgeFn(ticket);
        }
        if (detailSlaPausado) {
            detailSlaPausado.textContent = formatSlaPausadoSimNaoFn(ticket.slaPausado);
        }
        if (detailSlaPausaInicio) {
            detailSlaPausaInicio.textContent = ticket.slaPausado
                ? formatDateTime(ticket.slaPausaInicio)
                : '-';
        }
        if (detailSlaMinutosPausados) {
            detailSlaMinutosPausados.textContent = formatSlaMinutosPausadosFn(ticket.slaResolucaoMinutosPausados);
        }
        if (detailEscalonado) {
            detailEscalonado.innerHTML = ticket.escalonado
                ? formatEscalonamentoBadgeHtmlFn(ticket)
                : 'Não';
        }
        if (detailEscalonadoEm) {
            detailEscalonadoEm.textContent = ticket.escalonado
                ? formatDateTime(ticket.escalonadoEm)
                : '-';
        }
        if (detailEscalonamentoObservacao) {
            detailEscalonamentoObservacao.textContent = displayValue(ticket.escalonamentoObservacao);
        }
        updateDetailEscalonamentoActions(ticket);
        detailEncerramento.textContent = formatDateTime(ticket.dataEncerramento);
        detailCliente.textContent = displayValue(ticket.cliente);
        detailTelefone.textContent = displayValue(ticket.telefone);
        detailTelefoneContato.textContent = displayValue(ticket.telefoneContato);
        detailEmail.textContent = displayValue(ticket.email);
        detailEmpresa.textContent = displayValue(ticket.empresa);
        detailCnpj.textContent = displayValue(ticket.cnpj);
        detailCidade.textContent = displayValue(ticket.cidade);
        detailUf.textContent = displayValue(ticket.uf);
        detailContatoSolicitante.textContent = displayValue(ticket.contatoSolicitanteNome);
        detailContatoSolicitanteTelefone.textContent = displayValue(ticket.contatoSolicitanteTelefone);
        detailContatoSolicitanteEmail.textContent = displayValue(ticket.contatoSolicitanteEmail);
        detailGrupoCategoria.textContent = displayValue(ticket.grupoCategoriaNome);
        detailSubgrupoCategoria.textContent = displayValue(ticket.subgrupoCategoriaNome);
        if (detailMotivoEncerramento) {
            detailMotivoEncerramento.textContent = displayValue(ticket.motivoNome);
        }
        detailComentarioEncerramento.textContent = displayValue(ticket.comentarioEncerramento);
        detailEncerramentoResumo.textContent = formatDateTime(ticket.dataEncerramento);
        setInteracaoFormEnabled(!isTicketFinalizado(ticket.status));
        updateDetailActions(ticket.status);
        clearAlert(alertBoxInteracoes);
        detailModal.classList.add('ativo');
        loadTicketInteracoes(ticket.numeroTicket);
        loadTicketSatisfacao(ticket.numeroTicket, ticket.status);
    } catch (error) {
        showAlert(error.message, alertBoxTicketsEl);
    }
}

async function loadTicketInteracoes(ticketNumber) {
    try {
        const interacoes = await fetchTicketInteracoes(ticketNumber);
        renderTicketInteracoes(interacoes);
    } catch (error) {
        showAlert(error.message, alertBoxInteracoes);
    }
}

function renderTicketInteracoes(interacoes) {
    ticketInteracoesTimeline.innerHTML = '';
    if (!interacoes.length) {
        ticketInteracoesTimeline.textContent = 'Nenhuma interação registrada.';
        return;
    }

    interacoes.forEach(interacao => {
        const item = document.createElement('div');
        item.className = 'content-section';

        const header = document.createElement('strong');
        header.textContent = `${formatDateTime(interacao.criadoEm)} - ${displayValue(interacao.tipoInteracao)} - ${displayValue(interacao.visibilidade)}`;

        const mensagem = document.createElement('p');
        mensagem.textContent = displayValue(interacao.mensagem);

        item.appendChild(header);
        item.appendChild(mensagem);
        ticketInteracoesTimeline.appendChild(item);
    });
}

async function submitTicketInteracao(event) {
    event.preventDefault();
    if (!currentDetailTicketNumber) return;
    if (isTicketFinalizado(currentDetailTicketStatus)) {
        showAlert('Ticket finalizado. Novas interações não podem ser adicionadas.', alertBoxInteracoes);
        return;
    }

    if (!interacaoTipo.value) {
        showAlert('Tipo de interação é obrigatório.', alertBoxInteracoes);
        return;
    }
    if (!interacaoVisibilidade.value) {
        showAlert('Visibilidade é obrigatória.', alertBoxInteracoes);
        return;
    }
    if (!interacaoMensagem.value.trim()) {
        showAlert('Mensagem é obrigatória.', alertBoxInteracoes);
        return;
    }

    const payload = {
        tipoInteracao: interacaoTipo.value,
        visibilidade: interacaoVisibilidade.value,
        mensagem: interacaoMensagem.value.trim()
    };

    try {
        await ticketService.createInteracao(currentDetailTicketNumber, payload);
        interacaoMensagem.value = '';
        clearAlert(alertBoxInteracoes);
        await loadTicketInteracoes(currentDetailTicketNumber);
    } catch (error) {
        showAlert(error.message, alertBoxInteracoes);
    }
}

async function refreshCurrentTicketInteracoes() {
    if (!currentDetailTicketNumber) return;
    clearAlert(alertBoxInteracoes);
    await loadTicketInteracoes(currentDetailTicketNumber);
}

async function reabrirTicketAtual() {
    if (!currentDetailTicketNumber) return;
    try {
        await ticketService.reabrirTicket(currentDetailTicketNumber);
        await openDetails(currentDetailTicketNumber);
        refreshViewsAfterTicketChange();
    } catch (error) {
        showAlert(error.message, alertBoxInteracoes);
    }
}

async function confirmarEscalonamentoTicket() {
    if (!currentDetailTicketNumber) return;
    const observacao = escalonamentoObservacao?.value ?? '';
    if (confirmarEscalonamentoBtn) {
        confirmarEscalonamentoBtn.disabled = true;
    }
    try {
        const payload = {
            observacao: observacao.trim(),
            analistaId: getLoggedAnalystFn()?.id ?? null
        };
        await ticketService.escalonarTicket(currentDetailTicketNumber, payload);
        fecharModalEscalonamentoUi();
        await openDetails(currentDetailTicketNumber);
        if (pagesRef.tickets?.classList.contains('active')) {
            loadTicketTableFn();
        }
        loadDashboardSlaFn();
        await refreshNotificacoesUiFn();
        showAlert('Ticket escalonado com sucesso.', alertBoxInteracoes, 'success');
    } catch (error) {
        showAlert(error.message, alertBoxEscalonamento);
    } finally {
        if (confirmarEscalonamentoBtn) {
            confirmarEscalonamentoBtn.disabled = false;
        }
    }
}

async function removerEscalonamentoTicketAtual() {
    if (!currentDetailTicketNumber) return;
    if (!window.confirm('Remover escalonamento deste ticket?')) {
        return;
    }
    try {
        await ticketService.removerEscalonamentoTicket(currentDetailTicketNumber);
        await openDetails(currentDetailTicketNumber);
        if (pagesRef.tickets?.classList.contains('active')) {
            loadTicketTableFn();
        }
        loadDashboardSlaFn();
        await refreshNotificacoesUiFn();
        showAlert('Escalonamento removido.', alertBoxInteracoes, 'success');
    } catch (error) {
        showAlert(error.message, alertBoxInteracoes);
    }
}

function abrirEncerramentoDoDetalhe() {
    if (!currentDetailTicketNumber) return;
    openCloseTicketModal(currentDetailTicketNumber);
}

export function closeModalDetail() {
    detailModal.classList.remove('ativo');
    currentDetailTicketNumber = null;
    currentDetailTicketStatus = null;
    ticketInteracaoForm?.reset();
    setInteracaoFormEnabled(true);
    updateDetailActions(null);
    clearAlert(alertBoxInteracoes);
}

export async function openEncerramentoTicketModal(ticketNumber) {
    if (!ticketNumber) {
        return;
    }
    await openCloseTicketModal(ticketNumber);
}

function atualizarAvisoPesquisaEncerramento() {
    const semContato = deveDesabilitarPesquisaSimEncerramento(encerramentoContatoId);
    encerramentoSemContatoAviso?.classList.toggle('hidden', !semContato);
    if (encerrarPesquisaSim) {
        encerrarPesquisaSim.disabled = semContato;
    }
    encerramentoChoiceSimLabel?.classList.toggle('is-disabled', semContato);
    if (semContato && encerrarPesquisaNao) {
        encerrarPesquisaNao.checked = true;
    }
}

async function openCloseTicketModal(ticketNumber) {
    closeTicketForm.reset();
    clearAlert(alertBoxEncerramento);
    closeTicketNumber.value = ticketNumber;
    encerramentoContatoId = null;
    grupoCategoriaSelect.innerHTML = '<option value="">Selecione</option>';
    subgrupoCategoriaSelect.innerHTML = '<option value="">Selecione</option>';
    if (motivoEncerramentoSelect) {
        motivoEncerramentoSelect.innerHTML = '<option value="">Selecione a subcategoria</option>';
        motivoEncerramentoSelect.disabled = true;
    }
    motivoEncerramentoEmptyHint?.classList.add('hidden');

    try {
        const [grupos, ticket] = await Promise.all([
            fetchGruposCategoria(),
            ticketService.getTicketByNumero(ticketNumber).catch(() => null)
        ]);
        encerramentoContatoId = ticket?.contatoId ?? null;
        grupos.forEach(grupo => {
            const option = document.createElement('option');
            option.value = grupo.id;
            option.textContent = grupo.nome;
            grupoCategoriaSelect.appendChild(option);
        });
        atualizarAvisoPesquisaEncerramento();
        closeTicketModal.classList.add('ativo');
    } catch (error) {
        showAlert(error.message, alertBoxTicketsEl);
    }
}

export function closeCloseTicketModal() {
    closeTicketModal.classList.remove('ativo');
}

async function onGrupoCategoriaChange() {
    const grupoId = grupoCategoriaSelect.value;
    subgrupoCategoriaSelect.innerHTML = '<option value="">Selecione</option>';
    if (motivoEncerramentoSelect) {
        motivoEncerramentoSelect.innerHTML = '<option value="">Selecione a subcategoria</option>';
        motivoEncerramentoSelect.disabled = true;
    }
    motivoEncerramentoEmptyHint?.classList.add('hidden');
    if (!grupoId) return;

    try {
        const subgrupos = await fetchSubgruposCategoria(grupoId);
        subgrupos.forEach(subgrupo => {
            const option = document.createElement('option');
            option.value = subgrupo.id;
            option.textContent = subgrupo.nome;
            subgrupoCategoriaSelect.appendChild(option);
        });
    } catch (error) {
        showAlert(error.message, alertBoxEncerramento);
    }
}

async function onSubgrupoCategoriaChange() {
    const subgrupoId = subgrupoCategoriaSelect.value;
    if (!motivoEncerramentoSelect) {
        return;
    }
    motivoEncerramentoSelect.innerHTML = '<option value="">Selecione</option>';
    motivoEncerramentoEmptyHint?.classList.add('hidden');
    if (!subgrupoId) {
        motivoEncerramentoSelect.disabled = true;
        return;
    }
    try {
        const motivos = await categoriaService.listMotivos(subgrupoId);
        const lista = Array.isArray(motivos) ? motivos : [];
        if (!lista.length) {
            motivoEncerramentoSelect.disabled = true;
            motivoEncerramentoEmptyHint?.classList.remove('hidden');
            return;
        }
        lista.forEach(m => {
            const option = document.createElement('option');
            option.value = m.id;
            option.textContent = m.nome;
            motivoEncerramentoSelect.appendChild(option);
        });
        motivoEncerramentoSelect.disabled = false;
    } catch (error) {
        showAlert(error.message, alertBoxEncerramento);
    }
}

async function submitCloseTicket(event) {
    event.preventDefault();
    const pesquisaRadio = closeTicketForm?.querySelector('input[name="enviarPesquisaSatisfacao"]:checked');
    const validacao = validateEncerramentoPayload({
        grupoId: grupoCategoriaSelect.value,
        subgrupoId: subgrupoCategoriaSelect.value,
        motivoId: motivoEncerramentoSelect?.value,
        comentarioEncerramento: comentarioEncerramento.value,
        enviarPesquisaSatisfacao: pesquisaRadio?.value
    });
    if (!validacao.ok) {
        showAlert(validacao.message, alertBoxEncerramento);
        return;
    }

    try {
        const atualizado = await ticketService.encerrarTicket(closeTicketNumber.value, {
            grupoId: validacao.grupoId,
            subgrupoId: validacao.subgrupoId,
            motivoId: validacao.motivoId,
            comentarioEncerramento: validacao.comentarioEncerramento,
            enviarPesquisaSatisfacao: validacao.enviarPesquisaSatisfacao
        });
        const ticketEncerrado = closeTicketNumber.value;
        closeCloseTicketModal();
        refreshViewsAfterTicketChange();
        if (atualizado?.avaliacaoLinkPublico) {
            showAlert(
                `Ticket encerrado. Link da pesquisa: ${atualizado.avaliacaoLinkPublico}`,
                alertBoxTicketsEl || alertBoxEncerramento,
                'success'
            );
        }
        if (currentDetailTicketNumber === ticketEncerrado) {
            await openDetails(ticketEncerrado);
        }
    } catch (error) {
        showAlert(error.message, alertBoxEncerramento);
    }
}

async function downloadTicketPdf() {
    if (!currentDetailTicketNumber) {
        return;
    }
    try {
        const response = await ticketService.fetchTicketPdf(currentDetailTicketNumber);
        if (!response.ok) {
            const data = await response.json().catch(() => null);
            throw new Error(mensagemErroSessaoApi(response, data, 'Não foi possível gerar o PDF do ticket.'));
        }
        const blob = await response.blob();
        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = `ticket-${currentDetailTicketNumber}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(objectUrl);
    } catch (error) {
        showAlert(error.message, alertBoxInteracoes || alertBoxTicketsEl);
    }
}

function bindListeners() {
    if (listenersBound) return;
    listenersBound = true;

    ticketSatisfacaoForm?.addEventListener('submit', submitTicketSatisfacao);
    ticketInteracaoForm?.addEventListener('submit', submitTicketInteracao);
    salvarInteracaoBtn?.addEventListener('click', submitTicketInteracao);
    atualizarHistoricoBtn?.addEventListener('click', refreshCurrentTicketInteracoes);
    detailGerarPdfBtn?.addEventListener('click', downloadTicketPdf);
    detailEncerrarTicketBtn?.addEventListener('click', abrirEncerramentoDoDetalhe);
    detailEscalonarTicketBtn?.addEventListener('click', event => {
        event.preventDefault();
        event.stopPropagation();
        abrirModalEscalonamento();
    });
    fecharModalEscalonamento?.addEventListener('click', fecharModalEscalonamentoUi);
    cancelarEscalonamentoBtn?.addEventListener('click', fecharModalEscalonamentoUi);
    confirmarEscalonamentoBtn?.addEventListener('click', confirmarEscalonamentoTicket);
    modalEscalonamento?.addEventListener('click', event => {
        if (event.target === modalEscalonamento) {
            fecharModalEscalonamentoUi();
        }
    });
    detailRemoverEscalonamentoBtn?.addEventListener('click', removerEscalonamentoTicketAtual);
    detailReabrirTicketBtn?.addEventListener('click', reabrirTicketAtual);
    closeModal?.addEventListener('click', closeModalDetail);
    detailModal?.addEventListener('click', event => {
        if (event.target === detailModal) closeModalDetail();
    });
    closeTicketModalButton?.addEventListener('click', closeCloseTicketModal);
    closeTicketCancelButton?.addEventListener('click', closeCloseTicketModal);
    closeTicketModal?.addEventListener('click', event => {
        if (event.target === closeTicketModal) closeCloseTicketModal();
    });
    grupoCategoriaSelect?.addEventListener('change', onGrupoCategoriaChange);
    subgrupoCategoriaSelect?.addEventListener('change', onSubgrupoCategoriaChange);
    closeTicketForm?.addEventListener('submit', submitCloseTicket);
}

export function initTicketDetailsModal(deps) {
    showAlertFn = deps.showAlert || showAlertFn;
    clearAlertFn = deps.clearAlert || clearAlertFn;
    displayValueFn = deps.displayValue || displayValueFn;
    resolveTicketNumberFn = deps.resolveTicketNumber || resolveTicketNumberFn;
    formatDateTimeFn = deps.formatDateTime || formatDateTimeFn;
    formatPriorityFn = deps.formatPriority || formatPriorityFn;
    formatSlaPrimeiroAtendimentoLabelFn = deps.formatSlaPrimeiroAtendimentoLabel || formatSlaPrimeiroAtendimentoLabelFn;
    formatSlaBadgeHtmlFn = deps.formatSlaBadgeHtml || formatSlaBadgeHtmlFn;
    formatSlaResolucaoBadgeFn = deps.formatSlaResolucaoBadge || formatSlaResolucaoBadgeFn;
    formatSlaPausadoSimNaoFn = deps.formatSlaPausadoSimNao || formatSlaPausadoSimNaoFn;
    formatSlaMinutosPausadosFn = deps.formatSlaMinutosPausados || formatSlaMinutosPausadosFn;
    formatEscalonamentoBadgeHtmlFn = deps.formatEscalonamentoBadgeHtml || formatEscalonamentoBadgeHtmlFn;
    getLoggedAnalystFn = deps.getLoggedAnalyst || getLoggedAnalystFn;
    alertBoxTicketsEl = deps.alertBoxTickets || alertBoxTicketsEl;
    pagesRef = deps.pages || pagesRef;
    loadDashboardFn = deps.loadDashboard || loadDashboardFn;
    loadTicketTableFn = deps.loadTicketTable || loadTicketTableFn;
    loadAnalistasKanbanFn = deps.loadAnalistasKanban || loadAnalistasKanbanFn;
    loadDashboardSlaFn = deps.loadDashboardSla || loadDashboardSlaFn;
    refreshNotificacoesUiFn = deps.refreshNotificacoesUi || refreshNotificacoesUiFn;
    bindListeners();
}

export { fecharModalEscalonamentoUi };
