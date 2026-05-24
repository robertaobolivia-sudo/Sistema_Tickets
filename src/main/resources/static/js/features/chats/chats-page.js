import {
    CHATS_TAB_CHATS,
    CHATS_TAB_FILA,
    CHATS_TAB_HISTORICO,
    CHATS_TAB_INDEVIDOS,
    CHATS_COMPOSER_INDEVIDO_MSG,
    canOfferClassificarIndevido,
    canOfferReverterIndevido,
    isTicketIndevido,
    buildChatsTimelineEntries,
    buildEtiquetaVinculoPayload,
    etiquetaIdsFromEntities,
    validateEtiquetaIdsForSave,
    CHATS_COMPOSER_HISTORICO_MSG,
    formatChatsComposerCharCount,
    isChatsPanelDrawerViewport,
    countTicketsByTab,
    filterTicketsBySearchTerm,
    filterTicketsForChatsTab,
    formatAnexoTamanho,
    getAnexoCategoriaLabel,
    formatChatsListTimeForTicket,
    getChatsSearchPlaceholder,
    isChatsConversationReadOnly,
    sortTicketsForChatsList,
    getChatsEmptyMessage,
    CHATS_TIMELINE_SELECT_MSG,
    CHATS_CONVERSA_LOADING_MSG,
    CHATS_CONVERSA_LOADING_TITLE,
    getChatsHeaderLoadingProtocolText,
    isChatsPanelValueEmpty,
    buildChatsPanelDlHtml,
    getChatsPanelClienteRows,
    getChatsPanelContatoRows,
    getChatsPanelEntradaRows,
    getChatsPanelChamadoRows,
    buildChatsPanelHistoricoHtml,
    hasChatsPanelHistoricoUtil,
    resolveChatsPanelHistoricoState,
    CHATS_HISTORICO_STATE,
    getChatsPrimaryActionLabel,
    formatChatsCanalBadgeHtml,
    formatChatsStatusBadgeHtml,
    getChatsHeaderClienteTitle,
    applyChatsClienteHeaderArte,
    resolveChatsClienteHeaderArteUrl,
    sanitizeChatsHeaderArtePublicUrl,
    getChatsHeaderCanalBrandHtml,
    getChatsHeaderMetaProtocolAberturaHtml,
    buildChatsListCardHtml,
    formatChatsListElapsedCompact,
    formatRelativeWaitShort,
    getLatestInteractionIso,
    getEtiquetaSelectionForContext,
    getListPreviewFromTicket,
    isEtiquetaSelected,
    isInteracaoFromCliente,
    buildChatsMsgOrigemIndicatorHtml,
    formatChatsListTime,
    sanitizeEtiquetaCor,
    sortEtiquetasAtivas,
    toggleEtiquetaInSelection,
    observacaoAtendimentoParaTextarea,
    validateObservacaoAtendimentoForSave,
    getChatsEtiquetasContextKey,
    CHATS_ETIQUETAS_SEM_CONTATO_MSG,
    canManageChatsEtiquetas,
    hasChatsPanelContatoId,
    mapPendenciaToChatsListItem,
    parseChatsPendenciaListKey,
    buildChatsPendenciaDecisaoBannerText,
    isChatsPendenciaDecisaoItem,
    resolveChatsTabForTicketStatus
} from '@features/chats/chats-view.js';
import {
    getChatsManualStatusOptions,
    buildChatsStatusUpdateBody
} from '@features/chats/chats-status-operacional-view.js';
import { getLoggedAnalyst } from '@shared/auth/state.js';
import * as chatsService from '@features/chats/chats-service.js';
import * as chatsPendenciaService from '@features/chats/chats-pendencia-service.js';
import * as etiquetaService from '@features/configuracoes/etiqueta-service.js';
import * as ticketService from '@features/tickets/ticket-service.js';
import * as contatoEtiquetaService from '@features/contatos/contato-etiqueta-service.js';
import * as contatoTelefoneService from '@features/contatos/contato-telefone-service.js';
import {
    buildChatsListTelefonesBadge,
    enrichChatsPanelContatoRows
} from '@features/contatos/contato-gestao-telefones-view.js';
import * as clienteService from '@features/clientes/cliente-service.js';
import {
    initClassificarIndevidoModal,
    openClassificarIndevidoModal
} from '@components/classificar-indevido-modal/classificar-indevido-modal.js';

let showAlertFn = () => {};
let displayValueFn = v => (v == null || v === '' ? '-' : String(v));
let formatDateTimeFn = () => '-';
let formatPriorityBadgeHtmlFn = () => '';
let getStatusClassFn = () => 'status-badge';
let openDetailsFn = () => {};
let openEncerramentoFn = () => {};

let activeTab = CHATS_TAB_CHATS;
/** @type {{ numero: string, tab: string } | null} */
let pendingChatsOpen = null;
let allTickets = [];
let activeEtiquetas = [];
/** @type {Map<string, number[]>} */
const etiquetaSelectionByContext = new Map();
/** Etiquetas vinculadas ao ticket selecionado (inclui inativas legadas). */
let linkedEtiquetasOnPanel = [];
/** @type {Array<object>} */
let ticketAnexos = [];
let selectedNumero = null;
/** Detalhe do ticket selecionado (status atualizado após GET). */
let selectedTicketDetail = null;
/** Pendências pós-encerramento (Sprint 195). */
let pendenciasDecisao = [];
let selectedPendenciaId = null;
/** Evita aplicar histórico de conversa antiga após troca rápida. */
let historicoLoadSeq = 0;
/** @type {Map<number, number>} contatoId → quantidade de telefones adicionais */
const contatoTelefonesCountCache = new Map();
/** Cache leve clienteId → URL pública da arte (quando o ticket ainda não traz o campo). */
let clienteArteById = new Map();

function cacheClienteArteFromTicket(ticket) {
    const id = ticket?.clienteId;
    if (id == null) {
        return;
    }
    const url = resolveChatsClienteHeaderArteUrl(ticket);
    if (url) {
        clienteArteById.set(Number(id), url);
    }
}

async function hydrateTicketClienteArte(ticket) {
    if (!ticket) {
        return ticket;
    }
    cacheClienteArteFromTicket(ticket);
    const resolvedArte = resolveChatsClienteHeaderArteUrl(ticket);
    if (resolvedArte) {
        return { ...ticket, clienteArteHeaderChatsUrl: resolvedArte };
    }
    const id = ticket.clienteId;
    if (id == null) {
        return ticket;
    }
    const numId = Number(id);
    if (clienteArteById.has(numId) && clienteArteById.get(numId) == null) {
        clienteArteById.delete(numId);
    }
    if (clienteArteById.has(numId)) {
        const cached = clienteArteById.get(numId);
        return cached ? { ...ticket, clienteArteHeaderChatsUrl: cached } : ticket;
    }
    try {
        const cliente = await clienteService.getById(id);
        const safe = sanitizeChatsHeaderArtePublicUrl(cliente?.arteHeaderChatsUrl);
        clienteArteById.set(numId, safe);
        if (safe) {
            return { ...ticket, clienteArteHeaderChatsUrl: safe };
        }
    } catch {
        clienteArteById.set(numId, null);
    }
    return ticket;
}

function setConversationLoadingUi(active) {
    chatsMainEl?.classList.toggle('is-loading-conversa', Boolean(active));
    if (chatsMainEl) {
        chatsMainEl.setAttribute('aria-busy', active ? 'true' : 'false');
    }
}

function beginConversationLoadingUi(previewTicket) {
    setConversationLoadingUi(true);
    setChatsLayoutEmptyState(false);
    if (chatsTimeline) {
        chatsTimeline.innerHTML = '';
    }
    chatsTimelineEmpty?.classList.remove('hidden');
    if (chatsTimelineEmpty) {
        chatsTimelineEmpty.textContent = CHATS_CONVERSA_LOADING_MSG;
    }
    renderConversationHeaderLoading(previewTicket);
    ticketAnexos = [];
    const preview = previewTicket?.numeroTicket ? previewTicket : null;
    renderAnexosPanel(preview);
    updateComposerState(preview || { numeroTicket: previewTicket?.numeroTicket });
}

