package com.suporte.tickets.service;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardOperacaoClienteB2BServiceTest {

    @Test
    void tmaSoEmAtendimento() {
        Ticket t = new Ticket();
        t.setNumeroTicket("TK-1");
        t.setStatus(TicketStatus.ABERTO);
        t.setDataAbertura(LocalDateTime.now().minusMinutes(10));
        var dto = DashboardOperacaoClienteB2BService.mapearTicket(t, LocalDateTime.now());
        assertEquals("-", dto.getTmaFormatado());
        assertEquals("TICKET", dto.getTipo());
    }

    @Test
    void formatarDuracaoNegativa() {
        assertEquals("-", DashboardOperacaoClienteB2BService.formatarDuracao(-1));
    }
}
