import { MSG_ERRO, mensagemParaExibirUsuario } from '../core/messages.js';
import {
    isAdminPerfil,
    resolvePerfilAcessoCode,
    formatPerfilAcessoLabel,
    renderPerfilAcessoBadge
} from '../core/permissions.js';
import * as analistaService from '../services/analistaService.js';
import * as dashboardService from '../services/dashboardService.js';

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatPriorityBadgeHtmlFn = () => '';
let getStatusClassFn = () => 'status-badge';
let getTicketPriorityRowClassFn = () => '';
let sortTicketsByPriorityFn = (t) => t;
let renderAnalystAvatarFn = () => '';
let getAnalystDisplayNameFn = () => '-';
let renderStatusOperadorFn = () => '';
let summarizeTicketFn = () => '-';
let buildKanbanTicketMiniHtmlFn = () => '';
let setAnalystAvatarElementFn = () => {};
let openDetailsFn = () => {};
let alertBoxGlobal = null;

const analistasKanban = document.getElementById('analistasKanban');
const adminPerfisSection = document.getElementById('adminPerfisSection');
const adminPerfisList = document.getElementById('adminPerfisList');
const adminCadastroAnalistaForm = document.getElementById('adminCadastroAnalistaForm');
const alertBoxAdminAnalistas = document.getElementById('alertBoxAdminAnalistas');
const refreshAtendentesButton = document.getElementById('refreshAtendentesButton');
const analystQueueModal = document.getElementById('modalAnalistaFila');
const closeAnalystQueueModalButton = document.getElementById('fecharModalAnalistaFila');
const analystQueueAvatar = document.getElementById('analistaFilaAvatar');
const analystQueueName = document.getElementById('analistaFilaNome');
const analystQueueLevel = document.getElementById('analistaFilaNivel');
const analystQueueCount = document.getElementById('analistaFilaQuantidade');
const analystQueueTickets = document.getElementById('analistaFilaTickets');

let currentAnalystQueues = [];
let listenersBound = false;

const MENSAGEM_POLITICA_SENHA =
    'Senha deve ter no mínimo 8 caracteres, pelo menos 1 letra e pelo menos 1 número.';

export function initAtendentesPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatPriorityBadgeHtml) formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml;
    if (deps.getStatusClass) getStatusClassFn = deps.getStatusClass;
    if (deps.getTicketPriorityRowClass) getTicketPriorityRowClassFn = deps.getTicketPriorityRowClass;
    if (deps.sortTicketsByPriority) sortTicketsByPriorityFn = deps.sortTicketsByPriority;
    if (deps.renderAnalystAvatar) renderAnalystAvatarFn = deps.renderAnalystAvatar;
    if (deps.getAnalystDisplayName) getAnalystDisplayNameFn = deps.getAnalystDisplayName;
    if (deps.renderStatusOperador) renderStatusOperadorFn = deps.renderStatusOperador;
    if (deps.summarizeTicket) summarizeTicketFn = deps.summarizeTicket;
    if (deps.buildKanbanTicketMiniHtml) buildKanbanTicketMiniHtmlFn = deps.buildKanbanTicketMiniHtml;
    if (deps.setAnalystAvatarElement) setAnalystAvatarElementFn = deps.setAnalystAvatarElement;
    if (deps.openDetails) openDetailsFn = deps.openDetails;
    if (deps.alertBox) alertBoxGlobal = deps.alertBox;
    if (listenersBound) return;
    listenersBound = true;

    refreshAtendentesButton?.addEventListener('click', () => loadAtendentesPage());
    analistasKanban?.addEventListener('click', onAnalistasKanbanClick);
    adminPerfisSection?.addEventListener('click', onAdminPerfisSectionClick);
    adminCadastroAnalistaForm?.addEventListener('submit', onAdminCadastroAnalistaSubmit);
    analystQueueTickets?.addEventListener('click', event => {
        const button = event.target.closest('button[data-action="details"]');
        if (!button) return;
        closeAnalystQueue();
        openDetailsFn(button.dataset.ticket);
    });
    closeAnalystQueueModalButton?.addEventListener('click', closeAnalystQueue);
    analystQueueModal?.addEventListener('click', event => {
        if (event.target === analystQueueModal) closeAnalystQueue();
    });
}

export function updateAdminPerfisSectionVisibility() {
    if (!adminPerfisSection) {
        return;
    }
    const show = isAdminPerfil();
    adminPerfisSection.classList.toggle('hidden', !show);
    if (!show && adminPerfisList) {
        adminPerfisList.innerHTML = '';
    }
}

