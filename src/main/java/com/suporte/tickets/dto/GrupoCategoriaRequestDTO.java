package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoCategoriaRequestDTO {

    @NotBlank(message = "Nome do grupo e obrigatorio")
    private String nome;

    private String descricao;
}
