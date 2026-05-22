/**
 * Regras puras para listagem Chats (base em tickets existentes).
 */
import { formatPriority } from './presentation.js';
import {
    formatSlaBadgeHtml,
    formatSlaStatusLabel,
    getSlaResolucaoVisualStatus
} from '../rules/slaViewRules.js';

export const CHATS_TAB_CHATS = 'chats';
export const CHATS_TAB_FILA = 'fila';
export const CHATS_TAB_HISTORICO = 'historico';

const STATUS_FILA = new Set(['ABERTO']);
const STATUS_CHATS = new Set(['EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE']);
const STATUS_HISTORICO = new Set(['RESOLVIDO', 'CANCELADO']);
const STATUS_TICKET_ATIVO = new Set(['ABERTO', 'EM_ATENDIMENTO', 'AGUARDANDO_CLIENTE']);

export const CHATS_COMPOSER_HISTORICO_MSG =
    'Conversa encerrada. Histórico somente leitura.';

/** Limite exibido no contador do composer (envio ainda não integrado). */
export const CHATS_COMPOSER_MAX_LENGTH = 4000;

/** Largura máxima (px) em que o painel direito vira drawer. */
export const CHATS_PANEL_DRAWER_MAX_WIDTH = 1100;

export function isChatsPanelDrawerViewport(viewportWidth) {
    const w = Number(viewportWidth);
    if (!Number.isFinite(w) || w <= 0) {
        return false;
    }
    return w <= CHATS_PANEL_DRAWER_MAX_WIDTH;
}

export function formatChatsComposerCharCount(length, maxLength = CHATS_COMPOSER_MAX_LENGTH) {
    const max = Number(maxLength) > 0 ? Number(maxLength) : CHATS_COMPOSER_MAX_LENGTH;
    const len = Math.max(0, Math.min(Number(length) || 0, max));
    return `${len} / ${max}`;
}

export function filterTicketsForChatsTab(tickets, tab) {
    const list = Array.isArray(tickets) ? tickets : [];
    let filtered;
    if (tab === CHATS_TAB_FILA) {
        filtered = list.filter(t => STATUS_FILA.has(normalizeStatus(t?.status)));
    } else if (tab === CHATS_TAB_HISTORICO) {
        filtered = list.filter(t => STATUS_HISTORICO.has(normalizeStatus(t?.status)));
    } else {
        filtered = list.filter(t => STATUS_CHATS.has(normalizeStatus(t?.status)));
    }
    return filtered;
}

export function filterTicketsBySearchTerm(tickets, termo) {
    const list = Array.isArray(tickets) ? tickets : [];
    const q = (termo || '').trim().toLowerCase();
    if (!q) {
        return list;
    }
    return list.filter(t => ticketMatchesSearchTerm(t, q));
}

/** Busca por cliente, telefone, protocolo/número do ticket. */
export function ticketMatchesSearchTerm(ticket, termoLower) {
    const q = (termoLower || '').trim().toLowerCase();
    if (!q) {
        return true;
    }
    const parts = [
        ticket?.numeroTicket,
        ticket?.cliente,
        ticket?.telefone,
        ticket?.telefoneContato,
        ticket?.contatoSolicitanteTelefone,
        ticket?.contatoSolicitanteNome,
        ticket?.canal,
        ticket?.status
    ];
    return parts.some(p => String(p ?? '').toLowerCase().includes(q));
}

export function sortTicketsForChatsList(tickets, tab) {
    const list = Array.isArray(tickets) ? [...tickets] : [];
    if (tab !== CHATS_TAB_HISTORICO) {
        return list;
    }
    return list.sort((a, b) => {
        const ea = a?.dataEncerramento || a?.dataAbertura || '';
        const eb = b?.dataEncerramento || b?.dataAbertura || '';
        return String(eb).localeCompare(String(ea));
    });
}

export function isTicketHistoricoEncerrado(ticket) {
    return STATUS_HISTORICO.has(normalizeStatus(ticket?.status));
}

/** Status elegível para reaproveitamento de protocolo (WhatsApp/API futuro). */
export function isTicketStatusAtivo(status) {
    return STATUS_TICKET_ATIVO.has(normalizeStatus(status));
}

export function isChatsConversationReadOnly(tab, ticket) {
    if (tab === CHATS_TAB_HISTORICO) {
        return Boolean(ticket?.numeroTicket);
    }
    return isTicketHistoricoEncerrado(ticket);
}

export function getChatsConversationTitle(tab, ticket) {
    if (tab === CHATS_TAB_HISTORICO && ticket?.numeroTicket) {
        return 'Histórico da conversa';
    }
    const nome = ticket?.cliente;
    if (nome != null && String(nome).trim() !== '') {
        return String(nome).trim();
    }
    return 'Selecione uma conversa';
}

export function getChatsProtocolLine(tab, ticket, displayValue = v => String(v ?? '—')) {
    const protocolo = displayValue(ticket?.numeroTicket);
    if (tab === CHATS_TAB_HISTORICO && ticket?.cliente) {
        return `${displayValue(ticket.cliente)} · Protocolo ${protocolo}`;
    }
    return `Protocolo ${protocolo}`;
}

export function getChatsSearchPlaceholder(tab) {
    if (tab === CHATS_TAB_HISTORICO) {
        return 'Cliente, telefone ou protocolo';
    }
    return 'Nome, telefone ou nº do ticket';
}

export function formatChatsListTimeForTicket(ticket, tab) {
    if (tab === CHATS_TAB_HISTORICO) {
        return formatChatsListTime(ticket?.dataEncerramento || ticket?.dataAbertura);
    }
    return formatChatsListTime(ticket?.dataAbertura);
}

export function formatAnexoTamanho(bytes) {
    const n = Number(bytes);
    if (!Number.isFinite(n) || n < 0) {
        return '—';
    }
    if (n < 1024) {
        return `${n} B`;
    }
    if (n < 1024 * 1024) {
        return `${(n / 1024).toFixed(1)} KB`;
    }
    return `${(n / (1024 * 1024)).toFixed(1)} MB`;
}

export function getAnexoCategoriaLabel(tipoConteudo, nomeArquivo) {
    const tipo = String(tipoConteudo ?? '').toLowerCase();
    const nome = String(nomeArquivo ?? '').toLowerCase();
    if (tipo.startsWith('image/') || /\.(png|jpe?g|gif|webp)$/.test(nome)) {
        return 'Imagem';
    }
    if (tipo.startsWith('audio/') || /\.(mp3|wav|ogg|m4a)$/.test(nome)) {
        return 'Áudio';
    }
    if (tipo.startsWith('video/') || /\.(mp4|webm|mov)$/.test(nome)) {
        return 'Vídeo';
    }
    return 'Documento';
}

export function formatSatisfacaoTimelineSub(satisfacao) {
    if (!satisfacao || satisfacao.nota == null) {
        return '';
    }
    const nota = String(satisfacao.nota);
    const comentario = truncatePreview(satisfacao.comentario, 120);
    return comentario ? `Nota ${nota} · ${comentario}` : `Nota ${nota}`;
}

export function getChatsEmptyMessage(tab) {
    if (tab === CHATS_TAB_FILA) {
        return 'Nenhum chamado na fila no momento.';
    }
    if (tab === CHATS_TAB_HISTORICO) {
        return 'Nenhum atendimento encerrado.';
    }
    return 'Nenhum atendimento em andamento.';
}

export const CHATS_TIMELINE_SELECT_MSG =
    'Selecione uma conversa para visualizar o atendimento.';

/** Mensagem discreta na área central enquanto o detalhe da conversa carrega. */
export const CHATS_CONVERSA_LOADING_MSG = 'Carregando conversa…';

/** Título temporário no header central durante o carregamento. */
export const CHATS_CONVERSA_LOADING_TITLE = 'Carregando conversa…';

export function getChatsHeaderLoadingProtocolText(numeroTicket) {
    const n = numeroTicket != null && String(numeroTicket).trim() !== '' ? String(numeroTicket).trim() : '';
    return n ? `Protocolo ${n}` : 'Protocolo —';
}

export const CHATS_PANEL_HISTORICO_EMPTY = 'Sem histórico anterior localizado.';
export const CHATS_PANEL_HISTORICO_LOADING = 'Carregando histórico…';
export const CHATS_PANEL_HISTORICO_ERROR = 'Não foi possível carregar o histórico.';