export async function loadAtendentesPage() {
    await loadAnalistasKanban();
    await loadAdminPerfisGestao();
}

export async function loadAnalistasKanban() {
    if (!analistasKanban) return;
    try {
        const filas = await dashboardService.getFilasAnalistas();
        renderAnalistasKanban(filas);
    } catch (error) {
        analistasKanban.innerHTML = `<p class="empty-state">${escapeAttr(mensagemParaExibirUsuario(error.message))}</p>`;
    }
}

export async function loadAdminPerfisGestao() {
    updateAdminPerfisSectionVisibility();
    if (!isAdminPerfil() || !adminPerfisList) {
        return;
    }
    adminPerfisList.innerHTML = '<p class="empty-state">Carregando analistas...</p>';
    try {
        const analistas = await analistaService.listAnalistasAll();
        renderAdminPerfisGestao(analistas);
    } catch (error) {
        adminPerfisList.innerHTML = `<p class="empty-state">${escapeAttr(mensagemParaExibirUsuario(error.message))}</p>`;
    }
}

export function closeAnalystQueue() {
    analystQueueModal?.classList.remove('ativo');
}

function escapeAttr(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;');
}

function buildPerfilAcessoAdminControls(analystId, currentCode, selectIdPrefix = '') {
    const resolved = resolvePerfilAcessoCode({ perfilAcesso: currentCode });
    const selectId = `${selectIdPrefix}perfil-select-${analystId}`;
    const options = ['ADMIN', 'SUPERVISOR', 'ANALISTA']
        .map(code => `<option value="${code}" ${code === resolved ? 'selected' : ''}>${formatPerfilAcessoLabel(code)}</option>`)
        .join('');
    return `
        <div class="analyst-perfil-admin">
            <label class="analyst-perfil-admin-label" for="${selectId}">Perfil de acesso</label>
            <div class="analyst-perfil-admin-row">
                <select id="${selectId}" class="analyst-perfil-select" data-analyst-id="${analystId}" aria-label="Perfil de acesso do analista">
                    ${options}
                </select>
                <button type="button" class="btn-secondary btn-save-perfil" data-action="save-perfil" data-analyst-id="${analystId}">Salvar</button>
            </div>
        </div>
    `;
}

async function salvarPerfilAcessoAnalista(analystId, perfilAcesso) {
    if (!isAdminPerfil()) {
        showAlertFn(MSG_ERRO.ACESSO_ADMIN, alertBoxGlobal);
        return null;
    }
    const updated = await analistaService.updatePerfilAcesso(analystId, perfilAcesso);
    showAlertFn('Perfil de acesso atualizado com sucesso.', alertBoxGlobal);
    await loadAnalistasKanban();
    await loadAdminPerfisGestao();
    return updated;
}

function buildPerfilSelectOptions(selectedCode) {
    const resolved = selectedCode === 'ADMIN' || selectedCode === 'SUPERVISOR' || selectedCode === 'ANALISTA'
        ? selectedCode
        : 'ANALISTA';
    return ['ADMIN', 'SUPERVISOR', 'ANALISTA']
        .map(code => `<option value="${code}" ${code === resolved ? 'selected' : ''}>${formatPerfilAcessoLabel(code)}</option>`)
        .join('');
}

function renderAdminAtivoBadge(ativo) {
    const ativoFlag = ativo !== false;
    return ativoFlag
        ? '<span class="admin-ativo-badge admin-ativo-sim">Ativo</span>'
        : '<span class="admin-ativo-badge admin-ativo-nao">Inativo</span>';
}

function senhaAtendePoliticaMinima(senha) {
    if (senha == null || senha.length < 8) {
        return false;
    }
    let temLetra = false;
    let temNumero = false;
    for (let i = 0; i < senha.length; i++) {
        const c = senha.charAt(i);
        if (/[A-Za-zÀ-ÿ]/.test(c)) {
            temLetra = true;
        } else if (/\d/.test(c)) {
            temNumero = true;
        }
        if (temLetra && temNumero) {
            return true;
        }
    }
    return temLetra && temNumero;
}

function validarSenhaPoliticaParaEnvio(senha, obrigatoria) {
    const valor = senha != null ? String(senha) : '';
    if (!valor.length) {
        return obrigatoria ? 'Senha inicial é obrigatória.' : null;
    }
    if (!senhaAtendePoliticaMinima(valor)) {
        return MENSAGEM_POLITICA_SENHA;
    }
    return null;
}

