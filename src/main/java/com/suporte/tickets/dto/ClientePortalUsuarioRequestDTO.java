package com.suporte.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientePortalUsuarioRequestDTO {
    @NotBlank
    private String nome;
    @Email @NotBlank
    private String email;
    private String senha;
    @NotNull
    private Integer clienteId;
    private Boolean ativo = true;
}
