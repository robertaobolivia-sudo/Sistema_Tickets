package com.suporte.tickets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class IndicadoresClientesDTO {
    private long totalTickets;
    private long totalClientes;
    private List<IndicadorClienteItemDTO> rankingClientes = new ArrayList<>();
}
