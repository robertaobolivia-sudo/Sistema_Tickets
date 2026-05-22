package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorMotivoItemDTO;
import com.suporte.tickets.dto.IndicadoresPesquisaResumoDTO;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndicadoresEncerramentoAvaliacaoServiceTest {

    @Test
    void resolverStatusPesquisa_vazioRetornaNull() {
        assertNull(IndicadoresEncerramentoAvaliacaoService.resolverStatusPesquisa(null));
        assertNull(IndicadoresEncerramentoAvaliacaoService.resolverStatusPesquisa("  "));
    }

    @Test
    void resolverStatusPesquisa_normaliza() {
        assertEquals(TicketSatisfacaoStatus.PENDENTE,
                IndicadoresEncerramentoAvaliacaoService.resolverStatusPesquisa("pendente"));
    }

    @Test
    void agregarPesquisa_mediaIgnoraPendenteSemNota() throws Exception {
        TicketSatisfacao pendente = sat(TicketSatisfacaoStatus.PENDENTE, null);
        TicketSatisfacao respondida = sat(TicketSatisfacaoStatus.RESPONDIDA, 5);
        TicketSatisfacao manual = sat(TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE, 3);

        IndicadoresPesquisaResumoDTO p = (IndicadoresPesquisaResumoDTO) invokeStatic(
                "agregarPesquisa", List.of(pendente, respondida, manual));

        assertEquals(3, p.getTotalPesquisas());
        assertEquals(1, p.getPendentes());
        assertEquals(1, p.getRespondidas());
        assertEquals(1, p.getRegistradasManualmente());
        assertEquals(4.0, p.getMediaNota());
        assertEquals(1, p.getQuantidadeNota5());
        assertEquals(1, p.getQuantidadeNota3());
    }

    @Test
    void agregarTopMotivos_contaPorMotivo() throws Exception {
        Motivo m1 = motivo(1L, "Falta de uso");
        Motivo m2 = motivo(2L, "Resolvido");
        Ticket t1 = ticketEncerrado(m1);
        Ticket t2 = ticketEncerrado(m1);
        Ticket t3 = ticketEncerrado(m2);

        @SuppressWarnings("unchecked")
        List<IndicadorMotivoItemDTO> top = (List<IndicadorMotivoItemDTO>) invokeStatic(
                "agregarTopMotivos", List.of(t1, t2, t3));

        assertEquals(2, top.size());
        assertEquals(1L, top.get(0).getMotivoId());
        assertEquals(2L, top.get(0).getTotalTickets());
    }

    @Test
    void obter_periodoInvalido() {
        var service = new IndicadoresEncerramentoAvaliacaoService(null, null);
        assertThrows(IllegalArgumentException.class, () -> service.obter(
                java.time.LocalDate.of(2026, 5, 10),
                java.time.LocalDate.of(2026, 5, 1),
                null, null, null, null));
    }

    private static TicketSatisfacao sat(TicketSatisfacaoStatus status, Integer nota) {
        TicketSatisfacao s = new TicketSatisfacao();
        s.setStatus(status);
        s.setNota(nota);
        return s;
    }

    private static Motivo motivo(Long id, String nome) {
        Motivo m = new Motivo();
        m.setId(id);
        m.setNome(nome);
        SubgrupoCategoria sub = new SubgrupoCategoria();
        sub.setNome("Sub");
        m.setSubgrupoCategoria(sub);
        return m;
    }

    private static Ticket ticketEncerrado(Motivo motivo) {
        Ticket t = new Ticket();
        t.setMotivo(motivo);
        t.setDataEncerramento(LocalDateTime.now());
        return t;
    }

    private static Object invokeStatic(String name, Object arg) throws Exception {
        Method m = IndicadoresEncerramentoAvaliacaoService.class.getDeclaredMethod(name, List.class);
        m.setAccessible(true);
        return m.invoke(null, arg);
    }
}
