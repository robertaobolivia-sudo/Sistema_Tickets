package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Sprint F29: backup leve + DROP da coluna legada {@code tickets.conexao}.
 * Idempotente: se a coluna já foi removida, apenas registra log.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class TicketConexaoDropF29Patch implements ApplicationRunner {

    private static final String BACKUP_TABLE = "tickets_conexao_backup_f29";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!colunaConexaoExiste()) {
            log.info("Sprint F29: coluna tickets.conexao ausente — nada a fazer");
            return;
        }
        try {
            if (!tabelaBackupExiste()) {
                jdbcTemplate.execute("""
                        CREATE TABLE tickets_conexao_backup_f29 AS
                        SELECT id,
                               numero_ticket,
                               conexao,
                               cliente_id,
                               contato_id,
                               origem_ticket,
                               whatsapp_matriz_id,
                               data_abertura
                        FROM tickets
                        WHERE conexao IS NOT NULL AND TRIM(conexao) <> ''
                        """);
                log.info("Sprint F29: backup {} criado", BACKUP_TABLE);
            } else {
                log.info("Sprint F29: backup {} já existe — mantido", BACKUP_TABLE);
            }
        } catch (Exception ex) {
            log.warn("Sprint F29: falha ao criar backup {} (seguindo DROP): {}", BACKUP_TABLE, ex.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE tickets DROP COLUMN conexao");
            log.info("Sprint F29: coluna tickets.conexao removida");
        } catch (Exception ex) {
            log.warn("Sprint F29: DROP tickets.conexao ignorado: {}", ex.getMessage());
        }
    }

    private boolean colunaConexaoExiste() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'tickets'
                          AND COLUMN_NAME = 'conexao'
                        """,
                Integer.class);
        return count != null && count > 0;
    }

    private boolean tabelaBackupExiste() {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = ?
                        """,
                Integer.class,
                BACKUP_TABLE);
        return count != null && count > 0;
    }
}
