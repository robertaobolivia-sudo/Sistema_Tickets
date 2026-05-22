import { MSG_ERRO } from '../core/messages.js';
import { getLoggedAnalyst } from '../core/state.js';
import { setLoggedAnalyst, refreshLoggedAnalystFromServer } from '../core/auth.js';
import { resolvePerfilAcessoCode, renderPerfilAcessoBadge } from '../core/permissions.js';
import * as analistaService from '../services/analistaService.js';

const pagePerfil = document.getElementById('page-perfil');
const alertBoxPerfil = document.getElementById('alertBoxPerfil');
const perfilFotoInput = document.getElementById('perfilFotoInput');
const perfilFoto = document.getElementById('perfilFoto');
const perfilFotoWrapper = document.getElementById('perfilFotoWrapper');
const perfilFotoMenu = document.getElementById('perfilFotoMenu');
const perfilMostrarFotoBtn = document.getElementById('perfilMostrarFotoBtn');
const perfilCarregarFotoBtn = document.getElementById('perfilCarregarFotoBtn');
const perfilRemoverFotoBtn = document.getElementById('perfilRemoverFotoBtn');
const modalFotoVisualizar = document.getElementById('modalFotoVisualizar');
const fecharModalFotoVisualizar = document.getElementById('fecharModalFotoVisualizar');
const perfilFotoVisualizacao = document.getElementById('perfilFotoVisualizacao');
const modalFotoAjuste = document.getElementById('modalFotoAjuste');
const fecharModalFotoAjuste = document.getElementById('fecharModalFotoAjuste');
const perfilFotoAjusteImagem = document.getElementById('perfilFotoAjusteImagem');
const profileAdjustStage = document.getElementById('profileAdjustStage');
const profileAdjustImageFrame = document.getElementById('profileAdjustImageFrame');
const profileAdjustShade = document.getElementById('profileAdjustShade');
const profileAdjustCropRing = document.getElementById('profileAdjustCropRing');
const perfilZoomMais = document.getElementById('perfilZoomMais');
const perfilZoomMenos = document.getElementById('perfilZoomMenos');
const perfilFotoConfirmarTopo = document.getElementById('perfilFotoConfirmarTopo');
const perfilFotoConfirmarCheck = document.getElementById('perfilFotoConfirmarCheck');
const perfilNome = document.getElementById('perfilNome');
const perfilStatus = document.getElementById('perfilStatus');
const perfilFields = {
    nomeCompleto: document.getElementById('perfilNomeCompleto'),
    cpf: document.getElementById('perfilCpf'),
    dataNascimento: document.getElementById('perfilDataNascimento'),
    cep: document.getElementById('perfilCep'),
    rua: document.getElementById('perfilRua'),
    numero: document.getElementById('perfilNumero'),
    bairro: document.getElementById('perfilBairro'),
    cidade: document.getElementById('perfilCidade'),
    estado: document.getElementById('perfilEstado'),
    uf: document.getElementById('perfilUf'),
    pais: document.getElementById('perfilPais'),
    celular: document.getElementById('perfilCelular'),
    email: document.getElementById('perfilEmail'),
    nivel: document.getElementById('perfilNivel'),
    perfilAcesso: document.getElementById('perfilAcesso')
};

let showAlertFn = () => {};
let clearAlertFn = () => {};
let displayValueFn = (v) => (v == null ? '-' : String(v));
let formatDateFn = () => '-';
let setAnalystAvatarElementFn = () => {};
let getAnalystDisplayNameFn = () => '-';
let renderStatusOperadorFn = () => '-';
let loadAnalistasKanbanFn = async () => {};

