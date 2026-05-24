package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketFiltroDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.TicketOrigem;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Busca de tickets: filtros ativos (cliente, origem, datas). */
@ExtendWith(MockitoExtension.class)
class TicketBuscaFiltroTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketSatisfacaoRepository ticketSatisfacaoRepository;
    @Mock private TicketService ticketService;

    @InjectMocks
    private TicketBuscaService ticketBuscaService;

    @Test
    void filtroVazio_usaListagemPadrao() {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setNumeroTicket("TK-1");
        when(ticketService.listarTodos()).thenReturn(List.of(dto));

        TicketFiltroDTO filtro = new TicketFiltroDTO();

        List<TicketResponseDTO> resultado = ticketBuscaService.buscar(filtro);

        assertEquals(1, resultado.size());
        verify(ticketService).listarTodos();
        verify(ticketRepository, never()).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void filtroClienteId_continuaUsandoSpecification() {
        when(ticketRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        TicketFiltroDTO filtro = new TicketFiltroDTO();
        filtro.setClienteId(89);

        ticketBuscaService.buscar(filtro);

        verify(ticketRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void filtroOrigemTicket_continuaUsandoSpecification() {
        when(ticketRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        TicketFiltroDTO filtro = new TicketFiltroDTO();
        filtro.setOrigemTicket(TicketOrigem.RECEPTIVO_WHATSAPP.name());

        ticketBuscaService.buscar(filtro);

        verify(ticketRepository).findAll(any(Specification.class), eq(Sort.by(Sort.Direction.DESC, "dataAbertura")));
    }
}
