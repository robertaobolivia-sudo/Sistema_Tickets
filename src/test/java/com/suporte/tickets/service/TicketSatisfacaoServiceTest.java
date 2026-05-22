package com.suporte.tickets.service;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketSatisfacaoServiceTest {

    @Test
    void validarTicketPodeReceberSatisfacao_aceitaResolvido() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESOLVIDO);
        assertDoesNotThrow(() -> TicketSatisfacaoService.validarTicketPodeReceberSatisfacao(ticket));
    }

    @Test
    void validarTicketPodeReceberSatisfacao_rejeitaAberto() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.ABERTO);
        assertThrows(IllegalArgumentException.class,
                () -> TicketSatisfacaoService.validarTicketPodeReceberSatisfacao(ticket));
    }

    @Test
    void validarNota_rejeitaForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class, () -> TicketSatisfacaoService.validarNota(0));
        assertThrows(IllegalArgumentException.class, () -> TicketSatisfacaoService.validarNota(6));
    }
}
