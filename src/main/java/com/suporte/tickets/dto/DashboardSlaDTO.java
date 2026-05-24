package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSlaDTO {

    private DashboardSlaBlocoDTO primeiroAtendimento = new DashboardSlaBlocoDTO();
    private DashboardSlaBlocoDTO resolucao = new DashboardSlaBlocoDTO();
    private List<DashboardSlaPrioridadeDTO> porPrioridade = new ArrayList<>();
    private List<DashboardSlaTicketCriticoDTO> ticketsCriticosSla = new ArrayList<>();
    private long ticketsEscalonados;
    /** Sprint 284 — resumo operacional (tickets ativos, sem INDEVIDO). */
    private DashboardSlaVivoResumoDTO vivo = new DashboardSlaVivoResumoDTO();
}
