package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOperacaoCardDTO {

    private long quantidade;
    /** TME ou TMA formatado (HH:MM:SS) ou "-" */
    private String tempoMedioFormatado;
    /** Rótulo exibido ao lado do tempo: TME / TMA */
    private String tempoMedioRotulo;
}