function renderConversationHeaderLoading(previewTicket) {
    const numero = previewTicket?.numeroTicket;
    if (chatsClienteCanalWrap) {
        chatsClienteCanalWrap.innerHTML = '';
    }
    if (chatsClienteTitulo) {
        chatsClienteTitulo.textContent = CHATS_CONVERSA_LOADING_TITLE;
    }
    if (chatsConversaProtocolo) {
        chatsConversaProtocolo.textContent = getChatsHeaderLoadingProtocolText(numero);
    }
    applyChatsClienteHeaderArte(chatsClienteHeader, previewTicket || null, clienteArteById);
    applyChatsPrimaryActionButtons(previewTicket || null);
}
let listenersBound = false;
let chatsListTimerId = null;

const chatsLista = document.getElementById('chatsLista');
const chatsListaEmpty = document.getElementById('chatsListaEmpty');
const chatsBusca = document.getElementById('chatsBusca');
const chatsTabButtons = document.querySelectorAll('[data-chats-tab]');
const chatsCountChats = document.getElementById('chatsCountChats');
const chatsCountFila = document.getElementById('chatsCountFila');
const chatsCountHistorico = document.getElementById('chatsCountHistorico');
const chatsCountIndevidos = document.getElementById('chatsCountIndevidos');
const chatsClienteHeader = document.getElementById('chatsClienteHeader');
const chatsClienteCanalWrap = document.getElementById('chatsClienteCanalWrap');
const chatsClienteLogoSlot = document.getElementById('chatsClienteLogoSlot');
const chatsClienteTitulo = document.getElementById('chatsClienteTitulo');
const chatsConversaProtocolo = document.getElementById('chatsConversaProtocolo');
const chatsTimeline = document.getElementById('chatsTimeline');
const chatsTimelineEmpty = document.getElementById('chatsTimelineEmpty');
const chatsComposerInput = document.getElementById('chatsComposerInput');
const chatsComposerCount = document.getElementById('chatsComposerCount');
const chatsAnexoBtn = document.getElementById('chatsAnexoBtn');
const chatsEnviarBtn = document.getElementById('chatsEnviarBtn');
const chatsComposer = document.getElementById('chatsComposer');
const chatsComposerRow = document.getElementById('chatsComposerRow');
const chatsComposerHistoricoMsg = document.getElementById('chatsComposerHistoricoMsg');
const pageChatsEl = document.getElementById('page-chats');
const chatsMainEl = pageChatsEl?.querySelector('.chats-main');
const chatsPanelEl = pageChatsEl?.querySelector('.chats-panel');
const chatsPanelClienteDl = document.getElementById('chatsPanelClienteDl');
const chatsPanelContatoDl = document.getElementById('chatsPanelContatoDl');
const chatsPanelChamadoDl = document.getElementById('chatsPanelChamadoDl');
const chatsPanelHistorico = document.getElementById('chatsPanelHistorico');
const chatsPanelBlockCliente = document.getElementById('chatsPanelBlockCliente');
const chatsPanelBlockContato = document.getElementById('chatsPanelBlockContato');
const chatsPanelEntradaDl = document.getElementById('chatsPanelEntradaDl');
const chatsPanelBlockEntrada = document.getElementById('chatsPanelBlockEntrada');
const chatsPanelBlockChamado = document.getElementById('chatsPanelBlockChamado');
const chatsPanelBlockHistorico = document.getElementById('chatsPanelBlockHistorico');
const chatsPanelObservacoesText = document.getElementById('chatsPanelObservacoesText');
const chatsSalvarObservacaoBtn = document.getElementById('chatsSalvarObservacaoBtn');
const chatsObservacaoFeedback = document.getElementById('chatsObservacaoFeedback');
const chatsVerTicketBtn = document.getElementById('chatsVerTicketBtn');
const chatsClassificarIndevidoBtn = document.getElementById('chatsClassificarIndevidoBtn');
const chatsReverterIndevidoBtn = document.getElementById('chatsReverterIndevidoBtn');
const chatsPanelStatusFlow = document.getElementById('chatsPanelStatusFlow');
const chatsStatusSelect = document.getElementById('chatsStatusSelect');
const chatsStatusApplyBtn = document.getElementById('chatsStatusApplyBtn');
const chatsStatusFeedback = document.getElementById('chatsStatusFeedback');
const chatsTagsLista = document.getElementById('chatsTagsLista');
const chatsTagsLegacyHint = document.getElementById('chatsTagsLegacyHint');
const chatsTagsEmpty = document.getElementById('chatsTagsEmpty');
const chatsSalvarEtiquetasBtn = document.getElementById('chatsSalvarEtiquetasBtn');
const chatsEtiquetasFeedback = document.getElementById('chatsEtiquetasFeedback');
const chatsPendenciaDecisao = document.getElementById('chatsPendenciaDecisao');
const chatsPendenciaText = document.getElementById('chatsPendenciaText');
const chatsPendenciaVincularBtn = document.getElementById('chatsPendenciaVincularBtn');
const chatsPendenciaNovoTicketBtn = document.getElementById('chatsPendenciaNovoTicketBtn');
const chatsAnexosLista = document.getElementById('chatsAnexosLista');
const chatsAnexosEmpty = document.getElementById('chatsAnexosEmpty');
const chatsAdicionarAnexoBtn = document.getElementById('chatsAdicionarAnexoBtn');
const chatsAnexoInput = document.getElementById('chatsAnexoInput');
const chatsAnexoHint = document.getElementById('chatsAnexoHint');
const alertBoxChats = document.getElementById('alertBoxChats');
const chatsContextoBtn = document.getElementById('chatsContextoBtn');
const chatsPanelBackdrop = document.getElementById('chatsPanelBackdrop');
const chatsPanelCloseBtn = document.getElementById('chatsPanelCloseBtn');

const CHATS_PANEL_DRAWER_MQ = '(max-width: 1100px)';

function isChatsPanelDrawerMode() {
    return isChatsPanelDrawerViewport(window.innerWidth);
}

function setChatsPanelDrawerOpen(open) {
    if (!pageChatsEl) {
        return;
    }
    const wantOpen = Boolean(open) && isChatsPanelDrawerMode();
    pageChatsEl.classList.toggle('is-panel-open', wantOpen);
    chatsContextoBtn?.setAttribute('aria-expanded', wantOpen ? 'true' : 'false');
    chatsPanelBackdrop?.setAttribute('aria-hidden', wantOpen ? 'false' : 'true');
    document.body.classList.toggle('chats-panel-drawer-open', wantOpen);
}

function closeChatsPanelDrawer() {
    setChatsPanelDrawerOpen(false);
}

function openChatsPanelDrawer() {
    if (!isChatsPanelDrawerMode()) {
        return;
    }
    setChatsPanelDrawerOpen(true);
}

function bindChatsPanelDrawer() {
    chatsContextoBtn?.addEventListener('click', () => {
        if (pageChatsEl?.classList.contains('is-panel-open')) {
            closeChatsPanelDrawer();
        } else {
            openChatsPanelDrawer();
        }
    });
    chatsPanelCloseBtn?.addEventListener('click', closeChatsPanelDrawer);
    chatsPanelBackdrop?.addEventListener('click', closeChatsPanelDrawer);

    const mq = window.matchMedia(CHATS_PANEL_DRAWER_MQ);
    const onMqChange = () => {
        if (!isChatsPanelDrawerMode()) {
            closeChatsPanelDrawer();
        }
    };
    if (typeof mq.addEventListener === 'function') {
        mq.addEventListener('change', onMqChange);
    } else if (typeof mq.addListener === 'function') {
        mq.addListener(onMqChange);
    }

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && pageChatsEl?.classList.contains('is-panel-open')) {
            closeChatsPanelDrawer();
        }
    });
}

