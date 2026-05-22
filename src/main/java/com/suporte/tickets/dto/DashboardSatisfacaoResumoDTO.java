package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSatisfacaoResumoDTO {

    private String motivoMaisRecorrente;
    private long totalMotivoMaisRecorrente;
    private long totalPesquisas;
    private long totalRespondidas;
    private long totalPendentes;
    private long totalExpiradas;
    private Double mediaNota;
    private long totalFalhasEnvio;
    private long totalSimuladas;
    /** Período aplicado (ISO date), para exibição opcional. */
    private String dataInicio;
    private String dataFim;
}
