import * as contatoService from '@features/contatos/contato-service.js';
import * as contatoTelefoneService from '@features/contatos/contato-telefone-service.js';
import * as contatoEtiquetaService from '@features/contatos/contato-etiqueta-service.js';
import * as etiquetaService from '@features/configuracoes/etiqueta-service.js';
import {
    atualizarAvisoEtiquetaOperacional,
    buildContatoUpdatePayload,
    collectEtiquetaIdsFromContainer,
    renderEtiquetasCheckboxList
} from '@features/contatos/contato-gestao-edit-view.js';
import {
    mapOrigemSelectParaApi,
    renderListaTelefonesAdicionais,
    validarTelefoneAdicionalInformado
} from '@features/contatos/contato-gestao-telefones-view.js';
import { mensagemParaExibirUsuario } from '@shared/ui/messages.js';

const modal = document.getElementById('modalContatoGestaoEdit');
const fecharBtn = document.getElementById('fecharModalContatoGestaoEdit');
const cancelarBtn = document.getElementById('contatoGestaoEditCancelar');
const salvarBtn = document.getElementById('contatoGestaoEditSalvar');
const alertEl = document.getElementById('contatoGestaoEditAlert');

const fieldId = document.getElementById('contatoGestaoEditId');
const fieldCliente = document.getElementById('contatoGestaoEditCliente');
const fieldWhatsapp = document.getElementById('contatoGestaoEditWhatsapp');
const fieldNome = document.getElementById('contatoGestaoEditNome');
const fieldEmail = document.getElementById('contatoGestaoEditEmail');
const fieldEmpresa = document.getElementById('contatoGestaoEditEmpresaLocal');
const fieldCidade = document.getElementById('contatoGestaoEditCidade');
const fieldUf = document.getElementById('contatoGestaoEditUf');
const fieldObs = document.getElementById('contatoGestaoEditObservacoes');
const etiquetasBox = document.getElementById('contatoGestaoEditEtiquetas');
const avisoOperacionalEl = document.getElementById('contatoGestaoEditOperacionalAviso');
const telefonesListaEl = document.getElementById('contatoGestaoEditTelefonesLista');
const telefonesEmptyEl = document.getElementById('contatoGestaoEditTelefonesEmpty');
const telefonesAlertEl = document.getElementById('contatoGestaoEditTelefonesAlert');
const telefoneNovoInput = document.getElementById('contatoGestaoEditTelefoneNovo');
const telefoneOrigemSelect = document.getElementById('contatoGestaoEditTelefoneOrigem');
const telefoneIncluirBtn = document.getElementById('contatoGestaoEditTelefoneIncluir');

let showAlertFn = () => {};
let etiquetasAtivasModal = [];
let onSavedFn = () => {};
let listenersBound = false;
let saving = false;
let incluindoTelefone = false;

function showModalAlert(message, isError = true) {
    if (!alertEl) {
        if (isError) {
            showAlertFn(message);
        }
        return;
    }
    alertEl.textContent = message;
    alertEl.classList.toggle('hidden', !message);
    alertEl.classList.toggle('alert-error', isError);
    alertEl.classList.toggle('alert-success', !isError);
}

function clearModalAlert() {
    showModalAlert('', false);
    alertEl?.classList.add('hidden');
}

function fecharModal() {
    modal?.classList.remove('ativo');
    clearModalAlert();
    clearTelefonesAlert();
    if (telefoneNovoInput) {
        telefoneNovoInput.value = '';
    }
}

function showTelefonesAlert(message, isError = true) {
    if (!telefonesAlertEl) {
        if (isError && message) {
            showAlertFn(message);
        }
        return;
    }
    telefonesAlertEl.textContent = message;
    telefonesAlertEl.classList.toggle('hidden', !message);
    telefonesAlertEl.classList.toggle('alert-error', isError);
    telefonesAlertEl.classList.toggle('alert-success', !isError);
}

function clearTelefonesAlert() {
    showTelefonesAlert('', false);
    telefonesAlertEl?.classList.add('hidden');
}

async function carregarTelefonesAdicionais(contatoId) {
    if (!contatoId) {
        renderListaTelefonesAdicionais(telefonesListaEl, telefonesEmptyEl, []);
        return;
    }
    const itens = await contatoTelefoneService.listarTelefonesAdicionais(contatoId);
    renderListaTelefonesAdicionais(telefonesListaEl, telefonesEmptyEl, itens);
}

async function incluirTelefoneAdicional() {
    if (incluindoTelefone || !fieldId?.value) {
        return;
    }
    clearTelefonesAlert();
    try {
        validarTelefoneAdicionalInformado(telefoneNovoInput?.value);
        const contatoId = Number(fieldId.value);
        const origem = mapOrigemSelectParaApi(telefoneOrigemSelect?.value);
        const payload = { telefone: String(telefoneNovoInput.value).trim() };
        if (origem) {
            payload.origem = origem;
        }
        incluindoTelefone = true;
        if (telefoneIncluirBtn) {
            telefoneIncluirBtn.disabled = true;
        }
        await contatoTelefoneService.adicionarTelefoneAdicional(contatoId, payload);
        if (telefoneNovoInput) {
            telefoneNovoInput.value = '';
        }
        await carregarTelefonesAdicionais(contatoId);
        showTelefonesAlert('Telefone adicional incluído.', false);
    } catch (error) {
        showTelefonesAlert(mensagemParaExibirUsuario(error?.message || error));
    } finally {
        incluindoTelefone = false;
        if (telefoneIncluirBtn) {
            telefoneIncluirBtn.disabled = false;
        }
    }
}

