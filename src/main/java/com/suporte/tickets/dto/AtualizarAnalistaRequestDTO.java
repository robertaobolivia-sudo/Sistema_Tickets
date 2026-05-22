package com.suporte.tickets.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarAnalistaRequestDTO {

    private String nome;

    @Email(message = "E-mail invalido")
    private String email;

    private String nivel;

    private String perfilAcesso;

    private Boolean ativo;

    /** Se informada e nao vazia, substitui a senha do analista. */
    private String senha;
}