export function initChatsPage(deps = {}) {
    initClassificarIndevidoModal({ showAlert: deps.showAlert || showAlertFn });
    if (deps.showAlert) showAlertFn = deps.showAlert;
    if (deps.displayValue) displayValueFn = deps.displayValue;
    if (deps.formatDateTime) formatDateTimeFn = deps.formatDateTime;
    if (deps.formatPriorityBadgeHtml) formatPriorityBadgeHtmlFn = deps.formatPriorityBadgeHtml;
    if (deps.getStatusClass) getStatusClassFn = deps.getStatusClass;
    if (deps.openDetails) openDetailsFn = deps.openDetails;
    if (deps.openEncerramento) openEncerramentoFn = deps.openEncerramento;
    if (listenersBound) return;
    listenersBound = true;

    chatsTabButtons?.forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.getAttribute('data-chats-tab');
            if (!tab) return;
            setActiveTab(tab);
            renderConversationList();
            const visible = getVisibleTickets();
            if (!visible.length) {
                selectedNumero = null;
                clearConversationView();
                return;
            }
            const stillVisible =
                selectedNumero && visible.some(t => t.numeroTicket === selectedNumero);
            if (!stillVisible) {
                selectConversation(visible[0].numeroTicket);
            }
        });
    });

    chatsBusca?.addEventListener('input', () => renderConversationList());

    chatsLista?.addEventListener('click', event => {
        const item = event.target.closest('[data-chats-ticket]');
        if (!item) return;
        const numero = item.getAttribute('data-chats-ticket');
        if (numero) {
            selectConversation(numero);
        }
    });

    chatsVerTicketBtn?.addEventListener('click', onChatsPrimaryAction);
    chatsStatusApplyBtn?.addEventListener('click', () => {
        applyChatsStatusFromPanel().catch(() => {});
    });
    chatsClassificarIndevidoBtn?.addEventListener('click', () => {
        if (!selectedNumero) {
            return;
        }
        openClassificarIndevidoModal(selectedNumero, {
            onSuccess: async (atualizado, numero) => {
                mergeTicketInAllTickets(atualizado, numero);
                renderTabCounts();
                setActiveTab(CHATS_TAB_INDEVIDOS);
                renderConversationList();
                await selectConversation(numero);
                showAlertFn('Chamado classificado como indevido.', alertBoxChats);
            }
        });
    });

    chatsReverterIndevidoBtn?.addEventListener('click', async () => {
        if (!selectedNumero) {
            return;
        }
        try {
            chatsReverterIndevidoBtn.disabled = true;
            const atualizado = await ticketService.reverterTicketIndevido(selectedNumero);
            mergeTicketInAllTickets(atualizado, selectedNumero);
            renderTabCounts();
            renderConversationList();
            await selectConversation(selectedNumero);
            showAlertFn('Classificação indevida revertida. Chamado voltou para Aberto.', alertBoxChats);
        } catch (err) {
            showAlertFn(
                err?.message || 'Não foi possível reverter a classificação indevida.',
                alertBoxChats,
                'error'
            );
            chatsReverterIndevidoBtn.disabled = false;
        }
    });

    chatsAnexoBtn?.addEventListener('click', () => {
        if (chatsAdicionarAnexoBtn && !chatsAdicionarAnexoBtn.disabled) {
            chatsAnexoInput?.click();
            return;
        }
        if (chatsAnexoHint && !chatsAnexoHint.classList.contains('hidden')) {
            return;
        }
        showAlertFn('Selecione uma conversa para anexar arquivos.', alertBoxChats);
    });

    chatsAdicionarAnexoBtn?.addEventListener('click', () => {
        if (chatsAdicionarAnexoBtn?.disabled) {
            return;
        }
        chatsAnexoInput?.click();
    });

    chatsAnexoInput?.addEventListener('change', () => enviarAnexoSelecionado());

    chatsAnexosLista?.addEventListener('click', event => {
        const btn = event.target.closest('[data-anexo-download]');
        if (!btn || !selectedNumero) {
            return;
        }
        const anexoId = btn.getAttribute('data-anexo-download');
        const nome = btn.getAttribute('data-anexo-nome') || 'anexo';
        if (anexoId) {
            baixarAnexo(selectedNumero, anexoId, nome);
        }
    });

    chatsEnviarBtn?.addEventListener('click', () => {
        showAlertFn('Envio de mensagens será habilitado após integração com a API.', alertBoxChats);
    });

    chatsComposerInput?.addEventListener('input', refreshComposerCharCount);
    chatsComposerInput?.addEventListener('focus', () => {
        if (chatsComposerInput?.disabled) {
            if (chatsComposerHistoricoMsg && !chatsComposerHistoricoMsg.classList.contains('hidden')) {
                return;
            }
            showAlertFn('Digitação em tempo real será habilitada após integração com a API.', alertBoxChats);
        }
    });
    refreshComposerCharCount();

    chatsSalvarObservacaoBtn?.addEventListener('click', () => salvarObservacaoAtendimento());
    chatsSalvarEtiquetasBtn?.addEventListener('click', () => salvarEtiquetasTicket());

    chatsTagsLista?.addEventListener('click', event => {
        const chip = event.target.closest('[data-etiqueta-id]');
        if (!chip || !selectedNumero) {
            return;
        }
        const id = chip.getAttribute('data-etiqueta-id');
        const ctxKey = getEtiquetaContextKeyForSelection();
        const current = getEtiquetaSelectionForContext(etiquetaSelectionByContext, ctxKey);
        const next = toggleEtiquetaInSelection(current, id);
        if (ctxKey) {
            etiquetaSelectionByContext.set(ctxKey, next);
        }
        hideEtiquetasFeedback();
        renderTagsPanel();
    });

    chatsPendenciaVincularBtn?.addEventListener('click', () => executarDecisaoPendencia('vincular'));
    chatsPendenciaNovoTicketBtn?.addEventListener('click', () => executarDecisaoPendencia('novo'));

    bindChatsPanelDrawer();
}

export function scheduleOpenChatsConversation(numeroTicket, status) {
    const numero = String(numeroTicket ?? '').trim();
    if (!numero) {
        return;
    }
    pendingChatsOpen = {
        numero,
        tab: resolveChatsTabForTicketStatus(status)
    };
}

export async function loadChatsPage() {
    ensureChatsListTimer();
    clearAlertFn();
    try {
        const [tickets, etiquetas, pendentes] = await Promise.all([
            chatsService.listTicketsBase(),
            etiquetaService.listActive().catch(() => []),
            chatsPendenciaService.listInteracoesPendentes().catch(() => [])
        ]);
        allTickets = chatsService.coerceTicketsList(tickets);
        pendenciasDecisao = Array.isArray(pendentes) ? pendentes : [];
        syncChatsPageIdleFromTickets();
        activeEtiquetas = sortEtiquetasAtivas(etiquetas);
        if (pendingChatsOpen?.numero) {
            const { numero, tab } = pendingChatsOpen;
            pendingChatsOpen = null;
            setActiveTab(tab);
            if (chatsBusca) {
                chatsBusca.placeholder = getChatsSearchPlaceholder(activeTab);
            }
            renderTabCounts();
            renderTagsPanel();
            renderConversationList();
            if (allTickets.some(t => t.numeroTicket === numero)) {
                await selectConversation(numero);
            } else {
                selectedNumero = null;
                clearConversationView();
                showAlertFn(
                    'Conversa não encontrada na lista de Chats para este protocolo.',
                    alertBoxChats
                );
            }
            return;
        }
        if (chatsBusca) {
            chatsBusca.placeholder = getChatsSearchPlaceholder(activeTab);
        }
        renderTabCounts();
        renderTagsPanel();
        renderConversationList();
        if (!selectedNumero) {
            const visible = getVisibleTickets();
            if (visible.length) {
                await selectConversation(visible[0].numeroTicket);
            } else {
                clearConversationView();
            }
        } else if (!allTickets.some(t => t.numeroTicket === selectedNumero)) {
            selectedNumero = null;
            clearConversationView();
            renderConversationList();
        } else {
            await selectConversation(selectedNumero);
        }
    } catch (error) {
        allTickets = [];
        activeEtiquetas = [];
        syncChatsPageIdleFromTickets();
        renderTabCounts();
        renderTagsPanel();
        renderConversationList();
        clearConversationView();
        showAlertFn(error.message, alertBoxChats);
    }
}

function syncChatsPageIdleFromTickets() {
    const hasAny =
        allTickets.length > 0 ||
        (activeTab === CHATS_TAB_FILA && pendenciasDecisao.length > 0);
    setChatsLayoutEmptyState(!hasAny);
}

