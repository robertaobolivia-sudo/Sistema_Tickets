package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardAvaliacaoTempoRealDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAvaliacaoTempoRealServiceTest {

    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @InjectMocks
    private DashboardAvaliacaoTempoRealService service;

    @Test
    void agregaPesquisasEMedia() {
        Ticket t = new Ticket();
        t.setStatus(TicketStatus.RESOLVIDO);

        TicketSatisfacao ruim = new TicketSatisfacao();
        ruim.setTicket(t);
        ruim.setStatus(TicketSatisfacaoStatus.RESPONDIDA);
        ruim.setNota(1);

        TicketSatisfacao boa = new TicketSatisfacao();
        boa.setTicket(t);
        boa.setStatus(TicketSatisfacaoStatus.RESPONDIDA);
        boa.setNota(5);

        TicketSatisfacao pendente = new TicketSatisfacao();
        pendente.setTicket(t);
        pendente.setStatus(TicketSatisfacaoStatus.PENDENTE);

        when(ticketSatisfacaoRepository.findAllOperacionalExcluindoIndevido(TicketStatus.INDEVIDO))
                .thenReturn(List.of(ruim, boa, pendente));

        DashboardAvaliacaoTempoRealDTO dto = service.obter();
        assertEquals(2, dto.getPesquisasRespondidas());
        assertEquals(1, dto.getPesquisasPendentes());
        assertEquals(1, dto.getAvaliacoesRuins());
        assertEquals(3.0, dto.getMediaAtual());
    }

    @Test
    void mediaNullSemNotas() {
        Ticket t = new Ticket();
        t.setStatus(TicketStatus.RESOLVIDO);
        TicketSatisfacao pendente = new TicketSatisfacao();
        pendente.setTicket(t);
        pendente.setStatus(TicketSatisfacaoStatus.PENDENTE);
        when(ticketSatisfacaoRepository.findAllOperacionalExcluindoIndevido(TicketStatus.INDEVIDO))
                .thenReturn(List.of(pendente));
        assertNull(service.obter().getMediaAtual());
    }
}
