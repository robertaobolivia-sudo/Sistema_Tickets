package com.suporte.tickets.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Expiração da pesquisa: próxima sexta-feira às 18:00 (America/Sao_Paulo).
 */
public final class TicketSatisfacaoExpiracaoUtil {

    private static final LocalTime HORA_EXPIRACAO = LocalTime.of(18, 0);

    private TicketSatisfacaoExpiracaoUtil() {
    }

    public static LocalDateTime calcularProximaSexta18h(LocalDateTime referencia, ZoneId fuso) {
        ZonedDateTime z = referencia == null
                ? ZonedDateTime.now(fuso)
                : referencia.atZone(fuso);
        LocalDateTime base = z.toLocalDateTime();
        LocalDate sexta = base.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDateTime candidato = sexta.atTime(HORA_EXPIRACAO);
        if (!candidato.isAfter(base)) {
            candidato = sexta.plusWeeks(1).atTime(HORA_EXPIRACAO);
        }
        return candidato;
    }

    public static LocalDateTime calcularProximaSexta18hSla() {
        return calcularProximaSexta18h(LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA), CalendarioSlaHelper.FUSO_SLA);
    }
}
