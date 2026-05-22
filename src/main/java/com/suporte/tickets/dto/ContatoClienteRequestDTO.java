package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoClienteRequestDTO {

    @NotNull(message = "Cliente e obrigatorio")
    private Integer clienteId;

    @NotBlank(message = "Nome do contato e obrigatorio")
    private String nome;

    private String cargo;
    private String telefone;
    private String celular;
    private String email;
    private Boolean principal;
    private String observacoes;
}
