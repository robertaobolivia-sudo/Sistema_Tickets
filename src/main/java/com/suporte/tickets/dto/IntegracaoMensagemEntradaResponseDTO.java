package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegracaoMensagemEntradaResponseDTO {

    private boolean ticketCriado;
    private String numeroTicket;
    private String status;
    private boolean mensagemRegistrada;
    /** Sprint 195: mensagem aguardando decisão do analista (ticket anterior encerrado). */
    private boolean aguardandoDecisao;
    private Long pendenciaDecisaoId;
    /** Sprint 303: motivo operacional quando contato bloqueado por etiqueta (ex: INDEVIDO, PROPAGANDA). */
    private String motivoOperacional;
}
