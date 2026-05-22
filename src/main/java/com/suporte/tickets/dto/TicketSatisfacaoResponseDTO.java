package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSatisfacaoResponseDTO {

    private Long id;
    private String numeroTicket;
    private String status;
    private Integer nota;
    private String comentario;
    private LocalDateTime criadoEm;
    private LocalDateTime enviadaEm;
    private LocalDateTime respondidaEm;
    private LocalDateTime expiraEm;
    /** Token opaco para link público (somente preview interno, PENDENTE). */
    private String tokenRespostaPreview;
    private String envioStatus;
    private String linkAvaliacaoPublico;
}
