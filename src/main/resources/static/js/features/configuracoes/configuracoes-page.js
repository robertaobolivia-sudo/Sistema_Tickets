import { MSG_ERRO } from '@shared/ui/messages.js';
import {
    canManageConfiguracoes,
    canManageEtiquetas,
    applyConfiguracoesPermissions
} from '@shared/permissions/permissions.js';
import * as configuracaoService from '@features/configuracoes/configuracao-service.js';
import {
    initEtiquetasConfigSection,
    loadEtiquetasConfigSection,
    applyEtiquetasConfigPermissions
} from '@features/configuracoes/etiquetas-config-section.js';
import {
    initConexoesRevendaConfigSection,
    loadConexoesRevendaConfigSection,
    applyConexoesRevendaConfigPermissions
} from '@features/configuracoes/conexoes-revenda-config-section.js';
import {
    initMotivosConfigSection,
    loadMotivosConfigSection,
    applyMotivosConfigPermissions
} from '@features/configuracoes/motivos-config-section.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatPriorityFn = (v) => v || '-';
let formatPriorityBadgeHtmlFn = (v) => displayValueFn(v);

const alertBoxConfiguracoes = document.getElementById('alertBoxConfiguracoes');
const horarioUtilNome = document.getElementById('horarioUtilNome');
const horarioUtilHoraInicio = document.getElementById('horarioUtilHoraInicio');
const horarioUtilHoraFim = document.getElementById('horarioUtilHoraFim');
const horarioUtilSegunda = document.getElementById('horarioUtilSegunda');
const horarioUtilTerca = document.getElementById('horarioUtilTerca');
const horarioUtilQuarta = document.getElementById('horarioUtilQuarta');
const horarioUtilQuinta = document.getElementById('horarioUtilQuinta');
const horarioUtilSexta = document.getElementById('horarioUtilSexta');
const horarioUtilSabado = document.getElementById('horarioUtilSabado');
const horarioUtilDomingo = document.getElementById('horarioUtilDomingo');
const horarioUtilSalvarBtn = document.getElementById('horarioUtilSalvarBtn');
const horarioUtilRecarregarBtn = document.getElementById('horarioUtilRecarregarBtn');
const feriadoEditId = document.getElementById('feriadoEditId');
const feriadoData = document.getElementById('feriadoData');
const feriadoDescricao = document.getElementById('feriadoDescricao');
const feriadoTipo = document.getElementById('feriadoTipo');
const feriadoEscopo = document.getElementById('feriadoEscopo');
const feriadoAtivo = document.getElementById('feriadoAtivo');
const feriadoSalvarBtn = document.getElementById('feriadoSalvarBtn');
const feriadoLimparBtn = document.getElementById('feriadoLimparBtn');
const feriadoSeedBtn = document.getElementById('feriadoSeedBtn');
const feriadosTableBody = document.getElementById('feriadosTableBody');
const feriadosEmptyMessage = document.getElementById('feriadosEmptyMessage');
const slaMetasTableBody = document.getElementById('slaMetasTableBody');
const slaMetasEmptyMessage = document.getElementById('slaMetasEmptyMessage');
const slaMetasSeedBtn = document.getElementById('slaMetasSeedBtn');

let listenersBound = false;

