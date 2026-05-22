package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoRespostaRequestDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSatisfacaoResponderExpiracaoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @InjectMocks
    private TicketSatisfacaoService ticketSatisfacaoService;

    @Test
    void responder_pendenteValida_viraRespondida() {
        Ticket ticket = ticket("T-1", 1);
        TicketSatisfacao s = pendente(10L, LocalDateTime.now().plusDays(2));
        s.setTicket(ticket);
        when(ticketRepository.findByNumeroTicket("T-1")).thenReturn(Optional.of(ticket));
        when(ticketSatisfacaoRepository.findByTicket_Id(1)).thenReturn(Optional.of(s));
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        TicketSatisfacaoRespostaRequestDTO req = new TicketSatisfacaoRespostaRequestDTO();
        req.setNota(5);
        req.setComentario(" Otimo ");

        var dto = ticketSatisfacaoService.responderAvaliacao("T-1", req);

        assertEquals(TicketSatisfacaoStatus.RESPONDIDA, s.getStatus());
        assertEquals(5, s.getNota());
        assertEquals("Otimo", s.getComentario());
        assertEquals(TicketSatisfacaoStatus.RESPONDIDA.name(), dto.getStatus());
        assertEquals(5, dto.getNota());
    }

    @Test
    void responder_notaInvalida_erro() {
        assertThrows(IllegalArgumentException.class,
                () -> ticketSatisfacaoService.responderAvaliacao("T-1",
                        new TicketSatisfacaoRespostaRequestDTO(6, null)));
    }

    @Test
    void responder_jaRespondida_erro() {
        Ticket ticket = ticket("T-2", 2);
        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.RESPONDIDA);
        when(ticketRepository.findByNumeroTicket("T-2")).thenReturn(Optional.of(ticket));
        when(ticketSatisfacaoRepository.findByTicket_Id(2)).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class, () -> ticketSatisfacaoService.responderAvaliacao(
                "T-2", new TicketSatisfacaoRespostaRequestDTO(4, null)));
    }

    @Test
    void responder_naoEnviada_erro() {
        Ticket ticket = ticket("T-3", 3);
        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(TicketSatisfacaoStatus.NAO_ENVIADA);
        when(ticketRepository.findByNumeroTicket("T-3")).thenReturn(Optional.of(ticket));
        when(ticketSatisfacaoRepository.findByTicket_Id(3)).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class, () -> ticketSatisfacaoService.responderAvaliacao(
                "T-3", new TicketSatisfacaoRespostaRequestDTO(3, null)));
    }

    @Test
    void responder_expiradaNoPrazo_marcaExpiradaERejeita() {
        Ticket ticket = ticket("T-4", 4);
        TicketSatisfacao s = pendente(11L, LocalDateTime.now().minusHours(1));
        when(ticketRepository.findByNumeroTicket("T-4")).thenReturn(Optional.of(ticket));
        when(ticketSatisfacaoRepository.findByTicket_Id(4)).thenReturn(Optional.of(s));
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> ticketSatisfacaoService.responderAvaliacao(
                "T-4", new TicketSatisfacaoRespostaRequestDTO(2, null)));
        assertEquals(TicketSatisfacaoStatus.EXPIRADA, s.getStatus());
    }

    @Test
    void job_expiraPendentesVencidas_idempotente() {
        TicketSatisfacao vencida = pendente(20L, LocalDateTime.now().minusMinutes(5));

        when(ticketSatisfacaoRepository.findByStatusAndExpiraEmBefore(
                eq(TicketSatisfacaoStatus.PENDENTE), any(LocalDateTime.class)))
                .thenReturn(List.of(vencida))
                .thenReturn(List.of());
        when(ticketSatisfacaoRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        int n1 = ticketSatisfacaoService.marcarPendentesExpiradas();
        assertEquals(1, n1);
        assertEquals(TicketSatisfacaoStatus.EXPIRADA, vencida.getStatus());

        int n2 = ticketSatisfacaoService.marcarPendentesExpiradas();
        assertEquals(0, n2);
    }

    @Test
    void estaExpirada_noLimite() {
        LocalDateTime expira = LocalDateTime.of(2026, 5, 15, 18, 0);
        TicketSatisfacao s = pendente(1L, expira);
        assertEquals(true, TicketSatisfacaoService.estaExpirada(s, expira));
        assertEquals(false, TicketSatisfacaoService.estaExpirada(s, expira.minusMinutes(1)));
    }

    private static Ticket ticket(String numero, int id) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setNumeroTicket(numero);
        t.setStatus(TicketStatus.RESOLVIDO);
        return t;
    }

    private static TicketSatisfacao pendente(Long id, LocalDateTime expiraEm) {
        TicketSatisfacao s = new TicketSatisfacao();
        s.setId(id);
        s.setStatus(TicketSatisfacaoStatus.PENDENTE);
        s.setExpiraEm(expiraEm);
        return s;
    }
}