let pendingProfilePhotoFile = null;
let pendingProfilePhotoUrl = null;
let profilePhotoZoom = 1;
let profileCropCenter = { x: 0, y: 0 };
let profilePhotoDrag = null;
let profilePhotoEditor = null;
const PROFILE_CROP_TARGET_DIAMETER = 240;
const PROFILE_CROP_OUTPUT_SIZE = 512;
const PROFILE_ZOOM_MIN = 1;
const PROFILE_ZOOM_MAX = 3;
const PROFILE_ZOOM_STEP = 0.12;
let listenersBound = false;

function showAlert(message, box, type) {
    showAlertFn(message, box, type);
}

function displayValue(v) {
    return displayValueFn(v);
}

function formatDate(v) {
    return formatDateFn(v);
}

export function applyPerfilFotoWrapperState(hasPhoto) {
    perfilFotoWrapper?.classList.toggle('has-profile-photo', Boolean(hasPhoto));
}

export function renderPerfil() {
    if (!pagePerfil) return;
    const analyst = getLoggedAnalyst() || {};
    setAnalystAvatarElementFn(perfilFoto, analyst);
    if (perfilNome) perfilNome.textContent = getAnalystDisplayNameFn(analyst);
    if (perfilStatus) perfilStatus.innerHTML = renderStatusOperadorFn(analyst.statusOperador);

    if (perfilFields.nomeCompleto) perfilFields.nomeCompleto.textContent = displayValue(analyst.nomeCompleto || analyst.nome);
    if (perfilFields.cpf) perfilFields.cpf.textContent = displayValue(analyst.cpf);
    if (perfilFields.dataNascimento) perfilFields.dataNascimento.textContent = formatDate(analyst.dataNascimento);
    if (perfilFields.cep) perfilFields.cep.textContent = displayValue(analyst.cep);
    if (perfilFields.rua) perfilFields.rua.textContent = displayValue(analyst.rua);
    if (perfilFields.numero) perfilFields.numero.textContent = displayValue(analyst.numero);
    if (perfilFields.bairro) perfilFields.bairro.textContent = displayValue(analyst.bairro);
    if (perfilFields.cidade) perfilFields.cidade.textContent = displayValue(analyst.cidade);
    if (perfilFields.estado) perfilFields.estado.textContent = displayValue(analyst.estado);
    if (perfilFields.uf) perfilFields.uf.textContent = displayValue(analyst.uf);
    if (perfilFields.pais) perfilFields.pais.textContent = displayValue(analyst.pais);
    if (perfilFields.celular) perfilFields.celular.textContent = displayValue(analyst.celular);
    if (perfilFields.email) perfilFields.email.textContent = displayValue(analyst.email);
    if (perfilFields.nivel) perfilFields.nivel.textContent = displayValue(analyst.nivel);
    if (perfilFields.perfilAcesso) {
        perfilFields.perfilAcesso.innerHTML = renderPerfilAcessoBadge(resolvePerfilAcessoCode(analyst));
    }
}

function toggleProfilePhotoMenu(forceOpen = null) {
    if (!perfilFotoMenu) return;
    const shouldOpen = forceOpen === null
        ? perfilFotoMenu.classList.contains('hidden')
        : forceOpen;
    perfilFotoMenu.classList.toggle('hidden', !shouldOpen);
}

function closeProfilePhotoMenu() {
    toggleProfilePhotoMenu(false);
}

function openProfilePhotoView() {
    closeProfilePhotoMenu();
    if (!getLoggedAnalyst()?.fotoUrl) {
        alert('Nenhuma foto cadastrada.');
        return;
    }
    if (perfilFotoVisualizacao) {
        perfilFotoVisualizacao.src = getLoggedAnalyst().fotoUrl;
    }
    modalFotoVisualizar?.classList.add('ativo');
}

function closeProfilePhotoView() {
    modalFotoVisualizar?.classList.remove('ativo');
    if (perfilFotoVisualizacao) {
        perfilFotoVisualizacao.src = '';
    }
}

function getProfileAdjustStageSize() {
    const rect = profileAdjustStage?.getBoundingClientRect();
    return {
        width: rect?.width || 440,
        height: rect?.height || 440
    };
}