export function initConfiguracoesPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatPriority) formatPriorityFn = deps.formatPriority;
    if (deps.formatPriorityBadgeHtml) formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml;
    if (listenersBound) return;
    listenersBound = true;

    horarioUtilSalvarBtn?.addEventListener('click', salvarHorarioUtilPadrao);
    horarioUtilRecarregarBtn?.addEventListener('click', loadHorarioUtilPadrao);
    feriadoSalvarBtn?.addEventListener('click', salvarFeriado);
    feriadoLimparBtn?.addEventListener('click', limparFormularioFeriado);
    feriadoSeedBtn?.addEventListener('click', carregarFeriadosSaoPaulo2026);
    feriadosTableBody?.addEventListener('click', event => {
        const button = event.target.closest('button[data-feriado-action]');
        if (!button) return;
        const id = button.dataset.id;
        const action = button.dataset.feriadoAction;
        if (action === 'edit') {
            editarFeriado(id);
        } else if (action === 'ativar') {
            alterarStatusFeriado(id, true);
        } else if (action === 'inativar') {
            alterarStatusFeriado(id, false);
        }
    });
    slaMetasSeedBtn?.addEventListener('click', carregarSlaMetasDefault);
    slaMetasTableBody?.addEventListener('click', event => {
        const button = event.target.closest('button[data-sla-meta-action="save"]');
        if (!button) return;
        const prioridade = button.dataset.prioridade;
        if (prioridade) {
            salvarSlaMeta(prioridade);
        }
    });

    initEtiquetasConfigSection({
        showAlert: showAlertFn,
        displayValue: displayValueFn
    });
    initConexoesRevendaConfigSection({
        showAlert: (msg, type) => showAlertFn(msg, alertBoxConfiguracoes, type)
    });
    initMotivosConfigSection({
        showAlert: msg => showAlertFn(msg, alertBoxConfiguracoes)
    });
}

export function loadConfiguracoesPage() {
    applyConfiguracoesPermissions();
    applyEtiquetasConfigPermissions();
    applyConexoesRevendaConfigPermissions();
    applyMotivosConfigPermissions();
    if (!canManageEtiquetas()) {
        showAlertFn(MSG_ERRO.ACESSO_ADMIN, alertBoxConfiguracoes);
        return;
    }
    if (canManageConfiguracoes()) {
        loadHorarioUtilPadrao();
        loadFeriados();
        loadSlaMetas();
    }
    loadEtiquetasConfigSection();
    loadConexoesRevendaConfigSection();
    if (canManageConfiguracoes()) {
        loadMotivosConfigSection();
    }
}

function toTimeInputValue(hora) {
    if (!hora) return '';
    const parts = String(hora).trim().split(':');
    if (parts.length < 2) return '';
    const h = parts[0].padStart(2, '0');
    const m = parts[1].padStart(2, '0');
    return `${h}:${m}`;
}

function preencherFormularioHorarioUtil(data) {
    if (!data) return;
    if (horarioUtilNome) horarioUtilNome.value = data.nome || '';
    if (horarioUtilHoraInicio) horarioUtilHoraInicio.value = toTimeInputValue(data.horaInicio);
    if (horarioUtilHoraFim) horarioUtilHoraFim.value = toTimeInputValue(data.horaFim);
    if (horarioUtilSegunda) horarioUtilSegunda.checked = Boolean(data.segunda);
    if (horarioUtilTerca) horarioUtilTerca.checked = Boolean(data.terca);
    if (horarioUtilQuarta) horarioUtilQuarta.checked = Boolean(data.quarta);
    if (horarioUtilQuinta) horarioUtilQuinta.checked = Boolean(data.quinta);
    if (horarioUtilSexta) horarioUtilSexta.checked = Boolean(data.sexta);
    if (horarioUtilSabado) horarioUtilSabado.checked = Boolean(data.sabado);
    if (horarioUtilDomingo) horarioUtilDomingo.checked = Boolean(data.domingo);
}

function lerFormularioHorarioUtil() {
    return {
        nome: horarioUtilNome?.value?.trim() || '',
        horaInicio: horarioUtilHoraInicio?.value || '',
        horaFim: horarioUtilHoraFim?.value || '',
        segunda: Boolean(horarioUtilSegunda?.checked),
        terca: Boolean(horarioUtilTerca?.checked),
        quarta: Boolean(horarioUtilQuarta?.checked),
        quinta: Boolean(horarioUtilQuinta?.checked),
        sexta: Boolean(horarioUtilSexta?.checked),
        sabado: Boolean(horarioUtilSabado?.checked),
        domingo: Boolean(horarioUtilDomingo?.checked),
        ativo: true
    };
}