/** Estados do bloco Histórico resumido no painel. */
export const CHATS_HISTORICO_STATE = {
    LOADING: 'loading',
    SUCCESS: 'success',
    EMPTY: 'empty',
    ERROR: 'error'
};

/** @deprecated use CHATS_PANEL_HISTORICO_EMPTY */
export const CHATS_PANEL_HISTORICO_PLACEHOLDER = CHATS_PANEL_HISTORICO_EMPTY;

/** @typedef {{ label: string, value?: *, html?: string, text?: string }} ChatsPanelRow */

/** Valor vazio para ocultar linha no painel direito (—, -, null, etc.). */
export function isChatsPanelValueEmpty(value) {
    if (value == null || value === undefined) {
        return true;
    }
    const t = String(value).trim();
    if (!t) {
        return true;
    }
    const lower = t.toLowerCase();
    return (
        t === '-' ||
        t === '—' ||
        lower === 'null' ||
        lower === 'undefined' ||
        lower === 'nan' ||
        lower === 'invalid date'
    );
}

/** Texto normalizado para comparar duplicidade no painel. */
export function normalizeChatsPanelCompareText(value) {
    if (isChatsPanelValueEmpty(value)) {
        return '';
    }
    return String(value).trim().toLowerCase();
}

/** Apenas dígitos para comparar telefones (mín. 8 dígitos). */
export function normalizeChatsPanelPhone(value) {
    if (isChatsPanelValueEmpty(value)) {
        return '';
    }
    const digits = String(value).replace(/\D/g, '');
    if (digits.length >= 8) {
        return digits;
    }
    return normalizeChatsPanelCompareText(value);
}

/** Igualdade de valores no painel (texto ou telefone). */
export function chatsPanelValuesEqual(a, b, options = {}) {
    if (isChatsPanelValueEmpty(a) && isChatsPanelValueEmpty(b)) {
        return true;
    }
    if (isChatsPanelValueEmpty(a) || isChatsPanelValueEmpty(b)) {
        return false;
    }
    if (options.phone) {
        const pa = normalizeChatsPanelPhone(a);
        const pb = normalizeChatsPanelPhone(b);
        return pa !== '' && pb !== '' && pa === pb;
    }
    return normalizeChatsPanelCompareText(a) === normalizeChatsPanelCompareText(b);
}

/** Linhas do painel com valor preenchido. */
export function filterChatsPanelRows(rows) {
    const list = Array.isArray(rows) ? rows : [];
    return list.filter(row => {
        if (row.html != null && String(row.html).trim() !== '') {
            return true;
        }
        const text = row.text != null ? row.text : row.value;
        return !isChatsPanelValueEmpty(text);
    });
}

function formatChatsPanelCidadeUf(cidade, uf) {
    const c = isChatsPanelValueEmpty(cidade) ? '' : String(cidade).trim();
    const u = isChatsPanelValueEmpty(uf) ? '' : String(uf).trim();
    if (c && u) {
        return `${c}/${u}`;
    }
    return c || u || null;
}

export const CHATS_PANEL_LEGACY_HINT =
    'Dados do cadastro antigo do ticket (sem vínculo Contato).';

export const CHATS_ETIQUETAS_SOURCE_CONTATO = 'contato';
export const CHATS_ETIQUETAS_SOURCE_TICKET = 'ticket';

export const CHATS_ETIQUETAS_LEGACY_HINT =
    'Etiquetas vinculadas ao chamado (cadastro legado).';

/** Origem das etiquetas no painel Chats. */
export function resolveChatsEtiquetasSource(ticket) {
    return hasChatsPanelContatoId(ticket)
        ? CHATS_ETIQUETAS_SOURCE_CONTATO
        : CHATS_ETIQUETAS_SOURCE_TICKET;
}

/** Chave de seleção em memória (contato ou ticket). */
export const CHATS_PENDENCIA_PREFIX = 'PEND-';

export function isChatsPendenciaDecisaoItem(item) {
    return item?.pendenciaDecisaoId != null && Number(item.pendenciaDecisaoId) > 0;
}

export function getChatsPendenciaListKey(pendenciaId) {
    return `${CHATS_PENDENCIA_PREFIX}${pendenciaId}`;
}

export function parseChatsPendenciaListKey(key) {
    if (key == null || !String(key).startsWith(CHATS_PENDENCIA_PREFIX)) {
        return null;
    }
    const id = Number(String(key).slice(CHATS_PENDENCIA_PREFIX.length));
    return Number.isFinite(id) && id > 0 ? id : null;
}

/** Item sintético na lista da Fila para pendência de decisão. */
export function mapPendenciaToChatsListItem(pendencia) {
    if (!pendencia?.id) {
        return null;
    }
    const nome =
        pendencia.contatoNome ||
        pendencia.clienteNome ||
        pendencia.numeroTicketAnterior ||
        'Contato';
    return {
        pendenciaDecisaoId: pendencia.id,
        numeroTicket: getChatsPendenciaListKey(pendencia.id),
        numeroTicketAnterior: pendencia.numeroTicketAnterior,
        cliente: nome,
        status: 'PENDENTE_DECISAO',
        dataAbertura: pendencia.criadaEm,
        _pendenciaPayload: pendencia
    };
}

export function buildChatsPendenciaDecisaoBannerText(pendencia) {
    const anterior = pendencia?.numeroTicketAnterior
        ? String(pendencia.numeroTicketAnterior)
        : '—';
    const contato = pendencia?.contatoNome
        ? String(pendencia.contatoNome)
        : pendencia?.contatoWhatsapp || 'Contato';
    return `Este contato (${contato}) enviou nova mensagem após o encerramento do ticket ${anterior}. Escolha como prosseguir.`;
}

export function getChatsEtiquetasContextKey(ticket) {
    if (!ticket) {
        return '';
    }
    if (hasChatsPanelContatoId(ticket)) {
        return `contato:${ticket.contatoId}`;
    }
    const numero = ticket.numeroTicket;
    return numero ? `ticket:${numero}` : '';
}

/** Ticket com entidade Contato (Sprint 189+). */
export function hasChatsPanelContatoId(ticket) {
    const id = ticket?.contatoId;
    return id != null && Number(id) > 0;
}

/** Rótulos proibidos no painel direito (texto visível). */
export const CHATS_PANEL_FORBIDDEN_LABELS = [
    'Revenda',
    'Conexão',
    'Carteira',
    'Subcliente'
];

export function chatsPanelRowsContainForbiddenLabel(rows) {
    const list = Array.isArray(rows) ? rows : [];
    const lowerForbidden = CHATS_PANEL_FORBIDDEN_LABELS.map(s => s.toLowerCase());
    return list.some(row => {
        const label = row?.label != null ? String(row.label).trim().toLowerCase() : '';
        return lowerForbidden.some(f => label === f || label.includes(f));
    });
}

/** Cliente contratante F5 (ticket com contatoId). */
function getChatsPanelClienteRowsContratante(ticket) {
    const nome = ticket.cliente;
    const empresa = ticket.empresa;
    const candidatos = [];
    if (!isChatsPanelValueEmpty(nome)) {
        candidatos.push({ label: 'Nome', value: nome });
    }
    if (!isChatsPanelValueEmpty(ticket.email)) {
        candidatos.push({ label: 'E-mail', value: ticket.email });
    }
    if (!isChatsPanelValueEmpty(empresa) && !chatsPanelValuesEqual(empresa, nome)) {
        candidatos.push({ label: 'Empresa', value: empresa });
    }
    if (!isChatsPanelValueEmpty(ticket.cnpj)) {
        candidatos.push({ label: 'CNPJ', value: ticket.cnpj });
    }
    const cidadeUf = formatChatsPanelCidadeUf(ticket.cidade, ticket.uf);
    if (!isChatsPanelValueEmpty(cidadeUf)) {
        candidatos.push({ label: 'Cidade/UF', value: cidadeUf });
    }
    return filterChatsPanelRows(candidatos);
}

