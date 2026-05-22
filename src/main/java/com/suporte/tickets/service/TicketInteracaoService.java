package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketInteracaoRequestDTO;
import com.suporte.tickets.dto.TicketInteracaoResponseDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketInteracao;
import com.suporte.tickets.entity.TicketInteracaoTipo;
import com.suporte.tickets.entity.TicketInteracaoVisibilidade;
import com.suporte.tickets.repository.TicketInteracaoRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketInteracaoService {

    private final TicketInteracaoRepository ticketInteracaoRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public TicketInteracaoResponseDTO criarInteracaoManual(String numeroTicket, TicketInteracaoRequestDTO request) {
        validarRequest(request);

        Ticket ticket = buscarTicket(numeroTicket);

        TicketInteracao interacao = new TicketInteracao();
        interacao.setTicket(ticket);
        interacao.setTipoInteracao(request.getTipoInteracao());
        interacao.setVisibilidade(request.getVisibilidade());
        interacao.setMensagem(request.getMensagem().trim());

        return converterParaResponse(ticketInteracaoRepository.save(interacao));
    }

    @Transactional(readOnly = true)
    public List<TicketInteracaoResponseDTO> listarPorTicket(String numeroTicket) {
        buscarTicket(numeroTicket);
        return ticketInteracaoRepository.findByTicketNumeroTicketOrderByCriadoEmAsc(numeroTicket)
                .stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registrarAberturaAutomatica(Ticket ticket) {
        registrarAutomatica(
                ticket,
                TicketInteracaoTipo.ABERTURA,
                "Ticket aberto: " + textoSeguro(ticket.getMensagemInicial())
        );
    }

    @Transactional
    public void registrarEncerramentoAutomatico(Ticket ticket, String comentarioEncerramento) {
        registrarAutomatica(
                ticket,
                TicketInteracaoTipo.ENCERRAMENTO,
                "Ticket encerrado: " + textoSeguro(comentarioEncerramento)
        );
    }

    @Transactional
    public void registrarReaberturaAutomatica(Ticket ticket) {
        registrarAutomatica(
                ticket,
                TicketInteracaoTipo.COMENTARIO,
                "Ticket reaberto."
        );
    }

    @Transactional
    public void registrarMensagemEntradaExterna(Ticket ticket, String mensagem, String origemExternaId) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket e obrigatorio");
        }
        String texto = mensagem == null ? "" : mensagem.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Mensagem e obrigatoria");
        }
        if (origemExternaId != null && !origemExternaId.isBlank()) {
            texto = texto + " (ref: " + origemExternaId.trim() + ")";
        }
        TicketInteracao interacao = new TicketInteracao();
        interacao.setTicket(ticket);
        interacao.setTipoInteracao(TicketInteracaoTipo.MENSAGEM_CLIENTE);
        interacao.setVisibilidade(TicketInteracaoVisibilidade.PUBLICA);
        interacao.setMensagem(texto);
        ticketInteracaoRepository.save(interacao);
    }

    private void registrarAutomatica(Ticket ticket, TicketInteracaoTipo tipo, String mensagem) {
        TicketInteracao interacao = new TicketInteracao();
        interacao.setTicket(ticket);
        interacao.setTipoInteracao(tipo);
        interacao.setVisibilidade(TicketInteracaoVisibilidade.PUBLICA);
        interacao.setMensagem(mensagem);
        ticketInteracaoRepository.save(interacao);
    }

    private Ticket buscarTicket(String numeroTicket) {
        return ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket nao encontrado: " + numeroTicket));
    }

    private void validarRequest(TicketInteracaoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados da interacao sao obrigatorios");
        }
        if (request.getTipoInteracao() == null) {
            throw new IllegalArgumentException("Tipo de interacao e obrigatorio");
        }
        if (request.getTipoInteracao() != TicketInteracaoTipo.COMENTARIO
                && request.getTipoInteracao() != TicketInteracaoTipo.NOTA_INTERNA) {
            throw new IllegalArgumentException("Tipo de interacao manual invalido");
        }
        if (request.getVisibilidade() == null) {
            throw new IllegalArgumentException("Visibilidade e obrigatoria");
        }
        if (request.getMensagem() == null || request.getMensagem().isBlank()) {
            throw new IllegalArgumentException("Mensagem e obrigatoria");
        }
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private TicketInteracaoResponseDTO converterParaResponse(TicketInteracao interacao) {
        TicketInteracaoResponseDTO response = new TicketInteracaoResponseDTO();
        response.setId(interacao.getId());
        response.setNumeroTicket(interacao.getTicket().getNumeroTicket());
        response.setTipoInteracao(interacao.getTipoInteracao().name());
        response.setVisibilidade(interacao.getVisibilidade().name());
        response.setMensagem(interacao.getMensagem());
        response.setCriadoEm(interacao.getCriadoEm());
        return response;
    }
}
