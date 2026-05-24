import {
    loginPortal,
    savePortalSession,
    clearPortalSession,
    getPortalSession,
    getDashboardPortal,
    getTicketsPortal,
    listarUsuariosPortal,
    criarUsuarioPortal,
    atualizarUsuarioPortal
} from '@features/cliente-portal/cliente-portal-service.js';
import { listOrSearch as listClientes } from '@features/clientes/cliente-service.js';

let _loginScreen = null;
let _portalLoginScreen = null;
let _portalAppScreen = null;

function showPortalLogin() {
    _loginScreen?.classList.remove('screen-active');
    _portalLoginScreen?.classList.add('screen-active');
    _portalAppScreen?.classList.remove('screen-active');
}

function showCorporateLogin() {
    _loginScreen?.classList.add('screen-active');
    _portalLoginScreen?.classList.remove('screen-active');
    _portalAppScreen?.classList.remove('screen-active');
}

function openPortalApp(session) {
    _portalLoginScreen?.classList.remove('screen-active');
    _loginScreen?.classList.remove('screen-active');
    _portalAppScreen?.classList.add('screen-active');

    const nameEl = document.getElementById('portalTopbarNome');
    if (nameEl) nameEl.textContent = session.nome || '';
}

function showPortalPage(name) {
    document.querySelectorAll('.portal-page').forEach(p => p.classList.remove('portal-page-active'));
    document.getElementById(`portalPage${capitalize(name)}`)?.classList.add('portal-page-active');

    document.querySelectorAll('.portal-nav-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.portalPage === name);
    });
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function setPortalAlert(msg, type = 'error') {
    const el = document.getElementById('portalAppAlert');
    if (!el) return;
    el.textContent = msg;
    el.className = `alert ${type}`;
    el.classList.remove('hidden');
}

function clearPortalAlert() {
    const el = document.getElementById('portalAppAlert');
    if (el) { el.textContent = ''; el.classList.add('hidden'); }
}

async function loadPortalDashboard() {
    const container = document.getElementById('portalDashboardCards');
    const ticketsContainer = document.getElementById('portalDashboardTickets');
    if (!container) return;
    try {
        const data = await getDashboardPortal();
        renderPortalDashboard(data, container);
        if (ticketsContainer) renderPortalDashboardTickets(data, ticketsContainer);
    } catch (e) {
        container.innerHTML = `<p class="portal-empty">${e.message}</p>`;
    }
}

function renderPortalDashboard(data, container) {
    const cards = [
        { label: 'Total de Tickets', value: data.totalTickets ?? 0, highlight: false },
        { label: 'Abertos', value: data.ticketsAbertos ?? 0, highlight: false },
        { label: 'Em Atendimento', value: data.ticketsEmAtendimento ?? 0, highlight: false },
        { label: 'Resolvidos', value: data.ticketsResolvidos ?? 0, highlight: true },
        { label: 'Cancelados', value: data.ticketsCancelados ?? 0, highlight: false },
        {
            label: 'Satisfação Média',
            value: data.mediaAvaliacao != null ? data.mediaAvaliacao.toFixed(1) : '—',
            sub: data.totalAvaliacoes ? `${data.totalAvaliacoes} avaliação(ões)` : null,
            highlight: false
        }
    ];

    container.innerHTML = cards.map(c => `
        <div class="portal-metric-card${c.highlight ? ' portal-metric-card--highlight' : ''}">
            <span class="portal-metric-card-label">${c.label}</span>
            <strong class="portal-metric-card-value">${c.value}</strong>
            ${c.sub ? `<span class="portal-metric-card-sub">${c.sub}</span>` : ''}
        </div>
    `).join('');
}

function renderPortalDashboardTickets(data, container) {
    container.innerHTML = '';
}

