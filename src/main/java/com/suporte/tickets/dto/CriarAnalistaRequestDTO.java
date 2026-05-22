package com.suporte.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarAnalistaRequestDTO {

    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    @NotBlank(message = "Senha inicial e obrigatoria")
    private String senha;

    private String nivel;

    @NotBlank(message = "perfilAcesso e obrigatorio")
    private String perfilAcesso;

    private Boolean ativo;
}