function setActiveTab(tab) {
    activeTab = tab;
    chatsTabButtons?.forEach(btn => {
        const match = btn.getAttribute('data-chats-tab') === tab;
        btn.classList.toggle('active', match);
        btn.setAttribute('aria-selected', match ? 'true' : 'false');
    });
    if (chatsBusca) {
        chatsBusca.placeholder = getChatsSearchPlaceholder(activeTab);
    }
    const visible = getVisibleTickets();
    if (selectedNumero && !visible.some(t => t.numeroTicket === selectedNumero)) {
        selectedNumero = null;
        clearConversationView();
    }
}

function renderTabCounts() {
    const counts = countTicketsByTab(allTickets);
    if (chatsCountChats) chatsCountChats.textContent = String(counts[CHATS_TAB_CHATS] ?? 0);
    const filaExtra = pendenciasDecisao.length;
    if (chatsCountFila) {
        chatsCountFila.textContent = String((counts[CHATS_TAB_FILA] ?? 0) + filaExtra);
    }
    if (chatsCountHistorico) chatsCountHistorico.textContent = String(counts[CHATS_TAB_HISTORICO] ?? 0);
    if (chatsCountIndevidos) {
        chatsCountIndevidos.textContent = String(counts[CHATS_TAB_INDEVIDOS] ?? 0);
    }
}

function getVisibleTickets() {
    const byTab = filterTicketsForChatsTab(allTickets, activeTab);
    const searched = filterTicketsBySearchTerm(byTab, chatsBusca?.value);
    const sorted = sortTicketsForChatsList(searched, activeTab);
    if (activeTab !== CHATS_TAB_FILA || !pendenciasDecisao.length) {
        return sorted;
    }
    const pendItems = pendenciasDecisao
        .map(mapPendenciaToChatsListItem)
        .filter(Boolean);
    return [...pendItems, ...sorted];
}

function refreshChatsListCounters() {
    if (!chatsLista) {
        return;
    }
    chatsLista.querySelectorAll('[data-chats-wait-iso]').forEach(el => {
        const iso = el.getAttribute('data-chats-wait-iso');
        const text = formatChatsListElapsedCompact(iso);
        el.textContent = text || '—';
    });
}

function ensureChatsListTimer() {
    if (chatsListTimerId != null) {
        return;
    }
    chatsListTimerId = window.setInterval(() => {
        const page = document.getElementById('page-chats');
        if (!page?.classList.contains('active')) {
            return;
        }
        refreshChatsListCounters();
    }, 1000);
}

function stopChatsListTimer() {
    if (chatsListTimerId != null) {
        window.clearInterval(chatsListTimerId);
        chatsListTimerId = null;
    }
}

function renderConversationList() {
    if (!chatsLista) return;
    renderTabCounts();
    const items = getVisibleTickets();
    chatsLista.innerHTML = '';
    if (!items.length) {
        stopChatsListTimer();
        chatsListaEmpty?.classList.remove('hidden');
        if (chatsListaEmpty) {
            chatsListaEmpty.textContent = getChatsEmptyMessage(activeTab);
        }
        return;
    }
    chatsListaEmpty?.classList.add('hidden');

    const needsLiveCounter =
        activeTab === CHATS_TAB_FILA || activeTab === CHATS_TAB_CHATS;

    items.forEach(ticket => {
        const numero = ticket?.numeroTicket ? String(ticket.numeroTicket) : '';
        try {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'chats-list-item';
            if (isTicketIndevido(ticket)) {
                btn.classList.add('chats-list-item--indevido');
            }
            if (numero === selectedNumero) {
                btn.classList.add('active');
            }
            btn.setAttribute('data-chats-ticket', numero);
            if (numero) {
                btn.setAttribute('data-testid', `chats-card-${numero}`);
            }
            btn.setAttribute('role', 'listitem');
            const contatoId = ticket?.contatoId != null ? Number(ticket.contatoId) : 0;
            const telCount =
                contatoId > 0 && contatoTelefonesCountCache.has(contatoId)
                    ? contatoTelefonesCountCache.get(contatoId)
                    : 0;
            btn.innerHTML = buildChatsListCardHtml(ticket, activeTab, {
                displayValue: displayValueFn,
                telefonesAdicionaisCount: telCount,
                buildTelefonesBadge: buildChatsListTelefonesBadge
            });
            chatsLista.appendChild(btn);
        } catch {
            /* ignora item inválido para não esvaziar a lista inteira */
        }
    });

    refreshChatsListCounters();
    if (needsLiveCounter) {
        ensureChatsListTimer();
    } else {
        stopChatsListTimer();
    }
}

function renderHistoricoPanel(historicoState, historicoResumo = null) {
    const state = historicoState ?? CHATS_HISTORICO_STATE.LOADING;
    const html = buildChatsPanelHistoricoHtml(historicoResumo, {
        state,
        formatDateTime: formatDateTimeFn
    });
    if (chatsPanelHistorico) {
        chatsPanelHistorico.innerHTML = html;
    }
    chatsPanelBlockHistorico?.classList.remove('chats-panel-block--no-data');
}

async function selectConversation(numeroTicket) {
    if (!numeroTicket) return;
    const pendenciaId = parseChatsPendenciaListKey(numeroTicket);
    if (pendenciaId != null) {
        selectPendenciaDecisao(pendenciaId);
        return;
    }
    selectedPendenciaId = null;
    hidePendenciaDecisaoPanel();
    const loadSeq = ++historicoLoadSeq;
    selectedNumero = numeroTicket;
    renderConversationList();
    const preview = allTickets.find(t => t.numeroTicket === numeroTicket);
    beginConversationLoadingUi(preview);
    if (preview) {
        renderSidePanel(preview, { historicoState: CHATS_HISTORICO_STATE.LOADING });
    } else {
        renderHistoricoPanel(CHATS_HISTORICO_STATE.LOADING);
    }
    try {
        const historicoMode = activeTab === CHATS_TAB_HISTORICO;
        const historicoPromise = chatsService
            .getHistoricoResumido(numeroTicket)
            .then(data => ({ ok: true, data }))
            .catch(() => ({ ok: false }));

        const [detail, interacoes, satisfacao, anexos, historicoFetch] = await Promise.all([
            chatsService.getTicketDetail(numeroTicket),
            chatsService.listTicketInteracoes(numeroTicket).catch(() => []),
            historicoMode
                ? chatsService.getTicketSatisfacao(numeroTicket).catch(() => null)
                : Promise.resolve(null),
            chatsService.listTicketAnexos(numeroTicket).catch(() => []),
            historicoPromise
        ]);

        if (selectedNumero !== numeroTicket || loadSeq !== historicoLoadSeq) {
            return;
        }

        setConversationLoadingUi(false);

        const historicoState = resolveChatsPanelHistoricoState(historicoFetch);
        const historicoResumo = historicoFetch.ok ? historicoFetch.data : null;

        ticketAnexos = Array.isArray(anexos) ? anexos : [];
        const detailComArte = await hydrateTicketClienteArte(detail);
        selectedTicketDetail = detailComArte;
        const etiquetasVinculadas = await fetchEtiquetasForSelectedTicket(detailComArte).catch(() => []);
        applyLinkedEtiquetas(getChatsEtiquetasContextKey(detailComArte), etiquetasVinculadas);
        setChatsLayoutEmptyState(false);
        const lastInteractionIso = getLatestInteractionIso(interacoes);
        renderConversationHeader(detailComArte);
        renderTimeline(detail, interacoes, satisfacao);
        let telefonesAdicionais = [];
        if (hasChatsPanelContatoId(detailComArte)) {
            try {
                telefonesAdicionais = await contatoTelefoneService.listarTelefonesAdicionais(
                    detailComArte.contatoId
                );
                contatoTelefonesCountCache.set(detailComArte.contatoId, telefonesAdicionais.length);
            } catch {
                telefonesAdicionais = [];
            }
        }
        renderSidePanel(detailComArte, {
            lastInteractionIso,
            historicoResumo,
            historicoState,
            telefonesAdicionais
        });
        renderConversationList();
        renderTagsPanel();
        renderAnexosPanel(detail);
        updateComposerState(detail);
    } catch (error) {
        if (selectedNumero === numeroTicket && loadSeq === historicoLoadSeq) {
            setConversationLoadingUi(false);
            if (chatsTimeline) {
                chatsTimeline.innerHTML = '';
            }
            chatsTimelineEmpty?.classList.remove('hidden');
            if (chatsTimelineEmpty) {
                chatsTimelineEmpty.textContent = 'Não foi possível carregar a conversa.';
            }
            renderHistoricoPanel(CHATS_HISTORICO_STATE.ERROR);
        }
        showAlertFn(error.message, alertBoxChats);
    }
}

