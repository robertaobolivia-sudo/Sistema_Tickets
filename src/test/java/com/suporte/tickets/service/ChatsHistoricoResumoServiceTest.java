package com.suporte.tickets.service;

import com.suporte.tickets.dto.ChatsHistoricoResumoDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatsHistoricoResumoServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ChatsHistoricoResumoService service;

    @Test
    void buscar_excluiTicketAtualNasListas() {
        Cliente cliente = new Cliente();
        cliente.setId(10);
        Ticket atual = new Ticket();
        atual.setNumeroTicket("TK-100");
        atual.setCliente(cliente);
        atual.setStatus(TicketStatus.EM_ATENDIMENTO);

        Ticket outro = new Ticket();
        outro.setNumeroTicket("TK-099");
        outro.setStatus(TicketStatus.RESOLVIDO);
        outro.setDataAbertura(LocalDateTime.of(2026, 5, 1, 10, 0));
        outro.setDataEncerramento(LocalDateTime.of(2026, 5, 2, 12, 0));

        when(ticketRepository.findByNumeroTicket("TK-100")).thenReturn(Optional.of(atual));
        when(ticketRepository.countByCliente_Id(10)).thenReturn(2L);
        when(ticketRepository.findEncerradosByClienteExcluindoNumero(
                eq(10), any(), eq("TK-100"), any(Pageable.class)))
                .thenReturn(List.of(outro));
        when(ticketRepository.findRecentesByClienteExcluindoNumero(eq(10), eq("TK-100"), any(Pageable.class)))
                .thenReturn(List.of(outro));

        ChatsHistoricoResumoDTO dto = service.buscarPorNumeroTicket("TK-100");

        assertEquals(2L, dto.getTotalTicketsCliente());
        assertNotNull(dto.getUltimoTicketEncerrado());
        assertEquals("TK-099", dto.getUltimoTicketEncerrado().getNumeroTicket());
        assertEquals(1, dto.getTicketsRecentes().size());
        assertEquals("TK-099", dto.getTicketsRecentes().get(0).getNumeroTicket());
    }
}
