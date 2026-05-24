package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketAtivoServiceTest {

    @Test
    void normalizarTelefone_apenasDigitos() {
        assertEquals("11999990000", TicketAtivoService.normalizarTelefone("(11) 99999-0000"));
        assertNull(TicketAtivoService.normalizarTelefone("   "));
    }

    @Test
    void isStatusAtivo() {
        assertTrue(TicketAtivoService.isStatusAtivo(TicketStatus.EM_ATENDIMENTO));
        assertFalse(TicketAtivoService.isStatusAtivo(TicketStatus.RESOLVIDO));
        assertFalse(TicketAtivoService.isStatusAtivo(TicketStatus.CANCELADO));
        assertFalse(TicketAtivoService.isStatusAtivo(TicketStatus.INDEVIDO));
    }
}