function renderConversationHeader(ticket) {
    if (chatsClienteCanalWrap) {
        chatsClienteCanalWrap.innerHTML = ticket?.numeroTicket ? getChatsHeaderCanalBrandHtml(ticket) : '';
    }
    if (chatsClienteTitulo) {
        chatsClienteTitulo.textContent = getChatsHeaderClienteTitle(ticket);
    }
    if (chatsConversaProtocolo) {
        chatsConversaProtocolo.innerHTML = getChatsHeaderMetaProtocolAberturaHtml(ticket, activeTab, {
            displayValue: displayValueFn,
            formatDateTime: formatDateTimeFn
        });
    }
    applyChatsClienteHeaderArte(chatsClienteHeader, ticket, clienteArteById);
}

function renderTimeline(ticket, interacoes, satisfacao = null) {
    if (!chatsTimeline) return;
    chatsTimeline.innerHTML = '';
    const entries = buildChatsTimelineEntries(ticket, interacoes, { satisfacao });
    if (!entries.length) {
        chatsTimelineEmpty?.classList.remove('hidden');
        if (chatsTimelineEmpty) {
            chatsTimelineEmpty.textContent = 'Nenhuma mensagem registrada.';
        }
        return;
    }
    chatsTimelineEmpty?.classList.add('hidden');

    entries.forEach(entry => {
        if (entry.kind === 'date') {
            const sep = document.createElement('div');
            sep.className = 'chats-date-separator';
            sep.textContent = entry.label;
            chatsTimeline.appendChild(sep);
            return;
        }
        if (entry.kind === 'event') {
            const ev = document.createElement('div');
            ev.className = 'chats-event chats-event--system chats-timeline-event';
            const subHtml = entry.sub
                ? `<p class="chats-timeline-event-desc">${escapeHtml(entry.sub)}</p>`
                : '';
            ev.innerHTML = `<span class="chats-timeline-event-marker" aria-hidden="true"></span>
                <div class="chats-timeline-event-body">
                    <p class="chats-timeline-event-title">${escapeHtml(entry.text)}</p>
                    ${subHtml}
                    <time class="chats-timeline-event-time">${escapeHtml(formatDateTimeFn(entry.at))}</time>
                </div>`;
            chatsTimeline.appendChild(ev);
            return;
        }
        const interacao = entry.interacao;
        const fromCliente = isInteracaoFromCliente(interacao);
        const bubble = document.createElement('div');
        bubble.className = fromCliente ? 'chats-msg chats-msg-in' : 'chats-msg chats-msg-out';
        const body = document.createElement('div');
        body.className = 'chats-msg-body';
        body.textContent = displayValueFn(interacao.mensagem);
        const meta = document.createElement('div');
        meta.className = 'chats-msg-meta';
        const timeStr = escapeHtml(formatDateTimeFn(interacao.criadoEm));
        const tipoStr = escapeHtml(displayValueFn(interacao.tipoInteracao));
        const origemHtml = fromCliente ? buildChatsMsgOrigemIndicatorHtml(interacao) : '';
        meta.innerHTML = `<time class="chats-msg-time">${timeStr}</time>${origemHtml}<span class="chats-msg-type">${tipoStr}</span>`;
        bubble.appendChild(body);
        bubble.appendChild(meta);
        chatsTimeline.appendChild(bubble);
    });
    const wrap = chatsTimeline.parentElement;
    if (wrap) {
        wrap.scrollTop = wrap.scrollHeight;
    }
}

function renderPanelSection(dlEl, blockEl, rows) {
    const list = Array.isArray(rows) ? rows : [];
    if (dlEl) {
        dlEl.innerHTML = buildChatsPanelDlHtml(list);
    }
    if (blockEl) {
        blockEl.classList.toggle('chats-panel-block--no-data', !list.length);
    }
}

function renderSidePanel(ticket, panelOptions = {}) {
    const hasTicket = Boolean(ticket?.numeroTicket);
    const lastIso = panelOptions.lastInteractionIso ?? null;
    const chamadoOpts = {
        formatDateTime: formatDateTimeFn,
        tab: activeTab,
        lastInteractionIso: lastIso
    };
    if (!hasTicket) {
        renderPanelSection(chatsPanelClienteDl, chatsPanelBlockCliente, []);
        renderPanelSection(chatsPanelContatoDl, chatsPanelBlockContato, []);
        renderPanelSection(chatsPanelEntradaDl, chatsPanelBlockEntrada, []);
        renderPanelSection(chatsPanelChamadoDl, chatsPanelBlockChamado, []);
        if (chatsPanelHistorico) {
            chatsPanelHistorico.innerHTML = '';
        }
        chatsPanelBlockHistorico?.classList.add('chats-panel-block--no-data');
    } else {
        const clienteRows = getChatsPanelClienteRows(ticket).map(r => ({
            ...r,
            text: r.text != null ? r.text : displayValueFn(r.value)
        }));
        let contatoRows = getChatsPanelContatoRows(ticket);
        if (Array.isArray(panelOptions.telefonesAdicionais) && panelOptions.telefonesAdicionais.length) {
            contatoRows = enrichChatsPanelContatoRows(contatoRows, panelOptions.telefonesAdicionais);
        } else if (hasChatsPanelContatoId(ticket)) {
            contatoRows = enrichChatsPanelContatoRows(contatoRows, []);
        }
        contatoRows = contatoRows.map(r => ({
            ...r,
            text:
                r.html != null
                    ? r.text
                    : r.text != null
                      ? r.text
                      : displayValueFn(r.value)
        }));
        const entradaRows = getChatsPanelEntradaRows(ticket).map(r => ({
            ...r,
            text: r.text != null ? r.text : displayValueFn(r.value)
        }));
        const chamadoRows = getChatsPanelChamadoRows(ticket, chamadoOpts).map(r => {
            if (r.html) {
                return r;
            }
            if (r.label === 'Protocolo' || r.label === 'Status' || r.label === 'Canal') {
                return { ...r, text: r.text != null ? r.text : displayValueFn(r.value) };
            }
            return { ...r, text: r.text != null ? r.text : displayValueFn(r.value) };
        });
        renderPanelSection(chatsPanelClienteDl, chatsPanelBlockCliente, clienteRows);
        renderPanelSection(chatsPanelContatoDl, chatsPanelBlockContato, contatoRows);
        renderPanelSection(chatsPanelEntradaDl, chatsPanelBlockEntrada, entradaRows);
        renderPanelSection(chatsPanelChamadoDl, chatsPanelBlockChamado, chamadoRows);
        const historicoState =
            panelOptions.historicoState ??
            (panelOptions.historicoResumo === undefined
                ? CHATS_HISTORICO_STATE.LOADING
                : resolveChatsPanelHistoricoState({
                      ok: true,
                      data: panelOptions.historicoResumo
                  }));
        renderHistoricoPanel(historicoState, panelOptions.historicoResumo);
    }
    const readOnly = isChatsConversationReadOnly(activeTab, ticket);
    if (chatsPanelObservacoesText) {
        chatsPanelObservacoesText.value = observacaoAtendimentoParaTextarea(ticket?.observacaoAtendimento);
        chatsPanelObservacoesText.disabled = !ticket?.numeroTicket || readOnly;
    }
    if (chatsSalvarObservacaoBtn) {
        chatsSalvarObservacaoBtn.disabled = !ticket?.numeroTicket || readOnly;
    }
    hideObservacaoFeedback();
    applyChatsPrimaryActionButtons(ticket);
    renderChatsStatusFlow(ticket, readOnly);
    renderTagsPanel();
    updateComposerState(ticket);
}

async function enviarAnexoSelecionado() {
    const file = chatsAnexoInput?.files?.[0];
    if (!file || !selectedNumero) {
        return;
    }
    if (chatsAdicionarAnexoBtn) {
        chatsAdicionarAnexoBtn.disabled = true;
    }
    try {
        await chatsService.uploadTicketAnexo(selectedNumero, file);
        ticketAnexos = await chatsService.listTicketAnexos(selectedNumero).catch(() => []);
        const detail = allTickets.find(t => t.numeroTicket === selectedNumero);
        renderAnexosPanel(detail || { numeroTicket: selectedNumero });
        showAlertFn('Arquivo anexado com sucesso.', alertBoxChats);
    } catch (error) {
        showAlertFn(error.message, alertBoxChats);
    } finally {
        if (chatsAnexoInput) {
            chatsAnexoInput.value = '';
        }
        const detail = allTickets.find(t => t.numeroTicket === selectedNumero);
        renderAnexosPanel(detail || { numeroTicket: selectedNumero });
    }
}

