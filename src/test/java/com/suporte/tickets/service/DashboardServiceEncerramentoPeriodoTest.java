package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceEncerramentoPeriodoTest {

    @Test
    void normalizarDiasPeriodoEncerramentoAceita7_30_90() {
        assertEquals(7, DashboardService.normalizarDiasPeriodoEncerramento(7));
        assertEquals(30, DashboardService.normalizarDiasPeriodoEncerramento(30));
        assertEquals(90, DashboardService.normalizarDiasPeriodoEncerramento(90));
    }

    @Test
    void normalizarDiasPeriodoEncerramentoPadrao30() {
        assertEquals(30, DashboardService.normalizarDiasPeriodoEncerramento(null));
        assertEquals(30, DashboardService.normalizarDiasPeriodoEncerramento(15));
        assertEquals(30, DashboardService.normalizarDiasPeriodoEncerramento(365));
    }
}