async function loadPortalTickets() {
    const tableBody = document.getElementById('portalTicketsTableBody');
    if (!tableBody) return;
    tableBody.innerHTML = '<tr><td colspan="6" class="portal-empty">Carregando...</td></tr>';
    try {
        const tickets = await getTicketsPortal();
        renderPortalTickets(tickets, tableBody);
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="6" class="portal-empty">${e.message}</td></tr>`;
    }
}

function statusLabel(status) {
    const map = {
        ABERTO: 'Aberto',
        EM_ATENDIMENTO: 'Em Atendimento',
        AGUARDANDO_CLIENTE: 'Aguardando',
        RESOLVIDO: 'Resolvido',
        CANCELADO: 'Cancelado',
        INDEVIDO: 'Indevido'
    };
    return map[status] || status;
}

function statusClass(status) {
    const map = {
        ABERTO: 'badge-status badge-aberto',
        EM_ATENDIMENTO: 'badge-status badge-em-atendimento',
        AGUARDANDO_CLIENTE: 'badge-status badge-aguardando',
        RESOLVIDO: 'badge-status badge-resolvido',
        CANCELADO: 'badge-status badge-cancelado',
        INDEVIDO: 'badge-status badge-cancelado'
    };
    return map[status] || 'badge-status';
}

function formatDateShort(iso) {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function renderPortalTickets(tickets, tableBody) {
    if (!tickets || tickets.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="portal-empty">Nenhum ticket encontrado.</td></tr>';
        return;
    }
    tableBody.innerHTML = tickets.map(t => `
        <tr>
            <td><span class="portal-ticket-numero">${t.numeroTicket || '—'}</span></td>
            <td><span class="portal-ticket-titulo">${t.titulo || '—'}</span></td>
            <td><span class="${statusClass(t.status)}">${statusLabel(t.status)}</span></td>
            <td>${t.prioridade ? t.prioridade.replace('_', ' ') : '—'}</td>
            <td>${t.analistaNome || '—'}</td>
            <td>${formatDateShort(t.dataAbertura)}</td>
        </tr>
    `).join('');
}

export function initClientePortalPage({ loginScreen }) {
    _loginScreen = loginScreen;
    _portalLoginScreen = document.getElementById('portalLoginScreen');
    _portalAppScreen = document.getElementById('portalAppScreen');

    if (!_portalLoginScreen || !_portalAppScreen) return { tryRestorePortalSession: () => false };

    // Corporate login → portal link
    const irPortalBtn = document.getElementById('irParaPortalBtn');
    irPortalBtn?.addEventListener('click', e => {
        e.preventDefault();
        clearLoginAlert();
        showPortalLogin();
    });

    // Portal login → corporate link
    const voltarLoginBtn = document.getElementById('portalVoltarLoginBtn');
    voltarLoginBtn?.addEventListener('click', e => {
        e.preventDefault();
        clearPortalLoginAlert();
        showCorporateLogin();
    });

    // Portal login form
    const form = document.getElementById('portalLoginForm');
    form?.addEventListener('submit', async e => {
        e.preventDefault();
        clearPortalLoginAlert();
        const email = document.getElementById('portalLoginEmail')?.value?.trim() || '';
        const senha = document.getElementById('portalLoginSenha')?.value || '';
        if (!email || !senha) {
            setPortalLoginAlert('Preencha e-mail e senha.');
            return;
        }
        try {
            const session = await loginPortal(email, senha);
            savePortalSession(session);
            form.reset();
            openPortalApp(session);
            showPortalPage('dashboard');
            await loadPortalDashboard();
        } catch (err) {
            setPortalLoginAlert(err.message || 'E-mail ou senha inválidos.');
        }
    });

    // Portal nav buttons
    document.querySelectorAll('.portal-nav-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
            const page = btn.dataset.portalPage;
            if (!page) return;
            showPortalPage(page);
            clearPortalAlert();
            if (page === 'dashboard') await loadPortalDashboard();
            else if (page === 'tickets') await loadPortalTickets();
        });
    });

    // Portal logout
    document.getElementById('portalLogoutBtn')?.addEventListener('click', () => {
        clearPortalSession();
        showCorporateLogin();
        document.getElementById('portalLoginForm')?.reset();
    });

    function tryRestorePortalSession() {
        const existingSession = getPortalSession();
        if (existingSession?.id && existingSession?.authToken) {
            openPortalApp(existingSession);
            showPortalPage('dashboard');
            loadPortalDashboard();
            return true;
        }
        return false;
    }

    return { tryRestorePortalSession };
}

function setPortalLoginAlert(msg) {
    const el = document.getElementById('portalLoginAlert');
    if (!el) return;
    el.textContent = msg;
    el.className = 'alert error';
    el.classList.remove('hidden');
}

function clearPortalLoginAlert() {
    const el = document.getElementById('portalLoginAlert');
    if (el) { el.textContent = ''; el.classList.add('hidden'); }
}

function clearLoginAlert() {
    const el = document.getElementById('loginAlert');
    if (el) { el.textContent = ''; el.classList.add('hidden'); }
}

// ============================================================
// ADMIN: portal user management (Configurações page)
// ============================================================

let _portalUsuariosInit = false;

export async function loadPortalUsuariosAdmin() {
    const form = document.getElementById('portalUsuarioCadastroForm');
    const filtroSelect = document.getElementById('portalUsuarioFiltroCliente');
    const clienteSelect = document.getElementById('portalUsuarioClienteSelect');
    const lista = document.getElementById('portalUsuariosList');
    const alertBox = document.getElementById('alertBoxPortalUsuarios');
    if (!lista) return;

    let clientes = [];
    try {
        const result = await listClientes('');
        clientes = Array.isArray(result) ? result : (result?.content || result?.clientes || []);
    } catch (_) {}

    if (clienteSelect) {
        clienteSelect.innerHTML = '<option value="">Selecione...</option>';
        clientes.forEach(c => {
            clienteSelect.innerHTML += `<option value="${c.id}">${escapeHtml(c.nome)}</option>`;
        });
    }
    if (filtroSelect) {
        filtroSelect.innerHTML = '<option value="">Todos</option>';
        clientes.forEach(c => {
            filtroSelect.innerHTML += `<option value="${c.id}">${escapeHtml(c.nome)}</option>`;
        });
    }

    if (!_portalUsuariosInit) {
        _portalUsuariosInit = true;

        filtroSelect?.addEventListener('change', async () => {
            await refreshPortalUsuariosList(filtroSelect.value ? Number(filtroSelect.value) : null, lista, alertBox);
        });

        form?.addEventListener('submit', async e => {
            e.preventDefault();
            if (alertBox) { alertBox.textContent = ''; alertBox.classList.add('hidden'); }
            const data = Object.fromEntries(new FormData(form));
            const payload = {
                nome: data.nome,
                email: data.email,
                senha: data.senha,
                clienteId: Number(data.clienteId),
                ativo: data.ativo === 'on'
            };
            try {
                await criarUsuarioPortal(payload);
                form.reset();
                if (alertBox) {
                    alertBox.textContent = 'Usuário cadastrado com sucesso.';
                    alertBox.className = 'alert success';
                    alertBox.classList.remove('hidden');
                }
                await refreshPortalUsuariosList(null, lista, alertBox);
            } catch (err) {
                if (alertBox) {
                    alertBox.textContent = err.message || 'Erro ao cadastrar usuário.';
                    alertBox.className = 'alert error';
                    alertBox.classList.remove('hidden');
                }
            }
        });
    }

    await refreshPortalUsuariosList(null, lista, alertBox);
}

async function refreshPortalUsuariosList(clienteId, lista, alertBox) {
    if (!lista) return;
    lista.innerHTML = '<p class="empty-state">Carregando...</p>';
    try {
        const usuarios = await listarUsuariosPortal(clienteId);
        if (!usuarios?.length) {
            lista.innerHTML = '<p class="empty-state">Nenhum usuário cadastrado.</p>';
            return;
        }
        lista.innerHTML = usuarios.map(u => `
            <div class="admin-perfil-row" data-portal-user-id="${u.id}">
                <div class="admin-perfil-row-main">
                    <span class="admin-perfil-nome">${escapeHtml(u.nome)}</span>
                    <span class="admin-perfil-email">${escapeHtml(u.email)}</span>
                    <span class="admin-perfil-email">${escapeHtml(u.clienteNome || '—')}</span>
                    <span class="admin-ativo-badge ${u.ativo ? 'admin-ativo-sim' : 'admin-ativo-nao'}">${u.ativo ? 'Ativo' : 'Inativo'}</span>
                </div>
                <div class="admin-row-actions">
                    <button type="button" class="button button-secondary button-small" data-action="toggle-portal-user" data-id="${u.id}" data-ativo="${u.ativo}">${u.ativo ? 'Desativar' : 'Ativar'}</button>
                    <button type="button" class="button button-secondary button-small" data-action="reset-portal-pwd" data-id="${u.id}">Nova senha</button>
                </div>
            </div>
        `).join('');

        lista.querySelectorAll('[data-action="toggle-portal-user"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = Number(btn.dataset.id);
                const ativo = btn.dataset.ativo === 'true';
                try {
                    await atualizarUsuarioPortal(id, { ativo: !ativo });
                    await refreshPortalUsuariosList(clienteId, lista, alertBox);
                } catch (err) {
                    if (alertBox) {
                        alertBox.textContent = err.message;
                        alertBox.className = 'alert error';
                        alertBox.classList.remove('hidden');
                    }
                }
            });
        });

        lista.querySelectorAll('[data-action="reset-portal-pwd"]').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = Number(btn.dataset.id);
                const nova = prompt('Nova senha para este usuário (mín. 8 caracteres):');
                if (!nova || nova.length < 8) return;
                try {
                    await atualizarUsuarioPortal(id, { senha: nova });
                    if (alertBox) {
                        alertBox.textContent = 'Senha atualizada.';
                        alertBox.className = 'alert success';
                        alertBox.classList.remove('hidden');
                    }
                } catch (err) {
                    if (alertBox) {
                        alertBox.textContent = err.message;
                        alertBox.className = 'alert error';
                        alertBox.classList.remove('hidden');
                    }
                }
            });
        });
    } catch (err) {
        lista.innerHTML = `<p class="empty-state">${err.message}</p>`;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
