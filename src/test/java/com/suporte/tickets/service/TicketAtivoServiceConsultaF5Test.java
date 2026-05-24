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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketAtivoServiceConsultaF5Test {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketAtivoService ticketAtivoService;

    @Test
    void parClienteContato_naoMarcaLegado() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        Contato c2 = new Contato();
        c2.setId(200);
        Ticket ticket = ticket("TK-2", cliente, c2);
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), any(List.class)))
                .thenReturn(Optional.of(ticket));
        when(ticketService.converterParaResponseSeguro(ticket)).thenReturn(new TicketResponseDTO());

        Optional<TicketResponseDTO> found = ticketAtivoService.buscarTicketAtivo(10, 200, null);

        assertTrue(found.isPresent());
        assertEquals(TicketAtivoService.CONSULTA_ATIVO_MODO_CLIENTE_CONTATO, found.get().getConsultaAtivoModo());
        assertFalse(Boolean.TRUE.equals(found.get().getConsultaAtivoLegadoDeprecated()));
    }

    @Test
    void outroContato_naoRetornaTicketDoContato1() {
        when(ticketRepository.findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(
                eq(10), eq(200), any(List.class)))
                .thenReturn(Optional.empty());

        Optional<TicketResponseDTO> found = ticketAtivoService.buscarTicketAtivo(10, 200, null);

        assertTrue(found.isEmpty());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }

    @Test
    void soClienteId_naoRetornaTicket_F6() {
        Optional<TicketResponseDTO> found = ticketAtivoService.buscarTicketAtivo(10, null, null);

        assertTrue(found.isEmpty());
        verify(ticketRepository, never()).findFirstByCliente_IdAndStatusInOrderByDataAberturaDesc(any(), any());
    }

    @Test
    void F7_soTelefone_buscarTicketAtivo_vazio() {
        Optional<TicketResponseDTO> found = ticketAtivoService.buscarTicketAtivo(null, null, "5511999000001");

        assertTrue(found.isEmpty());
        verify(ticketRepository, never()).findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(any(), any(), any());
    }

    @Test
    void F36_semParClienteContato_buscarTicketAtivo_vazio() {
        Optional<TicketResponseDTO> found = ticketAtivoService.buscarTicketAtivo(null, 7, null);

        assertTrue(found.isEmpty());
        verify(ticketRepository, never()).findFirstByCliente_IdAndContato_IdAndStatusInOrderByDataAberturaDesc(any(), any(), any());
    }

    @Test
    void enriquecerRespostaConsultaAtivo_documentaLegado() {
        TicketResponseDTO dto = new TicketResponseDTO();
        TicketAtivoService.enriquecerRespostaConsultaAtivo(
                dto,
                TicketAtivoService.CONSULTA_ATIVO_MODO_LEGADO,
                TicketAtivoService.LEGADO_MOTIVO_CLIENTE_SEM_CONTATO_WHATSAPP);
        assertTrue(dto.getConsultaAtivoLegadoDeprecated());
    }

    private static Ticket ticket(String numero, Cliente cliente, Contato contato) {
        Ticket t = new Ticket();
        t.setNumeroTicket(numero);
        t.setStatus(TicketStatus.ABERTO);
        t.setCliente(cliente);
        t.setContato(contato);
        return t;
    }
}
