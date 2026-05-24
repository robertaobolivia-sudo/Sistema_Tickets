package com.suporte.tickets.service;

import com.suporte.tickets.dto.EncerrarTicketRequestDTO;
import com.suporte.tickets.dto.TicketAlertaDTO;
import com.suporte.tickets.dto.TicketAlertaReferenciaDTO;
import com.suporte.tickets.dto.TicketEscalonamentoRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketOrigem;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.TicketClassificacaoOperacional;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço de negócio para gerenciamento de tickets
 * 
 * Responsável por implementar a lógica de criação e manipulação de tickets,
 * incluindo a busca e criação de clientes, e geração de números sequenciais.
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoCategoriaRepository grupoCategoriaRepository;
    private final SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    private final MotivoService motivoService;
    private final TicketInteracaoService ticketInteracaoService;
    private final AnalistaService analistaService;
    private final ContatoService contatoService;
    private final WhatsappMatrizService whatsappMatrizService;
    private final TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    private final TicketSlaResolucaoService ticketSlaResolucaoService;
    private final TicketSlaPausaService ticketSlaPausaService;
    private final NotificacaoInternaService notificacaoInternaService;
    private final TicketSatisfacaoService ticketSatisfacaoService;
    private final PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;
    private final ContatoAtendimentoOrigemService contatoAtendimentoOrigemService;
    private final TicketStatusTransicaoService ticketStatusTransicaoService;

    /**
     * Cria um novo ticket a partir de dados de webhook
     * 
     * Implementa a seguinte lógica:
     * 1. Busca cliente pelo telefone
     * 2. Se não existir, cria novo cliente
     * 3. Cria novo ticket com número sequencial
     * 4. Salva no banco de dados
     * 5. Retorna dados do ticket criado
     * 
     * @param dto Dados do ticket recebido via webhook
     * @return DTO com dados do ticket criado
     */
    @Transactional
    public TicketResponseDTO criarTicketPorWebhook(TicketWebhookRequestDTO dto) {
        Cliente cliente = resolverClienteParaCriacao(dto);
        exigirContatoWhatsappAberturaOperacional(dto, cliente);

        // Passo 2: Gerar número sequencial do ticket
        String numeroTicket = gerarNumeroTicket();

        // Passo 3: Criar novo ticket
        Ticket novoTicket = new Ticket();
        novoTicket.setNumeroTicket(numeroTicket);
        novoTicket.setCliente(cliente);
        novoTicket.setCanal(dto.getCanal());
        novoTicket.setMensagemInicial(dto.getMensagem());
        if (dto.getWhatsappMatrizId() != null) {
            WhatsappMatriz matriz = whatsappMatrizService.buscarEntidadeAtivaPorId(dto.getWhatsappMatrizId());
            novoTicket.setWhatsappMatriz(matriz);
        }
        novoTicket.setStatus(TicketStatus.ABERTO);
        novoTicket.setPrioridade(resolverPrioridade(dto.getPrioridade()));
        novoTicket.setOrigemTicket(TicketOrigemResolver.resolverOrigemNaCriacao(dto));
        novoTicket.setDataAbertura(LocalDateTime.now());

        vincularContatoModeloAlvo(novoTicket, dto, cliente);
        if (dto.getWhatsappMatrizId() != null && novoTicket.getContato() == null) {
            vincularContatoWhatsAppAoTicket(novoTicket, dto, cliente);
        }
        if (novoTicket.getContato() != null) {
            contatoAtendimentoOrigemService.aplicarOrigemNoTicket(
                    novoTicket, novoTicket.getContato(), telefoneBrutoMensagemWhatsapp(dto));
        }

        if (novoTicket.getContato() != null && novoTicket.getContato().getId() != null) {
            Optional<Ticket> ativoMesmoContato = ticketRepository
                    .findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                            cliente.getId(),
                            novoTicket.getContato().getId(),
                            List.copyOf(TicketAtivoService.STATUS_ATIVOS));
            if (ativoMesmoContato.isPresent()) {
                throw new IllegalArgumentException(
                        "Ja existe ticket ativo para este cliente e contato: "
                                + ativoMesmoContato.get().getNumeroTicket());
            }
        }

        ticketSlaPrimeiroAtendimentoService.aplicarCalculoNaCriacao(novoTicket);
        ticketSlaResolucaoService.aplicarCalculoNaCriacao(novoTicket);

        // Passo 4: Salvar no banco
        Ticket ticketSalvo = ticketRepository.save(novoTicket);
        ticketInteracaoService.registrarAberturaAutomatica(ticketSalvo);

        // Passo 5: Retornar resposta
        return converterParaResponse(ticketSalvo);
    }

    /**
     * Sprint 303: cria ticket já como INDEVIDO quando contato tem etiqueta operacional.
     * Sem SLA, sem avaliação, sem fila. Terminal desde a criação.
     */
    @Transactional
    public TicketResponseDTO criarTicketEntradaOperacional(
            TicketWebhookRequestDTO dto, TicketClassificacaoOperacional motivo) {
        Cliente cliente = resolverClienteParaCriacao(dto);
        String numeroTicket = gerarNumeroTicket();
        LocalDateTime agora = LocalDateTime.now();

        Ticket ticket = new Ticket();
        ticket.setNumeroTicket(numeroTicket);
        ticket.setCliente(cliente);
        ticket.setCanal(dto.getCanal());
        ticket.setMensagemInicial(dto.getMensagem());
        if (dto.getWhatsappMatrizId() != null) {
            WhatsappMatriz matriz = whatsappMatrizService.buscarEntidadeAtivaPorId(dto.getWhatsappMatrizId());
            ticket.setWhatsappMatriz(matriz);
        }
        ticket.setStatus(TicketStatus.INDEVIDO);
        ticket.setPrioridade(resolverPrioridade(dto.getPrioridade()));
        ticket.setOrigemTicket(TicketOrigemResolver.resolverOrigemNaCriacao(dto));
        ticket.setDataAbertura(agora);
        ticket.setDataEncerramento(agora);
        ticket.setClassificacaoOperacional(motivo);
        ticket.setClassificadoOperacionalEm(agora);

        vincularContatoModeloAlvo(ticket, dto, cliente);
        if (dto.getWhatsappMatrizId() != null && ticket.getContato() == null) {
            vincularContatoWhatsAppAoTicket(ticket, dto, cliente);
        }
        if (ticket.getContato() != null) {
            contatoAtendimentoOrigemService.aplicarOrigemNoTicket(
                    ticket, ticket.getContato(), telefoneBrutoMensagemWhatsapp(dto));
        }

        Ticket salvo = ticketRepository.save(ticket);
        ticketInteracaoService.registrarEntradaOperacional(salvo, motivo.name());
        return converterParaResponseSeguro(salvo);
    }

    /**
     * Lista todos os tickets ordenados por data de abertura (decrescente)
     *
     * @return Lista de DTO com todos os tickets
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarTodos() {
        return ticketRepository.findAllByOrderByDataAberturaDesc()
                .stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketAlertaReferenciaDTO obterReferenciaAlerta() {
        Integer ultimoId = ticketRepository.findMaxId();
        LocalDateTime ultimaData = ticketRepository.findMaxDataAbertura().orElse(null);
        return new TicketAlertaReferenciaDTO(ultimoId != null ? ultimoId : 0, ultimaData);
    }

    @Transactional(readOnly = true)
    public List<TicketAlertaDTO> listarNovosParaAlerta(Integer aposId, LocalDateTime aposData, int limite) {
        int limit = Math.min(Math.max(limite, 1), 100);
        Pageable pageable = PageRequest.of(0, limit);
        List<Ticket> tickets;
        if (aposId != null) {
            tickets = ticketRepository.findByIdGreaterThanOrderByIdAsc(aposId, pageable);
        } else if (aposData != null) {
            tickets = ticketRepository.findByDataAberturaGreaterThanOrderByDataAberturaAsc(aposData, pageable);
        } else {
            return Collections.emptyList();
        }
        return tickets.stream()
                .map(this::converterParaAlerta)
                .collect(Collectors.toList());
    }

    /**
     * Busca um ticket pelo número
     * 
     * @param numeroTicket Número do ticket
     * @return DTO com dados do ticket
     * @throws RuntimeException se ticket não encontrado
     */
    @Transactional(readOnly = true)
    public TicketResponseDTO buscarPorNumero(String numeroTicket) {
        return converterParaResponse(buscarEntidadePorNumeroOuFalha(numeroTicket));
    }

    @Transactional(readOnly = true)
    public Ticket buscarEntidadePorNumeroOuFalha(String numeroTicket) {
        return ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
    }

    /**
     * Atualiza o status de um ticket
     * 
     * Regras:
     * - Se novo status for EM_ATENDIMENTO e dataPrimeiroAtendimento estiver nula,
     *   preenche com data/hora atual
     * 
     * @param numeroTicket Número do ticket
     * @param novoStatusStr String do novo status
     * @return DTO com ticket atualizado
     * @throws RuntimeException se ticket não encontrado
     * @throws IllegalArgumentException se status inválido
     */
    @Transactional
    public TicketResponseDTO atualizarStatus(String numeroTicket, String novoStatusStr) {
        return atualizarStatus(numeroTicket, novoStatusStr, null);
    }

    @Transactional
    public TicketResponseDTO atualizarStatus(String numeroTicket, String novoStatusStr, Long analistaId) {
        // Validar status
        if (!TicketStatus.isValido(novoStatusStr)) {
            throw new IllegalArgumentException("Status inválido: " + novoStatusStr);
        }

        // Buscar ticket
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));

        // Converter para enum
        TicketStatus novoStatus = TicketStatus.valueOf(novoStatusStr);
        TicketStatus statusAnterior = ticket.getStatus();

        if (novoStatus == TicketStatus.INDEVIDO) {
            throw new IllegalArgumentException(
                    "Use o endpoint de classificacao indevido com confirmacao do analista.");
        }
        if (novoStatus == TicketStatus.RESOLVIDO) {
            throw new IllegalArgumentException("Use o endpoint de encerramento para resolver o ticket.");
        }

        ticketStatusTransicaoService.validarTransicao(
                statusAnterior,
                novoStatus,
                TicketStatusTransicaoService.MotivoTransicao.ATUALIZACAO_MANUAL);

        if (novoStatus == TicketStatus.EM_ATENDIMENTO) {
            if (ticket.getStatus() == TicketStatus.EM_ATENDIMENTO && ticket.getAnalistaResponsavel() != null) {
                throw new IllegalArgumentException("Ticket ja esta em atendimento");
            }
            Analista analistaResponsavel = analistaId != null
                    ? analistaService.buscarPorId(analistaId)
                    : analistaService.buscarAnalistaPadrao();
            ticket.setAnalistaResponsavel(analistaResponsavel);
        }

        if (statusAnterior == TicketStatus.AGUARDANDO_CLIENTE && novoStatus != TicketStatus.AGUARDANDO_CLIENTE) {
            ticketSlaPausaService.finalizarPausa(ticket);
        }

        // Atualizar status
        ticket.setStatus(novoStatus);

        if (novoStatus == TicketStatus.AGUARDANDO_CLIENTE) {
            ticketSlaPausaService.iniciarPausa(ticket);
        }

        // Se mudando para EM_ATENDIMENTO, preencher dataPrimeiroAtendimento e avaliar SLA
        if (novoStatus == TicketStatus.EM_ATENDIMENTO && ticket.getDataPrimeiroAtendimento() == null) {
            LocalDateTime momentoAtendimento = LocalDateTime.now();
            ticket.setDataPrimeiroAtendimento(momentoAtendimento);
            ticketSlaPrimeiroAtendimentoService.avaliarPrimeiroAtendimento(ticket, momentoAtendimento);
        }

        // Salvar alterações
        Ticket ticketAtualizado = ticketRepository.save(ticket);

        return converterParaResponse(ticketAtualizado);
    }

    @Transactional
    public TicketResponseDTO reabrirTicket(String numeroTicket) {
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));

        TicketStatus statusAnterior = ticket.getStatus();
        ticketStatusTransicaoService.validarTransicao(
                statusAnterior,
                TicketStatus.ABERTO,
                TicketStatusTransicaoService.MotivoTransicao.REABERTURA);

        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setAnalistaResponsavel(null);

        Ticket ticketAtualizado = ticketRepository.save(ticket);
        ticketInteracaoService.registrarReaberturaAutomatica(ticketAtualizado);
        return converterParaResponse(ticketAtualizado);
    }

    /**
     * Encerra um ticket
     * 
     * Regras:
     * - Define status como RESOLVIDO
     * - Preenche dataEncerramento com data/hora atual
     * - Se dataPrimeiroAtendimento estiver nula, preenche também
     * 
     * @param numeroTicket Número do ticket
     * @return DTO com ticket atualizado
     * @throws RuntimeException se ticket não encontrado
     */
    @Transactional
    public TicketResponseDTO encerrarTicket(String numeroTicket, EncerrarTicketRequestDTO dto) {
        return encerrarTicket(numeroTicket, dto, null);
    }

    @Transactional
    public TicketResponseDTO encerrarTicket(String numeroTicket, EncerrarTicketRequestDTO dto, Long analistaId) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados de encerramento sao obrigatorios");
        }
        if (dto.getComentarioEncerramento() == null || dto.getComentarioEncerramento().isBlank()) {
            throw new IllegalArgumentException("Comentario de encerramento e obrigatorio");
        }

        // Buscar ticket
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));

        GrupoCategoria grupo = grupoCategoriaRepository.findById(dto.getGrupoId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo nao encontrado: " + dto.getGrupoId()));
        if (!Boolean.TRUE.equals(grupo.getAtivo())) {
            throw new IllegalArgumentException("Grupo inativo: " + dto.getGrupoId());
        }

        SubgrupoCategoria subgrupo = subgrupoCategoriaRepository.findById(dto.getSubgrupoId())
                .orElseThrow(() -> new IllegalArgumentException("Subgrupo nao encontrado: " + dto.getSubgrupoId()));
        if (!Boolean.TRUE.equals(subgrupo.getAtivo())) {
            throw new IllegalArgumentException("Subgrupo inativo: " + dto.getSubgrupoId());
        }
        if (!subgrupo.getGrupoCategoria().getId().equals(grupo.getId())) {
            throw new IllegalArgumentException("Subgrupo nao pertence ao grupo informado");
        }

        if (dto.getMotivoId() == null) {
            throw new IllegalArgumentException("Motivo e obrigatorio");
        }
        Motivo motivo = motivoService.buscarEntidadeAtiva(dto.getMotivoId());
        if (!motivo.getSubgrupoCategoria().getId().equals(subgrupo.getId())) {
            throw new IllegalArgumentException("Motivo nao pertence ao subgrupo informado");
        }

        if (!ticketStatusTransicaoService.isStatusAtivoOperacional(ticket.getStatus())) {
            throw new IllegalArgumentException("Somente tickets ativos podem ser encerrados.");
        }

        ticketSlaPausaService.finalizarPausaSeNecessario(ticket);

        ticketStatusTransicaoService.validarTransicao(
                ticket.getStatus(),
                TicketStatus.RESOLVIDO,
                TicketStatusTransicaoService.MotivoTransicao.ENCERRAMENTO);

        ticket.setStatus(TicketStatus.RESOLVIDO);

        // Preencher dataEncerramento
        LocalDateTime dataEncerramento = LocalDateTime.now();
        ticket.setDataEncerramento(dataEncerramento);
        ticketSlaResolucaoService.avaliarResolucao(ticket, dataEncerramento);

        // Se dataPrimeiroAtendimento estiver nula, preencher também
        if (ticket.getDataPrimeiroAtendimento() == null) {
            ticket.setDataPrimeiroAtendimento(LocalDateTime.now());
        }

        ticket.setGrupoCategoria(grupo);
        ticket.setSubgrupoCategoria(subgrupo);
        ticket.setMotivo(motivo);
        ticket.setComentarioEncerramento(dto.getComentarioEncerramento().trim());

        // Salvar alterações
        Ticket ticketAtualizado = ticketRepository.save(ticket);
        ticketInteracaoService.registrarEncerramentoAutomatico(
                ticketAtualizado,
                ticketAtualizado.getComentarioEncerramento()
        );

        boolean enviarPesquisa = Boolean.TRUE.equals(dto.getEnviarPesquisaSatisfacao());
        String tokenPesquisa = ticketSatisfacaoService.registrarDecisaoPosEncerramento(
                ticketAtualizado, enviarPesquisa, analistaId);

        TicketResponseDTO response = converterParaResponse(ticketAtualizado);
        ticketSatisfacaoService.preencherResumoNoTicketResponse(response, ticketAtualizado.getId());
        if (tokenPesquisa != null && !tokenPesquisa.isBlank()) {
            response.setAvaliacaoLinkPublico(pesquisaSatisfacaoEnvioService.montarLinkAvaliacao(tokenPesquisa));
        }
        return response;
    }

    @Transactional
    public TicketResponseDTO escalonarTicket(String numeroTicket, TicketEscalonamentoRequestDTO dto) {
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
        ticket.setEscalonado(true);
        ticket.setEscalonadoEm(LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA));
        if (dto != null && dto.getObservacao() != null && !dto.getObservacao().isBlank()) {
            ticket.setEscalonamentoObservacao(dto.getObservacao().trim());
        }
        if (dto != null && dto.getAnalistaId() != null) {
            ticket.setEscalonadoPorAnalistaId(dto.getAnalistaId());
        }
        Ticket ticketSalvo = ticketRepository.save(ticket);
        notificacaoInternaService.registrarTicketEscalonado(ticketSalvo);
        return converterParaResponse(ticketSalvo);
    }

    private static final int OBSERVACAO_ATENDIMENTO_MAX = 2000;

    @Transactional
    public TicketResponseDTO atualizarObservacaoAtendimento(String numeroTicket, String observacao) {
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
        ticket.setObservacaoAtendimento(normalizarObservacaoAtendimento(observacao));
        return converterParaResponse(ticketRepository.save(ticket));
    }

    static String normalizarObservacaoAtendimento(String observacao) {
        if (observacao == null) {
            return null;
        }
        String texto = observacao.trim();
        if (texto.isEmpty()) {
            return null;
        }
        if (texto.length() > OBSERVACAO_ATENDIMENTO_MAX) {
            throw new IllegalArgumentException(
                    "Observacao de atendimento deve ter no maximo " + OBSERVACAO_ATENDIMENTO_MAX + " caracteres.");
        }
        return texto;
    }

    @Transactional
    public TicketResponseDTO removerEscalonamento(String numeroTicket) {
        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
        ticket.setEscalonado(false);
        ticket.setEscalonadoEm(null);
        ticket.setEscalonamentoObservacao(null);
        ticket.setEscalonadoPorAnalistaId(null);
        Ticket ticketSalvo = ticketRepository.save(ticket);
        notificacaoInternaService.registrarEscalonamentoRemovido(numeroTicket);
        return converterParaResponse(ticketSalvo);
    }

    /**
     * Lista tickets por status
     * 
     * @param statusStr String do status
     * @return Lista de DTO com tickets do status especificado
     * @throws IllegalArgumentException se status inválido
     */
    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarPorStatus(String statusStr) {
        // Validar status
        if (!TicketStatus.isValido(statusStr)) {
            throw new IllegalArgumentException("Status inválido: " + statusStr);
        }

        TicketStatus status = TicketStatus.valueOf(statusStr);
        return ticketRepository.findByStatusOrderByDataAberturaDesc(status)
                .stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Resolve Cliente contratante na criação (id explícito ou buscarOuCriarCliente).
     */
    private Cliente resolverClienteParaCriacao(TicketWebhookRequestDTO dto) {
        if (dto.getClienteContratanteId() != null) {
            return clienteRepository.findById(dto.getClienteContratanteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Cliente nao encontrado: " + dto.getClienteContratanteId()));
        }
        return buscarOuCriarCliente(dto.getCliente(), dto.getTelefone());
    }

    /**
     * Sprint F6: abertura pela UI (clienteContratanteId) ou cliente com Contatos exige contatoWhatsappId.
     * Entrada WhatsApp (whatsappMatrizId) segue fluxo proprio.
     */
    private void exigirContatoWhatsappAberturaOperacional(TicketWebhookRequestDTO dto, Cliente cliente) {
        if (dto.getWhatsappMatrizId() != null) {
            return;
        }
        if (dto.getContatoWhatsappId() != null) {
            return;
        }
        if (dto.getClienteContratanteId() != null) {
            throw new IllegalArgumentException(
                    "Informe contatoWhatsappId para abrir atendimento deste cliente.");
        }
        if (contatoService.clientePossuiContatoWhatsappAtivo(cliente.getId())) {
            throw new IllegalArgumentException(
                    "Cliente possui Contatos cadastrados. Informe contatoWhatsappId para abrir o atendimento.");
        }
    }

    /**
     * Sprint F8: apenas {@code contatoWhatsappId} vincula {@link Ticket#contato}.
     * Legado removido F38; telefone automático só em fluxo Matriz.
     */
    private void vincularContatoModeloAlvo(Ticket ticket, TicketWebhookRequestDTO dto, Cliente cliente) {
        if (dto.getContatoWhatsappId() == null) {
            return;
        }
        Contato contato = contatoService.buscarEntidade(dto.getContatoWhatsappId());
        if (contato.getCliente() == null
                || contato.getCliente().getId() == null
                || !contato.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Contato nao pertence ao cliente do ticket");
        }
        ticket.setContato(contato);
    }

    /**
     * Sprint F8: cria Contato só a partir do telefone da mensagem (fluxo Matriz).
     */
    private void vincularContatoWhatsAppAoTicket(Ticket ticket, TicketWebhookRequestDTO dto, Cliente cliente) {
        String telefoneBruto = telefoneBrutoMensagemWhatsapp(dto);
        if (telefoneBruto == null) {
            return;
        }
        String nome = resolverNomeParaNovoContato(dto);
        var contatoResp = contatoService.criarSeNaoExistir(cliente.getId(), telefoneBruto, nome);
        Contato contato = contatoService.buscarEntidade(contatoResp.getId());
        ticket.setContato(contato);
    }

    private static String telefoneBrutoMensagemWhatsapp(TicketWebhookRequestDTO dto) {
        if (dto.getTelefone() != null && TicketAtivoService.normalizarTelefone(dto.getTelefone()) != null) {
            return dto.getTelefone().trim();
        }
        return null;
    }

    private static String resolverNomeParaNovoContato(TicketWebhookRequestDTO dto) {
        if (dto.getNomeContato() != null && !dto.getNomeContato().isBlank()) {
            return dto.getNomeContato().trim();
        }
        return null;
    }

    /** Busca ou cria Cliente por nome/telefone (F39: sem Carteira/Conexão). */
    private Cliente buscarOuCriarCliente(String nomeCliente, String telefone) {
        // Primeiro tenta buscar por telefone se existir
        if (telefone != null && !telefone.isEmpty()) {
            Optional<Cliente> clienteExistente = clienteRepository.findByTelefone(telefone);
            if (clienteExistente.isPresent()) {
                return clienteExistente.get();
            }
        }

        // Se não encontrou por telefone, tenta por nome
        Optional<Cliente> clienteExistente = clienteRepository.findByNome(nomeCliente);
        if (clienteExistente.isPresent()) {
            return clienteExistente.get();
        }

        // Se não encontrou nenhum, cria novo cliente
        Cliente novoCliente = new Cliente();
        novoCliente.setNome(nomeCliente);
        novoCliente.setTelefone(telefone);
        novoCliente.setTelefoneContato(telefone);

        return clienteRepository.save(novoCliente);
    }

    /**
     * Gera número sequencial para o ticket no formato TK-XXXXXX
     * 
     * @return String formatada com número do ticket
     */
    private String gerarNumeroTicket() {
        Integer proximoNumero = ticketRepository.getNextSequence();
        return String.format("TK-%06d", proximoNumero);
    }

    /**
     * Converte uma entidade Ticket para DTO de resposta
     * 
     * @param ticket Entidade Ticket
     * @return DTO com dados do ticket
     */
    @Transactional(readOnly = true)
    public TicketResponseDTO converterParaResponseSeguro(Ticket ticket) {
        return converterParaResponse(ticket);
    }

    private TicketResponseDTO converterParaResponse(Ticket ticket) {
        TicketResponseDTO response = new TicketResponseDTO();
        response.setId((long) ticket.getId());
        response.setNumeroTicket(valorOuPadrao(ticket.getNumeroTicket()));
        if (ticket.getAnalistaResponsavel() != null) {
            response.setAnalistaResponsavelId(ticket.getAnalistaResponsavel().getId());
            response.setAnalistaResponsavelNome(ticket.getAnalistaResponsavel().getNomeCompleto() != null
                    ? ticket.getAnalistaResponsavel().getNomeCompleto()
                    : ticket.getAnalistaResponsavel().getNome());
            response.setAnalistaResponsavelEmail(ticket.getAnalistaResponsavel().getEmail());
        } else {
            response.setAnalistaResponsavelNome("-");
        }
        if (ticket.getCliente() != null) {
            response.setClienteId(ticket.getCliente().getId());
            String arteCliente = ticket.getCliente().getArteHeaderChatsUrl();
            if (arteCliente != null && !arteCliente.isBlank()) {
                response.setClienteArteHeaderChatsUrl(arteCliente.trim());
            }
            response.setCliente(valorOuPadrao(ticket.getCliente().getNome()));
            response.setTelefone(valorOuPadrao(ticket.getCliente().getTelefone()));
            response.setTelefoneContato(valorOuPadrao(ticket.getCliente().getTelefoneContato()));
            response.setEmail(valorOuPadrao(ticket.getCliente().getEmail()));
            response.setEmpresa(valorOuPadrao(ticket.getCliente().getEmpresa()));
            response.setCnpj(valorOuPadrao(ticket.getCliente().getCnpj()));
            response.setCidade(valorOuPadrao(ticket.getCliente().getCidade()));
            response.setUf(valorOuPadrao(ticket.getCliente().getUf()));
        } else {
            response.setCliente("-");
            response.setTelefone("-");
            response.setTelefoneContato("-");
            response.setEmail("-");
            response.setEmpresa("-");
            response.setCnpj("-");
            response.setCidade("-");
            response.setUf("-");
        }
        if (ticket.getContato() != null) {
            com.suporte.tickets.entity.Contato contatoWa = ticket.getContato();
            response.setContatoId(contatoWa.getId());
            response.setContatoNome(valorOuPadrao(contatoWa.getNome()));
            response.setContatoWhatsapp(valorOuPadrao(contatoWa.getWhatsapp()));
            response.setContatoEmail(valorOuPadrao(contatoWa.getEmail()));
            response.setContatoEmpresaLocal(valorOuPadrao(contatoWa.getEmpresaLocal()));
            response.setContatoCidade(valorOuPadrao(contatoWa.getCidade()));
            response.setContatoUf(valorOuPadrao(contatoWa.getUf()));
            response.setContatoObservacoes(valorOuPadrao(contatoWa.getObservacoes()));
        } else {
            response.setContatoNome("-");
            response.setContatoWhatsapp("-");
            response.setContatoEmail("-");
            response.setContatoEmpresaLocal("-");
            response.setContatoCidade("-");
            response.setContatoUf("-");
            response.setContatoObservacoes("-");
        }
        if (ticket.getAtendimentoTelefone() != null && !ticket.getAtendimentoTelefone().isBlank()) {
            response.setAtendimentoTelefone(ticket.getAtendimentoTelefone().trim());
        }
        if (ticket.getAtendimentoTelefoneTipo() != null && !ticket.getAtendimentoTelefoneTipo().isBlank()) {
            response.setAtendimentoTelefoneTipo(ticket.getAtendimentoTelefoneTipo().trim());
        }
        if (ticket.getWhatsappMatriz() != null) {
            WhatsappMatriz matriz = ticket.getWhatsappMatriz();
            response.setWhatsappMatrizId(matriz.getId());
            response.setWhatsappMatrizNumero(valorOuPadrao(matriz.getNumero()));
            response.setWhatsappMatrizNome(
                    matriz.getNome() != null && !matriz.getNome().isBlank()
                            ? matriz.getNome().trim()
                            : "-");
        } else {
            response.setWhatsappMatrizNumero("-");
            response.setWhatsappMatrizNome("-");
        }
        TicketOrigem origem = TicketOrigemResolver.resolverOrigemParaExibicao(ticket);
        response.setOrigemTicket(origem != null ? origem.name() : null);
        response.setCanal(valorOuPadrao(ticket.getCanal()));
        response.setMensagemInicial(valorOuPadrao(ticket.getMensagemInicial()));
        response.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : "-");
        response.setPrioridade(ticket.getPrioridade() != null ? ticket.getPrioridade().name() : "-");
        response.setDataAbertura(ticket.getDataAbertura());
        response.setDataPrimeiroAtendimento(ticket.getDataPrimeiroAtendimento());
        response.setSlaPrimeiroAtendimentoVencimento(ticket.getSlaPrimeiroAtendimentoVencimento());
        response.setSlaPrimeiroAtendimentoCumprido(ticket.getSlaPrimeiroAtendimentoCumprido());
        response.setSlaPrimeiroAtendimentoCalculadoEm(ticket.getSlaPrimeiroAtendimentoCalculadoEm());
        response.setSlaPrimeiroAtendimentoStatus(
                ticketSlaPrimeiroAtendimentoService.calcularStatusLabel(ticket));
        response.setSlaResolucaoVencimento(ticket.getSlaResolucaoVencimento());
        response.setSlaResolucaoCumprido(ticket.getSlaResolucaoCumprido());
        response.setSlaResolucaoCalculadoEm(ticket.getSlaResolucaoCalculadoEm());
        response.setSlaResolucaoStatus(ticketSlaResolucaoService.calcularStatusLabel(ticket));
        response.setSlaPausado(Boolean.TRUE.equals(ticket.getSlaPausado()));
        response.setSlaPausaInicio(ticket.getSlaPausaInicio());
        response.setSlaResolucaoMinutosPausados(
                ticket.getSlaResolucaoMinutosPausados() != null ? ticket.getSlaResolucaoMinutosPausados() : 0L);
        response.setDataEncerramento(ticket.getDataEncerramento());
        response.setTmeMinutosUteis(ticket.getTmeMinutosUteis());
        response.setTmaMinutosUteis(ticket.getTmaMinutosUteis());
        if (ticket.getGrupoCategoria() != null) {
            response.setGrupoCategoriaId(ticket.getGrupoCategoria().getId());
            response.setGrupoCategoriaNome(valorOuPadrao(ticket.getGrupoCategoria().getNome()));
        } else {
            response.setGrupoCategoriaNome("-");
        }
        if (ticket.getSubgrupoCategoria() != null) {
            response.setSubgrupoCategoriaId(ticket.getSubgrupoCategoria().getId());
            response.setSubgrupoCategoriaNome(valorOuPadrao(ticket.getSubgrupoCategoria().getNome()));
        } else {
            response.setSubgrupoCategoriaNome("-");
        }
        if (ticket.getMotivo() != null) {
            response.setMotivoId(ticket.getMotivo().getId());
            response.setMotivoNome(valorOuPadrao(ticket.getMotivo().getNome()));
        } else {
            response.setMotivoNome("-");
        }
        response.setComentarioEncerramento(valorOuPadrao(ticket.getComentarioEncerramento()));
        if (ticket.getClassificacaoOperacional() != null) {
            response.setClassificacaoOperacional(ticket.getClassificacaoOperacional().name());
        }
        response.setClassificadoOperacionalEm(ticket.getClassificadoOperacionalEm());
        response.setClassificadoOperacionalPorAnalistaId(ticket.getClassificadoOperacionalPorAnalistaId());
        response.setComentarioClassificacaoOperacional(
                valorOuPadraoOpcional(ticket.getComentarioClassificacaoOperacional()));
        response.setObservacaoAtendimento(valorOuPadraoOpcional(ticket.getObservacaoAtendimento()));
        response.setEscalonado(Boolean.TRUE.equals(ticket.getEscalonado()));
        response.setEscalonadoEm(ticket.getEscalonadoEm());
        response.setEscalonamentoObservacao(valorOuPadraoOpcional(ticket.getEscalonamentoObservacao()));
        response.setEscalonadoPorAnalistaId(ticket.getEscalonadoPorAnalistaId());
        response.setEscalonadoPorNome(resolverNomeEscalonadoPor(ticket.getEscalonadoPorAnalistaId()));
        ticketSatisfacaoService.preencherResumoNoTicketResponse(response, ticket.getId());
        return response;
    }

    private String resolverNomeEscalonadoPor(Long analistaId) {
        if (analistaId == null) {
            return null;
        }
        try {
            Analista analista = analistaService.buscarPorId(analistaId);
            if (analista.getNomeCompleto() != null && !analista.getNomeCompleto().isBlank()) {
                return analista.getNomeCompleto();
            }
            return analista.getNome();
        } catch (Exception ex) {
            return null;
        }
    }

    private String valorOuPadraoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private PrioridadeTicket resolverPrioridade(String prioridadeInformada) {
        if (PrioridadeTicket.isValido(prioridadeInformada)) {
            return PrioridadeTicket.valueOf(prioridadeInformada.trim().toUpperCase());
        }
        return PrioridadeTicket.MEDIA;
    }

    private String valorOuPadrao(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        return valor.trim();
    }

    private TicketAlertaDTO converterParaAlerta(Ticket ticket) {
        TicketAlertaDTO dto = new TicketAlertaDTO();
        dto.setId(ticket.getId());
        dto.setNumeroTicket(ticket.getNumeroTicket());
        if (ticket.getCliente() != null) {
            dto.setCliente(valorOuPadrao(ticket.getCliente().getNome()));
        } else {
            dto.setCliente("-");
        }
        dto.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        dto.setDataAbertura(ticket.getDataAbertura());
        return dto;
    }

}