function lerFormularioAnalistaAdmin(form) {
    if (!form) {
        return null;
    }
    const nome = form.elements.nome?.value?.trim();
    const email = form.elements.email?.value?.trim();
    const nivel = form.elements.nivel?.value?.trim();
    const perfilAcesso = form.elements.perfilAcesso?.value;
    const ativo = form.elements.ativo?.checked;
    const senha = form.elements.senha?.value;
    const payload = { nome, email, nivel: nivel || 'Nível 1', perfilAcesso, ativo };
    if (senha != null && String(senha).length > 0) {
        payload.senha = senha;
    }
    return payload;
}

function renderAdminPerfisGestao(analistas) {
    if (!adminPerfisList) {
        return;
    }
    const lista = (analistas || []).slice().sort((a, b) => {
        const nomeA = (getAnalystDisplayNameFn(a) || '').toLocaleLowerCase('pt-BR');
        const nomeB = (getAnalystDisplayNameFn(b) || '').toLocaleLowerCase('pt-BR');
        return nomeA.localeCompare(nomeB, 'pt-BR');
    });
    adminPerfisList.innerHTML = '';
    if (!lista.length) {
        adminPerfisList.innerHTML = '<p class="empty-state">Nenhum analista cadastrado.</p>';
        return;
    }
    lista.forEach(analyst => {
        const perfilCode = resolvePerfilAcessoCode(analyst);
        const ativo = analyst.ativo !== false;
        const row = document.createElement('article');
        row.className = 'admin-perfil-row';
        row.dataset.analystId = analyst.id;
        row.setAttribute('role', 'listitem');
        row.innerHTML = `
            <div class="admin-perfil-row-main">
                <strong class="admin-perfil-nome">${escapeAttr(getAnalystDisplayNameFn(analyst))}</strong>
                <span class="admin-perfil-email">${escapeAttr(analyst.email || '-')}</span>
                <span class="admin-perfil-status">${renderStatusOperadorFn(analyst.statusOperador)}</span>
                <span data-perfil-badge>${renderPerfilAcessoBadge(perfilCode)}</span>
                <span data-ativo-badge>${renderAdminAtivoBadge(ativo)}</span>
            </div>
            <form class="admin-analista-form admin-analista-edit-form" data-analyst-edit-form>
                <div class="admin-form-grid">
                    <label>Nome<input type="text" name="nome" value="${escapeAttr(analyst.nome)}" required maxlength="150" /></label>
                    <label>E-mail<input type="email" name="email" value="${escapeAttr(analyst.email)}" required maxlength="150" /></label>
                    <label>Nível<input type="text" name="nivel" value="${escapeAttr(analyst.nivel || 'Nível 1')}" maxlength="50" /></label>
                    <label>Perfil de acesso<select name="perfilAcesso">${buildPerfilSelectOptions(perfilCode)}</select></label>
                    <label>Nova senha (opcional)<input type="password" name="senha" minlength="8" autocomplete="new-password" placeholder="Deixe em branco para manter" aria-describedby="adminSenhaPoliticaHint" /></label>
                    <label class="admin-checkbox-label">
                        <input type="checkbox" name="ativo" ${ativo ? 'checked' : ''} />
                        Ativo
                    </label>
                </div>
                <div class="admin-row-actions">
                    <button type="button" class="button button-primary" data-action="save-edit-analyst">Salvar dados</button>
                    <button type="button" class="btn-secondary" data-action="quick-save-perfil" data-analyst-id="${analyst.id}">Salvar só perfil</button>
                    <button type="button" class="btn-secondary" data-action="toggle-ativo" data-analyst-id="${analyst.id}" data-ativo="${ativo ? '1' : '0'}">
                        ${ativo ? 'Inativar' : 'Ativar'}
                    </button>
                </div>
            </form>
        `;
        adminPerfisList.appendChild(row);
    });
}