/** Fallback: cadastro legado no ticket (sem contatoId). */
function getChatsPanelClienteRowsLegacy(ticket) {
    const nome = ticket.cliente;
    const telefone = ticket.telefone;
    const telContato = ticket.telefoneContato;
    const empresa = ticket.empresa;
    const candidatos = [];
    if (!isChatsPanelValueEmpty(nome)) {
        candidatos.push({ label: 'Nome', value: nome });
    }
    if (!isChatsPanelValueEmpty(telefone)) {
        candidatos.push({ label: 'Telefone', value: telefone });
    }
    if (
        !isChatsPanelValueEmpty(telContato) &&
        !chatsPanelValuesEqual(telContato, telefone, { phone: true })
    ) {
        candidatos.push({ label: 'Tel. contato', value: telContato });
    }
    if (!isChatsPanelValueEmpty(ticket.email)) {
        candidatos.push({ label: 'E-mail', value: ticket.email });
    }
    if (!isChatsPanelValueEmpty(empresa) && !chatsPanelValuesEqual(empresa, nome)) {
        candidatos.push({ label: 'Empresa', value: empresa });
    }
    if (!isChatsPanelValueEmpty(ticket.cnpj)) {
        candidatos.push({ label: 'CNPJ', value: ticket.cnpj });
    }
    const cidadeUf = formatChatsPanelCidadeUf(ticket.cidade, ticket.uf);
    if (!isChatsPanelValueEmpty(cidadeUf)) {
        candidatos.push({ label: 'Cidade/UF', value: cidadeUf });
    }
    return filterChatsPanelRows(candidatos);
}

/** Seção Cliente no painel direito. */
export function getChatsPanelClienteRows(ticket) {
    if (!ticket?.numeroTicket) {
        return [];
    }
    if (hasChatsPanelContatoId(ticket)) {
        return getChatsPanelClienteRowsContratante(ticket);
    }
    return getChatsPanelClienteRowsLegacy(ticket);
}

/** Contato WhatsApp (contatoId). */
function getChatsPanelContatoRowsEntidade(ticket) {
    const nomeContratante = ticket.cliente;
    const candidatos = [];
    const nome = ticket.contatoNome;
    if (!isChatsPanelValueEmpty(nome) && !chatsPanelValuesEqual(nome, nomeContratante)) {
        candidatos.push({ label: 'Nome', value: nome });
    } else if (!isChatsPanelValueEmpty(nome)) {
        candidatos.push({ label: 'Nome', value: nome });
    }
    const wa = ticket.contatoWhatsapp;
    if (!isChatsPanelValueEmpty(wa)) {
        candidatos.push({ label: 'WhatsApp', value: wa });
    }
    if (!isChatsPanelValueEmpty(ticket.contatoEmail)) {
        candidatos.push({ label: 'E-mail', value: ticket.contatoEmail });
    }
    const empresaLocal = ticket.contatoEmpresaLocal;
    if (!isChatsPanelValueEmpty(empresaLocal)) {
        candidatos.push({ label: 'Empresa/local', value: empresaLocal });
    }
    const cidadeUf = formatChatsPanelCidadeUf(ticket.contatoCidade, ticket.contatoUf);
    if (!isChatsPanelValueEmpty(cidadeUf)) {
        candidatos.push({ label: 'Cidade/UF', value: cidadeUf });
    }
    if (!isChatsPanelValueEmpty(ticket.contatoObservacoes)) {
        candidatos.push({ label: 'Observações', value: ticket.contatoObservacoes });
    }
    return filterChatsPanelRows(candidatos);
}

/** Fallback Contato: contato solicitante legado no ticket. */
function getChatsPanelContatoRowsLegacy(ticket) {
    const nomeCliente = ticket.cliente;
    const telPrincipal = ticket.telefone || ticket.telefoneContato;
    const emailCliente = ticket.email;
    const candidatos = [];
    const nomeContato = ticket.contatoSolicitanteNome;
    if (
        !isChatsPanelValueEmpty(nomeContato) &&
        !chatsPanelValuesEqual(nomeContato, nomeCliente)
    ) {
        candidatos.push({ label: 'Nome', value: nomeContato });
    }
    const telSolic = ticket.contatoSolicitanteTelefone;
    if (
        !isChatsPanelValueEmpty(telSolic) &&
        !chatsPanelValuesEqual(telSolic, telPrincipal, { phone: true }) &&
        !chatsPanelValuesEqual(telSolic, ticket.telefoneContato, { phone: true })
    ) {
        candidatos.push({ label: 'WhatsApp', value: telSolic });
    }
    const emailSolic = ticket.contatoSolicitanteEmail;
    if (
        !isChatsPanelValueEmpty(emailSolic) &&
        !chatsPanelValuesEqual(emailSolic, emailCliente)
    ) {
        candidatos.push({ label: 'E-mail', value: emailSolic });
    }
    const rows = filterChatsPanelRows(candidatos);
    if (rows.length) {
        rows.push({
            html: `<p class="chats-panel-legacy-hint">${CHATS_PANEL_LEGACY_HINT}</p>`
        });
    }
    return rows;
}

/** Seção Contato — pessoa atendida. */
export function getChatsPanelContatoRows(ticket) {
    if (!ticket?.numeroTicket) {
        return [];
    }
    if (hasChatsPanelContatoId(ticket)) {
        return getChatsPanelContatoRowsEntidade(ticket);
    }
    return getChatsPanelContatoRowsLegacy(ticket);
}

/** Entrada do atendimento — WhatsApp Matriz. */
export function getChatsPanelEntradaRows(ticket) {
    if (!ticket?.numeroTicket) {
        return [];
    }
    const numero = ticket.whatsappMatrizNumero;
    const candidatos = [];
    if (!isChatsPanelValueEmpty(numero)) {
        candidatos.push({ label: 'WhatsApp matriz', value: numero });
    }
    const nome = ticket.whatsappMatrizNome;
    if (
        !isChatsPanelValueEmpty(nome) &&
        !chatsPanelValuesEqual(nome, numero)
    ) {
        candidatos.push({ label: 'Identificação', value: nome });
    }
    return filterChatsPanelRows(candidatos);
}

/**
 * Chamado atual — protocolo, canal, abertura e prioridade (sem status/SLA do cabeçalho).
 * @param {{ formatDateTime?: (iso: string) => string, tab?: string }} options
 */
export function getChatsPanelChamadoRows(ticket, options = {}) {
    if (!ticket?.numeroTicket) {
        return [];
    }
    const formatDateTimeFn = options.formatDateTime;
    const canal = getChatsCanalDisplay(ticket.canal);
    const prioridade = ticket.prioridade;
    const candidatos = [
        { label: 'Protocolo', value: ticket.numeroTicket },
        {
            label: 'Canal',
            value: canal.label,
            text: canal.key === 'SISTEMA' ? '' : canal.label
        },
        {
            label: 'Abertura',
            value: ticket.dataAbertura,
            text:
                ticket.dataAbertura && typeof formatDateTimeFn === 'function'
                    ? formatDateTimeFn(ticket.dataAbertura)
                    : ticket.dataAbertura
        }
    ];
    if (!isChatsPanelValueEmpty(prioridade)) {
        candidatos.push({
            label: 'Prioridade',
            value: prioridade,
            html: formatChatsPriorityBadgeHtml(prioridade)
        });
    }
    if (options.tab === CHATS_TAB_HISTORICO && !isChatsPanelValueEmpty(ticket.dataEncerramento)) {
        candidatos.push({
            label: 'Encerramento',
            value: ticket.dataEncerramento,
            text:
                typeof formatDateTimeFn === 'function'
                    ? formatDateTimeFn(ticket.dataEncerramento)
                    : ticket.dataEncerramento
        });
    }
    return filterChatsPanelRows(candidatos);
}

/** Resumo SLA/espera em uma linha (sem recalcular SLA). */
export function buildChatsPanelSlaSummaryText(ticket, tab, options = {}) {
    if (!ticket?.numeroTicket) {
        return '';
    }
    const waitIso = getChatsWaitReferenceIso(ticket, tab, options.lastInteractionIso || null);
    const compact = formatChatsListElapsedCompact(waitIso);
    const parts = [];
    if (compact && compact !== '—' && compact !== 'agora') {
        parts.push(`Espera ${compact}`);
    }
    const slaStatus = pickChatsPrimarySlaStatus(ticket);
    if (slaStatus && slaStatus !== 'NAO_CALCULADO') {
        parts.push(formatSlaStatusLabel(slaStatus));
    }
    return parts.join(' · ');
}

