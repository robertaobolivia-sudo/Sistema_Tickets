package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarPerfilAcessoRequestDTO {

    @NotBlank(message = "perfilAcesso e obrigatorio")
    private String perfilAcesso;
}
