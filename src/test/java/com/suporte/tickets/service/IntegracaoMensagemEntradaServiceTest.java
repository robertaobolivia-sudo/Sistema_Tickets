package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegracaoMensagemEntradaServiceTest {

    @Test
    void parseStatusResposta_valido() {
        assertEquals(TicketStatus.EM_ATENDIMENTO, parseStatus("EM_ATENDIMENTO"));
    }

    @Test
    void parseStatusResposta_invalidoRetornaAberto() {
        assertEquals(TicketStatus.ABERTO, parseStatus("-"));
    }

    private static TicketStatus parseStatus(String status) {
        if (status != null && TicketStatus.isValido(status)) {
            return TicketStatus.valueOf(status.trim().toUpperCase());
        }
        return TicketStatus.ABERTO;
    }
}
