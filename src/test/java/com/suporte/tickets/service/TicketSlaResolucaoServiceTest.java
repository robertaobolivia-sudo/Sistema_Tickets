package com.suporte.tickets.service;

import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSlaResolucaoServiceTest {

    @Mock
    private SlaMetaService slaMetaService;

    @Mock
    private SlaTempoUtilService slaTempoUtilService;

    @Mock
    private SlaVisualStatusHelper slaVisualStatusHelper;

    @InjectMocks
    private TicketSlaResolucaoService ticketSlaResolucaoService;

    @Test
    void resolucaoPausadaRetornaStatusPausadoMesmoComVencimentoPassado() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.AGUARDANDO_CLIENTE);
        ticket.setSlaPausado(true);
        ticket.setSlaResolucaoVencimento(LocalDateTime.now().minusHours(5));

        assertEquals(SlaStatus.PAUSADO, ticketSlaResolucaoService.calcularStatusResolucao(ticket));
    }

    @Test
    void resolucaoProximoVencimentoQuandoDentroDeDuasHoras() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
        ticket.setSlaPausado(false);
        ticket.setSlaResolucaoVencimento(LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA).plusHours(1));
        when(slaVisualStatusHelper.isProximoVencimento(any())).thenReturn(true);

        assertEquals(SlaStatus.PROXIMO_DO_VENCIMENTO, ticketSlaResolucaoService.calcularStatusResolucao(ticket));
    }
}
