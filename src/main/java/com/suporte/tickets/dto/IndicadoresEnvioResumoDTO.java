package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresEnvioResumoDTO {

    private long simuladas;
    private long falhas;
    private long semTentativa;
}
