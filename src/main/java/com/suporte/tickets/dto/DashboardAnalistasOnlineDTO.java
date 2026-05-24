package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalistasOnlineDTO {

    private List<DashboardAnalistaOnlineCardDTO> online = new ArrayList<>();
    private List<DashboardAnalistaOnlineCardDTO> ocupado = new ArrayList<>();
    private List<DashboardAnalistaOnlineCardDTO> ausente = new ArrayList<>();
    private List<DashboardAnalistaOnlineCardDTO> offline = new ArrayList<>();
    /** Sprint 286 — lista única ordenada para o grid. */
    private List<DashboardAnalistaOnlineCardDTO> operadores = new ArrayList<>();
}