function computeProfilePhotoLayout() {
    if (!profilePhotoEditor || !perfilFotoAjusteImagem) {
        return null;
    }
    const { naturalWidth, naturalHeight } = profilePhotoEditor;
    const { width: stageWidth, height: stageHeight } = getProfileAdjustStageSize();
    const containScale = Math.min(stageWidth / naturalWidth, stageHeight / naturalHeight);
    const displayScale = containScale * profilePhotoZoom;
    const displayWidth = naturalWidth * displayScale;
    const displayHeight = naturalHeight * displayScale;
    const imageLeft = (stageWidth - displayWidth) / 2;
    const imageTop = (stageHeight - displayHeight) / 2;
    const cropDiameter = Math.min(PROFILE_CROP_TARGET_DIAMETER, displayWidth, displayHeight);
    return {
        stageWidth,
        stageHeight,
        naturalWidth,
        naturalHeight,
        displayScale,
        displayWidth,
        displayHeight,
        imageLeft,
        imageTop,
        cropDiameter
    };
}

function clampProfileCropCenter(layout) {
    const radius = layout.cropDiameter / 2;
    profileCropCenter.x = Math.min(
        layout.imageLeft + layout.displayWidth - radius,
        Math.max(layout.imageLeft + radius, profileCropCenter.x)
    );
    profileCropCenter.y = Math.min(
        layout.imageTop + layout.displayHeight - radius,
        Math.max(layout.imageTop + radius, profileCropCenter.y)
    );
}

function applyProfilePhotoEditorLayout() {
    const layout = computeProfilePhotoLayout();
    if (!layout || !profileAdjustImageFrame || !profileAdjustCropRing || !profileAdjustShade) {
        return;
    }

    clampProfileCropCenter(layout);
    profilePhotoEditor.cropDiameter = layout.cropDiameter;

    profileAdjustImageFrame.style.left = `${layout.imageLeft}px`;
    profileAdjustImageFrame.style.top = `${layout.imageTop}px`;
    profileAdjustImageFrame.style.width = `${layout.displayWidth}px`;
    profileAdjustImageFrame.style.height = `${layout.displayHeight}px`;

    const radius = layout.cropDiameter / 2;
    profileAdjustCropRing.style.width = `${layout.cropDiameter}px`;
    profileAdjustCropRing.style.height = `${layout.cropDiameter}px`;
    profileAdjustCropRing.style.left = `${profileCropCenter.x - radius}px`;
    profileAdjustCropRing.style.top = `${profileCropCenter.y - radius}px`;

    const localCenterX = profileCropCenter.x - layout.imageLeft;
    const localCenterY = profileCropCenter.y - layout.imageTop;
    const maskValue = `radial-gradient(circle ${radius}px at ${localCenterX}px ${localCenterY}px, transparent ${radius - 1}px, black ${radius}px)`;
    profileAdjustShade.style.webkitMaskImage = maskValue;
    profileAdjustShade.style.maskImage = maskValue;
}

function initProfilePhotoEditor() {
    if (!perfilFotoAjusteImagem || !perfilFotoAjusteImagem.naturalWidth) {
        return;
    }
    profilePhotoEditor = {
        naturalWidth: perfilFotoAjusteImagem.naturalWidth,
        naturalHeight: perfilFotoAjusteImagem.naturalHeight
    };
    profilePhotoZoom = PROFILE_ZOOM_MIN;
    const layout = computeProfilePhotoLayout();
    if (!layout) {
        return;
    }
    profileCropCenter = {
        x: layout.imageLeft + layout.displayWidth / 2,
        y: layout.imageTop + layout.displayHeight / 2
    };
    applyProfilePhotoEditorLayout();
}

