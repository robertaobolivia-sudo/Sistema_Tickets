package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoEvolucaoDiaDTO;
import com.suporte.tickets.entity.TicketSatisfacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketSatisfacaoEvolucaoServiceTest {

    @Test
    void agruparPorDia_ordenadoECalculaMedia() {
        TicketSatisfacao a = avaliacao(LocalDate.of(2026, 5, 2), 5);
        TicketSatisfacao b = avaliacao(LocalDate.of(2026, 5, 1), 3);
        TicketSatisfacao c = avaliacao(LocalDate.of(2026, 5, 1), 1);

        List<TicketSatisfacaoEvolucaoDiaDTO> dias =
                TicketSatisfacaoEvolucaoService.agruparPorDia(List.of(a, b, c));

        assertEquals(2, dias.size());
        assertEquals(LocalDate.of(2026, 5, 1), dias.get(0).getData());
        assertEquals(2, dias.get(0).getTotalAvaliacoes());
        assertEquals(2.0, dias.get(0).getMediaNota());
        assertEquals(0, dias.get(0).getPositivas());
        assertEquals(1, dias.get(0).getNegativas());

        assertEquals(LocalDate.of(2026, 5, 2), dias.get(1).getData());
        assertEquals(1, dias.get(1).getPositivas());
    }

    private static TicketSatisfacao avaliacao(LocalDate dia, int nota) {
        TicketSatisfacao s = new TicketSatisfacao();
        s.setNota(nota);
        s.setCriadoEm(dia.atTime(10, 0));
        return s;
    }
}
