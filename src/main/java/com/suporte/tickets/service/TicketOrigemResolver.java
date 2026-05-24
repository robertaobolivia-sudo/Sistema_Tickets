package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketOrigem;

/**
 * Sprint F16: origem explícita do ticket (substitui inferência só por whatsappMatriz).
 */
public final class TicketOrigemResolver {

    private TicketOrigemResolver() {
    }

    public static TicketOrigem resolverOrigemNaCriacao(TicketWebhookRequestDTO dto) {
        if (dto.getWhatsappMatrizId() != null) {
            return TicketOrigem.RECEPTIVO_WHATSAPP;
        }
        if (dto.getClienteContratanteId() != null) {
            return TicketOrigem.ATIVO_MANUAL;
        }
        return TicketOrigem.RECEPTIVO_WHATSAPP;
    }

    /** Exibição: campo persistido ou inferência legada para tickets antigos. */
    public static TicketOrigem resolverOrigemParaExibicao(Ticket ticket) {
        if (ticket.getOrigemTicket() != null) {
            return ticket.getOrigemTicket();
        }
        if (ticket.getWhatsappMatriz() != null) {
            return TicketOrigem.RECEPTIVO_WHATSAPP;
        }
        if (ticket.getContato() != null) {
            return TicketOrigem.ATIVO_MANUAL;
        }
        return null;
    }
}