function openProfilePhotoAdjust(file) {
    if (!file) return;
    if (pendingProfilePhotoUrl) {
        URL.revokeObjectURL(pendingProfilePhotoUrl);
    }
    pendingProfilePhotoFile = file;
    pendingProfilePhotoUrl = URL.createObjectURL(file);
    profilePhotoEditor = null;
    if (perfilFotoAjusteImagem) {
        perfilFotoAjusteImagem.onload = () => initProfilePhotoEditor();
        perfilFotoAjusteImagem.src = pendingProfilePhotoUrl;
        if (perfilFotoAjusteImagem.complete) {
            initProfilePhotoEditor();
        }
    }
    modalFotoAjuste?.classList.add('ativo');
    requestAnimationFrame(() => {
        requestAnimationFrame(() => initProfilePhotoEditor());
    });
}

function closeProfilePhotoAdjust() {
    modalFotoAjuste?.classList.remove('ativo');
    pendingProfilePhotoFile = null;
    profilePhotoEditor = null;
    if (pendingProfilePhotoUrl) {
        URL.revokeObjectURL(pendingProfilePhotoUrl);
        pendingProfilePhotoUrl = null;
    }
    if (perfilFotoAjusteImagem) {
        perfilFotoAjusteImagem.onload = null;
        perfilFotoAjusteImagem.src = '';
    }
    if (profileAdjustImageFrame) {
        profileAdjustImageFrame.style.left = '';
        profileAdjustImageFrame.style.top = '';
        profileAdjustImageFrame.style.width = '';
        profileAdjustImageFrame.style.height = '';
    }
    if (profileAdjustShade) {
        profileAdjustShade.style.webkitMaskImage = '';
        profileAdjustShade.style.maskImage = '';
    }
    if (perfilFotoInput) {
        perfilFotoInput.value = '';
    }
    profilePhotoDrag = null;
}

function changeProfilePhotoZoom(delta) {
    if (!profilePhotoEditor) return;
    const previousZoom = profilePhotoZoom;
    const nextZoom = delta > 0
        ? Math.min(PROFILE_ZOOM_MAX, previousZoom + PROFILE_ZOOM_STEP)
        : Math.max(PROFILE_ZOOM_MIN, previousZoom - PROFILE_ZOOM_STEP);
    if (nextZoom === previousZoom) {
        return;
    }
    const layoutBefore = computeProfilePhotoLayout();
    if (!layoutBefore) {
        return;
    }
    const centerRatioX = (profileCropCenter.x - layoutBefore.imageLeft) / layoutBefore.displayWidth;
    const centerRatioY = (profileCropCenter.y - layoutBefore.imageTop) / layoutBefore.displayHeight;
    profilePhotoZoom = nextZoom;
    const layoutAfter = computeProfilePhotoLayout();
    if (!layoutAfter) {
        return;
    }
    profileCropCenter = {
        x: layoutAfter.imageLeft + layoutAfter.displayWidth * centerRatioX,
        y: layoutAfter.imageTop + layoutAfter.displayHeight * centerRatioY
    };
    applyProfilePhotoEditorLayout();
}

function startProfilePhotoDrag(event) {
    if (!profileAdjustCropRing || !pendingProfilePhotoFile || !profilePhotoEditor) return;
    if (event.target !== profileAdjustCropRing && !profileAdjustCropRing.contains(event.target)) {
        return;
    }
    profilePhotoDrag = {
        pointerId: event.pointerId,
        startX: event.clientX,
        startY: event.clientY,
        originX: profileCropCenter.x,
        originY: profileCropCenter.y
    };
    profileAdjustCropRing.classList.add('dragging');
    profileAdjustCropRing.setPointerCapture(event.pointerId);
}

function moveProfilePhotoDrag(event) {
    if (!profilePhotoDrag) return;
    profileCropCenter = {
        x: profilePhotoDrag.originX + event.clientX - profilePhotoDrag.startX,
        y: profilePhotoDrag.originY + event.clientY - profilePhotoDrag.startY
    };
    applyProfilePhotoEditorLayout();
}

