package com.suporte.tickets.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketObservacaoAtendimentoRequestDTO {

    @Size(max = 2000, message = "Observacao de atendimento deve ter no maximo 2000 caracteres")
    private String observacao;
}