function validarFormularioHorarioUtil(payload) {
    if (!payload.nome) {
        return 'Informe o nome da configuração.';
    }
    if (!payload.horaInicio || !payload.horaFim) {
        return 'Informe a hora inicial e a hora final.';
    }
    if (payload.horaInicio >= payload.horaFim) {
        return 'A hora inicial deve ser menor que a hora final.';
    }
    const algumDia = payload.segunda || payload.terca || payload.quarta || payload.quinta
        || payload.sexta || payload.sabado || payload.domingo;
    if (!algumDia) {
        return 'Selecione pelo menos um dia da semana.';
    }
    return null;
}

async function loadHorarioUtilPadrao() {
    clearAlertFn(alertBoxConfiguracoes);
    try {
        const data = await configuracaoService.getHorarioUtilPadrao();
        preencherFormularioHorarioUtil(data);
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

async function salvarHorarioUtilPadrao() {
    clearAlertFn(alertBoxConfiguracoes);
    const payload = lerFormularioHorarioUtil();
    const erro = validarFormularioHorarioUtil(payload);
    if (erro) {
        showAlertFn(erro, alertBoxConfiguracoes);
        return;
    }
    try {
        const data = await configuracaoService.saveHorarioUtilPadrao(payload);
        preencherFormularioHorarioUtil(data);
        showAlertFn('Horário útil salvo com sucesso.', alertBoxConfiguracoes, 'success');
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

function formatFeriadoDataBr(dataIso) {
    if (!dataIso) return '-';
    const parts = String(dataIso).split('-');
    if (parts.length !== 3) return displayValueFn(dataIso);
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function formatFeriadoEscopoLabel(escopo) {
    const map = {
        NACIONAL: 'Nacional',
        ESTADUAL_SP: 'Estadual SP',
        MUNICIPAL_SAO_PAULO: 'Municipal São Paulo'
    };
    return map[escopo] || displayValueFn(escopo);
}

function limparFormularioFeriado() {
    if (feriadoEditId) feriadoEditId.value = '';
    if (feriadoData) feriadoData.value = '';
    if (feriadoDescricao) feriadoDescricao.value = '';
    if (feriadoTipo) feriadoTipo.value = 'FIXO';
    if (feriadoEscopo) feriadoEscopo.value = 'NACIONAL';
    if (feriadoAtivo) feriadoAtivo.checked = true;
}

function lerFormularioFeriado() {
    return {
        data: feriadoData?.value || '',
        descricao: feriadoDescricao?.value?.trim() || '',
        tipo: feriadoTipo?.value || 'FIXO',
        escopo: feriadoEscopo?.value || 'NACIONAL',
        ativo: Boolean(feriadoAtivo?.checked)
    };
}

function validarFormularioFeriado(payload) {
    if (!payload.data) {
        return 'Informe a data do feriado.';
    }
    return null;
}

function preencherFormularioFeriado(feriado) {
    if (!feriado) return;
    if (feriadoEditId) feriadoEditId.value = feriado.id != null ? String(feriado.id) : '';
    if (feriadoData) feriadoData.value = feriado.data || '';
    if (feriadoDescricao) feriadoDescricao.value = feriado.descricao || '';
    if (feriadoTipo) feriadoTipo.value = feriado.tipo || 'FIXO';
    if (feriadoEscopo) feriadoEscopo.value = feriado.escopo || 'NACIONAL';
    if (feriadoAtivo) feriadoAtivo.checked = feriado.ativo !== false;
}

function renderTabelaFeriados(feriados) {
    if (!feriadosTableBody) return;
    feriadosTableBody.innerHTML = '';
    const lista = Array.isArray(feriados) ? feriados : [];
    if (!lista.length) {
        feriadosEmptyMessage?.classList.remove('hidden');
        return;
    }
    feriadosEmptyMessage?.classList.add('hidden');
    lista.forEach(feriado => {
        const row = document.createElement('tr');
        const ativo = feriado.ativo !== false;
        const statusLabel = ativo ? 'Ativo' : 'Inativo';
        const statusClass = ativo ? 'status-ativo' : 'status-inativo';
        row.innerHTML = `
            <td>${formatFeriadoDataBr(feriado.data)}</td>
            <td>${displayValueFn(feriado.descricao)}</td>
            <td>${displayValueFn(feriado.tipo)}</td>
            <td>${formatFeriadoEscopoLabel(feriado.escopo)}</td>
            <td><span class="${statusClass}">${statusLabel}</span></td>
            <td class="table-actions">
                <button type="button" class="button button-secondary button-small" data-feriado-action="edit" data-id="${feriado.id}">Editar</button>
                <button type="button" class="button button-secondary button-small" data-feriado-action="${ativo ? 'inativar' : 'ativar'}" data-id="${feriado.id}">${ativo ? 'Inativar' : 'Ativar'}</button>
            </td>
        `;
        feriadosTableBody.appendChild(row);
    });
}

async function loadFeriados() {
    try {
        const feriados = await configuracaoService.listFeriados();
        renderTabelaFeriados(feriados);
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

async function salvarFeriado() {
    clearAlertFn(alertBoxConfiguracoes);
    const payload = lerFormularioFeriado();
    const erro = validarFormularioFeriado(payload);
    if (erro) {
        showAlertFn(erro, alertBoxConfiguracoes);
        return;
    }
    const editId = feriadoEditId?.value?.trim();
    try {
        if (editId) {
            await configuracaoService.updateFeriado(editId, payload);
            showAlertFn('Feriado atualizado com sucesso.', alertBoxConfiguracoes, 'success');
        } else {
            await configuracaoService.createFeriado(payload);
            showAlertFn('Feriado cadastrado com sucesso.', alertBoxConfiguracoes, 'success');
        }
        limparFormularioFeriado();
        await loadFeriados();
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

function editarFeriado(id) {
    configuracaoService.listFeriados()
        .then(lista => {
            const feriado = lista.find(item => String(item.id) === String(id));
            if (!feriado) {
                showAlertFn('Feriado não encontrado.', alertBoxConfiguracoes);
                return;
            }
            preencherFormularioFeriado(feriado);
            feriadoData?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        })
        .catch(error => showAlertFn(error.message, alertBoxConfiguracoes));
}

async function alterarStatusFeriado(id, ativar) {
    clearAlertFn(alertBoxConfiguracoes);
    try {
        await configuracaoService.patchFeriadoStatus(id, ativar);
        showAlertFn(ativar ? 'Feriado ativado.' : 'Feriado inativado.', alertBoxConfiguracoes, 'success');
        await loadFeriados();
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

async function carregarFeriadosSaoPaulo2026() {
    clearAlertFn(alertBoxConfiguracoes);
    try {
        const resultado = await configuracaoService.seedFeriadosSaoPaulo2026();
        showAlertFn(resultado.mensagem || 'Carga de feriados concluída.', alertBoxConfiguracoes, 'success');
        await loadFeriados();
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

function validarSlaMetaPayload(payload) {
    const campos = [
        { valor: payload.prazoPrimeiroAtendimentoMinutos, nome: 'primeiro atendimento' },
        { valor: payload.prazoResolucaoMinutos, nome: 'resolução' }
    ];
    for (const campo of campos) {
        if (campo.valor === '' || campo.valor === null || campo.valor === undefined) {
            return `Informe o prazo de ${campo.nome} em minutos úteis.`;
        }
        const numero = Number(campo.valor);
        if (!Number.isInteger(numero) || numero <= 0) {
            return `O prazo de ${campo.nome} deve ser um número inteiro maior que zero.`;
        }
    }
    return null;
}

function renderSlaMetas(metas) {
    if (!slaMetasTableBody) return;
    slaMetasTableBody.innerHTML = '';
    const lista = Array.isArray(metas) ? metas : [];
    if (!lista.length) {
        slaMetasEmptyMessage?.classList.remove('hidden');
        return;
    }
    slaMetasEmptyMessage?.classList.add('hidden');
    const ordem = { CRITICA: 0, ALTA: 1, MEDIA: 2, BAIXA: 3 };
    lista.sort((a, b) => (ordem[a.prioridade] ?? 9) - (ordem[b.prioridade] ?? 9));

    lista.forEach(meta => {
        const ativo = meta.ativo !== false;
        const row = document.createElement('tr');
        row.dataset.prioridade = meta.prioridade;
        row.innerHTML = `
            <td>${formatPriorityBadgeHtmlFn(meta.prioridade)}</td>
            <td>
                <input type="number" min="1" step="1" class="sla-meta-input"
                    data-field="prazoPrimeiroAtendimentoMinutos"
                    value="${meta.prazoPrimeiroAtendimentoMinutos ?? ''}" />
            </td>
            <td>
                <input type="number" min="1" step="1" class="sla-meta-input"
                    data-field="prazoResolucaoMinutos"
                    value="${meta.prazoResolucaoMinutos ?? ''}" />
            </td>
            <td>
                <label class="checkbox-inline sla-meta-ativo-label">
                    <input type="checkbox" data-field="ativo" ${ativo ? 'checked' : ''} /> Ativo
                </label>
            </td>
            <td class="table-actions">
                <button type="button" class="button button-primary button-small" data-sla-meta-action="save" data-prioridade="${meta.prioridade}">Salvar</button>
            </td>
        `;
        slaMetasTableBody.appendChild(row);
    });
}

function lerSlaMetaDaLinha(prioridade) {
    const row = slaMetasTableBody?.querySelector(`tr[data-prioridade="${prioridade}"]`);
    if (!row) return null;
    const primeiro = row.querySelector('[data-field="prazoPrimeiroAtendimentoMinutos"]');
    const resolucao = row.querySelector('[data-field="prazoResolucaoMinutos"]');
    const ativoInput = row.querySelector('[data-field="ativo"]');
    return {
        prazoPrimeiroAtendimentoMinutos: primeiro?.value,
        prazoResolucaoMinutos: resolucao?.value,
        ativo: Boolean(ativoInput?.checked)
    };
}

async function loadSlaMetas() {
    try {
        const metas = await configuracaoService.listSlaMetas();
        renderSlaMetas(metas);
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

async function salvarSlaMeta(prioridade) {
    clearAlertFn(alertBoxConfiguracoes);
    const payload = lerSlaMetaDaLinha(prioridade);
    if (!payload) {
        showAlertFn('Meta de SLA não encontrada na tabela.', alertBoxConfiguracoes);
        return;
    }
    const erro = validarSlaMetaPayload(payload);
    if (erro) {
        showAlertFn(erro, alertBoxConfiguracoes);
        return;
    }
    const body = {
        prazoPrimeiroAtendimentoMinutos: Number(payload.prazoPrimeiroAtendimentoMinutos),
        prazoResolucaoMinutos: Number(payload.prazoResolucaoMinutos),
        ativo: payload.ativo
    };
    try {
        await configuracaoService.updateSlaMetaPrioridade(prioridade, body);
        showAlertFn(`Meta de SLA (${formatPriorityFn(prioridade)}) salva com sucesso.`, alertBoxConfiguracoes, 'success');
        await loadSlaMetas();
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}

async function carregarSlaMetasDefault() {
    clearAlertFn(alertBoxConfiguracoes);
    try {
        const resultado = await configuracaoService.seedSlaMetasDefault();
        showAlertFn(resultado.mensagem || 'Metas padrão processadas.', alertBoxConfiguracoes, 'success');
        await loadSlaMetas();
    } catch (error) {
        showAlertFn(error.message, alertBoxConfiguracoes);
    }
}
