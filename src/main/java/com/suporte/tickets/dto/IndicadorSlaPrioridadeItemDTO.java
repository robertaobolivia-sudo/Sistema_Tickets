package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorSlaPrioridadeItemDTO {
    private String prioridade;
    private long totalTickets;
    private long cumpridos;
    private long naoAvaliados;
    private Double percentualCumprimento;
    private Double tempoMedioMinutos;
}
