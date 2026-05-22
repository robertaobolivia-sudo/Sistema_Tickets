package com.suporte.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoRequestDTO {

    /** Obrigatorio na criacao. */
    private Integer clienteId;

    /** Obrigatorio na criacao; ignorado na atualizacao (imutavel). */
    private String whatsapp;

    private String nome;

    private String email;
    private String empresaLocal;
    private String cidade;
    private String uf;
    private String observacoes;
    private Boolean ativo;
    private Boolean criadoAutomaticamente;
}