function renderAnalistasKanban(filas) {
    analistasKanban.innerHTML = '';
    currentAnalystQueues = filas || [];
    if (!filas.length) {
        analistasKanban.innerHTML = '<p class="empty-state">Nenhum analista online.</p>';
        return;
    }

    filas.forEach(fila => {
        const analyst = {
            id: fila.id,
            nome: fila.nome,
            nomeCompleto: fila.nomeCompleto,
            email: fila.email,
            nivel: fila.nivel,
            perfilAcesso: fila.perfilAcesso,
            fotoUrl: fila.fotoUrl,
            statusOperador: fila.statusOperador,
            online: fila.online
        };
        const perfilCode = resolvePerfilAcessoCode(analyst);
        const adminPerfilControls = isAdminPerfil()
            ? buildPerfilAcessoAdminControls(fila.id, perfilCode)
            : '';
        const card = document.createElement('div');
        card.className = 'analyst-card';
        card.dataset.analystId = fila.id;

        const tickets = sortTicketsByPriorityFn(fila.tickets || []);
        card.innerHTML = `
            <div class="analyst-card-header">
                ${renderAnalystAvatarFn(analyst)}
                <div>
                    <strong>${getAnalystDisplayNameFn(fila)}</strong>
                    ${renderPerfilAcessoBadge(perfilCode)}
                    <span>${fila.nivel || '-'}</span>
                    ${renderStatusOperadorFn(fila.statusOperador)}
                </div>
            </div>
            <div class="analyst-card-meta">
                <span>${fila.quantidadeTickets || 0} ticket(s) em atendimento</span>
            </div>
            ${adminPerfilControls}
            <div class="analyst-card-tickets"></div>
        `;

        const list = card.querySelector('.analyst-card-tickets');
        if (!tickets.length) {
            list.innerHTML = '<p class="empty-state">Nenhum ticket em atendimento.</p>';
        } else {
            tickets.forEach(ticket => {
                const wrapper = document.createElement('div');
                wrapper.innerHTML = buildKanbanTicketMiniHtmlFn(ticket);
                if (wrapper.firstElementChild) {
                    list.appendChild(wrapper.firstElementChild);
                }
            });
        }

        analistasKanban.appendChild(card);
    });
}

function formatTempoAtendimento() {
    return '-';
}

function openAnalystQueue(analystId) {
    const fila = currentAnalystQueues.find(item => String(item.id) === String(analystId));
    if (!fila || !analystQueueModal) return;

    const analyst = {
        id: fila.id,
        nome: fila.nome,
        nomeCompleto: fila.nomeCompleto,
        email: fila.email,
        nivel: fila.nivel,
        fotoUrl: fila.fotoUrl,
        statusOperador: fila.statusOperador,
        online: fila.online
    };
    setAnalystAvatarElementFn(analystQueueAvatar, analyst);
    analystQueueName.textContent = getAnalystDisplayNameFn(fila);
    analystQueueLevel.textContent = fila.nivel || '-';
    analystQueueCount.textContent = `${fila.quantidadeTickets || 0} ticket(s) em atendimento`;
    analystQueueTickets.innerHTML = '';

    const tickets = sortTicketsByPriorityFn(fila.tickets || []);
    if (!tickets.length) {
        analystQueueTickets.innerHTML = '<p class="empty-state">Nenhum ticket em atendimento.</p>';
    } else {
        tickets.forEach(ticket => {
            const item = document.createElement('div');
            item.className = `expanded-ticket-card ${getTicketPriorityRowClassFn(ticket.prioridade, 'kanban')}`.trim();
            item.innerHTML = `
                <div>
                    <strong>${ticket.numeroTicket}</strong>
                    ${formatPriorityBadgeHtmlFn(ticket.prioridade)}
                    <p>${summarizeTicketFn(ticket)}</p>
                </div>
                <div class="expanded-ticket-grid">
                    <span>Cliente: ${ticket.cliente || '-'}</span>
                    <span>Origem: ${ticket.canal || '-'}</span>
                    <span>Status: ${ticket.status || '-'}</span>
                    <span>Tempo em atendimento: ${formatTempoAtendimento(ticket)}</span>
                </div>
                <button class="button button-secondary" data-action="details" data-ticket="${ticket.numeroTicket}">Ver Detalhes</button>
            `;
            analystQueueTickets.appendChild(item);
        });
    }

    analystQueueModal.classList.add('ativo');
}

async function handleSavePerfilAcessoClick(saveBtn, container, alertTarget) {
    const host = container || saveBtn.closest('.analyst-card, .admin-perfil-row');
    const select = host?.querySelector('.analyst-perfil-select')
        || host?.querySelector('select[name="perfilAcesso"]');
    const analystId = saveBtn.dataset.analystId || host?.dataset?.analystId;
    if (!select || !analystId) {
        return;
    }
    try {
        const updated = await salvarPerfilAcessoAnalista(analystId, select.value);
        if (updated && host) {
            const badgeHost = host.querySelector('[data-perfil-badge]');
            if (badgeHost) {
                badgeHost.innerHTML = renderPerfilAcessoBadge(resolvePerfilAcessoCode(updated));
            }
        }
    } catch (error) {
        showAlertFn(error.message, alertTarget || alertBoxAdminAnalistas);
    }
}

