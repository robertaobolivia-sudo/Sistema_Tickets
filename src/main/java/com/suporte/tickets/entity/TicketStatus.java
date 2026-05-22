package com.suporte.tickets.entity;

/**
 * Enum que define os possíveis status de um ticket
 * 
 * Status fluxo:
 * ABERTO -> EM_ATENDIMENTO -> RESOLVIDO
 * ABERTO -> AGUARDANDO_CLIENTE -> RESOLVIDO
 * ABERTO -> CANCELADO
 */
public enum TicketStatus {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em Atendimento"),
    AGUARDANDO_CLIENTE("Aguardando Cliente"),
    RESOLVIDO("Resolvido"),
    CANCELADO("Cancelado");

    private final String descricao;

    TicketStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Valida se a string é um status válido
     * @param status String do status
     * @return true se é válido
     */
    public static boolean isValido(String status) {
        if (status == null || status.isEmpty()) {
            return false;
        }
        try {
            TicketStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
