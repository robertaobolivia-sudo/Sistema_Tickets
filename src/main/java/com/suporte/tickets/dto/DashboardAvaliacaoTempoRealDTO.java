package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAvaliacaoTempoRealDTO {

    /** Média das notas já registradas (1–5); null se nenhuma resposta com nota. */
    private Double mediaAtual;
    /** Respostas com nota 1 ou 2. */
    private long avaliacoesRuins;
    private long pesquisasRespondidas;
    private long pesquisasPendentes;
    private long pesquisasExpiradas;
}
