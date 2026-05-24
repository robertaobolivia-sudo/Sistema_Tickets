package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Sprint F34: backup + DROP da tabela legada {@code ticket_etiquetas}.
 * Idempotente: se a tabela já foi removida, apenas registra log.
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class TicketEtiquetaDropF34Patch implements ApplicationRunner {

    private static final String SOURCE_TABLE = "ticket_etiquetas";
    private static final String BACKUP_TABLE = "ticket_etiquetas_backup_f34";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!tabelaExiste(SOURCE_TABLE)) {
            log.info("Sprint F34: tabela {} ausente — nada a fazer", SOURCE_TABLE);
            return;
        }
        try {
            if (!tabelaExiste(BACKUP_TABLE)) {
                Integer origem = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + SOURCE_TABLE, Integer.class);
                jdbcTemplate.execute("CREATE TABLE " + BACKUP_TABLE + " AS SELECT * FROM " + SOURCE_TABLE);
                Integer backup = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + BACKUP_TABLE, Integer.class);
                log.info(
                        "Sprint F34: backup {} criado — origem={}, backup={}",
                        BACKUP_TABLE,
                        origem,
                        backup);
            } else {
                log.info("Sprint F34: backup {} já existe — mantido", BACKUP_TABLE);
            }
        } catch (Exception ex) {
            log.warn("Sprint F34: falha ao criar backup {} (seguindo DROP): {}", BACKUP_TABLE, ex.getMessage());
        }
        try {
            jdbcTemplate.execute("DROP TABLE " + SOURCE_TABLE);
            log.info("Sprint F34: tabela {} removida", SOURCE_TABLE);
        } catch (Exception ex) {
            log.warn("Sprint F34: DROP {} ignorado: {}", SOURCE_TABLE, ex.getMessage());
        }
    }

    private boolean tabelaExiste(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                        """,
                Integer.class,
                tableName);
        return count != null && count > 0;
    }
}