/** Indica se há outros tickets além do atual no resumo da API. */
export function hasChatsPanelHistoricoUtil(resumo) {
    if (!resumo) {
        return false;
    }
    const total = Number(resumo.totalTicketsCliente) || 0;
    const ultimo = resumo.ultimoTicketEncerrado;
    const recentes = Array.isArray(resumo.ticketsRecentes) ? resumo.ticketsRecentes : [];
    if (total > 1) {
        return true;
    }
    if (ultimo?.numeroTicket) {
        return true;
    }
    return recentes.length > 0;
}

function formatChatsHistoricoDataRef(item, formatDateTime) {
    if (!item) {
        return '';
    }
    const enc = item.dataEncerramento;
    const ab = item.dataAbertura;
    const iso = enc || ab;
    if (!iso || typeof formatDateTime !== 'function') {
        return '';
    }
    const formatted = formatDateTime(iso);
    if (isChatsPanelValueEmpty(formatted)) {
        return '';
    }
    return enc ? `Encerrado ${formatted}` : `Aberto ${formatted}`;
}

function formatChatsHistoricoMotivo(item) {
    const g = item?.grupoCategoriaNome;
    if (isChatsPanelValueEmpty(g)) {
        return '';
    }
    return String(g).trim();
}

/**
 * Define estado de UI após resposta (ou falha) do histórico resumido.
 * @param {{ ok: boolean, data?: object|null }} fetchResult
 * @returns {'loading'|'success'|'empty'|'error'}
 */
export function resolveChatsPanelHistoricoState(fetchResult) {
    if (!fetchResult || fetchResult.ok !== true) {
        return CHATS_HISTORICO_STATE.ERROR;
    }
    return hasChatsPanelHistoricoUtil(fetchResult.data)
        ? CHATS_HISTORICO_STATE.SUCCESS
        : CHATS_HISTORICO_STATE.EMPTY;
}

/**
 * HTML compacto do histórico resumido (painel direito).
 * @param {object|null} resumo resposta de /api/chats/{numero}/historico-resumido
 * @param {{ formatDateTime?: function, state?: string }} options
 */
export function buildChatsPanelHistoricoHtml(resumo, options = {}) {
    const state = options.state ?? CHATS_HISTORICO_STATE.SUCCESS;
    if (state === CHATS_HISTORICO_STATE.LOADING) {
        return `<p class="chats-panel-placeholder chats-historico-state--loading" role="status">${escapeChatsHtml(CHATS_PANEL_HISTORICO_LOADING)}</p>`;
    }
    if (state === CHATS_HISTORICO_STATE.ERROR) {
        return `<p class="chats-panel-placeholder chats-historico-state--error" role="status">${escapeChatsHtml(CHATS_PANEL_HISTORICO_ERROR)}</p>`;
    }
    if (state === CHATS_HISTORICO_STATE.EMPTY || !hasChatsPanelHistoricoUtil(resumo)) {
        return `<p class="chats-panel-placeholder chats-historico-state--empty">${escapeChatsHtml(CHATS_PANEL_HISTORICO_EMPTY)}</p>`;
    }
    const formatDateTime = options.formatDateTime;
    const parts = [];
    const total = Number(resumo.totalTicketsCliente) || 0;
    if (total > 0) {
        parts.push(
            `<p class="chats-historico-meta"><span class="chats-historico-meta-label">Total de chamados</span> <span class="chats-historico-meta-value">${escapeChatsHtml(String(total))}</span></p>`
        );
    }
    const ultimo = resumo.ultimoTicketEncerrado;
    if (ultimo?.numeroTicket) {
        parts.push(buildChatsHistoricoItemHtml('Último encerrado', ultimo, formatDateTime));
    }
    const recentes = Array.isArray(resumo.ticketsRecentes) ? resumo.ticketsRecentes : [];
    if (recentes.length) {
        parts.push('<p class="chats-historico-subtitle">Recentes</p>');
        parts.push('<ul class="chats-historico-list">');
        for (const item of recentes) {
            parts.push(`<li>${buildChatsHistoricoItemInnerHtml(item, formatDateTime)}</li>`);
        }
        parts.push('</ul>');
    }
    return `<div class="chats-historico-body">${parts.join('')}</div>`;
}

function buildChatsHistoricoItemHtml(title, item, formatDateTime) {
    return `<div class="chats-historico-block"><p class="chats-historico-subtitle">${escapeChatsHtml(title)}</p>${buildChatsHistoricoItemInnerHtml(item, formatDateTime)}</div>`;
}

function buildChatsHistoricoItemInnerHtml(item, formatDateTime) {
    const numero = escapeChatsHtml(item?.numeroTicket || '');
    const statusHtml = formatChatsStatusBadgeHtml(item?.status, { compact: true });
    const dataRef = formatChatsHistoricoDataRef(item, formatDateTime);
    const motivo = formatChatsHistoricoMotivo(item);
    const metaBits = [];
    if (dataRef) {
        metaBits.push(`<span class="chats-historico-item-date">${escapeChatsHtml(dataRef)}</span>`);
    }
    if (motivo) {
        metaBits.push(`<span class="chats-historico-item-motivo">${escapeChatsHtml(motivo)}</span>`);
    }
    const meta = metaBits.length ? `<span class="chats-historico-item-meta">${metaBits.join('')}</span>` : '';
    return `<div class="chats-historico-item"><span class="chats-historico-item-protocol">${numero}</span> ${statusHtml}${meta}</div>`;
}

/** @deprecated use buildChatsPanelHistoricoHtml */
export function getChatsPanelHistoricoContent() {
    return { mode: 'empty', text: CHATS_PANEL_HISTORICO_EMPTY };
}

/** Monta HTML de linhas <dl> do painel. */
export function buildChatsPanelDlHtml(rows) {
    return filterChatsPanelRows(rows)
        .map(row => {
            const dd =
                row.html != null && String(row.html).trim() !== ''
                    ? row.html
                    : escapeChatsHtml(String(row.text != null ? row.text : row.value ?? ''));
            return `<div class="chats-dl-row"><dt>${escapeChatsHtml(row.label)}</dt><dd>${dd}</dd></div>`;
        })
        .join('');
}

/**
 * Rótulo e modo do botão principal no cabeçalho/painel Chats.
 * @returns {{ label: string, mode: 'none'|'detalhes'|'encerrar', disabled: boolean }}
 */
export function getChatsPrimaryActionLabel(ticket) {
    if (!ticket?.numeroTicket) {
        return { label: '', mode: 'none', disabled: true };
    }
    if (isTicketHistoricoEncerrado(ticket)) {
        return { label: 'Detalhes', mode: 'detalhes', disabled: false };
    }
    if (isTicketStatusAtivo(ticket?.status)) {
        return { label: 'Encerrar ticket', mode: 'encerrar', disabled: false };
    }
    return { label: 'Detalhes', mode: 'detalhes', disabled: false };
}

function normalizeStatus(status) {
    return status == null ? '' : String(status).trim().toUpperCase();
}

/** Heurística visual: mensagem do cliente vs atendente (interações do ticket). */
export function isInteracaoFromCliente(interacao) {
    const tipo = String(interacao?.tipoInteracao ?? '').toUpperCase();
    if (tipo.includes('CLIENTE')) {
        return true;
    }
    const vis = String(interacao?.visibilidade ?? '').toUpperCase();
    return vis === 'CLIENTE' || vis === 'PUBLICA_CLIENTE';
}

/** Normaliza id de etiqueta para comparação/seleção. */
export function normalizeEtiquetaId(id) {
    if (id == null || id === '') {
        return null;
    }
    const n = Number(id);
    return Number.isFinite(n) ? n : null;
}

export function sortEtiquetasAtivas(etiquetas) {
    const list = Array.isArray(etiquetas) ? [...etiquetas] : [];
    return list.sort((a, b) => String(a?.nome ?? '').localeCompare(String(b?.nome ?? ''), 'pt-BR'));
}

export function getEtiquetaSelectionForContext(selectionMap, contextKey) {
    if (!contextKey || !selectionMap || typeof selectionMap.get !== 'function') {
        return [];
    }
    const raw = selectionMap.get(contextKey);
    if (!Array.isArray(raw)) {
        return [];
    }
    return raw
        .map(normalizeEtiquetaId)
        .filter(id => id != null);
}