function preencherFormulario(contato, clienteRotulo, etiquetas, etiquetasVinculadas) {
    if (fieldId) {
        fieldId.value = contato?.id != null ? String(contato.id) : '';
    }
    if (fieldCliente) {
        fieldCliente.value = clienteRotulo || contato?.clienteNome || '—';
    }
    if (fieldWhatsapp) {
        fieldWhatsapp.value = contato?.whatsapp || '—';
    }
    if (fieldNome) {
        fieldNome.value = contato?.nome || '';
    }
    if (fieldEmail) {
        fieldEmail.value = contato?.email || '';
    }
    if (fieldEmpresa) {
        fieldEmpresa.value = contato?.empresaLocal || '';
    }
    if (fieldCidade) {
        fieldCidade.value = contato?.cidade || '';
    }
    if (fieldUf) {
        fieldUf.value = contato?.uf || '';
    }
    if (fieldObs) {
        fieldObs.value = contato?.observacoes || '';
    }
    etiquetasAtivasModal = Array.isArray(etiquetas) ? etiquetas : [];
    const ids = (etiquetasVinculadas || []).map(e => e.id);
    renderEtiquetasCheckboxList(etiquetasBox, etiquetasAtivasModal, ids);
    atualizarAvisoEtiquetaOperacional(avisoOperacionalEl, etiquetasAtivasModal, ids);
    if (etiquetasBox && !etiquetasBox.dataset.operacionalListen) {
        etiquetasBox.dataset.operacionalListen = '1';
        etiquetasBox.addEventListener('change', () => {
            atualizarAvisoEtiquetaOperacional(
                avisoOperacionalEl,
                etiquetasAtivasModal,
                collectEtiquetaIdsFromContainer(etiquetasBox)
            );
        });
    }
}

export async function abrirContatoGestaoEditModal(contatoResumo) {
    if (!modal || !contatoResumo?.id) {
        return;
    }
    clearModalAlert();
    modal.classList.add('ativo');
    if (salvarBtn) {
        salvarBtn.disabled = true;
    }
    try {
        const [contato, etiquetasAtivas, etiquetasVinculadas] = await Promise.all([
            contatoService.buscarPorId(contatoResumo.id),
            etiquetaService.listActive(),
            contatoEtiquetaService.listarEtiquetasContato(contatoResumo.id)
        ]);
        const rotulo =
            contatoResumo.clienteRazaoSocial || contato?.clienteNome || `Cliente #${contato?.clienteId ?? ''}`;
        preencherFormulario(contato, rotulo, etiquetasAtivas, etiquetasVinculadas);
        clearTelefonesAlert();
        await carregarTelefonesAdicionais(contatoResumo.id);
    } catch (error) {
        showModalAlert(mensagemParaExibirUsuario(error?.message || error));
        fecharModal();
        return;
    } finally {
        if (salvarBtn) {
            salvarBtn.disabled = false;
        }
    }
}

async function salvarContatoGestao() {
    if (saving || !fieldId?.value) {
        return;
    }
    saving = true;
    if (salvarBtn) {
        salvarBtn.disabled = true;
    }
    clearModalAlert();
    try {
        const contatoId = Number(fieldId.value);
        const payload = buildContatoUpdatePayload({
            nome: fieldNome?.value,
            email: fieldEmail?.value,
            empresaLocal: fieldEmpresa?.value,
            cidade: fieldCidade?.value,
            uf: fieldUf?.value,
            observacoes: fieldObs?.value,
            whatsapp: fieldWhatsapp?.value
        });
        await contatoService.atualizar(contatoId, payload);
        const etiquetaIds = collectEtiquetaIdsFromContainer(etiquetasBox);
        await contatoEtiquetaService.salvarEtiquetasContato(contatoId, etiquetaIds);
        showModalAlert('Contato atualizado com sucesso.', false);
        showAlertFn('Contato atualizado com sucesso.');
        onSavedFn();
        window.setTimeout(() => fecharModal(), 400);
    } catch (error) {
        showModalAlert(mensagemParaExibirUsuario(error?.message || error));
    } finally {
        saving = false;
        if (salvarBtn) {
            salvarBtn.disabled = false;
        }
    }
}

export function initContatoGestaoEditModal(deps = {}) {
    if (deps.showAlert) {
        showAlertFn = deps.showAlert;
    }
    if (deps.onSaved) {
        onSavedFn = deps.onSaved;
    }
    if (listenersBound) {
        return;
    }
    listenersBound = true;
    fecharBtn?.addEventListener('click', fecharModal);
    cancelarBtn?.addEventListener('click', fecharModal);
    salvarBtn?.addEventListener('click', () => salvarContatoGestao());
    telefoneIncluirBtn?.addEventListener('click', () => incluirTelefoneAdicional());
    telefoneNovoInput?.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            void incluirTelefoneAdicional();
        }
    });
    modal?.addEventListener('click', event => {
        if (event.target === modal) {
            fecharModal();
        }
    });
}
