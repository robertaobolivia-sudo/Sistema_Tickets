package com.suporte.tickets.entity;

/**
 * Motivo operacional ao classificar ticket como indevido (Sprint 274).
 */
public enum TicketClassificacaoOperacional {
    INDEVIDO,
    CONTATO_PESSOAL,
    PROPAGANDA;

    public static TicketClassificacaoOperacional fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Motivo operacional e obrigatorio.");
        }
        try {
            return TicketClassificacaoOperacional.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Motivo operacional invalido: " + valor);
        }
    }
}
