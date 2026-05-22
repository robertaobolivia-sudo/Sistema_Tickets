package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSatisfacaoDecisaoEncerramentoTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketSatisfacaoRepository ticketSatisfacaoRepository;
    @Mock
    private PesquisaSatisfacaoEnvioService pesquisaSatisfacaoEnvioService;

    @InjectMocks
    private TicketSatisfacaoService ticketSatisfacaoService;

    @Test
    void decisao_enviarComContato_criaPendente() {
        Ticket ticket = ticketResolvido(1, comContato());
        when(ticketSatisfacaoRepository.existsByTicket_Id(1)).thenReturn(false);
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        ticketSatisfacaoService.registrarDecisaoPosEncerramento(ticket, true, 99L);

        ArgumentCaptor<TicketSatisfacao> cap = ArgumentCaptor.forClass(TicketSatisfacao.class);
        verify(ticketSatisfacaoRepository).save(cap.capture());
        TicketSatisfacao s = cap.getValue();
        assertEquals(TicketSatisfacaoStatus.PENDENTE, s.getStatus());
        verify(pesquisaSatisfacaoEnvioService).enviarPesquisa(any(TicketSatisfacao.class), any(String.class), eq(99L));
        assertNotNull(s.getEnviadaEm());
        assertNotNull(s.getExpiraEm());
        assertEquals(99L, s.getSolicitadaPorAnalistaId());
        assertNull(s.getNota());
        assertNotNull(s.getTokenRespostaHash());
        assertNotNull(s.getTokenCriadoEm());
    }

    @Test
    void decisao_naoEnviar_criaNaoEnviada() {
        Ticket ticket = ticketResolvido(2, comContato());
        when(ticketSatisfacaoRepository.existsByTicket_Id(2)).thenReturn(false);
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        ticketSatisfacaoService.registrarDecisaoPosEncerramento(ticket, false, 1L);

        ArgumentCaptor<TicketSatisfacao> cap = ArgumentCaptor.forClass(TicketSatisfacao.class);
        verify(ticketSatisfacaoRepository).save(cap.capture());
        assertEquals(TicketSatisfacaoStatus.NAO_ENVIADA, cap.getValue().getStatus());
        assertNull(cap.getValue().getEnviadaEm());
    }

    @Test
    void decisao_enviarSemContato_criaNaoEnviada() {
        Ticket ticket = ticketResolvido(3, null);
        when(ticketSatisfacaoRepository.existsByTicket_Id(3)).thenReturn(false);
        when(ticketSatisfacaoRepository.save(any(TicketSatisfacao.class))).thenAnswer(i -> i.getArgument(0));

        ticketSatisfacaoService.registrarDecisaoPosEncerramento(ticket, true, 1L);

        ArgumentCaptor<TicketSatisfacao> cap = ArgumentCaptor.forClass(TicketSatisfacao.class);
        verify(ticketSatisfacaoRepository).save(cap.capture());
        assertEquals(TicketSatisfacaoStatus.NAO_ENVIADA, cap.getValue().getStatus());
    }

    @Test
    void decisao_jaExiste_naoDuplica() {
        Ticket ticket = ticketResolvido(4, comContato());
        when(ticketSatisfacaoRepository.existsByTicket_Id(4)).thenReturn(true);

        ticketSatisfacaoService.registrarDecisaoPosEncerramento(ticket, true, 1L);

        verify(ticketSatisfacaoRepository, never()).save(any());
    }

    private static Ticket ticketResolvido(int id, Contato contato) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setStatus(TicketStatus.RESOLVIDO);
        t.setContato(contato);
        return t;
    }

    private static Contato comContato() {
        Contato c = new Contato();
        c.setId(10);
        return c;
    }
}
