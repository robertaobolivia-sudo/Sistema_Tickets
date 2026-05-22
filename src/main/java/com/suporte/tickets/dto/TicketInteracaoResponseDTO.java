package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInteracaoResponseDTO {

    private Long id;
    private String numeroTicket;
    private String tipoInteracao;
    private String visibilidade;
    private String mensagem;
    private LocalDateTime criadoEm;
}
