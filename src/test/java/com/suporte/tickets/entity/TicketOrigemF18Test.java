package com.suporte.tickets.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketOrigemF18Test {

    @Test
    void isValido_aceitaSomenteDuasOrigens() {
        assertTrue(TicketOrigem.isValido("RECEPTIVO_WHATSAPP"));
        assertTrue(TicketOrigem.isValido(" ATIVO_MANUAL "));
        assertFalse(TicketOrigem.isValido("DESCONHECIDA"));
        assertFalse(TicketOrigem.isValido(""));
        assertFalse(TicketOrigem.isValido(null));
    }
}
