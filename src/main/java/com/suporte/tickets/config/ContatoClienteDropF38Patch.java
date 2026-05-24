package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sprint F38: backup + DROP legado {@code contatos_clientes} e {@code tickets.contato_solicitante_id}.
 */
@Component
@Order(4)
@RequiredArgsConstructor
@Slf4j
public class ContatoClienteDropF38Patch implements ApplicationRunner {

    private static final String SOURCE_TABLE = "contatos_clientes";
    private static final String BACKUP_CC = "contatos_clientes_backup_f38";
    private static final String BACKUP_TICKETS = "tickets_contato_solicitante_backup_f38";
    private static final String COL_SOLICITANTE = "contato_solicitante_id";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        logInventarioPreDrop();
        backupContatosClientes();
        backupTicketsSolicitante();
        removerForeignKeysSolicitante();
        removerColunaSolicitante();
        removerTabelaContatosClientes();
    }

    private void logInventarioPreDrop() {
        if (tabelaExiste(SOURCE_TABLE)) {
            log.info("Sprint F38 pre-drop: {} linhas em {}", count("SELECT COUNT(*) FROM " + SOURCE_TABLE), SOURCE_TABLE);
        }
        if (colunaSolicitanteExiste()) {
            log.info(
                    "Sprint F38 pre-drop: tickets com {} = {}",
                    COL_SOLICITANTE,
                    count("SELECT COUNT(*) FROM tickets WHERE " + COL_SOLICITANTE + " IS NOT NULL"));
        }
    }

    private void backupContatosClientes() {
        if (!tabelaExiste(SOURCE_TABLE)) {
            log.info("Sprint F38: tabela {} ausente — backup ignorado", SOURCE_TABLE);
            return;
        }
        try {
            if (!tabelaExiste(BACKUP_CC)) {
                Integer origem = count("SELECT COUNT(*) FROM " + SOURCE_TABLE);
                jdbcTemplate.execute("CREATE TABLE " + BACKUP_CC + " AS SELECT * FROM " + SOURCE_TABLE);
                Integer backup = count("SELECT COUNT(*) FROM " + BACKUP_CC);
                log.info("Sprint F38: backup {} — origem={}, backup={}", BACKUP_CC, origem, backup);
            } else {
                log.info("Sprint F38: backup {} já existe", BACKUP_CC);
            }
        } catch (Exception ex) {
            log.warn("Sprint F38: falha backup {}: {}", BACKUP_CC, ex.getMessage());
        }
    }

    private void backupTicketsSolicitante() {
        if (!colunaSolicitanteExiste()) {
            log.info("Sprint F38: coluna tickets.{} ausente — backup tickets ignorado", COL_SOLICITANTE);
            return;
        }
        try {
            if (!tabelaExiste(BACKUP_TICKETS)) {
                jdbcTemplate.execute("""
                        CREATE TABLE tickets_contato_solicitante_backup_f38 AS
                        SELECT id, numero_ticket, contato_solicitante_id, contato_id, cliente_id, origem_ticket, data_abertura
                        FROM tickets
                        WHERE contato_solicitante_id IS NOT NULL
                        """);
                Integer backup = count("SELECT COUNT(*) FROM " + BACKUP_TICKETS);
                log.info("Sprint F38: backup {} — linhas={}", BACKUP_TICKETS, backup);
            } else {
                log.info("Sprint F38: backup {} já existe", BACKUP_TICKETS);
            }
        } catch (Exception ex) {
            log.warn("Sprint F38: falha backup {}: {}", BACKUP_TICKETS, ex.getMessage());
        }
    }

    private void removerForeignKeysSolicitante() {
        if (!colunaSolicitanteExiste()) {
            return;
        }
        List<String> fks = jdbcTemplate.queryForList(
                """
                        SELECT CONSTRAINT_NAME
                        FROM information_schema.KEY_COLUMN_USAGE
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'tickets'
                          AND COLUMN_NAME = ?
                          AND REFERENCED_TABLE_NAME IS NOT NULL
                        """,
                String.class,
                COL_SOLICITANTE);
        for (String fk : fks) {
            try {
                jdbcTemplate.execute("ALTER TABLE tickets DROP FOREIGN KEY " + fk);
                log.info("Sprint F38: FK tickets.{} removida ({})", COL_SOLICITANTE, fk);
            } catch (Exception ex) {
                log.warn("Sprint F38: DROP FK {} ignorado: {}", fk, ex.getMessage());
            }
        }
    }

    private void removerColunaSolicitante() {
        if (!colunaSolicitanteExiste()) {
            log.info("Sprint F38: coluna tickets.{} já ausente", COL_SOLICITANTE);
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE tickets DROP COLUMN " + COL_SOLICITANTE);
            log.info("Sprint F38: coluna tickets.{} removida", COL_SOLICITANTE);
        } catch (Exception ex) {
            log.warn("Sprint F38: DROP coluna {} ignorado: {}", COL_SOLICITANTE, ex.getMessage());
        }
    }

    private void removerTabelaContatosClientes() {
        if (!tabelaExiste(SOURCE_TABLE)) {
            log.info("Sprint F38: tabela {} já ausente", SOURCE_TABLE);
            return;
        }
        try {
            jdbcTemplate.execute("DROP TABLE " + SOURCE_TABLE);
            log.info("Sprint F38: tabela {} removida", SOURCE_TABLE);
        } catch (Exception ex) {
            log.warn("Sprint F38: DROP {} ignorado: {}", SOURCE_TABLE, ex.getMessage());
        }
    }

    private int count(String sql) {
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class);
        return n != null ? n : 0;
    }

    private boolean tabelaExiste(String tableName) {
        Integer c = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM information_schema.TABLES
                        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                        """,
                Integer.class,
                tableName);
        return c != null && c > 0;
    }

    private boolean colunaSolicitanteExiste() {
        Integer c = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'tickets'
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                COL_SOLICITANTE);
        return c != null && c > 0;
    }
}
