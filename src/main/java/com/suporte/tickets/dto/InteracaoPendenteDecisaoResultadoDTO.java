package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoPendenteDecisaoResultadoDTO {

    private Long pendenciaId;
    private String status;
    private String numeroTicketAnterior;
    private String numeroTicket;
    private boolean ticketCriado;
}
