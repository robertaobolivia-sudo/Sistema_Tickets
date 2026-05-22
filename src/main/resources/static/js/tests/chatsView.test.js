import { describe, expect, it } from 'vitest';
import {
    CHATS_TAB_FILA,
    CHATS_TAB_HISTORICO,
    CHATS_TAB_CHATS,
    buildChatsTimelineEntries,
    observacaoAtendimentoParaTextarea,
    validateObservacaoAtendimentoForSave,
    buildEtiquetaVinculoPayload,
    etiquetaIdsFromEntities,
    activeEtiquetaIdsForSave,
    validateEtiquetaIdsForSave,
    countTicketsByTab,
    filterTicketsForChatsTab,
    filterTicketsBySearchTerm,
    getClienteIniciais,
    getEtiquetaSelectionForContext,
    getListPreviewFromTicket,
    isEtiquetaSelected,
    isInteracaoFromCliente,
    toggleEtiquetaInSelection,
    getChatsConversationTitle,
    getChatsProtocolLine,
    isChatsConversationReadOnly,
    isTicketHistoricoEncerrado,
    isTicketStatusAtivo,
    getChatsPrimaryActionLabel,
    getChatsPriorityModifier,
    getChatsStatusModifier,
    getChatsConversationStatusLabel,
    normalizeChatsCanalKey,
    getChatsCanalDisplay,
    formatChatsStatusBadgeHtml,
    formatChatsCanalBadgeHtml,
    getChatsHeaderProtocolCanalHtml,
    getChatsConexaoRevendaTitle,
    buildCarteirasArteByNomeIndex,
    resolveChatsConexaoHeaderArteUrl,
    resolveChatsClienteHeaderArteUrl,
    sanitizeChatsHeaderArtePublicUrl,
    sanitizeChatsConexaoHeaderArtePublicUrl,
    getChatsConexaoHeaderArteCssValue,
    getChatsHeaderCanalBrandHtml,
    getChatsHeaderMetaProtocolAberturaHtml,
    CHATS_CONEXAO_TITLE_FALLBACK,
    formatRelativeWaitShort,
    formatChatsListElapsedCompact,
    getChatsWaitReferenceIso,
    pickChatsPrimarySlaStatus,
    getChatsSlaAccentModifier,
    getLatestInteractionIso,
    getChatsListConexaoDisplay,
    getChatsListTemporalMeta,
    buildChatsListCardHtml,
    formatChatsPriorityBadgeHtml,
    CHATS_TIMELINE_SELECT_MSG,
    CHATS_CONVERSA_LOADING_MSG,
    CHATS_CONVERSA_LOADING_TITLE,
    getChatsHeaderLoadingProtocolText,
    isChatsPanelValueEmpty,
    filterChatsPanelRows,
    getChatsPanelClienteRows,
    getChatsPanelContatoRows,
    getChatsPanelEntradaRows,
    getChatsPanelChamadoRows,
    hasChatsPanelContatoId,
    chatsPanelRowsContainForbiddenLabel,
    CHATS_PANEL_LEGACY_HINT,
    resolveChatsEtiquetasSource,
    getChatsEtiquetasContextKey,
    CHATS_ETIQUETAS_SOURCE_CONTATO,
    CHATS_ETIQUETAS_SOURCE_TICKET,
    CHATS_ETIQUETAS_LEGACY_HINT,
    mapPendenciaToChatsListItem,
    parseChatsPendenciaListKey,
    isChatsPendenciaDecisaoItem,
    buildChatsPanelHistoricoHtml,
    hasChatsPanelHistoricoUtil,
    CHATS_PANEL_HISTORICO_EMPTY,
    CHATS_PANEL_HISTORICO_ERROR,
    CHATS_PANEL_HISTORICO_LOADING,
    CHATS_HISTORICO_STATE,
    formatChatsComposerCharCount,
    CHATS_COMPOSER_MAX_LENGTH,
    isChatsPanelDrawerViewport,
    CHATS_PANEL_DRAWER_MAX_WIDTH,
    resolveChatsPanelHistoricoState,
    buildChatsPanelSlaSummaryText,
    chatsPanelValuesEqual,
    normalizeChatsPanelPhone,
    sortTicketsForChatsList,
    ticketMatchesSearchTerm,
    formatSatisfacaoTimelineSub,
    formatAnexoTamanho,
    getAnexoCategoriaLabel
} from '../core/chatsView.js';