function endProfilePhotoDrag(event) {
    if (!profilePhotoDrag) return;
    profileAdjustCropRing?.classList.remove('dragging');
    if (profileAdjustCropRing?.hasPointerCapture(profilePhotoDrag.pointerId)) {
        profileAdjustCropRing.releasePointerCapture(profilePhotoDrag.pointerId);
    }
    profilePhotoDrag = null;
}

function getProfileCropSourceRect(layout) {
    const radius = layout.cropDiameter / 2;
    const circleLeft = profileCropCenter.x - radius;
    const circleTop = profileCropCenter.y - radius;
    const scaleX = layout.naturalWidth / layout.displayWidth;
    const scaleY = layout.naturalHeight / layout.displayHeight;
    const cropSizeX = layout.cropDiameter * scaleX;
    const cropSizeY = layout.cropDiameter * scaleY;
    return {
        cropX: (circleLeft - layout.imageLeft) * scaleX,
        cropY: (circleTop - layout.imageTop) * scaleY,
        cropSizeX,
        cropSizeY
    };
}

function buildCroppedProfilePhotoFile() {
    return new Promise((resolve, reject) => {
        if (!perfilFotoAjusteImagem || !profilePhotoEditor || !pendingProfilePhotoFile) {
            reject(new Error('Imagem de perfil indisponível para recorte.'));
            return;
        }

        applyProfilePhotoEditorLayout();
        const layout = computeProfilePhotoLayout();
        if (!layout) {
            reject(new Error('Não foi possível calcular o recorte da foto.'));
            return;
        }

        const { cropX, cropY, cropSizeX, cropSizeY } = getProfileCropSourceRect(layout);
        const outputSize = PROFILE_CROP_OUTPUT_SIZE;
        const canvas = document.createElement('canvas');
        canvas.width = outputSize;
        canvas.height = outputSize;
        const context = canvas.getContext('2d');
        if (!context) {
            reject(new Error('Não foi possível preparar o recorte da foto.'));
            return;
        }

        context.drawImage(
            perfilFotoAjusteImagem,
            cropX,
            cropY,
            cropSizeX,
            cropSizeY,
            0,
            0,
            outputSize,
            outputSize
        );

        const outputType = pendingProfilePhotoFile.type === 'image/png' ? 'image/png' : 'image/jpeg';
        canvas.toBlob(blob => {
            if (!blob) {
                reject(new Error('Não foi possível gerar a foto recortada.'));
                return;
            }
            const extension = outputType === 'image/png' ? '.png' : '.jpg';
            const fileName = pendingProfilePhotoFile.name.replace(/\.[^.]+$/, extension) || `perfil${extension}`;
            resolve(new File([blob], fileName, { type: outputType }));
        }, outputType, 0.92);
    });
}

async function confirmProfilePhotoUpload() {
    if (!getLoggedAnalyst()?.id) {
        showAlert(MSG_ERRO.SESSAO_EXPIRADA, alertBoxPerfil);
        return;
    }
    if (!pendingProfilePhotoFile) {
        showAlert('Selecione uma imagem PNG, JPG ou JPEG.', alertBoxPerfil);
        return;
    }
    try {
        const croppedFile = await buildCroppedProfilePhotoFile();
        const analista = await analistaService.uploadPhoto(getLoggedAnalyst().id, croppedFile);
        setLoggedAnalyst(analista);
        closeProfilePhotoAdjust();
        showAlert('Foto de perfil atualizada com sucesso.', alertBoxPerfil, 'success');
        await loadAnalistasKanbanFn();
    } catch (error) {
        showAlert(error.message, alertBoxPerfil);
    }
}

