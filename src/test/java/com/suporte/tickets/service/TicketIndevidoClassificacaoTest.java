package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClassificarTicketIndevidoRequestDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketClassificacaoOperacional;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketIndevidoClassificacaoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private GrupoCategoriaRepository grupoCategoriaRepository;
    @Mock
    private SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    @Mock
    private MotivoService motivoService;
    @Mock
    private TicketSlaPausaService ticketSlaPausaService;
    @Mock
    private TicketSlaResolucaoService ticketSlaResolucaoService;
    @Mock
    private TicketInteracaoService ticketInteracaoService;
    @Mock
    private TicketSatisfacaoService ticketSatisfacaoService;
    @Mock
    private TicketService ticketService;
    @Mock
    private AuditoriaService auditoriaService;
    @Spy
    private TicketStatusTransicaoService ticketStatusTransicaoService = new TicketStatusTransicaoService();
    @InjectMocks
    private TicketIndevidoService ticketIndevidoService;

    @Test
    void classificar_exigeConfirmacao() {
        ClassificarTicketIndevidoRequestDTO dto = request(false, "INDEVIDO", "teste");

        assertThrows(IllegalArgumentException.class,
                () -> ticketIndevidoService.classificarComoIndevido("TK-100", dto, 1L));
    }

    @Test
    void classificar_marcaIndevido_e_registraAuditoria() {
        Ticket ticket = ticketAtivo("TK-101");
        when(ticketRepository.findByNumeroTicket("TK-101")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketService.converterParaResponseSeguro(any())).thenReturn(new com.suporte.tickets.dto.TicketResponseDTO());

        ClassificarTicketIndevidoRequestDTO dto = request(true, "PROPAGANDA", "spam");

        ticketIndevidoService.classificarComoIndevido("TK-101", dto, 42L);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket salvo = captor.getValue();
        assertEquals(TicketStatus.INDEVIDO, salvo.getStatus());
        assertEquals(TicketClassificacaoOperacional.PROPAGANDA, salvo.getClassificacaoOperacional());
        assertEquals(42L, salvo.getClassificadoOperacionalPorAnalistaId());
        assertFalse(TicketAtivoService.isStatusAtivo(salvo.getStatus()));

        verify(ticketInteracaoService).registrarClassificacaoIndevido(eq(salvo), eq("PROPAGANDA"), eq("spam"));
        verify(ticketSatisfacaoService).tratarAvaliacaoAoClassificarIndevido(salvo);
        verify(auditoriaService).registrar(
                eq(AuditoriaService.ACAO_TICKET_CLASSIFICAR_INDEVIDO),
                eq(AuditoriaService.ENTIDADE_TICKET),
                eq("TK-101"),
                any(),
                eq(42L));
    }

    @Test
    void classificar_naoGeraAvaliacaoNova() {
        Ticket ticket = ticketAtivo("TK-102");
        when(ticketRepository.findByNumeroTicket("TK-102")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketService.converterParaResponseSeguro(any())).thenReturn(new com.suporte.tickets.dto.TicketResponseDTO());

        ticketIndevidoService.classificarComoIndevido("TK-102", request(true, "INDEVIDO", null), 1L);

        verify(ticketSatisfacaoService, never()).registrarDecisaoPosEncerramento(any(), eq(true), any());
    }

    @Test
    void ticketIndevido_naoEStatusAtivo() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.INDEVIDO);
        assertFalse(TicketAtivoService.isStatusAtivo(ticket.getStatus()));
        assertTrue(TicketAtivoService.STATUS_ENCERRADOS.contains(TicketStatus.INDEVIDO));
    }

    @Test
    void satisfacao_bloqueadaParaTicketIndevido() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.INDEVIDO);
        assertThrows(IllegalArgumentException.class,
                () -> TicketSatisfacaoService.validarTicketPodeReceberSatisfacao(ticket));
    }

    private static ClassificarTicketIndevidoRequestDTO request(
            boolean confirmacao, String motivo, String comentario) {
        ClassificarTicketIndevidoRequestDTO dto = new ClassificarTicketIndevidoRequestDTO();
        dto.setConfirmacao(confirmacao);
        dto.setMotivoOperacional(motivo);
        dto.setComentario(comentario);
        return dto;
    }

    private static Ticket ticketAtivo(String numero) {
        Ticket t = new Ticket();
        t.setId(1);
        t.setNumeroTicket(numero);
        t.setStatus(TicketStatus.EM_ATENDIMENTO);
        return t;
    }
}
