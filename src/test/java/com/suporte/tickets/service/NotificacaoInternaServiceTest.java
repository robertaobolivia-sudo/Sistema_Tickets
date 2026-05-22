package com.suporte.tickets.service;

import com.suporte.tickets.entity.NotificacaoInterna;
import com.suporte.tickets.entity.NotificacaoTipo;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.NotificacaoInternaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoInternaServiceTest {

    @Mock
    private NotificacaoInternaRepository notificacaoInternaRepository;

    @InjectMocks
    private NotificacaoInternaService notificacaoInternaService;

    @Test
    void registrarTicketEscalonadoCriaNotificacao() {
        Ticket ticket = new Ticket();
        ticket.setNumeroTicket("TK-000200");
        ticket.setEscalonamentoObservacao("Priorizar retorno");

        when(notificacaoInternaRepository.save(any(NotificacaoInterna.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificacaoInternaService.registrarTicketEscalonado(ticket);

        ArgumentCaptor<NotificacaoInterna> captor = ArgumentCaptor.forClass(NotificacaoInterna.class);
        verify(notificacaoInternaRepository).save(captor.capture());
        NotificacaoInterna salva = captor.getValue();
        assertEquals(NotificacaoTipo.TICKET_ESCALONADO, salva.getTipo());
        assertEquals("TK-000200", salva.getTicketNumero());
        assertTrue(salva.getMensagem().contains("Priorizar retorno"));
    }

    @Test
    void marcarComoLidaAtualizaFlag() {
        NotificacaoInterna notificacao = new NotificacaoInterna();
        notificacao.setId(1L);
        notificacao.setLida(false);
        when(notificacaoInternaRepository.findById(1L)).thenReturn(Optional.of(notificacao));
        when(notificacaoInternaRepository.save(notificacao)).thenReturn(notificacao);

        var dto = notificacaoInternaService.marcarComoLida(1L);

        assertTrue(dto.getLida());
        assertTrue(notificacao.getLida());
    }
}
