package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSlaTicketCriticoDTO {

    private String numero;
    private String cliente;
    private String prioridade;
    private String status;
    private String slaPrimeiroAtendimentoStatus;
    private String slaResolucaoStatus;
    private LocalDateTime slaPrimeiroAtendimentoVencimento;
    private LocalDateTime slaResolucaoVencimento;
    private LocalDateTime vencimentoMaisCritico;
    private Boolean slaPausado;
    private Boolean escalonado;
}
