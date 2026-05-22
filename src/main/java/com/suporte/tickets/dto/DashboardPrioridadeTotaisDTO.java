package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPrioridadeTotaisDTO {

    private long critica;
    private long alta;
    private long media;
    private long baixa;
    private long semPrioridade;
}
