package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Sprint F26/F28: PDF não expõe conexão legada.
 */
@ExtendWith(MockitoExtension.class)
class TicketPdfServiceF26Test {

    @Mock private TicketService ticketService;
    @Mock private TicketInteracaoService ticketInteracaoService;

    @InjectMocks
    private TicketPdfService ticketPdfService;

    @Test
    void linhasDadosTicket_semRotuloConexaoLegado() {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setNumeroTicket("TK-NEW");

        String[][] linhas = ticketPdfService.montarLinhasDadosTicket(dto);

        assertFalse(Arrays.stream(linhas).anyMatch(l -> l[0].contains("Conexão")));
    }
}
