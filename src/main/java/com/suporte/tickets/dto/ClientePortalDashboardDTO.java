package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientePortalDashboardDTO {
    private long totalTickets;
    private long ticketsAbertos;
    private long ticketsEmAtendimento;
    private long ticketsResolvidos;
    private long ticketsCancelados;
    private Double mediaAvaliacao;
    private long totalAvaliacoes;
}
