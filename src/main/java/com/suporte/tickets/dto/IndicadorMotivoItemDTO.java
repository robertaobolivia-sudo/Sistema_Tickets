package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorMotivoItemDTO {

    private Long motivoId;
    private String motivoNome;
    private String subcategoriaNome;
    private String categoriaNome;
    private long totalTickets;
}
