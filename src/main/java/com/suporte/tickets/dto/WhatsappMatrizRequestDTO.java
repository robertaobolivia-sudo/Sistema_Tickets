package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappMatrizRequestDTO {

    @NotNull(message = "Cliente e obrigatorio")
    private Integer clienteId;

    private String nome;
    private String numero;
    private Boolean ativo;
    private String provedor;
    private String identificadorExterno;
}
