package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketOrigem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sprint F17: preenche {@code origem_ticket} apenas onde ainda é NULL (idempotente).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketOrigemBackfillService {

    static final String SQL_RECEPTIVO = """
            UPDATE tickets
            SET origem_ticket = ?
            WHERE origem_ticket IS NULL
              AND whatsapp_matriz_id IS NOT NULL
            """;

    static final String SQL_ATIVO_MANUAL = """
            UPDATE tickets
            SET origem_ticket = ?
            WHERE origem_ticket IS NULL
              AND whatsapp_matriz_id IS NULL
              AND contato_id IS NOT NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public BackfillResult executar() {
        int receptivo = jdbcTemplate.update(SQL_RECEPTIVO, TicketOrigem.RECEPTIVO_WHATSAPP.name());
        int manual = jdbcTemplate.update(SQL_ATIVO_MANUAL, TicketOrigem.ATIVO_MANUAL.name());
        if (receptivo > 0 || manual > 0) {
            log.info("Sprint F17: backfill origem_ticket — receptivo={}, ativo_manual={}", receptivo, manual);
        }
        return new BackfillResult(receptivo, manual);
    }

    public record BackfillResult(int receptivoWhatsapp, int ativoManual) {
        public int total() {
            return receptivoWhatsapp + ativoManual;
        }
    }
}
