package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOperacaoChamadoB2BDTO {

    /** TICKET ou PENDENCIA_DECISAO */
    private String tipo;
    private String numeroTicket;
    private String contato;
    private String status;
    private String analista;
    private String tmeFormatado;
    private String tmaFormatado;
    private Long pendenciaDecisaoId;
}
