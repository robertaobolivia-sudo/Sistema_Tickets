package com.suporte.tickets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class IndicadoresSlaDTO {
    private long totalTicketsEncerrados;
    private long totalCumpridos;
    private Double percentualGeralCumprimento;
    /** Meta fixa 60 min (Sprint 308). */
    private int metaMinutos = 60;
    private List<IndicadorSlaPrioridadeItemDTO> porPrioridade = new ArrayList<>();
}
