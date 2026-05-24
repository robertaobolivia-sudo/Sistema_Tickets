package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketOrigem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOrigemBackfillF17Test {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TicketOrigemBackfillService service;

    @Test
    void executar_atualizaReceptivoDepoisManual_idempotentePorSql() {
        when(jdbcTemplate.update(eq(TicketOrigemBackfillService.SQL_RECEPTIVO), eq(TicketOrigem.RECEPTIVO_WHATSAPP.name())))
                .thenReturn(10);
        when(jdbcTemplate.update(eq(TicketOrigemBackfillService.SQL_ATIVO_MANUAL), eq(TicketOrigem.ATIVO_MANUAL.name())))
                .thenReturn(4);

        TicketOrigemBackfillService.BackfillResult result = service.executar();

        assertEquals(10, result.receptivoWhatsapp());
        assertEquals(4, result.ativoManual());
        assertEquals(14, result.total());
        verify(jdbcTemplate).update(TicketOrigemBackfillService.SQL_RECEPTIVO, TicketOrigem.RECEPTIVO_WHATSAPP.name());
        verify(jdbcTemplate).update(TicketOrigemBackfillService.SQL_ATIVO_MANUAL, TicketOrigem.ATIVO_MANUAL.name());
    }

    @Test
    void executar_semLinhas_retornaZero() {
        when(jdbcTemplate.update(eq(TicketOrigemBackfillService.SQL_RECEPTIVO), eq(TicketOrigem.RECEPTIVO_WHATSAPP.name())))
                .thenReturn(0);
        when(jdbcTemplate.update(eq(TicketOrigemBackfillService.SQL_ATIVO_MANUAL), eq(TicketOrigem.ATIVO_MANUAL.name())))
                .thenReturn(0);

        TicketOrigemBackfillService.BackfillResult result = service.executar();

        assertEquals(0, result.total());
    }
}
