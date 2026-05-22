package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketSatisfacaoExpiracaoUtilTest {

    @Test
    void proximaSexta18h_quintaUsaSextaDaSemana() {
        LocalDateTime ref = LocalDate.of(2026, 5, 14).atTime(10, 0);
        LocalDateTime exp = TicketSatisfacaoExpiracaoUtil.calcularProximaSexta18h(
                ref, CalendarioSlaHelper.FUSO_SLA);
        assertEquals(LocalDate.of(2026, 5, 15), exp.toLocalDate());
        assertEquals(LocalTime.of(18, 0), exp.toLocalTime());
    }

    @Test
    void proximaSexta18h_sextaApos18hUsaProximaSemana() {
        LocalDateTime ref = LocalDate.of(2026, 5, 15).atTime(19, 0);
        LocalDateTime exp = TicketSatisfacaoExpiracaoUtil.calcularProximaSexta18h(
                ref, CalendarioSlaHelper.FUSO_SLA);
        assertEquals(LocalDate.of(2026, 5, 22), exp.toLocalDate());
        assertEquals(LocalTime.of(18, 0), exp.toLocalTime());
    }

    @Test
    void proximaSexta18h_sextaAntes18hUsaMesmoDia() {
        LocalDateTime ref = LocalDate.of(2026, 5, 15).atTime(17, 0);
        LocalDateTime exp = TicketSatisfacaoExpiracaoUtil.calcularProximaSexta18h(
                ref, CalendarioSlaHelper.FUSO_SLA);
        assertEquals(DayOfWeek.FRIDAY, exp.getDayOfWeek());
        assertEquals(LocalDate.of(2026, 5, 15), exp.toLocalDate());
    }
}
