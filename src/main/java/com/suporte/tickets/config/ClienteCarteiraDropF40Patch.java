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
 * Sprint F40: backup + DROP FK/coluna legada {@code clientes.carteira_id}.
 */
@Component
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class ClienteCarteiraDropF40Patch implements ApplicationRunner {

    private static final String BACKUP_TABLE = "clientes_carteira_backup_f40";
    private static final String COL_CARTEIRA = "carteira_id";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!colunaCarteiraExiste()) {
            log.info("Sprint F40: coluna clientes.{} ausente — nada a fazer", COL_CARTEIRA);
            return;
        }
        int comCarteira = count(
                "SELECT COUNT(*) FROM clientes WHERE " + COL_CARTEIRA + " IS NOT NULL");
        log.info("Sprint F40 pre-drop: clientes com {} = {}", COL_CARTEIRA, comCarteira);
        backupClientesCarteira();
        removerForeignKeysCarteira();
        removerColunaCarteira();
    }

    private void backupClientesCarteira() {
        try {
            if (!tabelaExiste(BACKUP_TABLE)) {
                jdbcTemplate.execute("""
                        CREATE TABLE clientes_carteira_backup_f40 AS
                        SELECT id, nome, empresa, carteira_id
                        FROM clientes
                        WHERE carteira_id IS NOT NULL
                        """);
                int backup = count("SELECT COUNT(*) FROM " + BACKUP_TABLE);
                log.info("Sprint F40: backup {} — linhas={}", BACKUP_TABLE, backup);
            } else {
                log.info("Sprint F40: backup {} já existe", BACKUP_TABLE);
            }
        } catch (Exception ex) {
            log.warn("Sprint F40: falha backup {}: {}", BACKUP_TABLE, ex.getMessage());
        }
    }

    private void removerForeignKeysCarteira() {
        List<String> fks = jdbcTemplate.queryForList(
                """
                        SELECT CONSTRAINT_NAME
                        FROM information_schema.KEY_COLUMN_USAGE
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'clientes'
                          AND COLUMN_NAME = ?
                          AND REFERENCED_TABLE_NAME IS NOT NULL
                        """,
                String.class,
                COL_CARTEIRA);
        for (String fk : fks) {
            try {
                jdbcTemplate.execute("ALTER TABLE clientes DROP FOREIGN KEY " + fk);
                log.info("Sprint F40: FK clientes.{} removida ({})", COL_CARTEIRA, fk);
            } catch (Exception ex) {
                log.warn("Sprint F40: DROP FK {} ignorado: {}", fk, ex.getMessage());
            }
        }
    }

    private void removerColunaCarteira() {
        if (!colunaCarteiraExiste()) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE clientes DROP COLUMN " + COL_CARTEIRA);
            log.info("Sprint F40: coluna clientes.{} removida", COL_CARTEIRA);
        } catch (Exception ex) {
            log.warn("Sprint F40: DROP coluna {} ignorado: {}", COL_CARTEIRA, ex.getMessage());
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

    private boolean colunaCarteiraExiste() {
        Integer c = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE()
                          AND TABLE_NAME = 'clientes'
                          AND COLUMN_NAME = ?
                        """,
                Integer.class,
                COL_CARTEIRA);
        return c != null && c > 0;
    }
}
