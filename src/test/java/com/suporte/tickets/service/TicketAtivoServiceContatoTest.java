package com.suporte.tickets.service;

import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoClienteRepository;
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
class TicketAtivoServiceContatoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContatoClienteRepository contatoClienteRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketAtivoService ticketAtivoService;

    @Test
    void mesmoClienteEContato_retornaTicketDoPar() {
        Ticket ticketA = ticket("TK-A", 10, 100);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(100), any(List.class)))
                .thenReturn(Optional.of(ticketA));

        Optional<Ticket> found = ticketAtivoService.buscarEntidadeAtiva(10, 100, null, "5511999000001");

        assertTrue(found.isPresent());
        assertEquals("TK-A", found.get().getNumeroTicket());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }

    @Test
    void outroContato_mesmoCliente_naoBuscaPorClienteSo() {
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), any(List.class)))
                .thenReturn(Optional.empty());

        Optional<Ticket> found = ticketAtivoService.buscarEntidadeAtiva(10, 200, null, "5511999000002");

        assertTrue(found.isEmpty());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }

    @Test
    void semContatoWhatsapp_mantemLegadoPorCliente() {
        Ticket ticketCliente = ticket("TK-LEG", 10, null);
        when(ticketRepository.findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(eq(10), any(List.class)))
                .thenReturn(Optional.of(ticketCliente));

        Optional<Ticket> found = ticketAtivoService.buscarEntidadeAtiva(10, null, null, null);

        assertTrue(found.isPresent());
        assertEquals("TK-LEG", found.get().getNumeroTicket());
    }

    private static Ticket ticket(String numero, int clienteId, Integer contatoId) {
        Cliente cliente = new Cliente();
        cliente.setId(clienteId);
        Ticket t = new Ticket();
        t.setNumeroTicket(numero);
        t.setCliente(cliente);
        t.setStatus(TicketStatus.ABERTO);
        if (contatoId != null) {
            Contato c = new Contato();
            c.setId(contatoId);
            t.setContato(c);
        }
        return t;
    }
}
