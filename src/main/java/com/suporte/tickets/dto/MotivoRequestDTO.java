package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotivoRequestDTO {

    @NotNull(message = "Subcategoria e obrigatoria")
    private Long subgrupoId;

    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    private String descricao;
}
