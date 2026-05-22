package com.suporte.tickets.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {

    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @NotBlank(message = "Telefone e obrigatorio")
    private String telefone;

    @NotBlank(message = "Telefone de contato e obrigatorio")
    private String telefoneContato;

    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    private String email;

    private String empresa;
    private String cnpj;
    private String cidade;
    private String uf;
    private String endereco;
    /** Legado: preferir não enviar na tela Clientes (Sprint 188). */
    private Integer carteiraId;
    /** Legado: ignorado no {@code ClienteService} (não cria Carteira por nome). */
    private String carteira;
    private String status;
    /** Legado (banco): preferir SEM_CLASSIFICACAO; N1/N2 não são padrão de produto. */
    private String classificacaoCliente;
    private String observacoes;
}