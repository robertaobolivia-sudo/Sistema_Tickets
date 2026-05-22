package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaCalculoTesteResponseDTO {

    private String inicio;
    private long minutosUteis;
    private String vencimento;
    private String timezone;
}