async function handleSaveEditAnalyst(row) {
    const form = row?.querySelector('[data-analyst-edit-form]');
    const analystId = row?.dataset?.analystId;
    if (!form || !analystId) {
        return;
    }
    const payload = lerFormularioAnalistaAdmin(form);
    if (!payload?.nome || !payload?.email) {
        showAlertFn(MSG_ERRO.CAMPOS_OBRIGATORIOS, alertBoxAdminAnalistas);
        return;
    }
    const senhaInput = form.elements.senha?.value;
    const erroSenha = validarSenhaPoliticaParaEnvio(senhaInput, false);
    if (erroSenha) {
        showAlertFn(erroSenha, alertBoxAdminAnalistas);
        return;
    }
    try {
        await analistaService.update(analystId, payload);
        showAlertFn('Analista atualizado com sucesso.', alertBoxAdminAnalistas, 'success');
        await loadAdminPerfisGestao();
        await loadAnalistasKanban();
    } catch (error) {
        showAlertFn(error.message, alertBoxAdminAnalistas);
    }
}

async function handleToggleAtivoAnalyst(btn, row) {
    const analystId = btn.dataset.analystId;
    if (!analystId || !row) {
        return;
    }
    const novoAtivo = btn.dataset.ativo !== '1';
    try {
        await analistaService.update(analystId, { ativo: novoAtivo });
        showAlertFn(novoAtivo ? 'Analista ativado.' : 'Analista inativado.', alertBoxAdminAnalistas, 'success');
        await loadAdminPerfisGestao();
        await loadAnalistasKanban();
    } catch (error) {
        showAlertFn(error.message, alertBoxAdminAnalistas);
    }
}

async function onAnalistasKanbanClick(event) {
    const saveBtn = event.target.closest('button[data-action="save-perfil"]');
    if (saveBtn) {
        event.stopPropagation();
        await handleSavePerfilAcessoClick(saveBtn, saveBtn.closest('.analyst-card'));
        return;
    }
    if (event.target.closest('.analyst-perfil-admin')) {
        event.stopPropagation();
        return;
    }
    const button = event.target.closest('button[data-action="details"]');
    if (button) {
        event.stopPropagation();
        return openDetailsFn(button.dataset.ticket);
    }
    const card = event.target.closest('.analyst-card');
    if (card) {
        openAnalystQueue(card.dataset.analystId);
    }
}

async function onAdminPerfisSectionClick(event) {
    const row = event.target.closest('.admin-perfil-row');
    const quickPerfilBtn = event.target.closest('button[data-action="quick-save-perfil"]');
    if (quickPerfilBtn && row) {
        event.preventDefault();
        await handleSavePerfilAcessoClick(quickPerfilBtn, row, alertBoxAdminAnalistas);
        return;
    }
    const saveEditBtn = event.target.closest('button[data-action="save-edit-analyst"]');
    if (saveEditBtn && row) {
        event.preventDefault();
        await handleSaveEditAnalyst(row);
        return;
    }
    const toggleBtn = event.target.closest('button[data-action="toggle-ativo"]');
    if (toggleBtn && row) {
        event.preventDefault();
        await handleToggleAtivoAnalyst(toggleBtn, row);
    }
}

async function onAdminCadastroAnalistaSubmit(event) {
    event.preventDefault();
    if (!isAdminPerfil()) {
        return;
    }
    const payload = lerFormularioAnalistaAdmin(adminCadastroAnalistaForm);
    if (!payload?.nome || !payload?.email) {
        showAlertFn(MSG_ERRO.CAMPOS_OBRIGATORIOS, alertBoxAdminAnalistas);
        return;
    }
    const erroSenha = validarSenhaPoliticaParaEnvio(adminCadastroAnalistaForm.elements.senha?.value, true);
    if (erroSenha) {
        showAlertFn(erroSenha, alertBoxAdminAnalistas);
        return;
    }
    try {
        await analistaService.create(payload);
        showAlertFn('Analista cadastrado com sucesso.', alertBoxAdminAnalistas, 'success');
        adminCadastroAnalistaForm.reset();
        const ativoInput = adminCadastroAnalistaForm.elements.ativo;
        if (ativoInput) {
            ativoInput.checked = true;
        }
        await loadAdminPerfisGestao();
    } catch (error) {
        showAlertFn(error.message, alertBoxAdminAnalistas);
    }
}
