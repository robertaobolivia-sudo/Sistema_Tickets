package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketObservacaoAtendimentoTest {

    @Test
    void normalizar_vazioRetornaNull() {
        assertNull(TicketService.normalizarObservacaoAtendimento(null));
        assertNull(TicketService.normalizarObservacaoAtendimento("   "));
    }

    @Test
    void normalizar_trimTexto() {
        assertEquals("Nota interna", TicketService.normalizarObservacaoAtendimento("  Nota interna  "));
    }

    @Test
    void normalizar_excedeLimiteLancaErro() {
        assertThrows(IllegalArgumentException.class, () ->
                TicketService.normalizarObservacaoAtendimento("x".repeat(2001)));
    }
}
