package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditoriaRetencaoServiceTest {

    @Test
    void exigirAntesDe_rejeitaNulo() {
        assertThrows(IllegalArgumentException.class, () -> AuditoriaRetencaoService.exigirAntesDe(null));
    }

    @Test
    void validarDataLimitePermitidaParaExclusao_rejeitaDataRecente() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuditoriaRetencaoService.validarDataLimitePermitidaParaExclusao(ontem));
        assertEquals(true, ex.getMessage().contains("30"));
    }

    @Test
    void validarDataLimitePermitidaParaExclusao_aceitaDataAntiga() {
        LocalDate limite = LocalDate.now().minusDays(AuditoriaRetencaoService.DIAS_PROTECAO_EXCLUSAO + 1);
        assertDoesNotThrow(() -> AuditoriaRetencaoService.validarDataLimitePermitidaParaExclusao(limite));
    }
}
