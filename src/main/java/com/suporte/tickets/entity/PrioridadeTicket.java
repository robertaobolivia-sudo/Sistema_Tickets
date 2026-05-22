package com.suporte.tickets.entity;

public enum PrioridadeTicket {
    BAIXA,
    MEDIA,
    ALTA,
    CRITICA;

    public static boolean isValido(String prioridade) {
        if (prioridade == null || prioridade.isBlank()) {
            return false;
        }
        try {
            PrioridadeTicket.valueOf(prioridade.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
