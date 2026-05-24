package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClientePendenciasDTO;
import com.suporte.tickets.dto.DashboardGrupoDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Sprint F26/F29: Dashboard agrupa por Cliente (sem coluna conexao). */
@ExtendWith(MockitoExtension.class)
class DashboardClienteAgrupamentoTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private AnalistaRepository analistaRepository;
    @Mock private TicketService ticketService;
    @Mock private IndicadoresEncerramentoAvaliacaoService indicadoresEncerramentoAvaliacaoService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void resumo_ticketsPorCliente_agrupaPorNomeCliente() {
        when(ticketRepository.count()).thenReturn(2L);
        when(ticketRepository.countByStatus(any())).thenReturn(0L);
        when(ticketRepository.countByAnalistaResponsavelIsNullAndStatusIn(any())).thenReturn(0L);
        when(ticketRepository.countByDataAberturaGreaterThanEqual(any())).thenReturn(0L);
        when(ticketRepository.countByStatusAndDataEncerramentoGreaterThanEqual(any(), any())).thenReturn(0L);
        when(clienteRepository.countByAtivoTrue()).thenReturn(1L);
        when(analistaRepository.countByAtivoTrueAndStatusOperador(any())).thenReturn(0L);

        Cliente cliente = new Cliente();
        cliente.setNome("Bruno Fast");
        Ticket t1 = new Ticket();
        t1.setCliente(cliente);
        Ticket t2 = new Ticket();
        t2.setCliente(cliente);
        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));

        var resumo = dashboardService.obterResumo();

        List<DashboardGrupoDTO> porCliente = resumo.getTicketsPorCliente();
        assertEquals(1, porCliente.size());
        assertEquals("Bruno Fast", porCliente.get(0).getNome());
        assertEquals(2L, porCliente.get(0).getTotal());
    }

    @Test
    void pendencias_agrupamPorCliente() {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente X");
        Ticket t = new Ticket();
        t.setCliente(cliente);
        t.setStatus(TicketStatus.ABERTO);
        t.setNumeroTicket("TK-1");
        when(ticketRepository.findByStatusInOrderByDataAberturaAsc(any())).thenReturn(List.of(t));

        List<ClientePendenciasDTO> grupos = dashboardService.listarPendenciasPorCliente();

        assertEquals(1, grupos.size());
        assertEquals("Cliente X", grupos.get(0).getCliente());
    }
}
