package com.suporte.tickets.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalistaResponseDTO {

    private Long id;
    private String nome;
    private String nomeCompleto;
    private String cpf;
    private String cep;
    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String uf;
    private String pais;
    private String celular;
    private LocalDate dataNascimento;
    private String email;
    private String nivel;
    /** Preenchido apenas na resposta do login; nao exposto em listagens. */
    private String authToken;
    private String perfilAcesso;
    private String fotoUrl;
    private String statusOperador;
    private Boolean online;
    private Boolean ativo;
    private LocalDateTime dataCadastro;
}
