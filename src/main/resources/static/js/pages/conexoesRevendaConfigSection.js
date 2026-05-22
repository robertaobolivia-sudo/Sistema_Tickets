import { canManageEtiquetas } from '../core/permissions.js';
import * as carteiraService from '../services/carteiraService.js';

let showAlertFn = () => {};

const carteiraEditId = document.getElementById('carteiraEditId');
const carteiraNome = document.getElementById('carteiraNome');
const carteiraSalvarBtn = document.getElementById('carteiraSalvarBtn');
const carteiraLimparBtn = document.getElementById('carteiraLimparBtn');
const carteirasTableBody = document.getElementById('carteirasTableBody');
const carteirasEmptyMessage = document.getElementById('carteirasEmptyMessage');

let listenersBound = false;

export function initConexoesRevendaConfigSection(deps = {}) {
    if (deps.showAlert) {
        showAlertFn = deps.showAlert;
    }
    if (listenersBound) {
        return;
    }
    listenersBound = true;

    carteiraSalvarBtn?.addEventListener('click', salvarCarteira);
    carteiraLimparBtn?.addEventListener('click', limparFormularioCarteira);
    carteirasTableBody?.addEventListener('click', event => {
        const btn = event.target.closest('button[data-carteira-action="edit"]');
        if (!btn || !canManageEtiquetas()) {
            return;
        }
        const id = btn.getAttribute('data-id');
        if (id) {
            editarCarteira(id);
        }
    });
}

export function applyConexoesRevendaConfigPermissions() {
    const allowed = canManageEtiquetas();
    [carteiraSalvarBtn, carteiraLimparBtn, carteiraNome].forEach(el => {
        if (el) {
            el.disabled = !allowed;
        }
    });
}

export async function loadConexoesRevendaConfigSection() {
    applyConexoesRevendaConfigPermissions();
    await carregarListaCarteiras();
}

async function salvarCarteira() {
    if (!canManageEtiquetas()) {
        return;
    }
    const nome = carteiraNome?.value?.trim();
    if (!nome) {
        showAlertFn('Informe o nome da conexão/revenda.', 'error');
        return;
    }
    const id = carteiraEditId?.value?.trim();
    try {
        let salvo;
        if (id) {
            salvo = await carteiraService.updateCarteira(id, { nome });
        } else {
            salvo = await carteiraService.createCarteira({ nome });
            if (carteiraEditId && salvo?.id != null) {
                carteiraEditId.value = String(salvo.id);
            }
        }
        if (carteiraEditId && salvo?.id != null) {
            carteiraEditId.value = String(salvo.id);
        }
        showAlertFn('Conexão/revenda salva.', 'success');
        await carregarListaCarteiras();
    } catch (err) {
        showAlertFn(err?.message || 'Falha ao salvar.', 'error');
    }
}

function limparFormularioCarteira() {
    if (carteiraEditId) {
        carteiraEditId.value = '';
    }
    if (carteiraNome) {
        carteiraNome.value = '';
    }
}

async function editarCarteira(id) {
    try {
        const c = await carteiraService.getCarteira(id);
        if (carteiraEditId) {
            carteiraEditId.value = c?.id != null ? String(c.id) : '';
        }
        if (carteiraNome) {
            carteiraNome.value = c?.nome || '';
        }
    } catch (err) {
        showAlertFn(err?.message || 'Falha ao carregar cadastro.', 'error');
    }
}

async function carregarListaCarteiras() {
    if (!carteirasTableBody) {
        return;
    }
    try {
        const lista = await carteiraService.listCarteiras();
        const rows = Array.isArray(lista) ? lista : [];
        carteirasTableBody.innerHTML = rows
            .map(
                c => `
            <tr>
                <td>${escapeHtml(c.nome)}</td>
                <td>
                    <button type="button" class="button button-small button-secondary" data-carteira-action="edit" data-id="${c.id}">Editar</button>
                </td>
            </tr>`
            )
            .join('');
        if (carteirasEmptyMessage) {
            carteirasEmptyMessage.classList.toggle('hidden', rows.length > 0);
        }
    } catch (err) {
        carteirasTableBody.innerHTML = '';
        if (carteirasEmptyMessage) {
            carteirasEmptyMessage.classList.remove('hidden');
            carteirasEmptyMessage.textContent = 'Não foi possível carregar conexões/revendas.';
        }
    }
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
