package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAlertaReferenciaDTO {

    private Integer ultimoId;
    private LocalDateTime ultimaDataAbertura;
}
