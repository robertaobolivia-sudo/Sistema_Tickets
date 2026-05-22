package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatsHistoricoTicketResumoDTO {

    private String numeroTicket;
    private String status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataEncerramento;
    private String grupoCategoriaNome;
}
