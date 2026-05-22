package com.suporte.tickets.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSatisfacaoRequestDTO {

    @NotNull(message = "Nota e obrigatoria")
    @Min(value = 1, message = "Nota deve ser entre 1 e 5")
    @Max(value = 5, message = "Nota deve ser entre 1 e 5")
    private Integer nota;

    @Size(max = 500, message = "Comentario deve ter no maximo 500 caracteres")
    private String comentario;
}