async function removeProfilePhoto() {
    closeProfilePhotoMenu();
    if (!getLoggedAnalyst()?.id) {
        showAlert(MSG_ERRO.SESSAO_EXPIRADA, alertBoxPerfil);
        return;
    }
    try {
        const analista = await analistaService.removePhoto(getLoggedAnalyst().id);
        setLoggedAnalyst(analista);
        showAlert('Foto de perfil removida com sucesso.', alertBoxPerfil, 'success');
        await loadAnalistasKanbanFn();
    } catch (error) {
        showAlert(error.message, alertBoxPerfil);
    }
}

function handleProfilePhotoFileChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
    if (!allowedTypes.includes(file.type)) {
        showAlert('Selecione uma imagem PNG, JPG ou JPEG.', alertBoxPerfil);
        event.target.value = '';
        return;
    }
    closeProfilePhotoMenu();
    openProfilePhotoAdjust(file);
}

export function closePerfilOverlays() {
    closeProfilePhotoMenu();
    closeProfilePhotoView();
    closeProfilePhotoAdjust();
}

export function loadPerfilPage() {
    return refreshLoggedAnalystFromServer()
        .then(() => renderPerfil())
        .catch(() => renderPerfil());
}

function openProfilePhotoMenuFromAvatar(event) {
    event?.stopPropagation();
    toggleProfilePhotoMenu(true);
}

export function initPerfilPage(deps = {}) {
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.clearAlert) clearAlertFn = deps.clearAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDate) formatDateFn = deps.formatDate;
    if (deps.setAnalystAvatarElement) setAnalystAvatarElementFn = deps.setAnalystAvatarElement;
    if (deps.getAnalystDisplayName) getAnalystDisplayNameFn = deps.getAnalystDisplayName;
    if (deps.renderStatusOperador) renderStatusOperadorFn = deps.renderStatusOperador;
    if (deps.loadAnalistasKanban) loadAnalistasKanbanFn = deps.loadAnalistasKanban;
    if (listenersBound) return;
    listenersBound = true;

    perfilFotoWrapper?.addEventListener('click', openProfilePhotoMenuFromAvatar);
    perfilFotoWrapper?.addEventListener('keydown', event => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            openProfilePhotoMenuFromAvatar(event);
        }
    });
    perfilFotoMenu?.addEventListener('click', event => event.stopPropagation());
    document.addEventListener('click', event => {
        if (!event.target.closest?.('.profile-photo-column')) {
            closeProfilePhotoMenu();
        }
    });
    perfilMostrarFotoBtn?.addEventListener('click', openProfilePhotoView);
    perfilCarregarFotoBtn?.addEventListener('click', () => {
        closeProfilePhotoMenu();
        perfilFotoInput?.click();
    });
    perfilRemoverFotoBtn?.addEventListener('click', removeProfilePhoto);
    perfilFotoInput?.addEventListener('change', handleProfilePhotoFileChange);
    fecharModalFotoVisualizar?.addEventListener('click', closeProfilePhotoView);
    modalFotoVisualizar?.addEventListener('click', event => {
        if (event.target === modalFotoVisualizar) closeProfilePhotoView();
    });
    fecharModalFotoAjuste?.addEventListener('click', closeProfilePhotoAdjust);
    modalFotoAjuste?.addEventListener('click', event => {
        if (event.target === modalFotoAjuste) closeProfilePhotoAdjust();
    });
    perfilZoomMais?.addEventListener('click', () => changeProfilePhotoZoom(0.1));
    perfilZoomMenos?.addEventListener('click', () => changeProfilePhotoZoom(-0.1));
    perfilFotoConfirmarTopo?.addEventListener('click', confirmProfilePhotoUpload);
    perfilFotoConfirmarCheck?.addEventListener('click', confirmProfilePhotoUpload);
    profileAdjustCropRing?.addEventListener('pointerdown', startProfilePhotoDrag);
    profileAdjustCropRing?.addEventListener('pointermove', moveProfilePhotoDrag);
    profileAdjustCropRing?.addEventListener('pointerup', endProfilePhotoDrag);
    profileAdjustCropRing?.addEventListener('pointercancel', endProfilePhotoDrag);
}