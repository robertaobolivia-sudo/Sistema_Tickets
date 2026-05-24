package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContatoTelefoneRequestDTO {

    @NotBlank
    private String telefone;

    /** Opcional: {@link com.suporte.tickets.entity.ContatoTelefone#ORIGEM_CADASTRO_MANUAL} etc. */
    private String origem;
}
