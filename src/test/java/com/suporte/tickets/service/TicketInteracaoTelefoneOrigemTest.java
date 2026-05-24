package com.suporte.tickets.service;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketInteracao;
import com.suporte.tickets.entity.TicketInteracaoTipo;
import com.suporte.tickets.repository.TicketInteracaoRepository;
import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketInteracaoTelefoneOrigemTest {

    @Mock
    private TicketInteracaoRepository ticketInteracaoRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ContatoAtendimentoOrigemService contatoAtendimentoOrigemService;

    @InjectMocks
    private TicketInteracaoService ticketInteracaoService;

    @Test
    void mensagemEntrada_aplicaOrigemNaInteracao() {
        Contato contato = new Contato();
        contato.setId(69);
        contato.setWhatsappNormalizado("5511980030111");
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-TEST");
        ticket.setContato(contato);
        when(ticketInteracaoRepository.save(any(TicketInteracao.class))).thenAnswer(i -> i.getArgument(0));

        ticketInteracaoService.registrarMensagemEntradaExterna(
                ticket, "Oi", "ext-1", "5512942833853");

        ArgumentCaptor<TicketInteracao> cap = ArgumentCaptor.forClass(TicketInteracao.class);
        verify(ticketInteracaoRepository).save(cap.capture());
        verify(contatoAtendimentoOrigemService)
                .aplicarOrigemNaInteracao(any(TicketInteracao.class), eq(contato), eq("5512942833853"));
        assertEquals(TicketInteracaoTipo.MENSAGEM_CLIENTE, cap.getValue().getTipoInteracao());
    }

    @Test
    void abertura_copiaOrigemDoTicket() {
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-AB");
        ticket.setMensagemInicial("Inicio");
        ticket.setAtendimentoTelefone("5511980030111");
        ticket.setAtendimentoTelefoneNormalizado("5511980030111");
        ticket.setAtendimentoTelefoneTipo(ContatoAtendimentoOrigemService.TIPO_PRINCIPAL);
        when(ticketInteracaoRepository.save(any(TicketInteracao.class))).thenAnswer(i -> i.getArgument(0));

        ticketInteracaoService.registrarAberturaAutomatica(ticket);

        ArgumentCaptor<TicketInteracao> cap = ArgumentCaptor.forClass(TicketInteracao.class);
        verify(ticketInteracaoRepository).save(cap.capture());
        assertEquals(ContatoAtendimentoOrigemService.TIPO_PRINCIPAL, cap.getValue().getTelefoneOrigemTipo());
    }
}
