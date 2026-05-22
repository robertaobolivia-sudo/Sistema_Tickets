import { canManageEtiquetas } from '../core/permissions.js';
import {
    formatEtiquetaStatusClass,
    formatEtiquetaStatusLabel,
    validateEtiquetaFormPayload,
    etiquetaCorParaExibicao
} from '../core/etiquetasGestaoView.js';
import * as etiquetaService from '../services/etiquetaService.js';

let showAlertFn = () => {};
let displayValueFn = (v) => (v == null || v === '' ? '—' : String(v));

const etiquetaEditId = document.getElementById('etiquetaEditId');
const etiquetaNome = document.getElementById('etiquetaNome');
const etiquetaDescricao = document.getElementById('etiquetaDescricao');
const etiquetaCor = document.getElementById('etiquetaCor');
const etiquetaCorPicker = document.getElementById('etiquetaCorPicker');
const etiquetaSalvarBtn = document.getElementById('etiquetaSalvarBtn');
const etiquetaLimparBtn = document.getElementById('etiquetaLimparBtn');
const etiquetasTableBody = document.getElementById('etiquetasTableBody');
const etiquetasEmptyMessage = document.getElementById('etiquetasEmptyMessage');

let listenersBound = false;

export function initEtiquetasConfigSection(deps = {}) {
    if (deps.showAlert) {
        showAlertFn = deps.showAlert;
    }
    if (deps.displayValue) {
        displayValueFn = deps.displayValue;
    }
    if (listenersBound) {
        return;
    }
    listenersBound = true;

    etiquetaSalvarBtn?.addEventListener('click', salvarEtiqueta);
    etiquetaLimparBtn?.addEventListener('click', limparFormularioEtiqueta);
    etiquetaCorPicker?.addEventListener('input', () => {
        if (etiquetaCor && etiquetaCorPicker.value) {
            etiquetaCor.value = etiquetaCorPicker.value;
        }
    });
    etiquetaCor?.addEventListener('input', () => {
        const v = etiquetaCor.value?.trim();
        if (etiquetaCorPicker && /^#[0-9A-Fa-f]{6}$/i.test(v)) {
            etiquetaCorPicker.value = v;
        }
    });
    etiquetasTableBody?.addEventListener('click', event => {
        const btn = event.target.closest('button[data-etiqueta-action]');
        if (!btn || !canManageEtiquetas()) {
            return;
        }
        const id = btn.getAttribute('data-id');
        const action = btn.getAttribute('data-etiqueta-action');
        if (!id) {
            return;
        }
        if (action === 'edit') {
            editarEtiqueta(id);
        } else if (action === 'ativar') {
            alterarStatusEtiqueta(id, true);
        } else if (action === 'inativar') {
            alterarStatusEtiqueta(id, false);
        }
    });
}

export function applyEtiquetasConfigPermissions() {
    const allowed = canManageEtiquetas();
    [etiquetaSalvarBtn, etiquetaLimparBtn, etiquetaNome, etiquetaDescricao, etiquetaCor, etiquetaCorPicker].forEach(
        el => {
            if (el) {
                el.disabled = !allowed;
            }
        }
    );
}

export async function loadEtiquetasConfigSection() {
    applyEtiquetasConfigPermissions();
    if (!canManageEtiquetas()) {
        return;
    }
    await carregarListaEtiquetas();
}

function limparFormularioEtiqueta() {
    if (etiquetaEditId) {
        etiquetaEditId.value = '';
    }
    if (etiquetaNome) {
        etiquetaNome.value = '';
    }
    if (etiquetaDescricao) {
        etiquetaDescricao.value = '';
    }
    if (etiquetaCor) {
        etiquetaCor.value = '';
    }
    if (etiquetaCorPicker) {
        etiquetaCorPicker.value = '#2563eb';
    }
}

async function carregarListaEtiquetas() {
    if (!etiquetasTableBody) {
        return;
    }
    try {
        const lista = await etiquetaService.listAll();
        renderTabelaEtiquetas(Array.isArray(lista) ? lista : []);
    } catch (error) {
        renderTabelaEtiquetas([]);
        showAlertFn(error.message);
    }
}

