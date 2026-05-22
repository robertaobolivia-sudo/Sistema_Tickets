package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditoriaConsultaServiceTest {

    @Test
    void normalizarLimite_padraoQuandoZeroOuNegativo() {
        assertEquals(AuditoriaConsultaService.LIMITE_PADRAO, AuditoriaConsultaService.normalizarLimite(0));
        assertEquals(AuditoriaConsultaService.LIMITE_PADRAO, AuditoriaConsultaService.normalizarLimite(-1));
    }

    @Test
    void normalizarLimite_capEm200() {
        assertEquals(200, AuditoriaConsultaService.normalizarLimite(500));
        assertEquals(25, AuditoriaConsultaService.normalizarLimite(25));
    }
}
