package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorAtendenteItemDTO {
    private Long analistaId;
    private String analistaNome;
    private long totalTickets;
    private Double tmaMinutosUteis;
    private Double percentualSlaCumprido;
    private Double mediaAvaliacao;
    private long totalAvaliacoes;
}