async function baixarAnexo(numeroTicket, anexoId, nomeArquivo) {
    try {
        await chatsService.downloadTicketAnexoBlob(numeroTicket, anexoId, nomeArquivo);
    } catch (error) {
        showAlertFn(error.message, alertBoxChats);
    }
}

function renderAnexosPanel(ticket) {
    const lista = Array.isArray(ticketAnexos) ? ticketAnexos : [];
    const readOnly = isChatsConversationReadOnly(activeTab, ticket);
    const temTicket = Boolean(ticket?.numeroTicket);

    if (chatsAnexosEmpty) {
        chatsAnexosEmpty.classList.toggle('hidden', lista.length > 0);
        chatsAnexosEmpty.textContent = 'Nenhum arquivo anexado.';
    }
    if (chatsAnexosLista) {
        chatsAnexosLista.innerHTML = '';
        chatsAnexosLista.classList.toggle('hidden', !lista.length);
        lista.forEach(anexo => {
            const li = document.createElement('li');
            li.className = 'chats-anexo-item';
            li.setAttribute('role', 'listitem');
            const categoria = getAnexoCategoriaLabel(anexo.tipoConteudo, anexo.nomeArquivo);
            const tamanho = formatAnexoTamanho(anexo.tamanhoBytes);
            const data = anexo.criadoEm ? formatDateTimeFn(anexo.criadoEm) : '—';
            const podeBaixar = anexo.downloadDisponivel === true;
            li.innerHTML = `
                <div class="chats-anexo-item-row">
                    <span class="chats-anexo-nome">${escapeHtml(displayValueFn(anexo.nomeArquivo))}</span>
                    <span class="chats-anexo-meta">${escapeHtml(categoria)}</span>
                </div>
                <span class="chats-anexo-meta">${escapeHtml(tamanho)} · ${escapeHtml(data)}</span>
                <div class="chats-anexo-actions">
                    ${
                        podeBaixar
                            ? `<button type="button" class="button button-secondary button-small" data-anexo-download="${escapeHtml(String(anexo.id))}" data-anexo-nome="${escapeHtml(String(anexo.nomeArquivo ?? 'anexo'))}">Baixar</button>`
                            : '<span class="chats-anexo-meta">Download indisponível</span>'
                    }
                </div>
            `;
            chatsAnexosLista.appendChild(li);
        });
    }

    if (chatsAdicionarAnexoBtn) {
        chatsAdicionarAnexoBtn.disabled = !temTicket || readOnly;
    }
    if (chatsAnexoHint) {
        if (!temTicket) {
            chatsAnexoHint.textContent = 'Selecione uma conversa para ver anexos.';
            chatsAnexoHint.classList.remove('hidden');
        } else if (readOnly) {
            chatsAnexoHint.textContent =
                'Conversa encerrada. Novos anexos só em atendimentos abertos.';
            chatsAnexoHint.classList.remove('hidden');
        } else {
            chatsAnexoHint.classList.add('hidden');
        }
    }
}

function refreshComposerCharCount() {
    if (!chatsComposerCount || !chatsComposerInput) {
        return;
    }
    chatsComposerCount.textContent = formatChatsComposerCharCount(chatsComposerInput.value.length);
}

function updateComposerState(ticket) {
    const readOnly = isChatsConversationReadOnly(activeTab, ticket);
    const indevido = isTicketIndevido(ticket);
    const showHistoricoMsg = readOnly && Boolean(ticket?.numeroTicket);
    chatsComposer?.classList.toggle('is-historico-readonly', showHistoricoMsg);
    if (chatsComposerHistoricoMsg) {
        chatsComposerHistoricoMsg.textContent = indevido
            ? CHATS_COMPOSER_INDEVIDO_MSG
            : CHATS_COMPOSER_HISTORICO_MSG;
        chatsComposerHistoricoMsg.classList.toggle('hidden', !showHistoricoMsg);
    }
    if (chatsComposerRow) {
        chatsComposerRow.classList.toggle('hidden', showHistoricoMsg);
    }
    if (chatsComposerInput) {
        chatsComposerInput.disabled = true;
        chatsComposerInput.placeholder = showHistoricoMsg
            ? CHATS_COMPOSER_HISTORICO_MSG
            : 'Digite uma mensagem';
    }
    if (chatsAnexoBtn) {
        chatsAnexoBtn.disabled = showHistoricoMsg || !ticket?.numeroTicket;
    }
    if (chatsEnviarBtn) {
        chatsEnviarBtn.disabled = true;
    }
    chatsComposer?.classList.toggle('is-disabled', !ticket?.numeroTicket || showHistoricoMsg);
    refreshComposerCharCount();
}

async function salvarObservacaoAtendimento() {
    if (!selectedNumero) {
        return;
    }
    const validacao = validateObservacaoAtendimentoForSave(chatsPanelObservacoesText?.value);
    if (!validacao.ok) {
        showObservacaoFeedback(validacao.message, true);
        return;
    }
    if (chatsSalvarObservacaoBtn) {
        chatsSalvarObservacaoBtn.disabled = true;
    }
    try {
        const atualizado = await ticketService.saveObservacaoAtendimento(selectedNumero, validacao.value);
        const obs = atualizado?.observacaoAtendimento;
        if (chatsPanelObservacoesText) {
            chatsPanelObservacoesText.value = observacaoAtendimentoParaTextarea(obs);
        }
        syncTicketObservacaoInList(selectedNumero, obs);
        if (
            selectedTicketDetail &&
            String(selectedTicketDetail.numeroTicket) === String(selectedNumero)
        ) {
            selectedTicketDetail = { ...selectedTicketDetail, observacaoAtendimento: obs ?? null };
        }
        const msg = validacao.value
            ? 'Observação salva com sucesso.'
            : 'Observação removida.';
        showObservacaoFeedback(msg, false);
    } catch (error) {
        showObservacaoFeedback(error.message, true);
    } finally {
        const ticketRef =
            selectedTicketDetail ||
            allTickets.find(t => t.numeroTicket === selectedNumero) ||
            { numeroTicket: selectedNumero };
        const readOnly = isChatsConversationReadOnly(activeTab, ticketRef);
        if (chatsSalvarObservacaoBtn && selectedNumero) {
            chatsSalvarObservacaoBtn.disabled = readOnly;
        }
    }
}

function syncTicketObservacaoInList(numero, observacao) {
    const t = allTickets.find(x => x.numeroTicket === numero);
    if (t) {
        t.observacaoAtendimento = observacao ?? null;
    }
}

function showObservacaoFeedback(message, isError) {
    if (!chatsObservacaoFeedback) {
        return;
    }
    chatsObservacaoFeedback.textContent = message;
    chatsObservacaoFeedback.classList.remove('hidden', 'is-error');
    if (isError) {
        chatsObservacaoFeedback.classList.add('is-error');
    }
}

function hideObservacaoFeedback() {
    chatsObservacaoFeedback?.classList.add('hidden');
}

function getTicketRefForEtiquetas() {
    return (
        selectedTicketDetail ||
        allTickets.find(t => t.numeroTicket === selectedNumero) ||
        (selectedNumero ? { numeroTicket: selectedNumero } : null)
    );
}

function getEtiquetaContextKeyForSelection() {
    return getChatsEtiquetasContextKey(getTicketRefForEtiquetas());
}

async function fetchEtiquetasForSelectedTicket(ticket) {
    if (!ticket || !canManageChatsEtiquetas(ticket)) {
        return [];
    }
    return contatoEtiquetaService.listarEtiquetasContato(ticket.contatoId);
}

function applyLinkedEtiquetas(contextKey, etiquetas) {
    const list = Array.isArray(etiquetas) ? etiquetas : [];
    const activeKey = getEtiquetaContextKeyForSelection();
    if (contextKey && activeKey === contextKey) {
        linkedEtiquetasOnPanel = list;
    }
    if (contextKey) {
        etiquetaSelectionByContext.set(contextKey, etiquetaIdsFromEntities(list));
    }
}

