package com.suporte.tickets.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SlaVisualStatusHelper {

    private static final int HORAS_PROXIMO_VENCIMENTO = 2;

    public boolean isProximoVencimento(LocalDateTime vencimento) {
        if (vencimento == null) {
            return false;
        }
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        if (vencimento.isBefore(agora)) {
            return false;
        }
        return !vencimento.isAfter(agora.plusHours(HORAS_PROXIMO_VENCIMENTO));
    }
}
