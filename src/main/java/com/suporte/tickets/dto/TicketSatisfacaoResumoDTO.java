package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSatisfacaoResumoDTO {

    private long totalAvaliacoes;
    private Double mediaGeral;
    private long quantidadeNota1;
    private long quantidadeNota2;
    private long quantidadeNota3;
    private long quantidadeNota4;
    private long quantidadeNota5;
    /** Percentual 0–100 (notas 4 e 5). */
    private double percentualPositivas;
    /** Percentual 0–100 (notas 1 e 2). */
    private double percentualNegativas;
}
