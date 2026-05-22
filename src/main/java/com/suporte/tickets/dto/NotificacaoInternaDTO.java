package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoInternaDTO {

    private Long id;
    private String tipo;
    private String titulo;
    private String mensagem;
    private String ticketNumero;
    private Boolean lida;
    private LocalDateTime criadoEm;
    private LocalDateTime lidaEm;
}
