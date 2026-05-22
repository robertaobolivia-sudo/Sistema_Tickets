package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorContagemDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndicadoresChamadosServiceTest {

    @Test
    void agruparPorAtendente_ordenadoPorTotal() throws Exception {
        Ticket t1 = ticketComAnalista("Ana");
        Ticket t2 = ticketComAnalista("Ana");
        Ticket t3 = ticketComAnalista("Bob");

        @SuppressWarnings("unchecked")
        List<IndicadorContagemDTO> ranking = (List<IndicadorContagemDTO>) invokeStatic(
                "agruparPorAtendente", List.of(t1, t2, t3));

        assertEquals(2, ranking.size());
        assertEquals("Ana", ranking.get(0).getRotulo());
        assertEquals(2, ranking.get(0).getTotal());
        assertEquals("Bob", ranking.get(1).getRotulo());
    }

    private static Ticket ticketComAnalista(String nome) {
        Analista a = new Analista();
        a.setNome(nome);
        Ticket t = new Ticket();
        t.setAnalistaResponsavel(a);
        t.setStatus(TicketStatus.ABERTO);
        t.setPrioridade(PrioridadeTicket.MEDIA);
        Cliente c = new Cliente();
        c.setId(1);
        t.setCliente(c);
        return t;
    }

    private static Object invokeStatic(String methodName, List<Ticket> tickets) throws Exception {
        Method m = IndicadoresChamadosService.class.getDeclaredMethod(methodName, List.class);
        m.setAccessible(true);
        return m.invoke(null, tickets);
    }
}
