import { canManageConfiguracoes } from '../core/permissions.js';
import * as categoriaService from '../services/categoriaService.js';

let showAlertFn = () => {};
let displayValueFn = (v) => (v == null || v === '' ? '—' : String(v));

const motivoEditId = document.getElementById('motivoEditId');
const motivoSubgrupoSelect = document.getElementById('motivoSubgrupoSelect');
const motivoNome = document.getElementById('motivoNome');
const motivoDescricao = document.getElementById('motivoDescricao');
const motivoSalvarBtn = document.getElementById('motivoSalvarBtn');
const motivoLimparBtn = document.getElementById('motivoLimparBtn');
const motivoFiltroSubgrupo = document.getElementById('motivoFiltroSubgrupo');
const motivosTableBody = document.getElementById('motivosTableBody');
const motivosEmptyMessage = document.getElementById('motivosEmptyMessage');

let listenersBound = false;
let subgruposCache = [];

export function initMotivosConfigSection(deps = {}) {
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

    motivoSalvarBtn?.addEventListener('click', salvarMotivo);
    motivoLimparBtn?.addEventListener('click', limparFormularioMotivo);
    motivoFiltroSubgrupo?.addEventListener('change', carregarListaMotivos);
    motivosTableBody?.addEventListener('click', event => {
        const btn = event.target.closest('button[data-motivo-action]');
        if (!btn || !canManageConfiguracoes()) {
            return;
        }
        const id = btn.getAttribute('data-id');
        const action = btn.getAttribute('data-motivo-action');
        if (!id) {
            return;
        }
        if (action === 'edit') {
            editarMotivo(id);
        } else if (action === 'ativar') {
            alterarStatusMotivo(id, true);
        } else if (action === 'inativar') {
            alterarStatusMotivo(id, false);
        }
    });
}

export function applyMotivosConfigPermissions() {
    const allowed = canManageConfiguracoes();
    [motivoSalvarBtn, motivoLimparBtn, motivoNome, motivoDescricao, motivoSubgrupoSelect, motivoFiltroSubgrupo].forEach(
        el => {
            if (el) {
                el.disabled = !allowed;
            }
        }
    );
}

export async function loadMotivosConfigSection() {
    applyMotivosConfigPermissions();
    if (!canManageConfiguracoes()) {
        return;
    }
    await carregarSubgruposNosSelects();
    await carregarListaMotivos();
}

async function carregarSubgruposNosSelects() {
    try {
        subgruposCache = await categoriaService.listAllSubgrupos();
        const options = (Array.isArray(subgruposCache) ? subgruposCache : [])
            .map(s => {
                const label = s.grupoNome ? `${s.grupoNome} → ${s.nome}` : s.nome;
                return `<option value="${s.id}">${label}</option>`;
            })
            .join('');
        if (motivoSubgrupoSelect) {
            motivoSubgrupoSelect.innerHTML = `<option value="">Selecione</option>${options}`;
        }
        if (motivoFiltroSubgrupo) {
            motivoFiltroSubgrupo.innerHTML = `<option value="">Todas (ativas)</option>${options}`;
        }
    } catch (error) {
        showAlertFn(error.message);
    }
}

async function carregarListaMotivos() {
    if (!motivosTableBody) {
        return;
    }
    const filtro = motivoFiltroSubgrupo?.value || '';
    try {
        const lista = await categoriaService.listMotivos(filtro || undefined);
        const rows = Array.isArray(lista) ? lista : [];
        if (!rows.length) {
            motivosTableBody.innerHTML = '';
            motivosEmptyMessage?.classList.remove('hidden');
            return;
        }
        motivosEmptyMessage?.classList.add('hidden');
        motivosTableBody.innerHTML = rows
            .map(m => {
                const subLabel = m.grupoNome
                    ? `${displayValueFn(m.grupoNome)} / ${displayValueFn(m.subgrupoNome)}`
                    : displayValueFn(m.subgrupoNome);
                const status = m.ativo ? 'Ativo' : 'Inativo';
                const toggle = m.ativo
                    ? `<button type="button" class="button button-small button-secondary" data-motivo-action="inativar" data-id="${m.id}">Inativar</button>`
                    : `<button type="button" class="button button-small button-primary" data-motivo-action="ativar" data-id="${m.id}">Ativar</button>`;
                return `<tr>
                    <td>${subLabel}</td>
                    <td>${displayValueFn(m.nome)}</td>
                    <td>${status}</td>
                    <td>
                        <button type="button" class="button button-small button-secondary" data-motivo-action="edit" data-id="${m.id}">Editar</button>
                        ${toggle}
                    </td>
                </tr>`;
            })
            .join('');
    } catch (error) {
        showAlertFn(error.message);
    }
}

function limparFormularioMotivo() {
    if (motivoEditId) {
        motivoEditId.value = '';
    }
    if (motivoNome) {
        motivoNome.value = '';
    }
    if (motivoDescricao) {
        motivoDescricao.value = '';
    }
    if (motivoSubgrupoSelect) {
        motivoSubgrupoSelect.value = '';
    }
}

async function salvarMotivo() {
    const subgrupoId = motivoSubgrupoSelect?.value;
    const nome = motivoNome?.value?.trim();
    if (!subgrupoId || !nome) {
        showAlertFn('Subcategoria e nome são obrigatórios.');
        return;
    }
    const payload = {
        subgrupoId: Number(subgrupoId),
        nome,
        descricao: motivoDescricao?.value?.trim() || null
    };
    try {
        const id = motivoEditId?.value;
        if (id) {
            await categoriaService.updateMotivo(id, payload);
        } else {
            await categoriaService.createMotivo(payload);
        }
        limparFormularioMotivo();
        await carregarListaMotivos();
        showAlertFn('Motivo salvo.', 'success');
    } catch (error) {
        showAlertFn(error.message);
    }
}

async function editarMotivo(id) {
    try {
        const m = await categoriaService.getMotivo(id);
        if (!m) {
            return;
        }
        if (motivoEditId) {
            motivoEditId.value = String(m.id);
        }
        if (motivoSubgrupoSelect && m.subgrupoId) {
            motivoSubgrupoSelect.value = String(m.subgrupoId);
        }
        if (motivoNome) {
            motivoNome.value = m.nome || '';
        }
        if (motivoDescricao) {
            motivoDescricao.value = m.descricao || '';
        }
    } catch (error) {
        showAlertFn(error.message);
    }
}

async function alterarStatusMotivo(id, ativar) {
    try {
        if (ativar) {
            await categoriaService.ativarMotivo(id);
        } else {
            await categoriaService.inativarMotivo(id);
        }
        await carregarListaMotivos();
    } catch (error) {
        showAlertFn(error.message);
    }
}
