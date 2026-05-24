package com.suporte.tickets.service;

import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.InteracaoPendenteDecisaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteracaoPendenteDecisaoServiceTest {

    @Mock
    private InteracaoPendenteDecisaoRepository pendenteRepository;

    @Mock
    private TicketInteracaoService ticketInteracaoService;

    @Mock
    private TicketService ticketService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private InteracaoPendenteDecisaoService service;

    @Test
    void vincular_naoReabreTicket() {
        InteracaoPendenteDecisao p = pendente(1L);
        when(pendenteRepository.findByIdAndStatus(1L, InteracaoPendenteDecisaoStatus.PENDENTE))
                .thenReturn(Optional.of(p));

        service.vincularAoTicketAnterior(1L, 99L);

        verify(ticketInteracaoService).registrarMensagemEntradaExterna(
                eq(p.getTicketAnterior()), eq("Oi"), eq(null), eq(null));
        verify(ticketService, never()).atualizarStatus(any(), any());
        assertEquals(InteracaoPendenteDecisaoStatus.VINCULADA_ANTERIOR, p.getStatus());
    }

    @Test
    void vincular_rejeitaPendenciaJaDecidida() {
        when(pendenteRepository.findByIdAndStatus(2L, InteracaoPendenteDecisaoStatus.PENDENTE))
                .thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> service.vincularAoTicketAnterior(2L, 1L));
    }

    private static InteracaoPendenteDecisao pendente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("Fenix");
        Contato contato = new Contato();
        contato.setId(10);
        Ticket anterior = new Ticket();
        anterior.setNumeroTicket("TK-001");
        anterior.setStatus(TicketStatus.RESOLVIDO);
        anterior.setCliente(cliente);
        InteracaoPendenteDecisao p = new InteracaoPendenteDecisao();
        p.setId(id);
        p.setCliente(cliente);
        p.setContato(contato);
        p.setTicketAnterior(anterior);
        p.setMensagem("Oi");
        p.setStatus(InteracaoPendenteDecisaoStatus.PENDENTE);
        return p;
    }
}