export function toggleEtiquetaInSelection(selectedIds, etiquetaId) {
    const id = normalizeEtiquetaId(etiquetaId);
    if (id == null) {
        return Array.isArray(selectedIds) ? [...selectedIds] : [];
    }
    const set = new Set(
        (Array.isArray(selectedIds) ? selectedIds : [])
            .map(normalizeEtiquetaId)
            .filter(x => x != null)
    );
    if (set.has(id)) {
        set.delete(id);
    } else {
        set.add(id);
    }
    return [...set].sort((a, b) => a - b);
}

export function isEtiquetaSelected(selectedIds, etiquetaId) {
    const id = normalizeEtiquetaId(etiquetaId);
    if (id == null) {
        return false;
    }
    return (Array.isArray(selectedIds) ? selectedIds : [])
        .map(normalizeEtiquetaId)
        .includes(id);
}

/** Cor de etiqueta segura para uso em style inline. */
export function sanitizeEtiquetaCor(cor) {
    const c = String(cor ?? '').trim();
    if (/^#[0-9A-Fa-f]{3,8}$/.test(c)) {
        return c;
    }
    return '';
}

/** Extrai ids de etiquetas retornadas pela API. */
export function etiquetaIdsFromEntities(etiquetas) {
    return (Array.isArray(etiquetas) ? etiquetas : [])
        .map(e => normalizeEtiquetaId(e?.id))
        .filter(id => id != null);
}

/** Ids selecionáveis no painel (somente etiquetas ativas) para envio no PUT. */
export function activeEtiquetaIdsForSave(selectedIds, activeEtiquetas) {
    const activeSet = new Set(etiquetaIdsFromEntities(activeEtiquetas));
    return (Array.isArray(selectedIds) ? selectedIds : [])
        .map(normalizeEtiquetaId)
        .filter(id => id != null && activeSet.has(id));
}

export function validateEtiquetaIdsForSave(selectedIds, activeEtiquetas) {
    const etiquetaIds = activeEtiquetaIdsForSave(selectedIds, activeEtiquetas);
    return { ok: true, etiquetaIds };
}

/**
 * Payload para persistência (vínculo ticket ↔ etiquetas ativas).
 */
export function buildEtiquetaVinculoPayload(contextKey, etiquetaIds, extra = {}) {
    const ids = (Array.isArray(etiquetaIds) ? etiquetaIds : [])
        .map(normalizeEtiquetaId)
        .filter(id => id != null)
        .sort((a, b) => a - b);
    return {
        contextKey: contextKey ? String(contextKey) : '',
        etiquetaIds: ids,
        ...extra
    };
}