async function salvarEtiquetasTicket() {
    const ticketRef = getTicketRefForEtiquetas();
    const ctxKey = getChatsEtiquetasContextKey(ticketRef);
    if (!ctxKey) {
        return;
    }
    if (!canManageChatsEtiquetas(ticketRef)) {
        showEtiquetasFeedback(CHATS_ETIQUETAS_SEM_CONTATO_MSG, true);
        return;
    }
    const selected = getEtiquetaSelectionForContext(etiquetaSelectionByContext, ctxKey);
    const validacao = validateEtiquetaIdsForSave(selected, activeEtiquetas);
    if (!validacao.ok) {
        showEtiquetasFeedback(validacao.message, true);
        return;
    }
    if (chatsSalvarEtiquetasBtn) {
        chatsSalvarEtiquetasBtn.disabled = true;
    }
    try {
        const salvas = await contatoEtiquetaService.salvarEtiquetasContato(
            ticketRef.contatoId,
            validacao.etiquetaIds
        );
        applyLinkedEtiquetas(ctxKey, salvas);
        renderTagsPanel();
        showEtiquetasFeedback('Etiquetas salvas com sucesso.', false);
    } catch (error) {
        showEtiquetasFeedback(error.message, true);
    } finally {
        if (chatsSalvarEtiquetasBtn && selectedNumero) {
            chatsSalvarEtiquetasBtn.disabled = false;
        }
    }
}

function showEtiquetasFeedback(message, isError) {
    if (!chatsEtiquetasFeedback) {
        return;
    }
    chatsEtiquetasFeedback.textContent = message;
    chatsEtiquetasFeedback.classList.remove('hidden', 'is-error');
    if (isError) {
        chatsEtiquetasFeedback.classList.add('is-error');
    }
}

function hideEtiquetasFeedback() {
    chatsEtiquetasFeedback?.classList.add('hidden');
}

function renderTagsPanel() {
    if (!chatsTagsLista) {
        return;
    }
    chatsTagsLista.innerHTML = '';
    const lista = activeEtiquetas;
    const hasActive = lista.length > 0;
    const inativasLegadas = (Array.isArray(linkedEtiquetasOnPanel) ? linkedEtiquetasOnPanel : [])
        .filter(e => e && e.ativo === false);

    if (!hasActive && !inativasLegadas.length) {
        chatsTagsEmpty?.classList.remove('hidden');
        if (chatsSalvarEtiquetasBtn) {
            chatsSalvarEtiquetasBtn.disabled = true;
        }
        return;
    }
    chatsTagsEmpty?.classList.add('hidden');

    const ticketRef = getTicketRefForEtiquetas();
    const ctxKey = getChatsEtiquetasContextKey(ticketRef);
    const selected = ctxKey ? getEtiquetaSelectionForContext(etiquetaSelectionByContext, ctxKey) : [];
    if (chatsTagsLegacyHint) {
        const indisponivel = !canManageChatsEtiquetas(ticketRef);
        chatsTagsLegacyHint.textContent = indisponivel ? CHATS_ETIQUETAS_SEM_CONTATO_MSG : '';
        chatsTagsLegacyHint.classList.toggle('hidden', !indisponivel);
    }

    const readOnly = isChatsConversationReadOnly(activeTab, ticketRef || { numeroTicket: selectedNumero });
    const canSelect = Boolean(selectedNumero) && !readOnly && canManageChatsEtiquetas(ticketRef);
    if (chatsSalvarEtiquetasBtn) {
        chatsSalvarEtiquetasBtn.disabled = !canSelect;
    }

    inativasLegadas.forEach(etiqueta => {
        appendEtiquetaChip(etiqueta, selected, canSelect, true);
    });

    lista.forEach(etiqueta => {
        appendEtiquetaChip(etiqueta, selected, canSelect, false);
    });
}

function appendEtiquetaChip(etiqueta, selected, canSelect, legadoInativa) {
    if (!chatsTagsLista) {
        return;
    }
    const id = etiqueta.id;
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'chats-tag-chip';
    if (legadoInativa) {
        btn.classList.add('legacy-inativa');
    }
    if (isEtiquetaSelected(selected, id)) {
        btn.classList.add('selected');
        btn.setAttribute('aria-selected', 'true');
    } else {
        btn.setAttribute('aria-selected', 'false');
    }
    btn.setAttribute('data-etiqueta-id', String(id));
    btn.setAttribute('role', 'option');
    btn.disabled = !canSelect;
    const tituloBase = etiqueta.descricao ? String(etiqueta.descricao) : String(etiqueta.nome ?? '');
    btn.title = legadoInativa ? `${tituloBase} (inativa — vínculo legado)` : tituloBase;
    const cor = sanitizeEtiquetaCor(etiqueta.cor);
    const swatch = cor ? `<span class="chats-tag-swatch" style="background:${cor}"></span>` : '';
    btn.innerHTML = `${swatch}<span>${escapeHtml(String(etiqueta.nome ?? '—'))}</span>`;
    chatsTagsLista.appendChild(btn);
}

/** Payload atual de etiquetas ativas selecionadas (para integrações futuras). */
export function getPendingEtiquetaVinculoForTicket(numeroTicket) {
    if (!numeroTicket) {
        return null;
    }
    const ticketRef = allTickets.find(t => t.numeroTicket === numeroTicket) || { numeroTicket };
    const ctxKey = getChatsEtiquetasContextKey(ticketRef);
    const selected = getEtiquetaSelectionForContext(etiquetaSelectionByContext, ctxKey);
    const validacao = validateEtiquetaIdsForSave(selected, activeEtiquetas);
    return buildEtiquetaVinculoPayload(ctxKey, validacao.etiquetaIds);
}

function setChatsLayoutEmptyState(isEmpty) {
    chatsMainEl?.classList.toggle('chats-main--empty', isEmpty);
    chatsPanelEl?.classList.toggle('chats-panel--empty', isEmpty);
    pageChatsEl?.classList.toggle('chats-page--idle', isEmpty);
}

function hideChatsStatusFeedback() {
    if (!chatsStatusFeedback) {
        return;
    }
    chatsStatusFeedback.textContent = '';
    chatsStatusFeedback.classList.add('hidden');
}

function renderChatsStatusFlow(ticket, readOnly) {
    if (!chatsPanelStatusFlow || !chatsStatusSelect || !chatsStatusApplyBtn) {
        return;
    }
    hideChatsStatusFeedback();
    const options = getChatsManualStatusOptions(ticket);
    const show = options.length > 0 && !readOnly;
    chatsPanelStatusFlow.classList.toggle('hidden', !show);
    if (!show) {
        chatsStatusSelect.innerHTML = '';
        chatsStatusApplyBtn.disabled = true;
        return;
    }
    chatsStatusSelect.innerHTML = options
        .map(o => `<option value="${o.code}">${o.label}</option>`)
        .join('');
    chatsStatusApplyBtn.disabled = false;
}

async function applyChatsStatusFromPanel() {
    if (!selectedNumero || !chatsStatusSelect || chatsStatusApplyBtn?.disabled) {
        return;
    }
    const target = chatsStatusSelect.value;
    if (!target) {
        return;
    }
    const analistaId = getLoggedAnalyst()?.id ?? null;
    if (target === 'EM_ATENDIMENTO' && analistaId == null) {
        showAlertFn('Faça login novamente para iniciar o atendimento.', alertBoxChats);
        return;
    }
    chatsStatusApplyBtn.disabled = true;
    try {
        const body = buildChatsStatusUpdateBody(target, analistaId);
        const atualizado = await ticketService.updateTicketStatus(selectedNumero, body);
        mergeTicketInAllTickets(atualizado, selectedNumero);
        renderTabCounts();
        const tabDestino = resolveChatsTabForTicketStatus(atualizado?.status);
        if (tabDestino && tabDestino !== activeTab) {
            setActiveTab(tabDestino);
        }
        renderConversationList();
        await selectConversation(selectedNumero);
        showAlertFn('Status do chamado atualizado.', alertBoxChats);
    } catch (error) {
        showAlertFn(error?.message || 'Não foi possível alterar o status.', alertBoxChats);
    } finally {
        const ticket =
            selectedTicketDetail ||
            allTickets.find(t => t.numeroTicket === selectedNumero);
        const readOnly = isChatsConversationReadOnly(activeTab, ticket);
        renderChatsStatusFlow(ticket, readOnly);
    }
}

