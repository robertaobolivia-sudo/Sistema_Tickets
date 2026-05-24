package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketEscalonamentoRequestDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketEscalonamentoServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private TicketInteracaoService ticketInteracaoService;
    @Mock
    private AnalistaService analistaService;
    @Mock
    private TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    @Mock
    private TicketSlaResolucaoService ticketSlaResolucaoService;
    @Mock
    private TicketSlaPausaService ticketSlaPausaService;
    @Mock
    private NotificacaoInternaService notificacaoInternaService;
    @Mock
    private TicketSatisfacaoService ticketSatisfacaoService;
    @Mock
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;
    @Mock
    private MotivoService motivoService;
    @Mock
    private ContatoService contatoService;
    @Mock
    private WhatsappMatrizService whatsappMatrizService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void escalonarTicketMarcaCampos() {
        Ticket ticket = new Ticket();
        ticket.setId(99);
        ticket.setNumeroTicket("TK-000099");
        ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
        when(ticketRepository.findByNumeroTicket("TK-000099")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketSlaPrimeiroAtendimentoService.calcularStatusLabel(any())).thenReturn("DENTRO_DO_PRAZO");
        when(ticketSlaResolucaoService.calcularStatusLabel(any())).thenReturn("VENCIDO");

        TicketEscalonamentoRequestDTO dto = new TicketEscalonamentoRequestDTO("Acompanhamento necessário", 11L);
        TicketResponseDTO response = ticketService.escalonarTicket("TK-000099", dto);

        assertTrue(response.getEscalonado());
        assertTrue(ticket.getEscalonado());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void removerEscalonamentoLimpaCampos() {
        Ticket ticket = new Ticket();
        ticket.setId(100);
        ticket.setNumeroTicket("TK-000100");
        ticket.setEscalonado(true);
        when(ticketRepository.findByNumeroTicket("TK-000100")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketSlaPrimeiroAtendimentoService.calcularStatusLabel(any())).thenReturn("NAO_CALCULADO");
        when(ticketSlaResolucaoService.calcularStatusLabel(any())).thenReturn("NAO_CALCULADO");

        TicketResponseDTO response = ticketService.removerEscalonamento("TK-000100");

        assertFalse(response.getEscalonado());
        assertFalse(Boolean.TRUE.equals(ticket.getEscalonado()));
    }
}
