package com.suporte.tickets.service;

import com.suporte.tickets.dto.NotificacaoContadorDTO;
import com.suporte.tickets.dto.NotificacaoInternaDTO;
import com.suporte.tickets.entity.NotificacaoInterna;
import com.suporte.tickets.entity.NotificacaoTipo;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.NotificacaoInternaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoInternaService {

    private final NotificacaoInternaRepository notificacaoInternaRepository;

    @Transactional
    public void registrarTicketEscalonado(Ticket ticket) {
        if (ticket == null || ticket.getNumeroTicket() == null) {
            return;
        }
        String observacao = ticket.getEscalonamentoObservacao();
        String mensagem = "O ticket " + ticket.getNumeroTicket() + " foi escalonado.";
        if (observacao != null && !observacao.isBlank()) {
            mensagem += " Observação: " + observacao.trim();
        }
        criar(NotificacaoTipo.TICKET_ESCALONADO, "Ticket escalonado", mensagem, ticket.getNumeroTicket());
    }

    @Transactional
    public void registrarEscalonamentoRemovido(String numeroTicket) {
        if (numeroTicket == null || numeroTicket.isBlank()) {
            return;
        }
        String mensagem = "O escalonamento do ticket " + numeroTicket.trim() + " foi removido.";
        criar(NotificacaoTipo.ESCALONAMENTO_REMOVIDO, "Escalonamento removido", mensagem, numeroTicket.trim());
    }

    @Transactional(readOnly = true)
    public List<NotificacaoInternaDTO> listarRecentes() {
        return notificacaoInternaRepository.findTop50ByOrderByCriadoEmDesc()
                .stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacaoInternaDTO> listarNaoLidas() {
        return notificacaoInternaRepository.findTop50ByLidaFalseOrderByCriadoEmDesc()
                .stream()
                .map(this::converter)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificacaoContadorDTO contarNaoLidas() {
        return new NotificacaoContadorDTO(notificacaoInternaRepository.countByLidaFalse());
    }

    @Transactional
    public NotificacaoInternaDTO marcarComoLida(Long id) {
        NotificacaoInterna notificacao = notificacaoInternaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada: " + id));
        if (!Boolean.TRUE.equals(notificacao.getLida())) {
            notificacao.setLida(true);
            notificacao.setLidaEm(LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA));
            notificacao = notificacaoInternaRepository.save(notificacao);
        }
        return converter(notificacao);
    }

    @Transactional
    public long marcarTodasComoLidas() {
        List<NotificacaoInterna> naoLidas = notificacaoInternaRepository.findByLidaFalseOrderByCriadoEmDesc();
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        int atualizadas = 0;
        for (NotificacaoInterna notificacao : naoLidas) {
            notificacao.setLida(true);
            notificacao.setLidaEm(agora);
            atualizadas++;
        }
        if (!naoLidas.isEmpty()) {
            notificacaoInternaRepository.saveAll(naoLidas);
        }
        return atualizadas;
    }

    @Transactional
    public boolean criarSeNaoExistirNaoLida(NotificacaoTipo tipo, String titulo, String mensagem, String ticketNumero) {
        if (ticketNumero == null || ticketNumero.isBlank()) {
            return false;
        }
        String numero = ticketNumero.trim();
        if (notificacaoInternaRepository.existsByTipoAndTicketNumeroAndLidaFalse(tipo, numero)) {
            return false;
        }
        criar(tipo, titulo, mensagem, numero);
        return true;
    }

    private void criar(NotificacaoTipo tipo, String titulo, String mensagem, String ticketNumero) {
        NotificacaoInterna notificacao = new NotificacaoInterna();
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setTicketNumero(ticketNumero);
        notificacao.setLida(false);
        notificacao.setCriadoEm(LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA));
        notificacaoInternaRepository.save(notificacao);
    }

    private NotificacaoInternaDTO converter(NotificacaoInterna notificacao) {
        return new NotificacaoInternaDTO(
                notificacao.getId(),
                notificacao.getTipo() != null ? notificacao.getTipo().name() : null,
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.getTicketNumero(),
                Boolean.TRUE.equals(notificacao.getLida()),
                notificacao.getCriadoEm(),
                notificacao.getLidaEm()
        );
    }
}