export function getClienteIniciais(nome) {
    const parts = String(nome ?? '')
        .trim()
        .split(/\s+/)
        .filter(Boolean);
    if (!parts.length) {
        return '?';
    }
    if (parts.length === 1) {
        return parts[0].slice(0, 2).toUpperCase();
    }
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

export function truncatePreview(text, maxLen = 72) {
    const t = String(text ?? '').trim().replace(/\s+/g, ' ');
    if (!t) {
        return '';
    }
    if (t.length <= maxLen) {
        return t;
    }
    return `${t.slice(0, maxLen)}…`;
}

export function getListPreviewFromTicket(ticket) {
    const raw =
        ticket?.mensagemInicial ||
        ticket?.escalonamentoObservacao ||
        ticket?.comentarioEncerramento ||
        '';
    const preview = truncatePreview(raw);
    return preview || 'Sem prévia de mensagem';
}

export function getDateKeyFromIso(iso) {
    if (iso == null || iso === '') {
        return '';
    }
    const text = String(iso);
    return text.length >= 10 ? text.slice(0, 10) : '';
}

export function formatDateSeparatorLabel(iso) {
    const key = getDateKeyFromIso(iso);
    if (!key) {
        return '';
    }
    const [y, m, d] = key.split('-');
    return `${d}/${m}/${y}`;
}

export function formatChatsListTime(isoDateTime) {
    if (isoDateTime == null || isoDateTime === '') {
        return '';
    }
    const parsed = new Date(String(isoDateTime).includes('T') ? isoDateTime : `${isoDateTime}T12:00:00`);
    if (Number.isNaN(parsed.getTime())) {
        return '';
    }
    const now = new Date();
    const pad = n => String(n).padStart(2, '0');
    if (parsed.toDateString() === now.toDateString()) {
        return `${pad(parsed.getHours())}:${pad(parsed.getMinutes())}`;
    }
    return `${pad(parsed.getDate())}/${pad(parsed.getMonth() + 1)}`;
}

export function countTicketsByTab(tickets) {
    const list = Array.isArray(tickets) ? tickets : [];
    return {
        [CHATS_TAB_CHATS]: filterTicketsForChatsTab(list, CHATS_TAB_CHATS).length,
        [CHATS_TAB_FILA]: filterTicketsForChatsTab(list, CHATS_TAB_FILA).length,
        [CHATS_TAB_HISTORICO]: filterTicketsForChatsTab(list, CHATS_TAB_HISTORICO).length
    };
}

/** Rótulo operacional no Chats (não altera status do ticket no backend). */
const CHATS_CONVERSATION_STATUS_LABELS = {
    ABERTO: 'Aguardando atendimento',
    EM_ATENDIMENTO: 'Em atendimento',
    AGUARDANDO_CLIENTE: 'Aguardando cliente',
    RESOLVIDO: 'Encerrado',
    CANCELADO: 'Cancelado'
};

export function getChatsConversationStatusLabel(status) {
    const s = normalizeStatus(status);
    return CHATS_CONVERSATION_STATUS_LABELS[s] || '—';
}

export function getChatsStatusModifier(status) {
    const s = normalizeStatus(status);
    if (s === 'ABERTO') {
        return 'aberto';
    }
    if (s === 'EM_ATENDIMENTO') {
        return 'em-atendimento';
    }
    if (s === 'AGUARDANDO_CLIENTE') {
        return 'aguardando';
    }
    if (s === 'RESOLVIDO') {
        return 'resolvido';
    }
    if (s === 'CANCELADO') {
        return 'cancelado';
    }
    return 'default';
}

/** Normaliza canal do ticket para exibição multicanal (sem schema novo). */
export function normalizeChatsCanalKey(canal) {
    const raw = canal == null ? '' : String(canal).trim();
    if (!raw) {
        return 'SISTEMA';
    }
    const upper = raw.toUpperCase();
    if (upper.includes('WHATS') || upper === 'WHATSAPP' || upper === 'WPP') {
        return 'WHATSAPP';
    }
    if (upper.includes('API') || upper === 'WEBHOOK' || upper === 'INTEGRACAO') {
        return 'API';
    }
    return 'SISTEMA';
}

export function getChatsCanalDisplay(canal) {
    const key = normalizeChatsCanalKey(canal);
    const labels = {
        WHATSAPP: 'WhatsApp',
        API: 'API',
        SISTEMA: 'Sistema'
    };
    const modifiers = {
        WHATSAPP: 'whatsapp',
        API: 'api',
        SISTEMA: 'sistema'
    };
    return {
        key,
        label: labels[key] || 'Sistema',
        modifier: modifiers[key] || 'sistema'
    };
}

export function getChatsPriorityModifier(priority) {
    const p = priority == null ? '' : String(priority).trim().toUpperCase();
    if (p === 'CRITICA') {
        return 'critica';
    }
    if (p === 'ALTA') {
        return 'alta';
    }
    if (p === 'MEDIA') {
        return 'media';
    }
    if (p === 'BAIXA') {
        return 'baixa';
    }
    return 'default';
}

/** HTML de badge de prioridade com classes semânticas do Chats. */
export function formatChatsPriorityBadgeHtml(prioridade) {
    if (!prioridade || prioridade === '-') {
        return '';
    }
    const mod = getChatsPriorityModifier(prioridade);
    const label = formatPriority(prioridade);
    return `<span class="chats-priority-pill chats-priority-pill--${mod}">${escapeChatsHtml(label)}</span>`;
}

/** HTML de badge de status operacional no Chats. */
export function formatChatsStatusBadgeHtml(status, options = {}) {
    const mod = getChatsStatusModifier(status);
    const label =
        typeof options.formatLabel === 'function'
            ? options.formatLabel(status)
            : getChatsConversationStatusLabel(status);
    const extra = options.compact ? ' chats-status-pill--compact' : '';
    const list = options.list ? ' chats-status-pill--list' : '';
    return `<span class="chats-status-pill chats-status-pill--${mod}${extra}${list}">${escapeChatsHtml(label)}</span>`;
}

/** HTML de canal da conversa (preparação multicanal). */
export function formatChatsCanalBadgeHtml(canal, options = {}) {
    const { label, modifier } = getChatsCanalDisplay(canal);
    const list = options.list ? ' chats-canal-pill--list' : '';
    return `<span class="chats-canal-pill chats-canal-pill--${modifier}${list}">${escapeChatsHtml(label)}</span>`;
}

const CHATS_SLA_SEVERITY = {
    VENCIDO: 5,
    VIOLADO: 5,
    PROXIMO_DO_VENCIMENTO: 4,
    PAUSADO: 3,
    DENTRO_DO_PRAZO: 1,
    CUMPRIDO: 0,
    NAO_CALCULADO: 0
};

export function normalizeChatsSlaStatus(status) {
    if (status == null || status === '') {
        return 'NAO_CALCULADO';
    }
    return String(status).trim().toUpperCase();
}

/** ISO de referência para “tempo de espera” (sem recalcular SLA). */
export function getChatsWaitReferenceIso(ticket, tab, lastInteractionIso = null) {
    if (!ticket) {
        return null;
    }
    const last = lastInteractionIso && String(lastInteractionIso).trim() ? String(lastInteractionIso) : null;
    if (last) {
        return last;
    }
    const status = normalizeStatus(ticket.status);
    if (tab === CHATS_TAB_HISTORICO) {
        return ticket.dataEncerramento || ticket.dataAbertura || null;
    }
    if (status === 'ABERTO') {
        return ticket.dataAbertura || null;
    }
    if (status === 'AGUARDANDO_CLIENTE') {
        return ticket.dataPrimeiroAtendimento || ticket.dataAbertura || null;
    }
    return ticket.dataPrimeiroAtendimento || ticket.dataAbertura || null;
}

export function parseChatsDateTime(iso) {
    if (iso == null || iso === '') {
        return null;
    }
    const text = String(iso);
    const parsed = new Date(text.includes('T') ? text : `${text}T12:00:00`);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
}

/**
 * Duração compacta para o card da lista (sem “há”, sem TME).
 * Ex.: 12min 35s, 9h 12min, 2d.
 */
export function formatChatsListElapsedCompact(iso, referenceNow = new Date()) {
    const parsed = parseChatsDateTime(iso);
    if (!parsed) {
        return '—';
    }
    const diffMs = referenceNow.getTime() - parsed.getTime();
    if (diffMs < 0) {
        return 'agora';
    }
    const totalSec = Math.floor(diffMs / 1000);
    const days = Math.floor(totalSec / 86400);
    if (days >= 1) {
        return `${days}d`;
    }
    const hours = Math.floor(totalSec / 3600);
    const minutes = Math.floor((totalSec % 3600) / 60);
    const seconds = totalSec % 60;
    if (hours >= 1) {
        return minutes > 0 ? `${hours}h ${minutes}min` : `${hours}h`;
    }
    if (minutes >= 1) {
        return seconds > 0 ? `${minutes}min ${seconds}s` : `${minutes}min`;
    }
    return seconds > 0 ? `${seconds}s` : 'agora';
}

/** Tempo relativo curto: “há 5 min”, “há 2h”, “há 1d”. */
export function formatRelativeWaitShort(iso, referenceNow = new Date()) {
    const parsed = parseChatsDateTime(iso);
    if (!parsed) {
        return '';
    }
    const diffMs = referenceNow.getTime() - parsed.getTime();
    if (diffMs < 0) {
        return 'agora';
    }
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) {
        return 'agora';
    }
    if (minutes < 60) {
        return `há ${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    if (hours < 24) {
        return `há ${hours}h`;
    }
    const days = Math.floor(hours / 24);
    return `há ${days}d`;
}

export function getLatestInteractionIso(interacoes) {
    const list = Array.isArray(interacoes) ? interacoes : [];
    let max = '';
    list.forEach(item => {
        const at = item?.criadoEm;
        if (at && String(at) > max) {
            max = String(at);
        }
    });
    return max || null;
}

/** Escolhe o status SLA mais crítico já calculado no backend (exibição apenas). */
export function pickChatsPrimarySlaStatus(ticket) {
    if (!ticket) {
        return 'NAO_CALCULADO';
    }
    if (ticket.slaPausado) {
        return 'PAUSADO';
    }
    const candidates = [
        normalizeChatsSlaStatus(ticket.slaPrimeiroAtendimentoStatus),
        normalizeChatsSlaStatus(getSlaResolucaoVisualStatus(ticket))
    ];
    let picked = 'NAO_CALCULADO';
    let score = -1;
    candidates.forEach(status => {
        const sev = CHATS_SLA_SEVERITY[status] ?? 0;
        if (sev > score) {
            score = sev;
            picked = status;
        }
    });
    return picked;
}

export function getChatsSlaAccentModifier(slaStatus, tab) {
    if (tab === CHATS_TAB_HISTORICO) {
        return 'historico';
    }
    const s = normalizeChatsSlaStatus(slaStatus);
    if (s === 'VENCIDO' || s === 'VIOLADO') {
        return 'vencido';
    }
    if (s === 'PROXIMO_DO_VENCIMENTO') {
        return 'proximo';
    }
    if (s === 'PAUSADO') {
        return 'pausado';
    }
    if (s === 'DENTRO_DO_PRAZO' || s === 'CUMPRIDO') {
        return 'dentro';
    }
    return 'neutro';
}

export function formatChatsSlaDueLine(label, iso, formatDateTimeFn) {
    if (!iso) {
        return '';
    }
    const formatted =
        typeof formatDateTimeFn === 'function' ? formatDateTimeFn(iso) : String(iso);
    if (!formatted || formatted === '-') {
        return '';
    }
    return `${label} ${formatted}`;
}

/** Resumo para lista lateral (espera + destaque SLA). */
export function getChatsListSlaWaitMeta(ticket, tab) {
    const waitIso = getChatsWaitReferenceIso(ticket, tab);
    const waitLabel = formatRelativeWaitShort(waitIso);
    const slaStatus = pickChatsPrimarySlaStatus(ticket);
    const accent = getChatsSlaAccentModifier(slaStatus, tab);
    const slaShort =
        slaStatus === 'NAO_CALCULADO' ? '' : formatSlaStatusLabel(slaStatus);
    return { waitIso, waitLabel, slaStatus, accent, slaShort };
}

export const CHATS_CONEXAO_TITLE_FALLBACK = 'Conexão não informada';

const CHATS_HEADER_ARTE_URL_PREFIX_CONEXAO = '/uploads/conexoes/header-chats/';
const CHATS_HEADER_ARTE_URL_PREFIX_CLIENTE = '/uploads/clientes/header-chats/';

/** Chave de vínculo por nome (ticket.conexao / ticket.carteira ↔ Carteira.nome). */
export function normalizeChatsConexaoMatchKey(value) {
    const text = value == null ? '' : String(value).trim();
    if (!text || text === '-' || text === '—') {
        return '';
    }
    return text.toLowerCase();
}

function sanitizeChatsHeaderArtePublicUrlWithPrefix(url, prefix) {
    const u = url == null ? '' : String(url).trim();
    if (!u.startsWith(prefix)) {
        return null;
    }
    const rest = u.slice(prefix.length);
    if (!rest || rest.includes('..') || rest.includes('\\') || /["'\s]/.test(rest)) {
        return null;
    }
    return u;
}

/** Aceita URL pública de arte do Cliente ou legado Carteira/Conexão. */
export function sanitizeChatsHeaderArtePublicUrl(url) {
    return (
        sanitizeChatsHeaderArtePublicUrlWithPrefix(url, CHATS_HEADER_ARTE_URL_PREFIX_CLIENTE) ||
        sanitizeChatsHeaderArtePublicUrlWithPrefix(url, CHATS_HEADER_ARTE_URL_PREFIX_CONEXAO)
    );
}

/** @deprecated Preferir sanitizeChatsHeaderArtePublicUrl — mantém compatibilidade com testes legados. */
export function sanitizeChatsConexaoHeaderArtePublicUrl(url) {
    return sanitizeChatsHeaderArtePublicUrl(url);
}

/**
 * Arte do header priorizando cadastro do Cliente (Sprint 181).
 * @param {object|null|undefined} ticket
 */
export function resolveChatsClienteHeaderArteUrl(ticket) {
    if (!ticket) {
        return null;
    }
    const nested = ticket?.cliente?.arteHeaderChatsUrl;
    const flat = ticket?.clienteArteHeaderChatsUrl;
    return (
        sanitizeChatsHeaderArtePublicUrl(nested) ||
        sanitizeChatsHeaderArtePublicUrl(flat) ||
        null
    );
}

/** Valor CSS para `--chats-conexao-header-bg-image`. */
export function getChatsConexaoHeaderArteCssValue(url) {
    const safe = sanitizeChatsHeaderArtePublicUrl(url);
    if (!safe) {
        return null;
    }
    const escaped = safe.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
    return `url("${escaped}")`;
}

export function buildCarteirasArteByNomeIndex(carteiras) {
    const map = new Map();
    if (!Array.isArray(carteiras)) {
        return map;
    }
    for (const c of carteiras) {
        const key = normalizeChatsConexaoMatchKey(c?.nome);
        const arteUrl = sanitizeChatsHeaderArtePublicUrl(c?.arteHeaderChatsUrl);
        if (key && arteUrl) {
            map.set(key, arteUrl);
        }
    }
    return map;
}

/**
 * Resolve arte do header (ordem Sprint 181):
 * 1) Cliente (nested ou clienteArteHeaderChatsUrl no ticket)
 * 2) ticket.arteHeaderChatsUrl (futuro / legado direto)
 * 3) clienteArteById (cache opcional)
 * 4) Carteira por nome (fallback temporário)
 * @param {object|null} ticket
 * @param {Map<string, string>|null} carteirasArteByNome
 * @param {Map<number, string>|null} [clienteArteById]
 */
export function resolveChatsConexaoHeaderArteUrl(ticket, carteirasArteByNome, clienteArteById = null) {
    const fromCliente = resolveChatsClienteHeaderArteUrl(ticket);
    if (fromCliente) {
        return fromCliente;
    }
    const onTicket = sanitizeChatsHeaderArtePublicUrl(ticket?.arteHeaderChatsUrl);
    if (onTicket) {
        return onTicket;
    }
    const clienteId = ticket?.clienteId;
    if (clienteId != null && clienteArteById?.size) {
        const fromId = sanitizeChatsHeaderArtePublicUrl(clienteArteById.get(Number(clienteId)));
        if (fromId) {
            return fromId;
        }
    }
    if (!ticket?.numeroTicket || !carteirasArteByNome?.size) {
        return null;
    }
    const keys = [
        normalizeChatsConexaoMatchKey(ticket?.conexao),
        normalizeChatsConexaoMatchKey(ticket?.carteira)
    ].filter(Boolean);
    for (const key of keys) {
        const fromCarteira = carteirasArteByNome.get(key);
        const safe = sanitizeChatsHeaderArtePublicUrl(fromCarteira);
        if (safe) {
            return safe;
        }
    }
    return null;
}

/** Aplica ou remove background WL no `#chatsConexaoHeader`. */
export function applyChatsConexaoHeaderArte(headerEl, ticket, carteirasArteByNome, clienteArteById = null) {
    if (!headerEl) {
        return;
    }
    const bgLayer = headerEl.querySelector('.chats-conexao-header-bg');
    const arteUrl = resolveChatsConexaoHeaderArteUrl(ticket, carteirasArteByNome, clienteArteById);
    const cssValue = getChatsConexaoHeaderArteCssValue(arteUrl);
    if (cssValue) {
        headerEl.classList.add('chats-conexao-header--has-custom-bg');
        bgLayer?.classList.remove('chats-conexao-header--bg-fallback');
        headerEl.style.setProperty('--chats-conexao-header-bg-image', cssValue);
    } else {
        headerEl.classList.remove('chats-conexao-header--has-custom-bg');
        bgLayer?.classList.add('chats-conexao-header--bg-fallback');
        headerEl.style.removeProperty('--chats-conexao-header-bg-image');
    }
}

/** Nome principal da Conexão/Revenda no header central (campo `conexao` + fallbacks seguros). */
export function getChatsConexaoRevendaTitle(ticket) {
    if (!ticket?.numeroTicket) {
        return 'Selecione uma conversa';
    }
    const direct = ticket?.conexao == null ? '' : String(ticket.conexao).trim();
    if (direct && direct !== '-' && direct !== '—') {
        return direct;
    }
    const alt = getChatsListConexaoDisplay(ticket);
    if (alt) {
        return alt;
    }
    return CHATS_CONEXAO_TITLE_FALLBACK;
}

/** Indicação discreta do canal ao lado da marca (pill só fora de Sistema). */
export function getChatsHeaderCanalBrandHtml(ticket) {
    if (!ticket?.numeroTicket) {
        return '';
    }
    const { label, modifier, key } = getChatsCanalDisplay(ticket?.canal);
    if (key === 'SISTEMA') {
        return `<span class="chats-conexao-canal-label chats-conexao-canal-label--sistema">${escapeChatsHtml(label)}</span>`;
    }
    return `<span class="chats-canal-pill chats-canal-pill--${modifier} chats-canal-pill--header">${escapeChatsHtml(label)}</span>`;
}

/** Metadados secundários: protocolo e abertura (sem nome do cliente). */
export function getChatsHeaderMetaProtocolAberturaHtml(ticket, tab, options = {}) {
    if (!ticket?.numeroTicket) {
        return 'Protocolo —';
    }
    const displayValue =
        typeof options.displayValue === 'function' ? options.displayValue : v => String(v ?? '—');
    const formatDateTime = options.formatDateTime;
    const parts = [`Protocolo ${displayValue(ticket.numeroTicket)}`];
    if (ticket?.dataAbertura && typeof formatDateTime === 'function') {
        const abertura = formatDateTime(ticket.dataAbertura);
        if (!isChatsPanelValueEmpty(abertura)) {
            parts.push(`Abertura ${abertura}`);
        }
    }
    return escapeChatsHtml(parts.join(' · '));
}

/** Conexão secundária no card (não exibe canal “Sistema” nem valores vazios). */
export function getChatsListConexaoDisplay(ticket) {
    const candidates = [ticket?.conexao, ticket?.empresa, ticket?.carteira];
    for (const raw of candidates) {
        const text = raw == null ? '' : String(raw).trim();
        if (!text || text === '-' || text === '—') {
            continue;
        }
        if (text.toLowerCase() === 'sistema') {
            continue;
        }
        return text;
    }
    return null;
}

/** Metadados do bloco temporal no card da lista (sem pills). */
export function getChatsListTemporalMeta(ticket, tab) {
    const waitIso = getChatsWaitReferenceIso(ticket, tab);
    if (tab === CHATS_TAB_FILA) {
        return {
            label: 'Aguardando atendimento',
            showLabel: true,
            waitIso,
            largeCounter: true,
            liveCounter: true
        };
    }
    if (tab === CHATS_TAB_HISTORICO) {
        return {
            label: '',
            showLabel: false,
            waitIso,
            largeCounter: false,
            liveCounter: false
        };
    }
    return {
        label: '',
        showLabel: false,
        waitIso,
        largeCounter: true,
        liveCounter: true
    };
}

/** HTML do card minimalista da lista esquerda (sem avatar). */
export function buildChatsListCardHtml(ticket, tab, options = {}) {
    const displayValue =
        typeof options.displayValue === 'function' ? options.displayValue : v => String(v ?? '—');
    if (isChatsPendenciaDecisaoItem(ticket)) {
        const nome = displayValue(ticket?.cliente);
        const ref = ticket?.numeroTicketAnterior
            ? displayValue(ticket.numeroTicketAnterior)
            : '—';
        return `
            <span class="chats-list-item-main">
                <span class="chats-list-item-name">${escapeChatsHtml(nome)}</span>
                <span class="chats-list-item-ticket chats-list-item-ticket--pendencia">Nova mensagem após encerramento · ${escapeChatsHtml(ref)}</span>
            </span>
            <span class="chats-list-item-timecol">
                <span class="chats-list-item-time-label">Decisão</span>
                <span class="chats-list-item-time-counter chats-list-item-time-counter--pendencia">!</span>
            </span>
        `;
    }
    const nome = displayValue(ticket?.cliente);
    const temporal = getChatsListTemporalMeta(ticket, tab);
    const counterText = temporal.waitIso
        ? formatChatsListElapsedCompact(temporal.waitIso)
        : '—';
    const counterClass = temporal.largeCounter
        ? 'chats-list-item-time-counter'
        : 'chats-list-item-time-counter chats-list-item-time-counter--muted';
    const timecolClass =
        tab === CHATS_TAB_HISTORICO
            ? 'chats-list-item-timecol chats-list-item-timecol--encerrados'
            : 'chats-list-item-timecol';
    const labelLine =
        temporal.showLabel && temporal.label
            ? `<span class="chats-list-item-time-label">${escapeChatsHtml(temporal.label)}</span>`
            : '';
    const waitIsoAttr =
        temporal.liveCounter && temporal.waitIso
            ? ` data-chats-wait-iso="${escapeChatsHtml(String(temporal.waitIso))}"`
            : '';
    return `
            <span class="chats-list-item-main">
                <span class="chats-list-item-name">${escapeChatsHtml(nome)}</span>
                <span class="chats-list-item-ticket">${escapeChatsHtml(displayValue(ticket?.numeroTicket))}</span>
            </span>
            <span class="${timecolClass}">
                ${labelLine}
                <span class="${counterClass}"${waitIsoAttr}>${escapeChatsHtml(counterText)}</span>
            </span>
        `;
}

export function formatChatsListWaitSlaHtml(ticket, tab) {
    const meta = getChatsListSlaWaitMeta(ticket, tab);
    const waitPart = meta.waitLabel
        ? `<span class="chats-list-wait chats-list-wait--${meta.accent}">${escapeChatsHtml(meta.waitLabel)}</span>`
        : '';
    const slaPart =
        meta.slaShort && meta.accent !== 'dentro' && meta.accent !== 'neutro' && meta.accent !== 'historico'
            ? `<span class="chats-list-sla-hint chats-list-sla-hint--${meta.accent}">${escapeChatsHtml(meta.slaShort)}</span>`
            : meta.slaStatus === 'NAO_CALCULADO' && tab !== CHATS_TAB_HISTORICO
              ? `<span class="chats-list-sla-hint chats-list-sla-hint--neutro">SLA —</span>`
              : '';
    if (!waitPart && !slaPart) {
        return '';
    }
    return `<span class="chats-list-wait-row">${waitPart}${slaPart}</span>`;
}

/** Bloco discreto SLA/espera no cabeçalho central. */
export function buildChatsHeaderSlaHtml(ticket, tab, options = {}) {
    const formatDateTimeFn = options.formatDateTime;
    const lastIso = options.lastInteractionIso || null;
    const waitIso = getChatsWaitReferenceIso(ticket, tab, lastIso);
    const waitLabel = formatRelativeWaitShort(waitIso);
    const slaStatus = pickChatsPrimarySlaStatus(ticket);
    const accent = getChatsSlaAccentModifier(slaStatus, tab);
    const slaBadge =
        slaStatus === 'NAO_CALCULADO'
            ? '<span class="chats-sla-line chats-sla-line--neutro">SLA não calculado</span>'
            : formatSlaBadgeHtml(slaStatus).replace(
                  'sla-badge',
                  'chats-sla-badge sla-badge'
              );
    const lines = [];
    if (waitLabel) {
        lines.push(
            `<span class="chats-sla-wait chats-sla-wait--${accent}">Espera ${escapeChatsHtml(waitLabel)}</span>`
        );
    }
    const v1 = formatChatsSlaDueLine('1º atend.:', ticket?.slaPrimeiroAtendimentoVencimento, formatDateTimeFn);
    const v2 = formatChatsSlaDueLine('Resolução:', ticket?.slaResolucaoVencimento, formatDateTimeFn);
    if (v1) {
        lines.push(`<span class="chats-sla-due">${escapeChatsHtml(v1)}</span>`);
    }
    if (v2) {
        lines.push(`<span class="chats-sla-due">${escapeChatsHtml(v2)}</span>`);
    }
    return `<div class="chats-header-sla-inner chats-header-sla-inner--${accent}">${lines.join('')}${slaBadge}</div>`;
}

/** @deprecated use getChatsHeaderMetaProtocolAberturaHtml + getChatsHeaderCanalBrandHtml */
export function getChatsHeaderProtocolCanalHtml(tab, ticket, displayValue = v => String(v ?? '—')) {
    return getChatsHeaderMetaProtocolAberturaHtml(ticket, tab, { displayValue });
}

function escapeChatsHtml(text) {
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

/** Timeline unificada: eventos do ticket + separadores de data + mensagens. */
export const OBSERVACAO_ATENDIMENTO_MAX_LENGTH = 2000;

/** Normaliza texto para exibição no textarea (não trunca). */
export function observacaoAtendimentoParaTextarea(valor) {
    if (valor == null || valor === undefined) {
        return '';
    }
    const t = String(valor).trim();
    if (t === '-' || t === '—') {
        return '';
    }
    return t;
}

/** Valida antes de salvar; texto vazio é permitido (limpar). */
export function validateObservacaoAtendimentoForSave(text) {
    const raw = text == null ? '' : String(text);
    const trimmed = raw.trim();
    if (trimmed.length > OBSERVACAO_ATENDIMENTO_MAX_LENGTH) {
        return {
            ok: false,
            message: `A observação pode ter no máximo ${OBSERVACAO_ATENDIMENTO_MAX_LENGTH} caracteres.`
        };
    }
    return { ok: true, value: trimmed };
}

export function buildChatsTimelineEntries(ticket, interacoes, options = {}) {
    const satisfacao = options?.satisfacao ?? null;
    const timed = [];

    const pushEvent = (at, text, sub) => {
        if (!at) {
            return;
        }
        timed.push({
            kind: 'event',
            at,
            text,
            sub: sub != null ? String(sub) : ''
        });
    };

    pushEvent(
        ticket?.dataAbertura,
        'Chamado aberto',
        ticket?.numeroTicket ? String(ticket.numeroTicket) : ''
    );
    pushEvent(ticket?.dataPrimeiroAtendimento, 'Atendimento iniciado', '');
    if (ticket?.escalonado) {
        pushEvent(
            ticket?.escalonadoEm || ticket?.dataAbertura,
            'Chamado escalonado',
            truncatePreview(ticket?.escalonamentoObservacao, 120)
        );
    }

    const sorted = [...(Array.isArray(interacoes) ? interacoes : [])].sort((a, b) =>
        String(a?.criadoEm ?? '').localeCompare(String(b?.criadoEm ?? ''))
    );
    sorted.forEach(interacao => {
        const tipo = String(interacao?.tipoInteracao ?? '').toUpperCase();
        if (tipo === 'ENCERRAMENTO') {
            pushEvent(
                interacao.criadoEm,
                'Registro de encerramento',
                truncatePreview(interacao.mensagem, 120)
            );
            return;
        }
        if (interacao?.criadoEm) {
            timed.push({ kind: 'message', at: interacao.criadoEm, interacao });
        }
    });

    const statusFinal = ticket?.status ? String(ticket.status) : '';
    const encerramentoSub = [statusFinal, truncatePreview(ticket?.comentarioEncerramento, 80)]
        .filter(Boolean)
        .join(' · ');
    pushEvent(ticket?.dataEncerramento, 'Chamado encerrado', encerramentoSub);

    if (satisfacao && satisfacao.nota != null) {
        pushEvent(
            satisfacao.criadoEm || ticket?.dataEncerramento,
            'Avaliação de satisfação',
            formatSatisfacaoTimelineSub(satisfacao)
        );
    }

    timed.sort((a, b) => String(a.at ?? '').localeCompare(String(b.at ?? '')));

    const entries = [];
    let lastDateKey = '';
    timed.forEach(item => {
        const dateKey = getDateKeyFromIso(item.at);
        if (dateKey && dateKey !== lastDateKey) {
            entries.push({
                kind: 'date',
                dateKey,
                label: formatDateSeparatorLabel(item.at)
            });
            lastDateKey = dateKey;
        }
        if (item.kind === 'event') {
            entries.push({
                kind: 'event',
                at: item.at,
                text: item.text,
                sub: item.sub
            });
        } else {
            entries.push({ kind: 'message', interacao: item.interacao });
        }
    });
    return entries;
}
