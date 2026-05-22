package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de alteração de status de ticket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarStatusRequestDTO {

    @NotBlank(message = "Status é obrigatório")
    private String status;

    private Long analistaId;
}
