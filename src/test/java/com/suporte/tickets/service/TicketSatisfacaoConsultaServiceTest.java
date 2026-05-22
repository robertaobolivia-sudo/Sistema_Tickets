package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoFiltros;
import com.suporte.tickets.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TicketSatisfacaoConsultaServiceTest {

    @Mock
    private com.suporte.tickets.repository.TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @InjectMocks
    private TicketSatisfacaoConsultaService ticketSatisfacaoConsultaService;

    @Test
    void resolverFiltros_normalizaTermoEStatus() {
        TicketSatisfacaoFiltros filtros = ticketSatisfacaoConsultaService.resolverFiltros(
                null, null, 4, "RESOLVIDO", "  Acme  ", null);
        assertEquals(4, filtros.nota());
        assertEquals(TicketStatus.RESOLVIDO, filtros.statusTicket());
        assertEquals("Acme", filtros.termoCliente());
    }

    @Test
    void resolverFiltros_rejeitaNotaInvalida() {
        assertThrows(IllegalArgumentException.class, () ->
                ticketSatisfacaoConsultaService.resolverFiltros(null, null, 6, null, null, null));
    }

    @Test
    void resolverFiltros_termoVazioViraNull() {
        TicketSatisfacaoFiltros filtros =
                ticketSatisfacaoConsultaService.resolverFiltros(null, null, null, null, "   ", null);
        assertNull(filtros.termoCliente());
    }

    @Test
    void resolverFiltros_clienteIdIgnoraTermo() {
        TicketSatisfacaoFiltros filtros =
                ticketSatisfacaoConsultaService.resolverFiltros(null, null, null, null, "Acme", 66);
        assertEquals(66, filtros.clienteId());
        assertNull(filtros.termoCliente());
    }
}
