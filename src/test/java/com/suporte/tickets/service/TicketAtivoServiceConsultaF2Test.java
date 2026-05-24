package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAtivoServiceConsultaF2Test {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketAtivoService ticketAtivoService;

    @Test
    void buscarTicketAtivo_comContatoWhatsapp_usaParSeguro() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        Contato contato2 = new Contato();
        contato2.setId(200);
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-2");
        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setCliente(cliente);
        ticket.setContato(contato2);

        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), any(List.class)))
                .thenReturn(Optional.of(ticket));
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setNumeroTicket("TK-2");
        when(ticketService.converterParaResponseSeguro(ticket)).thenReturn(dto);

        Optional<TicketResponseDTO> found =
                ticketAtivoService.buscarTicketAtivo(10, 200, "5511999000002");

        assertTrue(found.isPresent());
        assertEquals("TK-2", found.get().getNumeroTicket());
        assertEquals(TicketAtivoService.CONSULTA_ATIVO_MODO_CLIENTE_CONTATO, found.get().getConsultaAtivoModo());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }
}
