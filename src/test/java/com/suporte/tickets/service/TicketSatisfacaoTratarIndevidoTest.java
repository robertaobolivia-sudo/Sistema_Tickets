package com.suporte.tickets.service;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSatisfacaoTratarIndevidoTest {

    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;

    @InjectMocks
    private TicketSatisfacaoService ticketSatisfacaoService;

    @Test
    void tratarAvaliacao_pendenteViraNaoEnviada() {
        Ticket ticket = new Ticket();
        ticket.setId(7);
        ticket.setNumeroTicket("TK-7");
        ticket.setStatus(TicketStatus.INDEVIDO);
        TicketSatisfacao sat = new TicketSatisfacao();
        sat.setStatus(TicketSatisfacaoStatus.PENDENTE);
        sat.setTicket(ticket);
        when(ticketSatisfacaoRepository.findByTicket_Id(7)).thenReturn(Optional.of(sat));
        when(ticketSatisfacaoRepository.save(sat)).thenReturn(sat);

        ticketSatisfacaoService.tratarAvaliacaoAoClassificarIndevido(ticket);

        assertEquals(TicketSatisfacaoStatus.NAO_ENVIADA, sat.getStatus());
        verify(ticketSatisfacaoRepository).save(sat);
    }
}
