import * as avaliacaoPublicaService from '@features/satisfacao/avaliacao-publica-service.js';
import { resolveAvaliacaoPublicaUiState } from '@features/satisfacao/avaliacao-publica-view.js';

const screen = document.getElementById('avaliacaoPublicaScreen');
const subtitulo = document.getElementById('avaliacaoPublicaSubtitulo');
const clienteEl = document.getElementById('avaliacaoPublicaCliente');
const protocoloEl = document.getElementById('avaliacaoPublicaProtocolo');
const form = document.getElementById('avaliacaoPublicaForm');
const estadoEl = document.getElementById('avaliacaoPublicaEstado');
const alertEl = document.getElementById('avaliacaoPublicaAlert');
const comentarioInput = document.getElementById('avaliacaoPublicaComentario');
const enviarBtn = document.getElementById('avaliacaoPublicaEnviar');
const notaButtons = document.querySelectorAll('.avaliacao-nota-btn');

let tokenAtual = null;
let notaSelecionada = null;

function showAlert(msg, type = 'error') {
    if (!alertEl) return;
    alertEl.textContent = msg;
    alertEl.className = `alert alert-${type}`;
    alertEl.classList.remove('hidden');
}

function clearAlert() {
    alertEl?.classList.add('hidden');
}

function renderUi(dados) {
    const ui = resolveAvaliacaoPublicaUiState(dados);
    subtitulo.textContent = dados?.mensagemOrientativa || ui.subtituloPadrao;
    if (dados?.clienteNome) {
        clienteEl.textContent = dados.clienteNome;
        clienteEl.classList.remove('hidden');
    } else {
        clienteEl.classList.add('hidden');
    }
    if (dados?.protocoloMascarado) {
        protocoloEl.textContent = dados.protocoloMascarado;
        protocoloEl.classList.remove('hidden');
    } else {
        protocoloEl.classList.add('hidden');
    }
    form?.classList.toggle('hidden', !ui.mostrarFormulario);
    estadoEl?.classList.toggle('hidden', !ui.mostrarEstadoFinal);
    if (ui.mostrarEstadoFinal && estadoEl) {
        estadoEl.textContent = ui.textoEstadoFinal;
    }
    if (estadoEl) {
        estadoEl.classList.remove(
            'avaliacao-publica-estado--pendente',
            'avaliacao-publica-estado--respondida',
            'avaliacao-publica-estado--expirada',
            'avaliacao-publica-estado--invalida'
        );
        const variant = ui.estadoVariant || 'invalida';
        estadoEl.classList.add('avaliacao-publica-estado', `avaliacao-publica-estado--${variant}`);
    }
}

async function carregar() {
    clearAlert();
    notaSelecionada = null;
    notaButtons.forEach(btn => btn.classList.remove('is-selected'));
    try {
        const dados = await avaliacaoPublicaService.fetchAvaliacaoPublica(tokenAtual);
        renderUi(dados);
    } catch (error) {
        form?.classList.add('hidden');
        estadoEl?.classList.remove('hidden');
        if (estadoEl) {
            estadoEl.textContent = 'Link inválido ou indisponível.';
            estadoEl.classList.remove(
                'avaliacao-publica-estado--pendente',
                'avaliacao-publica-estado--respondida',
                'avaliacao-publica-estado--expirada'
            );
            estadoEl.classList.add('avaliacao-publica-estado', 'avaliacao-publica-estado--invalida');
        }
        showAlert(error.message);
    }
}

async function enviar() {
    clearAlert();
    if (!notaSelecionada) {
        showAlert('Selecione uma nota de 1 a 5.');
        return;
    }
    enviarBtn.disabled = true;
    try {
        const dados = await avaliacaoPublicaService.responderAvaliacaoPublica(tokenAtual, {
            nota: notaSelecionada,
            comentario: comentarioInput?.value?.trim() || null
        });
        renderUi(dados);
        clearAlert();
    } catch (error) {
        showAlert(error.message);
        await carregar();
    } finally {
        enviarBtn.disabled = false;
    }
}

export function initAvaliacaoPublicaPage(token) {
    if (!token || !screen) {
        return false;
    }
    tokenAtual = token;
    notaButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            notaSelecionada = Number(btn.dataset.nota);
            notaButtons.forEach(b => b.classList.remove('is-selected'));
            btn.classList.add('is-selected');
        });
    });
    enviarBtn?.addEventListener('click', enviar);
    document.getElementById('loginScreen')?.classList.remove('screen-active');
    document.getElementById('appScreen')?.classList.remove('screen-active');
    screen.classList.add('screen-active');
    carregar();
    return true;
}
