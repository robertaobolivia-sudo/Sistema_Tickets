import * as ticketService from '@features/tickets/ticket-service.js';
import { buildClassificarIndevidoPayload } from '@features/tickets/classificar-indevido-view.js';

const modal = document.getElementById('modalClassificarIndevido');
const form = document.getElementById('classificarIndevidoForm');
const numeroInput = document.getElementById('classificarIndevidoNumero');
const motivoSelect = document.getElementById('classificarIndevidoMotivo');
const comentarioInput = document.getElementById('classificarIndevidoComentario');
const confirmacaoCheck = document.getElementById('classificarIndevidoConfirmacao');
const alertBox = document.getElementById('alertBoxClassificarIndevido');
const btnFechar = document.getElementById('fecharModalClassificarIndevido');
const btnCancelar = document.getElementById('cancelarClassificarIndevidoBtn');
const btnConfirmar = document.getElementById('confirmarClassificarIndevidoBtn');

let showAlertFn = () => {};
let onSuccessFn = async () => {};
let listenersBound = false;

function showAlert(message, isError = false) {
    if (!alertBox) {
        return;
    }
    alertBox.textContent = message;
    alertBox.classList.remove('hidden', 'alert-success', 'alert-error');
    alertBox.classList.add(isError ? 'alert-error' : 'alert-success');
}

function hideAlert() {
    alertBox?.classList.add('hidden');
}

function openModal() {
    modal?.classList.add('ativo');
    document.body.classList.add('modal-open');
}

function closeModal() {
    modal?.classList.remove('ativo');
    document.body.classList.remove('modal-open');
    hideAlert();
}

export function openClassificarIndevidoModal(numeroTicket, { onSuccess } = {}) {
    if (!modal || !numeroTicket) {
        return;
    }
    if (typeof onSuccess === 'function') {
        onSuccessFn = onSuccess;
    }
    if (numeroInput) {
        numeroInput.value = numeroTicket;
    }
    if (motivoSelect) {
        motivoSelect.value = 'INDEVIDO';
    }
    if (comentarioInput) {
        comentarioInput.value = '';
    }
    if (confirmacaoCheck) {
        confirmacaoCheck.checked = false;
    }
    hideAlert();
    openModal();
}

async function submitForm(event) {
    event.preventDefault();
    hideAlert();
    const numero = numeroInput?.value?.trim();
    if (!numero) {
        showAlert('Ticket não informado.', true);
        return;
    }
    try {
        const payload = buildClassificarIndevidoPayload({
            confirmacao: confirmacaoCheck?.checked === true,
            motivoOperacional: motivoSelect?.value,
            comentario: comentarioInput?.value
        });
        if (btnConfirmar) {
            btnConfirmar.disabled = true;
        }
        const atualizado = await ticketService.classificarTicketIndevido(numero, payload);
        closeModal();
        await onSuccessFn(atualizado, numero);
    } catch (error) {
        const msg =
            error && typeof error.message === 'string'
                ? error.message
                : 'Não foi possível classificar o ticket.';
        showAlert(msg, true);
    } finally {
        if (btnConfirmar) {
            btnConfirmar.disabled = false;
        }
    }
}

function bindListeners() {
    if (listenersBound) {
        return;
    }
    listenersBound = true;
    form?.addEventListener('submit', submitForm);
    btnFechar?.addEventListener('click', closeModal);
    btnCancelar?.addEventListener('click', closeModal);
    modal?.addEventListener('click', event => {
        if (event.target === modal) {
            closeModal();
        }
    });
}

export function initClassificarIndevidoModal(deps = {}) {
    if (deps.showAlert) {
        showAlertFn = deps.showAlert;
    }
    bindListeners();
}