function renderTabelaEtiquetas(lista) {
    if (!etiquetasTableBody) {
        return;
    }
    etiquetasTableBody.innerHTML = '';
    if (!lista.length) {
        etiquetasEmptyMessage?.classList.remove('hidden');
        return;
    }
    etiquetasEmptyMessage?.classList.add('hidden');

    lista.forEach(et => {
        const tr = document.createElement('tr');
        const cor = etiquetaCorParaExibicao(et.cor);
        const statusClass = formatEtiquetaStatusClass(et.ativo === true);
        const statusLabel = formatEtiquetaStatusLabel(et.ativo === true);
        const id = et.id;
        const ativo = et.ativo === true;
        tr.innerHTML = `
            <td>
                <span class="etiqueta-cor-cell">
                    <span class="etiqueta-cor-swatch" style="background:${escapeAttr(cor)}"></span>
                    <span>${escapeHtml(displayValueFn(et.nome))}</span>
                </span>
            </td>
            <td>${escapeHtml(displayValueFn(et.descricao))}</td>
            <td><code class="etiqueta-cor-code">${escapeHtml(et.cor ? String(et.cor) : '—')}</code></td>
            <td><span class="etiqueta-status-badge ${statusClass}">${escapeHtml(statusLabel)}</span></td>
            <td class="etiqueta-actions-cell">
                <button type="button" class="button button-secondary button-small" data-etiqueta-action="edit" data-id="${escapeAttr(String(id))}">Editar</button>
                ${
                    ativo
                        ? `<button type="button" class="button button-secondary button-small" data-etiqueta-action="inativar" data-id="${escapeAttr(String(id))}">Inativar</button>`
                        : `<button type="button" class="button button-primary button-small" data-etiqueta-action="ativar" data-id="${escapeAttr(String(id))}">Ativar</button>`
                }
            </td>
        `;
        etiquetasTableBody.appendChild(tr);
    });
}

function editarEtiqueta(idStr) {
    const id = Number(idStr);
    if (!Number.isFinite(id)) {
        return;
    }
    etiquetaService
        .listAll()
        .then(lista => {
            const et = (Array.isArray(lista) ? lista : []).find(e => Number(e.id) === id);
            if (!et) {
                showAlertFn('Etiqueta não encontrada.');
                return;
            }
            if (etiquetaEditId) {
                etiquetaEditId.value = String(et.id);
            }
            if (etiquetaNome) {
                etiquetaNome.value = et.nome || '';
            }
            if (etiquetaDescricao) {
                etiquetaDescricao.value = et.descricao || '';
            }
            const cor = et.cor ? String(et.cor).trim() : '';
            if (etiquetaCor) {
                etiquetaCor.value = cor;
            }
            if (etiquetaCorPicker && /^#[0-9A-Fa-f]{6}$/i.test(cor)) {
                etiquetaCorPicker.value = cor;
            }
        })
        .catch(err => showAlertFn(err.message));
}

async function salvarEtiqueta() {
    if (!canManageEtiquetas()) {
        return;
    }
    const validacao = validateEtiquetaFormPayload({
        nome: etiquetaNome?.value,
        descricao: etiquetaDescricao?.value,
        cor: etiquetaCor?.value
    });
    if (!validacao.ok) {
        showAlertFn(validacao.message);
        return;
    }
    const editId = etiquetaEditId?.value?.trim();
    try {
        if (editId) {
            await etiquetaService.update(editId, validacao.payload);
            showAlertFn('Etiqueta atualizada com sucesso.', 'success');
        } else {
            await etiquetaService.create(validacao.payload);
            showAlertFn('Etiqueta cadastrada com sucesso.', 'success');
        }
        limparFormularioEtiqueta();
        await carregarListaEtiquetas();
    } catch (error) {
        showAlertFn(error.message);
    }
}

async function alterarStatusEtiqueta(idStr, ativar) {
    try {
        if (ativar) {
            await etiquetaService.activate(idStr);
            showAlertFn('Etiqueta ativada.', 'success');
        } else {
            await etiquetaService.deactivate(idStr);
            showAlertFn('Etiqueta inativada.', 'success');
        }
        await carregarListaEtiquetas();
    } catch (error) {
        showAlertFn(error.message);
    }
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function escapeAttr(text) {
    return escapeHtml(text).replace(/"/g, '&quot;');
}
