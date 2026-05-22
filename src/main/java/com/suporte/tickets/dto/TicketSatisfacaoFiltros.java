package com.suporte.tickets.dto;

import com.suporte.tickets.entity.TicketStatus;

import java.time.LocalDateTime;

/**
 * Filtros compartilhados entre resumo e exportação CSV de satisfação.
 */
public record TicketSatisfacaoFiltros(
        LocalDateTime inicio,
        LocalDateTime fim,
        Integer nota,
        TicketStatus statusTicket,
        String termoCliente,
        Integer clienteId) {
}
