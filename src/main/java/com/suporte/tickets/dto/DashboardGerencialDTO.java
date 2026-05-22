package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardGerencialDTO {

    private DashboardPrioridadeTotaisDTO totaisPorPrioridade = new DashboardPrioridadeTotaisDTO();
    private List<DashboardGrupoDTO> topGrupos = new ArrayList<>();
    private List<DashboardSubgrupoResumoDTO> topSubgrupos = new ArrayList<>();
    private List<TicketResponseDTO> ticketsCriticosAltos = new ArrayList<>();
}
