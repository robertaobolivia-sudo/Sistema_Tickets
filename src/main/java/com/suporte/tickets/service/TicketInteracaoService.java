package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketInteracaoRequestDTO;
import com.suporte.tickets.dto.TicketInteracaoResponseDTO;
import com.suporte.tickets.entity.Contato;
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
    private final ContatoAtendimentoOrigemService contatoAtendimentoOrigemService;

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
        TicketInteracao interacao = new TicketInteracao();
        interacao.setTicket(ticket);
        interacao.setTipoInteracao(TicketInteracaoTipo.ABERTURA);
        interacao.setVisibilidade(TicketInteracaoVisibilidade.PUBLICA);
        interacao.setMensagem("Ticket aberto: " + textoSeguro(ticket.getMensagemInicial()));
        copiarOrigemTicketParaInteracao(ticket, interacao);
        ticketInteracaoRepository.save(interacao);
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
    public void registrarEntradaOperacional(Ticket ticket, String motivoOperacional) {
        registrarAutomatica(
                ticket,
                TicketInteracaoTipo.ENCERRAMENTO,
                "Entrada bloqueada por etiqueta operacional: " + textoSeguro(motivoOperacional));
    }

    @Transactional
    public void registrarClassificacaoIndevido(Ticket ticket, String motivoOperacional, String comentario) {
        String msg = "Ticket classificado como indevido (" + textoSeguro(motivoOperacional) + ")";
        if (comentario != null && !comentario.isBlank()) {
            msg = msg + ": " + textoSeguro(comentario);
        }
        registrarAutomatica(ticket, TicketInteracaoTipo.ENCERRAMENTO, msg);
    }

    @Transactional
    public void registrarReversaoIndevido(Ticket ticket) {
        registrarAutomatica(
                ticket,
                TicketInteracaoTipo.COMENTARIO,
                "Classificação indevida revertida. Ticket retornou para Aberto.");
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
        registrarMensagemEntradaExterna(ticket, mensagem, origemExternaId, null);
    }

    public void registrarMensagemEntradaExterna(
            Ticket ticket, String mensagem, String origemExternaId, String telefoneEntrada) {
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
        aplicarOrigemMensagem(ticket, interacao, telefoneEntrada);
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

    private void aplicarOrigemMensagem(Ticket ticket, TicketInteracao interacao, String telefoneEntrada) {
        Contato contato = ticket != null ? ticket.getContato() : null;
        if (contato == null || telefoneEntrada == null || telefoneEntrada.isBlank()) {
            return;
        }
        contatoAtendimentoOrigemService.aplicarOrigemNaInteracao(interacao, contato, telefoneEntrada);
    }

    private static void copiarOrigemTicketParaInteracao(Ticket ticket, TicketInteracao interacao) {
        if (ticket == null || interacao == null) {
            return;
        }
        if (ticket.getAtendimentoTelefone() != null && !ticket.getAtendimentoTelefone().isBlank()) {
            interacao.setTelefoneOrigem(ticket.getAtendimentoTelefone().trim());
        }
        if (ticket.getAtendimentoTelefoneNormalizado() != null
                && !ticket.getAtendimentoTelefoneNormalizado().isBlank()) {
            interacao.setTelefoneOrigemNormalizado(ticket.getAtendimentoTelefoneNormalizado().trim());
        }
        if (ticket.getAtendimentoTelefoneTipo() != null && !ticket.getAtendimentoTelefoneTipo().isBlank()) {
            interacao.setTelefoneOrigemTipo(ticket.getAtendimentoTelefoneTipo().trim());
        }
    }

    private TicketInteracaoResponseDTO converterParaResponse(TicketInteracao interacao) {
        TicketInteracaoResponseDTO response = new TicketInteracaoResponseDTO();
        response.setId(interacao.getId());
        response.setNumeroTicket(interacao.getTicket().getNumeroTicket());
        response.setTipoInteracao(interacao.getTipoInteracao().name());
        response.setVisibilidade(interacao.getVisibilidade().name());
        response.setMensagem(interacao.getMensagem());
        response.setCriadoEm(interacao.getCriadoEm());
        if (interacao.getTelefoneOrigem() != null && !interacao.getTelefoneOrigem().isBlank()) {
            response.setTelefoneOrigem(interacao.getTelefoneOrigem().trim());
        }
        if (interacao.getTelefoneOrigemTipo() != null && !interacao.getTelefoneOrigemTipo().isBlank()) {
            response.setTelefoneOrigemTipo(interacao.getTelefoneOrigemTipo().trim());
        }
        return response;
    }
}