function applyChatsPrimaryActionButtons(ticket) {
    if (chatsClassificarIndevidoBtn) {
        const showClassificar = canOfferClassificarIndevido(ticket);
        chatsClassificarIndevidoBtn.classList.toggle('hidden', !showClassificar);
        chatsClassificarIndevidoBtn.disabled = !showClassificar;
    }
    if (chatsReverterIndevidoBtn) {
        const showReverter = canOfferReverterIndevido(ticket);
        chatsReverterIndevidoBtn.classList.toggle('hidden', !showReverter);
        chatsReverterIndevidoBtn.disabled = !showReverter;
    }
    if (!chatsVerTicketBtn) {
        return;
    }
    const { label, mode, disabled } = getChatsPrimaryActionLabel(ticket);
    chatsVerTicketBtn.textContent = label || 'Detalhes';
    chatsVerTicketBtn.disabled = disabled;
    chatsVerTicketBtn.dataset.chatsAction = mode;
}

function mergeTicketInAllTickets(atualizado, numeroTicket) {
    const numero = numeroTicket || atualizado?.numeroTicket;
    if (!numero) {
        return;
    }
    const idx = allTickets.findIndex(t => t.numeroTicket === numero);
    const merged = { ...(idx >= 0 ? allTickets[idx] : {}), ...(atualizado || {}) };
    if (idx >= 0) {
        allTickets[idx] = merged;
    } else if (atualizado) {
        allTickets.push(merged);
    }
    if (selectedTicketDetail && selectedTicketDetail.numeroTicket === numero) {
        selectedTicketDetail = { ...selectedTicketDetail, ...merged };
    }
}

function onChatsPrimaryAction() {
    if (!selectedNumero) {
        return;
    }
    const ticket =
        selectedTicketDetail ||
        allTickets.find(t => t.numeroTicket === selectedNumero) ||
        { numeroTicket: selectedNumero };
    const { mode, disabled } = getChatsPrimaryActionLabel(ticket);
    if (disabled) {
        return;
    }
    if (mode === 'encerrar') {
        openEncerramentoFn(selectedNumero);
        return;
    }
    if (mode === 'detalhes') {
        openDetailsFn(selectedNumero);
    }
}

function hidePendenciaDecisaoPanel() {
    chatsPendenciaDecisao?.classList.add('hidden');
    if (chatsPendenciaVincularBtn) chatsPendenciaVincularBtn.disabled = true;
    if (chatsPendenciaNovoTicketBtn) chatsPendenciaNovoTicketBtn.disabled = true;
}

function selectPendenciaDecisao(pendenciaId) {
    selectedPendenciaId = pendenciaId;
    selectedNumero = mapPendenciaToChatsListItem({ id: pendenciaId })?.numeroTicket || null;
    selectedTicketDetail = null;
    renderConversationList();
    setConversationLoadingUi(false);
    setChatsLayoutEmptyState(false);
    const pendencia = pendenciasDecisao.find(p => p.id === pendenciaId);
    if (chatsClienteTitulo) {
        chatsClienteTitulo.textContent = pendencia?.contatoNome || 'Nova mensagem';
    }
    if (chatsConversaProtocolo) {
        chatsConversaProtocolo.textContent = pendencia?.numeroTicketAnterior
            ? `Após ${pendencia.numeroTicketAnterior}`
            : 'Pós-encerramento';
    }
    applyChatsClienteHeaderArte(chatsClienteHeader, null, clienteArteById);
    applyChatsPrimaryActionButtons(null);
    if (chatsClassificarIndevidoBtn) {
        chatsClassificarIndevidoBtn.classList.add('hidden');
    }
    if (chatsReverterIndevidoBtn) {
        chatsReverterIndevidoBtn.classList.add('hidden');
    }
    if (chatsTimeline) chatsTimeline.innerHTML = '';
    chatsTimelineEmpty?.classList.add('hidden');
    if (pendencia && chatsPendenciaText) {
        const msgPreview = pendencia.mensagem
            ? `\n\n“${String(pendencia.mensagem).slice(0, 280)}”`
            : '';
        chatsPendenciaText.textContent =
            buildChatsPendenciaDecisaoBannerText(pendencia) + msgPreview;
    }
    chatsPendenciaDecisao?.classList.remove('hidden');
    if (chatsPendenciaVincularBtn) chatsPendenciaVincularBtn.disabled = false;
    if (chatsPendenciaNovoTicketBtn) chatsPendenciaNovoTicketBtn.disabled = false;
    renderSidePanel(null);
    updateComposerState(null);
    if (chatsComposerInput) {
        chatsComposerInput.disabled = true;
    }
    renderTagsPanel();
    renderAnexosPanel(null);
}

async function executarDecisaoPendencia(acao) {
    if (!selectedPendenciaId) {
        return;
    }
    const id = selectedPendenciaId;
    if (chatsPendenciaVincularBtn) chatsPendenciaVincularBtn.disabled = true;
    if (chatsPendenciaNovoTicketBtn) chatsPendenciaNovoTicketBtn.disabled = true;
    try {
        const resultado =
            acao === 'vincular'
                ? await chatsPendenciaService.vincularAoTicketAnterior(id)
                : await chatsPendenciaService.gerarNovoTicket(id);
        pendenciasDecisao = pendenciasDecisao.filter(p => p.id !== id);
        selectedPendenciaId = null;
        hidePendenciaDecisaoPanel();
        await loadChatsPage();
        const alvo = resultado?.numeroTicket;
        if (alvo) {
            await selectConversation(alvo);
        }
        showAlertFn(
            acao === 'vincular'
                ? 'Mensagem vinculada ao ticket anterior.'
                : `Novo ticket ${alvo || ''} criado.`,
            alertBoxChats
        );
    } catch (error) {
        showAlertFn(error.message, alertBoxChats);
        if (chatsPendenciaVincularBtn) chatsPendenciaVincularBtn.disabled = false;
        if (chatsPendenciaNovoTicketBtn) chatsPendenciaNovoTicketBtn.disabled = false;
    }
}

function clearConversationView() {
    selectedNumero = null;
    selectedTicketDetail = null;
    selectedPendenciaId = null;
    hidePendenciaDecisaoPanel();
    setConversationLoadingUi(false);
    setChatsLayoutEmptyState(true);
    if (chatsClienteCanalWrap) chatsClienteCanalWrap.innerHTML = '';
    if (chatsClienteTitulo) chatsClienteTitulo.textContent = 'Selecione uma conversa';
    if (chatsConversaProtocolo) chatsConversaProtocolo.textContent = 'Protocolo —';
    applyChatsClienteHeaderArte(chatsClienteHeader, null, clienteArteById);
    applyChatsPrimaryActionButtons(null);
    if (chatsClassificarIndevidoBtn) {
        chatsClassificarIndevidoBtn.classList.add('hidden');
    }
    if (chatsReverterIndevidoBtn) {
        chatsReverterIndevidoBtn.classList.add('hidden');
    }
    if (chatsTimeline) chatsTimeline.innerHTML = '';
    chatsTimelineEmpty?.classList.remove('hidden');
    if (chatsTimelineEmpty) {
        chatsTimelineEmpty.textContent = CHATS_TIMELINE_SELECT_MSG;
    }
    renderSidePanel(null);
    if (chatsPanelObservacoesText) {
        chatsPanelObservacoesText.value = '';
        chatsPanelObservacoesText.disabled = true;
    }
    if (chatsSalvarObservacaoBtn) {
        chatsSalvarObservacaoBtn.disabled = true;
    }
    if (chatsSalvarEtiquetasBtn) {
        chatsSalvarEtiquetasBtn.disabled = true;
    }
    linkedEtiquetasOnPanel = [];
    ticketAnexos = [];
    renderAnexosPanel(null);
    hideObservacaoFeedback();
    hideEtiquetasFeedback();
    updateComposerState(null);
}

function clearAlertFn() {
    alertBoxChats?.classList.add('hidden');
}

function escapeHtml(text) {
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}
