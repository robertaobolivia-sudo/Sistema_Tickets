package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAlertaDTO {

    private Integer id;
    private String numeroTicket;
    private String cliente;
    private String status;
    private LocalDateTime dataAbertura;
}
