package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketFiltroDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketRelatorioCsvMotivoAvaliacaoTest {

    @Mock
    private TicketBuscaService ticketBuscaService;

    @InjectMocks
    private TicketRelatorioCsvService ticketRelatorioCsvService;

    @Test
    void csv_contemColunasMotivoEAvaliacao() {
        TicketResponseDTO t = new TicketResponseDTO();
        t.setNumeroTicket("TK-1");
        t.setCliente("Acme");
        t.setStatus("RESOLVIDO");
        t.setMotivoNome("Configuracao");
        t.setSatisfacaoStatus("RESPONDIDA");
        t.setSatisfacaoNota(5);
        t.setSatisfacaoComentario("Otimo");
        t.setSatisfacaoEnvioStatus("Simulado");
        t.setSatisfacaoEnviadaEm(LocalDateTime.of(2026, 5, 1, 10, 0));
        t.setSatisfacaoRespondidaEm(LocalDateTime.of(2026, 5, 2, 11, 0));

        when(ticketBuscaService.buscar(any(TicketFiltroDTO.class))).thenReturn(List.of(t));

        byte[] csv = ticketRelatorioCsvService.gerarCsv(new TicketFiltroDTO());
        String texto = new String(csv, StandardCharsets.UTF_8);

        assertTrue(texto.contains("Motivo"));
        assertTrue(texto.contains("Status pesquisa"));
        assertTrue(texto.contains("Configuracao"));
        assertTrue(texto.contains("Respondida"));
        assertTrue(texto.contains("5"));
        assertFalse(texto.toLowerCase().contains("token"));
        assertFalse(texto.toLowerCase().contains("hash"));
    }

    @Test
    void helper_semAvaliacao_naoQuebra() {
        TicketResponseDTO dto = new TicketResponseDTO();
        TicketSatisfacaoResumoRelatorioHelper.aplicarEmDto(dto, null);
        assertTrue(dto.getSatisfacaoStatus() == null);
    }
}
