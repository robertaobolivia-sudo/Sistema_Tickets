package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Classificação explícita de ticket como indevido (Sprint 274).
 */
@Data
public class ClassificarTicketIndevidoRequestDTO {

    /** Deve ser true — confirmação do analista (Sprint 273). */
    @NotNull(message = "Confirmacao e obrigatoria")
    private Boolean confirmacao;

    @NotBlank(message = "Motivo operacional e obrigatorio")
    private String motivoOperacional;

    private String comentario;

    private Long grupoId;
    private Long subgrupoId;
    private Long motivoId;
}
