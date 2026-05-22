package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaRequestDTO {

    @NotBlank(message = "Nome da etiqueta e obrigatorio")
    private String nome;

    private String descricao;

    private String cor;
}
