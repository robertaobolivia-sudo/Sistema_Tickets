package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClassificarTicketIndevidoRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketClassificacaoOperacional;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classificação de ticket como indevido com confirmação do analista (Sprint 274).
 * Não altera etiquetas do Contato.
 */
@Service
@RequiredArgsConstructor
public class TicketIndevidoService {

    private final TicketRepository ticketRepository;
    private final GrupoCategoriaRepository grupoCategoriaRepository;
    private final SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    private final MotivoService motivoService;
    private final TicketSlaPausaService ticketSlaPausaService;
    private final TicketSlaResolucaoService ticketSlaResolucaoService;
    private final TicketInteracaoService ticketInteracaoService;
    private final TicketSatisfacaoService ticketSatisfacaoService;
    private final TicketService ticketService;
    private final AuditoriaService auditoriaService;
    private final TicketStatusTransicaoService ticketStatusTransicaoService;

    @Transactional
    public TicketResponseDTO classificarComoIndevido(
            String numeroTicket, ClassificarTicketIndevidoRequestDTO dto, Long analistaId) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados de classificacao sao obrigatorios.");
        }
        if (!Boolean.TRUE.equals(dto.getConfirmacao())) {
            throw new IllegalArgumentException(
                    "Confirme a classificacao como indevido (confirmacao=true).");
        }
        if (analistaId == null) {
            throw new IllegalArgumentException("Sessao do analista e obrigatoria.");
        }

        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));

        if (ticket.getStatus() == TicketStatus.INDEVIDO) {
            throw new IllegalArgumentException("Ticket ja esta classificado como indevido.");
        }
        ticketStatusTransicaoService.validarTransicao(
                ticket.getStatus(),
                TicketStatus.INDEVIDO,
                TicketStatusTransicaoService.MotivoTransicao.CLASSIFICACAO_INDEVIDO);

        TicketClassificacaoOperacional motivoOp =
                TicketClassificacaoOperacional.fromString(dto.getMotivoOperacional());

        aplicarMotivoEncerramentoOpcional(ticket, dto);

        ticketSlaPausaService.finalizarPausaSeNecessario(ticket);

        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        ticket.setStatus(TicketStatus.INDEVIDO);
        ticket.setDataEncerramento(agora);
        ticket.setClassificacaoOperacional(motivoOp);
        ticket.setClassificadoOperacionalEm(agora);
        ticket.setClassificadoOperacionalPorAnalistaId(analistaId);
        if (dto.getComentario() != null && !dto.getComentario().isBlank()) {
            ticket.setComentarioClassificacaoOperacional(dto.getComentario().trim());
        }

        ticket.setSlaResolucaoCumprido(null);
        ticketSlaResolucaoService.avaliarResolucao(ticket, agora);

        Ticket salvo = ticketRepository.save(ticket);

        ticketInteracaoService.registrarClassificacaoIndevido(
                salvo,
                motivoOp.name(),
                salvo.getComentarioClassificacaoOperacional());
        ticketSatisfacaoService.tratarAvaliacaoAoClassificarIndevido(salvo);

        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_CLASSIFICAR_INDEVIDO,
                AuditoriaService.ENTIDADE_TICKET,
                salvo.getNumeroTicket(),
                "Classificado indevido: " + motivoOp.name() + " por analista " + analistaId,
                analistaId);

        TicketResponseDTO response = ticketService.converterParaResponseSeguro(salvo);
        ticketSatisfacaoService.preencherResumoNoTicketResponse(response, salvo.getId());
        return response;
    }

    @Transactional
    public TicketResponseDTO reverterIndevido(String numeroTicket, Long analistaId) {
        if (analistaId == null) {
            throw new IllegalArgumentException("Sessao do analista e obrigatoria.");
        }

        Ticket ticket = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));

        if (ticket.getStatus() != TicketStatus.INDEVIDO) {
            throw new IllegalArgumentException(
                    "Somente tickets com status INDEVIDO podem ser revertidos.");
        }

        if (ticket.getClassificadoOperacionalEm() == null) {
            throw new IllegalArgumentException(
                    "Ticket nao possui data de classificacao operacional registrada.");
        }

        LocalDate diaClassificacao = ticket.getClassificadoOperacionalEm().toLocalDate();
        LocalDate hoje = LocalDate.now(CalendarioSlaHelper.FUSO_SLA);
        if (!diaClassificacao.equals(hoje)) {
            throw new IllegalArgumentException(
                    "Reversao permitida somente no mesmo dia da classificacao (America/Sao_Paulo).");
        }

        ticketStatusTransicaoService.validarTransicao(
                ticket.getStatus(),
                TicketStatus.ABERTO,
                TicketStatusTransicaoService.MotivoTransicao.REVERTER_INDEVIDO);

        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setDataEncerramento(null);
        ticket.setClassificacaoOperacional(null);
        ticket.setClassificadoOperacionalEm(null);
        ticket.setClassificadoOperacionalPorAnalistaId(null);
        ticket.setComentarioClassificacaoOperacional(null);
        ticket.setSlaResolucaoCumprido(null);

        Ticket salvo = ticketRepository.save(ticket);

        ticketInteracaoService.registrarReversaoIndevido(salvo);

        auditoriaService.registrar(
                AuditoriaService.ACAO_TICKET_REVERTER_INDEVIDO,
                AuditoriaService.ENTIDADE_TICKET,
                salvo.getNumeroTicket(),
                "Classificacao indevida revertida por analista " + analistaId,
                analistaId);

        TicketResponseDTO response = ticketService.converterParaResponseSeguro(salvo);
        ticketSatisfacaoService.preencherResumoNoTicketResponse(response, salvo.getId());
        return response;
    }

    private void aplicarMotivoEncerramentoOpcional(Ticket ticket, ClassificarTicketIndevidoRequestDTO dto) {
        if (dto.getGrupoId() == null && dto.getSubgrupoId() == null && dto.getMotivoId() == null) {
            return;
        }
        if (dto.getGrupoId() == null || dto.getSubgrupoId() == null || dto.getMotivoId() == null) {
            throw new IllegalArgumentException(
                    "Informe grupo, subgrupo e motivo de encerramento, ou omita os tres.");
        }
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
        Motivo motivo = motivoService.buscarEntidadeAtiva(dto.getMotivoId());
        if (!motivo.getSubgrupoCategoria().getId().equals(subgrupo.getId())) {
            throw new IllegalArgumentException("Motivo nao pertence ao subgrupo informado");
        }
        ticket.setGrupoCategoria(grupo);
        ticket.setSubgrupoCategoria(subgrupo);
        ticket.setMotivo(motivo);
    }
}
