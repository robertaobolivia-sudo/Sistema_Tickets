package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Pendência operacional no Dashboard (por Cliente contratante). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPendenciaClienteDTO {

    private String numeroTicket;
    private String cliente;
    private String contato;
    private String assunto;
    private String status;
    private String tipoStatus;
    private String analista;
    private String tmaFormatado;
    private LocalDateTime dataAbertura;
}
