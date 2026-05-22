package com.suporte.tickets.service;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSlaPausaServiceTest {

    @Mock
    private SlaTempoUtilService slaTempoUtilService;

    @InjectMocks
    private TicketSlaPausaService ticketSlaPausaService;

    @Test
    void finalizarPausaAcumulaMinutosEProrrogaVencimento() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.AGUARDANDO_CLIENTE);
        ticket.setSlaPausado(true);
        ticket.setSlaPausaInicio(LocalDateTime.of(2026, 5, 19, 10, 0));
        ticket.setSlaResolucaoVencimento(LocalDateTime.of(2026, 5, 19, 17, 0));
        ticket.setSlaResolucaoMinutosPausados(5L);

        LocalDateTime vencimentoOriginal = ticket.getSlaResolucaoVencimento();
        LocalDateTime vencimentoProrrogado = LocalDateTime.of(2026, 5, 19, 17, 30);
        when(slaTempoUtilService.calcularMinutosUteisEntre(
                eq(ticket.getSlaPausaInicio()),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(25L);
        when(slaTempoUtilService.adicionarMinutosUteis(vencimentoOriginal, 25L))
                .thenReturn(vencimentoProrrogado);

        ticketSlaPausaService.finalizarPausa(ticket);

        assertFalse(Boolean.TRUE.equals(ticket.getSlaPausado()));
        assertNull(ticket.getSlaPausaInicio());
        assertEquals(30L, ticket.getSlaResolucaoMinutosPausados());
        assertEquals(vencimentoProrrogado, ticket.getSlaResolucaoVencimento());
        verify(slaTempoUtilService).adicionarMinutosUteis(vencimentoOriginal, 25L);
    }

    @Test
    void finalizarPausaSemMinutosUteisAindaLimpaFlags() {
        Ticket ticket = new Ticket();
        ticket.setSlaPausado(true);
        ticket.setSlaPausaInicio(LocalDateTime.of(2026, 5, 19, 19, 0));
        when(slaTempoUtilService.calcularMinutosUteisEntre(
                eq(ticket.getSlaPausaInicio()),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);

        ticketSlaPausaService.finalizarPausa(ticket);

        assertFalse(Boolean.TRUE.equals(ticket.getSlaPausado()));
        assertNull(ticket.getSlaPausaInicio());
    }
}
