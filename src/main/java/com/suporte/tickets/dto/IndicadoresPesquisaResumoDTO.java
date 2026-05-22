package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresPesquisaResumoDTO {

    private long totalPesquisas;
    private long pendentes;
    private long respondidas;
    private long expiradas;
    private long naoEnviadas;
    private long registradasManualmente;
    private Double mediaNota;
    private long quantidadeNota1;
    private long quantidadeNota2;
    private long quantidadeNota3;
    private long quantidadeNota4;
    private long quantidadeNota5;
}
