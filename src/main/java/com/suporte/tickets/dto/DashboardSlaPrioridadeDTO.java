package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSlaPrioridadeDTO {

    private String prioridade;
    private long total;
    private long slaPrimeiroViolado;
    private long slaResolucaoViolado;
    private long slaResolucaoVencido;
}