describe('chatsView', () => {
    const tickets = [
        { numeroTicket: 'T1', status: 'ABERTO', cliente: 'A' },
        { numeroTicket: 'T2', status: 'EM_ATENDIMENTO', cliente: 'B' },
        { numeroTicket: 'T3', status: 'RESOLVIDO', cliente: 'C' }
    ];

    it('filtra aba fila', () => {
        const fila = filterTicketsForChatsTab(tickets, CHATS_TAB_FILA);
        expect(fila).toHaveLength(1);
        expect(fila[0].numeroTicket).toBe('T1');
    });

    it('filtra aba chats', () => {
        const chats = filterTicketsForChatsTab(tickets, CHATS_TAB_CHATS);
        expect(chats).toHaveLength(1);
        expect(chats[0].numeroTicket).toBe('T2');
    });

    it('filtra aba historico', () => {
        const hist = filterTicketsForChatsTab(tickets, CHATS_TAB_HISTORICO);
        expect(hist).toHaveLength(1);
        expect(hist[0].numeroTicket).toBe('T3');
    });

    it('busca por termo', () => {
        const r = filterTicketsBySearchTerm(tickets, 'EM_ATENDIMENTO');
        expect(r).toHaveLength(1);
        expect(r[0].numeroTicket).toBe('T2');
    });

    it('detecta interacao do cliente', () => {
        expect(isInteracaoFromCliente({ tipoInteracao: 'MENSAGEM_CLIENTE' })).toBe(true);
        expect(isInteracaoFromCliente({ visibilidade: 'INTERNA' })).toBe(false);
    });

    it('alterna seleção de etiquetas', () => {
        let sel = toggleEtiquetaInSelection([], 1);
        expect(sel).toEqual([1]);
        sel = toggleEtiquetaInSelection(sel, 2);
        expect(sel).toEqual([1, 2]);
        sel = toggleEtiquetaInSelection(sel, 1);
        expect(sel).toEqual([2]);
        expect(isEtiquetaSelected(sel, 2)).toBe(true);
    });

    it('mantém seleção por contexto em mapa', () => {
        const map = new Map();
        map.set('contato:5', [1]);
        expect(getEtiquetaSelectionForContext(map, 'contato:5')).toEqual([1]);
        expect(getEtiquetaSelectionForContext(map, 'ticket:TK-2')).toEqual([]);
    });

    it('monta payload de vínculo', () => {
        const p = buildEtiquetaVinculoPayload('contato:9', [3, 1]);
        expect(p.contextKey).toBe('contato:9');
        expect(p.etiquetaIds).toEqual([1, 3]);
    });

    it('extrai ids de etiquetas da API', () => {
        expect(etiquetaIdsFromEntities([{ id: 2 }, { id: '3' }, {}])).toEqual([2, 3]);
    });

    it('filtra ids ativos para salvar', () => {
        const ativas = [{ id: 1 }, { id: 2 }];
        expect(activeEtiquetaIdsForSave([1, 2, 99], ativas)).toEqual([1, 2]);
        const v = validateEtiquetaIdsForSave([2, 5], ativas);
        expect(v.ok).toBe(true);
        expect(v.etiquetaIds).toEqual([2]);
    });

    it('gera iniciais do cliente', () => {
        expect(getClienteIniciais('Maria Silva')).toBe('MS');
        expect(getClienteIniciais('')).toBe('?');
    });

    it('prévia da lista usa mensagem inicial', () => {
        const p = getListPreviewFromTicket({ mensagemInicial: 'Olá, preciso de ajuda' });
        expect(p).toContain('ajuda');
    });

    it('conta tickets por aba', () => {
        const c = countTicketsByTab([
            { status: 'ABERTO' },
            { status: 'EM_ATENDIMENTO' },
            { status: 'RESOLVIDO' }
        ]);
        expect(c[CHATS_TAB_FILA]).toBe(1);
        expect(c[CHATS_TAB_CHATS]).toBe(1);
        expect(c[CHATS_TAB_HISTORICO]).toBe(1);
    });

    it('valida observação de atendimento para salvar', () => {
        expect(validateObservacaoAtendimentoForSave('  ok  ').value).toBe('ok');
        expect(validateObservacaoAtendimentoForSave('').ok).toBe(true);
        expect(validateObservacaoAtendimentoForSave('x'.repeat(2001)).ok).toBe(false);
    });

    it('textarea ignora traço legado', () => {
        expect(observacaoAtendimentoParaTextarea('-')).toBe('');
        expect(observacaoAtendimentoParaTextarea('texto')).toBe('texto');
    });

    it('timeline inclui evento de abertura e mensagem', () => {
        const entries = buildChatsTimelineEntries(
            { dataAbertura: '2026-05-20T10:00:00', numeroTicket: 'T1' },
            [{ criadoEm: '2026-05-20T11:00:00', mensagem: 'Oi', tipoInteracao: 'NOTA' }]
        );
        expect(entries.some(e => e.kind === 'event')).toBe(true);
        expect(entries.some(e => e.kind === 'message')).toBe(true);
    });

    it('busca por telefone e protocolo', () => {
        const t = {
            numeroTicket: 'TK-100',
            cliente: 'Loja',
            telefoneContato: '11999990000'
        };
        expect(ticketMatchesSearchTerm(t, 'tk-100')).toBe(true);
        expect(ticketMatchesSearchTerm(t, '99999')).toBe(true);
        expect(ticketMatchesSearchTerm(t, 'outro')).toBe(false);
    });

    it('ordena histórico por encerramento mais recente', () => {
        const sorted = sortTicketsForChatsList(
            [
                { numeroTicket: 'A', status: 'RESOLVIDO', dataEncerramento: '2026-05-01T10:00:00' },
                { numeroTicket: 'B', status: 'RESOLVIDO', dataEncerramento: '2026-05-10T10:00:00' }
            ],
            CHATS_TAB_HISTORICO
        );
        expect(sorted[0].numeroTicket).toBe('B');
    });

    it('identifica status de ticket ativo', () => {
        expect(isTicketStatusAtivo('EM_ATENDIMENTO')).toBe(true);
        expect(isTicketStatusAtivo('RESOLVIDO')).toBe(false);
    });

    it('título e readonly no histórico', () => {
        const ticket = { numeroTicket: 'T1', cliente: 'Ana', status: 'RESOLVIDO' };
        expect(getChatsConversationTitle(CHATS_TAB_HISTORICO, ticket)).toBe('Histórico da conversa');
        expect(isChatsConversationReadOnly(CHATS_TAB_HISTORICO, ticket)).toBe(true);
        expect(isTicketHistoricoEncerrado(ticket)).toBe(true);
    });

    it('linha de protocolo no histórico inclui cliente', () => {
        const line = getChatsProtocolLine(
            CHATS_TAB_HISTORICO,
            { numeroTicket: 'T9', cliente: 'Beta' },
            v => v
        );
        expect(line).toContain('Beta');
        expect(line).toContain('T9');
    });

    it('formata tamanho e categoria de anexo', () => {
        expect(formatAnexoTamanho(500)).toBe('500 B');
        expect(formatAnexoTamanho(2048)).toContain('KB');
        expect(getAnexoCategoriaLabel('image/png', 'foto.png')).toBe('Imagem');
        expect(getAnexoCategoriaLabel('application/pdf', 'doc.pdf')).toBe('Documento');
    });

    it('detecta valor vazio no painel direito', () => {
        expect(isChatsPanelValueEmpty(null)).toBe(true);
        expect(isChatsPanelValueEmpty('-')).toBe(true);
        expect(isChatsPanelValueEmpty('—')).toBe(true);
        expect(isChatsPanelValueEmpty('Cliente X')).toBe(false);
    });

    it('mensagem central quando nenhuma conversa selecionada', () => {
        expect(CHATS_TIMELINE_SELECT_MSG).toContain('Selecione uma conversa');
        expect(CHATS_CONVERSA_LOADING_MSG).toBe('Carregando conversa…');
        expect(CHATS_CONVERSA_LOADING_TITLE).toBe(CHATS_CONVERSA_LOADING_MSG);
        expect(getChatsHeaderLoadingProtocolText('TK-99')).toBe('Protocolo TK-99');
        expect(getChatsHeaderLoadingProtocolText('')).toBe('Protocolo —');
    });

    it('botão principal: encerrar para ticket ativo', () => {
        const action = getChatsPrimaryActionLabel({
            numeroTicket: 'T1',
            status: 'EM_ATENDIMENTO'
        });
        expect(action).toEqual({
            label: 'Encerrar ticket',
            mode: 'encerrar',
            disabled: false
        });
    });

    it('prioridade média e status em atendimento usam modificadores distintos', () => {
        expect(getChatsPriorityModifier('MEDIA')).toBe('media');
        expect(getChatsStatusModifier('EM_ATENDIMENTO')).toBe('em-atendimento');
        expect(getChatsPriorityModifier('MEDIA')).not.toBe(getChatsStatusModifier('EM_ATENDIMENTO'));
    });

    it('rótulos operacionais de status da conversa', () => {
        expect(getChatsConversationStatusLabel('ABERTO')).toBe('Aguardando atendimento');
        expect(getChatsConversationStatusLabel('EM_ATENDIMENTO')).toBe('Em atendimento');
        expect(getChatsConversationStatusLabel('AGUARDANDO_CLIENTE')).toBe('Aguardando cliente');
        expect(getChatsConversationStatusLabel('RESOLVIDO')).toBe('Encerrado');
        expect(getChatsConversationStatusLabel('CANCELADO')).toBe('Cancelado');
    });

    it('formatChatsStatusBadgeHtml não usa label genérico Ativo', () => {
        const html = formatChatsStatusBadgeHtml('ABERTO');
        expect(html).toContain('Aguardando atendimento');
        expect(html).not.toContain('Ativo');
    });

    it('painel direito: oculta campos vazios e monta seções', () => {
        const full = {
            numeroTicket: 'TK-1',
            cliente: 'Maria',
            telefone: '11999990000',
            email: 'm@x.com',
            empresa: 'ACME',
            contatoSolicitanteNome: 'João',
            status: 'EM_ATENDIMENTO',
            prioridade: 'MEDIA',
            canal: 'API',
            dataAbertura: '2026-05-20T10:00:00'
        };
        expect(getChatsPanelClienteRows(full).some(r => r.label === 'Nome')).toBe(true);
        expect(getChatsPanelClienteRows({ numeroTicket: 'T', cliente: 'A' }).some(r => r.label === 'CNPJ')).toBe(
            false
        );
        expect(getChatsPanelContatoRows(full).some(r => r.label === 'Nome')).toBe(true);
        const chamado = getChatsPanelChamadoRows(full, { tab: CHATS_TAB_CHATS });
        expect(chamado.some(r => r.label === 'Protocolo')).toBe(true);
        expect(chamado.find(r => r.label === 'Canal')?.text).toBe('API');
        expect(chamado.some(r => r.label === 'Status')).toBe(false);
        expect(buildChatsPanelHistoricoHtml(null, { state: CHATS_HISTORICO_STATE.EMPTY })).toContain(
            CHATS_PANEL_HISTORICO_EMPTY
        );
        expect(buildChatsPanelHistoricoHtml(null, { state: CHATS_HISTORICO_STATE.ERROR })).toContain(
            CHATS_PANEL_HISTORICO_ERROR
        );
        expect(buildChatsPanelHistoricoHtml(null, { state: CHATS_HISTORICO_STATE.ERROR })).not.toContain(
            CHATS_PANEL_HISTORICO_EMPTY
        );
        expect(buildChatsPanelHistoricoHtml(null, { state: CHATS_HISTORICO_STATE.LOADING })).toContain(
            CHATS_PANEL_HISTORICO_LOADING
        );
        expect(resolveChatsPanelHistoricoState({ ok: false })).toBe(CHATS_HISTORICO_STATE.ERROR);
        expect(resolveChatsPanelHistoricoState({ ok: true, data: { totalTicketsCliente: 1 } })).toBe(
            CHATS_HISTORICO_STATE.EMPTY
        );
        const resumo = {
            totalTicketsCliente: 3,
            ultimoTicketEncerrado: {
                numeroTicket: 'TK-1',
                status: 'RESOLVIDO',
                dataEncerramento: '2026-05-19T10:00:00',
                grupoCategoriaNome: 'Rede'
            },
            ticketsRecentes: [
                { numeroTicket: 'TK-2', status: 'ABERTO', dataAbertura: '2026-05-20T08:00:00' }
            ]
        };
        expect(hasChatsPanelHistoricoUtil(resumo)).toBe(true);
        const html = buildChatsPanelHistoricoHtml(resumo, {
            formatDateTime: iso => (iso ? '20/05/2026' : '')
        });
        expect(html).toContain('Total de chamados');
        expect(html).toContain('TK-1');
        expect(html).not.toContain('TK-ATUAL');
        expect(filterChatsPanelRows([{ label: 'X', value: '—' }])).toHaveLength(0);
        expect(buildChatsPanelSlaSummaryText(full, CHATS_TAB_CHATS)).toContain('Espera');
    });

    it('painel: deduplica empresa/carteira/telefone e contato sem atendente interno', () => {
        const fenix = {
            numeroTicket: 'TK-99',
            cliente: 'Fênix',
            telefone: '11987654321',
            telefoneContato: '(11) 98765-4321',
            email: 'contato@fenix.com.br',
            empresa: 'Fênix',
            carteira: 'Fênix',
            contatoSolicitanteNome: 'Fênix',
            contatoSolicitanteTelefone: '11987654321',
            analistaResponsavelNome: 'João Falcone',
            canal: 'API',
            dataAbertura: '2026-05-20T10:00:00'
        };
        const labels = getChatsPanelClienteRows(fenix).map(r => r.label);
        expect(labels).toContain('Nome');
        expect(labels).toContain('Telefone');
        expect(labels).toContain('E-mail');
        expect(labels).not.toContain('Empresa');
        expect(labels).not.toContain('Carteira');
        expect(labels).not.toContain('Tel. contato');
        const contato = getChatsPanelContatoRows(fenix);
        expect(contato.some(r => r.label === 'Atendente')).toBe(false);
        expect(contato.some(r => r.label === 'Solicitante')).toBe(false);
        expect(chatsPanelValuesEqual('(11) 98765-4321', '11987654321', { phone: true })).toBe(true);
        const comSolicitante = getChatsPanelContatoRows({
            ...fenix,
            contatoSolicitanteNome: 'Carlos Lima',
            contatoSolicitanteTelefone: '11911112222'
        });
        expect(comSolicitante.some(r => r.label === 'Nome')).toBe(true);
        expect(comSolicitante.some(r => r.label === 'WhatsApp')).toBe(true);
    });

    it('Sprint 193: painel com contatoId separa Cliente, Contato e Entrada', () => {
        const novo = {
            numeroTicket: 'TK-200',
            contatoId: 10,
            cliente: 'Fênix',
            clienteId: 1,
            email: 'contrato@fenix.com.br',
            empresa: 'Fênix Ltda',
            contatoNome: 'Carlos Souza',
            contatoWhatsapp: '11 98888-7777',
            contatoEmail: 'carlos@empresa.com',
            contatoEmpresaLocal: 'Loja Centro',
            contatoCidade: 'São Paulo',
            contatoUf: 'SP',
            whatsappMatrizNumero: '11 3000-0001',
            whatsappMatrizNome: 'Matriz SP',
            canal: 'WHATSAPP',
            dataAbertura: '2026-05-20T10:00:00'
        };
        expect(hasChatsPanelContatoId(novo)).toBe(true);
        const cliente = getChatsPanelClienteRows(novo);
        expect(cliente.some(r => r.label === 'Nome' && r.value === 'Fênix')).toBe(true);
        expect(cliente.some(r => r.label === 'Telefone')).toBe(false);
        expect(chatsPanelRowsContainForbiddenLabel(cliente)).toBe(false);
        const contato = getChatsPanelContatoRows(novo);
        expect(contato.some(r => r.label === 'WhatsApp')).toBe(true);
        expect(contato.some(r => r.label === 'Empresa/local')).toBe(true);
        expect(chatsPanelRowsContainForbiddenLabel(contato)).toBe(false);
        const entrada = getChatsPanelEntradaRows(novo);
        expect(entrada.some(r => r.label === 'WhatsApp matriz')).toBe(true);
        const chamado = getChatsPanelChamadoRows(novo, { tab: CHATS_TAB_CHATS });
        expect(chamado.some(r => r.label === 'Protocolo')).toBe(true);
        const all = [...cliente, ...contato, ...entrada, ...chamado];
        expect(chatsPanelRowsContainForbiddenLabel(all)).toBe(false);
    });

    it('Sprint 195: pendência pós-encerramento na lista', () => {
        const item = mapPendenciaToChatsListItem({
            id: 3,
            contatoNome: 'Carlos',
            numeroTicketAnterior: 'TK-10',
            criadaEm: '2026-05-21T10:00:00'
        });
        expect(isChatsPendenciaDecisaoItem(item)).toBe(true);
        expect(item.numeroTicket).toBe('PEND-3');
        expect(parseChatsPendenciaListKey('PEND-3')).toBe(3);
        const html = buildChatsListCardHtml(item, CHATS_TAB_FILA);
        expect(html).toContain('Nova mensagem após encerramento');
    });

    it('Sprint 194: contexto de etiquetas por contato ou ticket', () => {
        const comContato = { numeroTicket: 'TK-1', contatoId: 7 };
        expect(resolveChatsEtiquetasSource(comContato)).toBe(CHATS_ETIQUETAS_SOURCE_CONTATO);
        expect(getChatsEtiquetasContextKey(comContato)).toBe('contato:7');
        const legado = { numeroTicket: 'TK-2' };
        expect(resolveChatsEtiquetasSource(legado)).toBe(CHATS_ETIQUETAS_SOURCE_TICKET);
        expect(getChatsEtiquetasContextKey(legado)).toBe('ticket:TK-2');
        expect(CHATS_ETIQUETAS_LEGACY_HINT).toContain('legado');
    });

    it('Sprint 193: ticket sem contatoId mantém fallback legado', () => {
        const legado = {
            numeroTicket: 'TK-1',
            cliente: 'Maria',
            telefone: '11999990000',
            contatoSolicitanteNome: 'João',
            dataAbertura: '2026-05-20T10:00:00'
        };
        expect(hasChatsPanelContatoId(legado)).toBe(false);
        const contato = getChatsPanelContatoRows(legado);
        expect(contato.some(r => r.html && String(r.html).includes(CHATS_PANEL_LEGACY_HINT))).toBe(
            true
        );
        expect(getChatsPanelEntradaRows(legado)).toHaveLength(0);
    });

    it('composer: contador de caracteres formatado', () => {
        expect(formatChatsComposerCharCount(0)).toBe(`0 / ${CHATS_COMPOSER_MAX_LENGTH}`);
        expect(formatChatsComposerCharCount(120)).toBe(`120 / ${CHATS_COMPOSER_MAX_LENGTH}`);
        expect(formatChatsComposerCharCount(99999)).toBe(`${CHATS_COMPOSER_MAX_LENGTH} / ${CHATS_COMPOSER_MAX_LENGTH}`);
    });

    it('responsividade: drawer do painel em viewport estreita', () => {
        expect(CHATS_PANEL_DRAWER_MAX_WIDTH).toBe(1100);
        expect(isChatsPanelDrawerViewport(1100)).toBe(true);
        expect(isChatsPanelDrawerViewport(1101)).toBe(false);
        expect(isChatsPanelDrawerViewport(800)).toBe(true);
        expect(isChatsPanelDrawerViewport(0)).toBe(false);
        expect(isChatsPanelDrawerViewport(null)).toBe(false);
    });

    it('arte header: prioridade Cliente > ticket direto > Carteira legado', () => {
        const index = buildCarteirasArteByNomeIndex([
            { nome: 'Fênix', arteHeaderChatsUrl: '/uploads/conexoes/header-chats/carteira-1-a.png' }
        ]);
        const clienteUrl = '/uploads/clientes/header-chats/cliente-9.png';
        expect(
            resolveChatsConexaoHeaderArteUrl(
                {
                    numeroTicket: 'TK-1',
                    conexao: 'Fênix',
                    clienteArteHeaderChatsUrl: clienteUrl
                },
                index
            )
        ).toBe(clienteUrl);
        expect(
            resolveChatsClienteHeaderArteUrl({
                cliente: { arteHeaderChatsUrl: clienteUrl }
            })
        ).toBe(clienteUrl);
        expect(
            resolveChatsConexaoHeaderArteUrl(
                {
                    numeroTicket: 'TK-3',
                    conexao: 'Fênix',
                    arteHeaderChatsUrl: '/uploads/conexoes/header-chats/direto.png'
                },
                index
            )
        ).toBe('/uploads/conexoes/header-chats/direto.png');
        expect(
            resolveChatsConexaoHeaderArteUrl({ numeroTicket: 'TK-1', conexao: 'Fênix' }, index)
        ).toBe('/uploads/conexoes/header-chats/carteira-1-a.png');
        expect(sanitizeChatsHeaderArtePublicUrl('../../../etc/passwd')).toBeNull();
        expect(getChatsConexaoHeaderArteCssValue(clienteUrl)).toContain('url(');
        expect(
            resolveChatsConexaoHeaderArteUrl({ numeroTicket: 'TK-2', conexao: 'Sem arte' }, index)
        ).toBeNull();
    });

    it('header conexão: título, canal na marca e meta sem cliente', () => {
        expect(getChatsConexaoRevendaTitle({ numeroTicket: 'TK-1', conexao: 'Revenda Norte' })).toBe('Revenda Norte');
        expect(
            getChatsConexaoRevendaTitle({ numeroTicket: 'TK-1', cliente: 'Maria', conexao: '—' })
        ).toBe(CHATS_CONEXAO_TITLE_FALLBACK);
        const meta = getChatsHeaderMetaProtocolAberturaHtml(
            { numeroTicket: 'TK-1', cliente: 'Maria', dataAbertura: '2026-05-20T10:00:00' },
            CHATS_TAB_HISTORICO,
            { formatDateTime: () => '20/05/2026, 10:00' }
        );
        expect(meta).toContain('Protocolo');
        expect(meta).toContain('Abertura');
        expect(meta).not.toContain('Maria');
        const sis = getChatsHeaderCanalBrandHtml({ numeroTicket: 'TK-1', canal: 'SISTEMA' });
        expect(sis).not.toContain('chats-canal-pill');
        const wa = getChatsHeaderCanalBrandHtml({ numeroTicket: 'TK-2', canal: 'whatsapp' });
        expect(wa).toContain('chats-canal-pill--whatsapp');
        const legacy = getChatsHeaderProtocolCanalHtml(CHATS_TAB_CHATS, { numeroTicket: 'TK-1', canal: 'SISTEMA' });
        expect(legacy).toContain('Protocolo TK-1');
        expect(legacy).not.toContain('chats-canal-pill');
    });

    it('canal: fallback Sistema e normalização WhatsApp/API', () => {
        expect(normalizeChatsCanalKey(null)).toBe('SISTEMA');
        expect(normalizeChatsCanalKey('')).toBe('SISTEMA');
        expect(normalizeChatsCanalKey('whatsapp')).toBe('WHATSAPP');
        expect(normalizeChatsCanalKey('API')).toBe('API');
        expect(getChatsCanalDisplay('Whatsapp').label).toBe('WhatsApp');
        expect(formatChatsCanalBadgeHtml('webhook')).toContain('chats-canal-pill--api');
    });

    it('tempo de espera relativo curto (cabeçalho/outros)', () => {
        const now = new Date('2026-05-20T12:00:00');
        expect(formatRelativeWaitShort('2026-05-20T11:55:00', now)).toBe('há 5 min');
        expect(formatRelativeWaitShort('2026-05-20T10:00:00', now)).toBe('há 2h');
        expect(formatRelativeWaitShort('2026-05-18T12:00:00', now)).toBe('há 2d');
    });

    it('temporizador compacto da lista sem “há” nem TME', () => {
        const now = new Date('2026-05-20T12:00:00');
        expect(formatChatsListElapsedCompact('2026-05-20T11:47:25', now)).toBe('12min 35s');
        expect(formatChatsListElapsedCompact('2026-05-20T02:48:00', now)).toBe('9h 12min');
        expect(formatChatsListElapsedCompact('2026-05-18T12:00:00', now)).toBe('2d');
        expect(formatChatsListElapsedCompact('2026-05-20T11:59:30', now)).toBe('30s');
    });

    it('referência de espera usa abertura na fila e última interação quando informada', () => {
        const ticket = { status: 'ABERTO', dataAbertura: '2026-05-20T08:00:00' };
        expect(getChatsWaitReferenceIso(ticket, CHATS_TAB_FILA)).toBe('2026-05-20T08:00:00');
        expect(
            getChatsWaitReferenceIso(ticket, CHATS_TAB_CHATS, '2026-05-20T11:00:00')
        ).toBe('2026-05-20T11:00:00');
        expect(getLatestInteractionIso([{ criadoEm: '2026-05-20T09:00:00' }, { criadoEm: '2026-05-20T10:00:00' }])).toBe(
            '2026-05-20T10:00:00'
        );
    });

    it('getChatsListConexaoDisplay ignora Sistema (não usado no card)', () => {
        expect(getChatsListConexaoDisplay({ conexao: 'Sistema' })).toBeNull();
        expect(getChatsListConexaoDisplay({ conexao: 'FastComércio' })).toBe('FastComércio');
    });

    it('card da lista: sem avatar, só nome, ticket e tempo', () => {
        const now = new Date('2026-05-20T12:00:00');
        const base = {
            numeroTicket: 'T-100',
            cliente: 'Maria Silva',
            conexao: 'Fênix',
            canal: 'WHATSAPP',
            status: 'ABERTO',
            motivo: 'Problema X',
            dataAbertura: '2026-05-20T10:00:00'
        };
        const htmlFila = buildChatsListCardHtml(base, CHATS_TAB_FILA, { displayValue: v => String(v) });
        expect(htmlFila).not.toMatch(/avatar|chats-list-item-conexao|img|placeholder/i);
        expect(htmlFila).toContain('Maria Silva');
        expect(htmlFila).toContain('T-100');
        expect(htmlFila).toContain('Aguardando atendimento');
        expect(htmlFila).toContain('data-chats-wait-iso');
        expect(htmlFila).not.toContain('Fênix');
        expect(htmlFila).not.toContain('Sistema');
        expect(htmlFila).not.toContain('chats-status-pill');
        expect(htmlFila).not.toContain('chats-canal-pill');
        expect(htmlFila).not.toContain('há ');
        expect(formatChatsListElapsedCompact(base.dataAbertura, now)).toBe('2h');

        const htmlAtendendo = buildChatsListCardHtml(
            { ...base, status: 'EM_ATENDIMENTO' },
            CHATS_TAB_CHATS,
            { displayValue: v => String(v) }
        );
        expect(htmlAtendendo).not.toContain('Em atendimento');
        expect(htmlAtendendo).not.toContain('chats-list-item-time-label');
        expect(htmlAtendendo).toContain('data-chats-wait-iso');

        const htmlEnc = buildChatsListCardHtml(
            { ...base, status: 'RESOLVIDO', dataEncerramento: '2026-05-19T12:00:00' },
            CHATS_TAB_HISTORICO,
            { displayValue: v => String(v) }
        );
        expect(htmlEnc).not.toContain('chats-list-item-avatar');
        expect(htmlEnc).not.toContain('Encerrado');
        expect(htmlEnc).not.toContain('data-chats-wait-iso');
    });

    it('meta temporal: Fila com rótulo; Atendendo só tempo', () => {
        const fila = getChatsListTemporalMeta({ status: 'ABERTO' }, CHATS_TAB_FILA);
        expect(fila.showLabel).toBe(true);
        expect(fila.label).toBe('Aguardando atendimento');
        expect(fila.liveCounter).toBe(true);

        const atend = getChatsListTemporalMeta({ status: 'AGUARDANDO_CLIENTE' }, CHATS_TAB_CHATS);
        expect(atend.showLabel).toBe(false);
        expect(atend.label).toBe('');
        expect(atend.liveCounter).toBe(true);
    });

    it('SLA visual prioriza status mais crítico do backend', () => {
        const ticket = {
            slaPrimeiroAtendimentoStatus: 'DENTRO_DO_PRAZO',
            slaResolucaoStatus: 'VENCIDO'
        };
        expect(pickChatsPrimarySlaStatus(ticket)).toBe('VENCIDO');
        expect(getChatsSlaAccentModifier('VENCIDO', CHATS_TAB_FILA)).toBe('vencido');
        expect(getChatsSlaAccentModifier('PAUSADO', CHATS_TAB_CHATS)).toBe('pausado');
        expect(pickChatsPrimarySlaStatus({ slaPausado: true, slaResolucaoStatus: 'DENTRO_DO_PRAZO' })).toBe(
            'PAUSADO'
        );
    });

    it('formatChatsPriorityBadgeHtml usa classe chats-priority-pill', () => {
        const html = formatChatsPriorityBadgeHtml('CRITICA');
        expect(html).toContain('chats-priority-pill--critica');
        expect(html).toContain('Crítica');
    });

    it('botão principal: detalhes para encerrado', () => {
        const action = getChatsPrimaryActionLabel({
            numeroTicket: 'T2',
            status: 'RESOLVIDO'
        });
        expect(action.label).toBe('Detalhes');
        expect(action.mode).toBe('detalhes');
        expect(action.disabled).toBe(false);
    });

    it('timeline inclui satisfação quando informada', () => {
        const entries = buildChatsTimelineEntries(
            {
                dataAbertura: '2026-05-20T10:00:00',
                dataEncerramento: '2026-05-20T12:00:00',
                status: 'RESOLVIDO',
                numeroTicket: 'T1'
            },
            [],
            { satisfacao: { nota: 5, comentario: 'Ótimo', criadoEm: '2026-05-20T13:00:00' } }
        );
        expect(entries.some(e => e.kind === 'event' && e.text.includes('satisfação'))).toBe(true);
        expect(formatSatisfacaoTimelineSub({ nota: 4, comentario: '' })).toContain('Nota 4');
    });
});
