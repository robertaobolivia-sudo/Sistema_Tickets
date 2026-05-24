package com.suporte.tickets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class IndicadoresAtendentesDTO {
    private long totalTickets;
    private List<IndicadorAtendenteItemDTO> atendentes = new ArrayList<>();
}
