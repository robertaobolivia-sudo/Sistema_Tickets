package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Sprint 211: ambientes com {@code ticket_satisfacao.nota} NOT NULL no MySQL falham ao encerrar sem pesquisa.
 * Hibernate {@code ddl-auto=update} nem sempre altera nullability de coluna existente.
 */
@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
@Slf4j
public class TicketSatisfacaoNotaNullablePatch implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE ticket_satisfacao MODIFY COLUMN nota INT NULL");
            log.info("Sprint 211: ticket_satisfacao.nota ajustada para NULL no MySQL");
        } catch (Exception ex) {
            log.warn("Sprint 211: nao foi possivel ajustar ticket_satisfacao.nota (pode ja estar nullable): {}", ex.getMessage());
        }
    }
}
