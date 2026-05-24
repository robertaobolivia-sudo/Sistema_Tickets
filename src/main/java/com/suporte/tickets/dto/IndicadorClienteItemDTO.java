package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorClienteItemDTO {
    private Integer clienteId;
    private String clienteNome;
    private long totalTickets;
    private Double tmeMinutosUteis;
    private Double tmaMinutosUteis;
    private Double percentualSlaCumprido;
}
