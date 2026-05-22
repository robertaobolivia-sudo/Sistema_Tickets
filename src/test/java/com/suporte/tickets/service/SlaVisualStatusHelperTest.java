package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlaVisualStatusHelperTest {

    private final SlaVisualStatusHelper helper = new SlaVisualStatusHelper();

    @Test
    void proximoVencimentoDentroDeDuasHoras() {
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        assertTrue(helper.isProximoVencimento(agora.plusMinutes(90)));
    }

    @Test
    void naoEhProximoSeVencimentoJaPassou() {
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        assertFalse(helper.isProximoVencimento(agora.minusMinutes(10)));
    }

    @Test
    void naoEhProximoSeVencimentoMaisDeDuasHoras() {
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        assertFalse(helper.isProximoVencimento(agora.plusHours(3)));
    }
}
