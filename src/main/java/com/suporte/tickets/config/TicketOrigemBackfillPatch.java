package com.suporte.tickets.config;

import com.suporte.tickets.service.TicketOrigemBackfillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sprint F17: backfill leve de {@code origem_ticket} para tickets antigos (somente NULL).
 */
@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
@Slf4j
public class TicketOrigemBackfillPatch implements ApplicationRunner {

    private final TicketOrigemBackfillService ticketOrigemBackfillService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ticketOrigemBackfillService.executar();
        } catch (Exception ex) {
            log.warn("Sprint F17: backfill origem_ticket ignorado (tabela/coluna ausente?): {}", ex.getMessage());
        }
    }
}
