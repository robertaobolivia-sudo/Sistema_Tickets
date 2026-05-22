package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubgrupoCategoriaRequestDTO {

    @NotNull(message = "Grupo e obrigatorio")
    private Long grupoId;

    @NotBlank(message = "Nome do subgrupo e obrigatorio")
    private String nome;

    private String descricao;
}
