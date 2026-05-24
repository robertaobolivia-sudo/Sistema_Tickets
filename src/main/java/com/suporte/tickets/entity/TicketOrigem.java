package com.suporte.tickets.entity;

/**
 * Origem operacional do ticket (Sprint F16).
 * Apenas duas origens válidas para novos atendimentos.
 */
public enum TicketOrigem {
    RECEPTIVO_WHATSAPP,
    ATIVO_MANUAL;

    public static boolean isValido(String valor) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        try {
            valueOf(valor.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
