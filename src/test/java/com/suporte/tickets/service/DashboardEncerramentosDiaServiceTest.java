package com.suporte.tickets.service;

import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardEncerramentosDiaServiceTest {

    @Test
    void rotuloRecorrenciaUsaMotivo() {
        Ticket ticket = new Ticket();
        Motivo motivo = new Motivo();
        motivo.setNome("  Instalação  ");
        ticket.setMotivo(motivo);
        assertEquals("Instalação", DashboardEncerramentosDiaService.rotuloRecorrencia(ticket));
    }

    @Test
    void rotuloRecorrenciaSemMotivo() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESOLVIDO);
        assertEquals("Sem motivo", DashboardEncerramentosDiaService.rotuloRecorrencia(ticket));
    }
}
