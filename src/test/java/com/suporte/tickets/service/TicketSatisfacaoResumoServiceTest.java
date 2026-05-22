package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoResumoDTO;
import com.suporte.tickets.entity.TicketSatisfacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TicketSatisfacaoResumoServiceTest {

    @Test
    void agregar_vazio() {
        TicketSatisfacaoResumoDTO dto = TicketSatisfacaoResumoService.agregar(List.of());
        assertEquals(0, dto.getTotalAvaliacoes());
        assertNull(dto.getMediaGeral());
    }

    @Test
    void agregar_calculaMediaEPercentuais() {
        TicketSatisfacao a = new TicketSatisfacao();
        a.setNota(5);
        TicketSatisfacao b = new TicketSatisfacao();
        b.setNota(1);
        TicketSatisfacaoResumoDTO dto = TicketSatisfacaoResumoService.agregar(List.of(a, b));
        assertEquals(2, dto.getTotalAvaliacoes());
        assertEquals(3.0, dto.getMediaGeral());
        assertEquals(50.0, dto.getPercentualPositivas());
        assertEquals(50.0, dto.getPercentualNegativas());
        assertEquals(1, dto.getQuantidadeNota1());
        assertEquals(1, dto.getQuantidadeNota5());
    }

    @Test
    void agregar_ignoraPendenteSemNota() {
        TicketSatisfacao comNota = new TicketSatisfacao();
        comNota.setNota(5);
        TicketSatisfacao pendente = new TicketSatisfacao();
        pendente.setNota(null);
        TicketSatisfacaoResumoDTO dto = TicketSatisfacaoResumoService.agregar(List.of(comNota, pendente));
        assertEquals(2, dto.getTotalAvaliacoes());
        assertEquals(5.0, dto.getMediaGeral());
    }
}
