package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoDTO {

    private long totalTickets;
    private long ticketsAbertos;
    private long ticketsEmAtendimento;
    private long ticketsResolvidos;
    private long ticketsCancelados;
    private long ticketsSemAnalista;
    private long ticketsAbertosHoje;
    private long ticketsResolvidosHoje;
    private long clientesAtivos;
    private long analistasOnline;
    private long analistasAusentes;
    private long analistasOffline;
    private String tempoMedioPrimeiroAtendimento = "-";
    private String tempoMedioResolucao = "-";
    private List<DashboardGrupoDTO> ticketsPorStatus = new ArrayList<>();
    private List<DashboardGrupoDTO> ticketsPorConexao = new ArrayList<>();
    private List<DashboardAnalistaResumoDTO> ticketsPorAnalista = new ArrayList<>();
}
