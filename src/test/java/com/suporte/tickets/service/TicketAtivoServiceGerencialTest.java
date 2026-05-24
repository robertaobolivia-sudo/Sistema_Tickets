package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketAtivoServiceGerencialTest {

    @Test
    void indevidoForaAtendimentoOperacional() {
        assertFalse(TicketAtivoService.isAtendimentoOperacionalValido(TicketStatus.INDEVIDO));
        assertTrue(TicketAtivoService.isTicketIndevido(TicketStatus.INDEVIDO));
        assertTrue(TicketAtivoService.isAtendimentoOperacionalValido(TicketStatus.ABERTO));
        assertFalse(TicketAtivoService.isStatusAtivo(TicketStatus.INDEVIDO));
    }
}
