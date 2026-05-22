package com.suporte.tickets.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analistas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Analista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do analista e obrigatorio")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 150)
    private String nomeCompleto;

    @Column(length = 20)
    private String cpf;

    @Column(length = 12)
    private String cep;

    @Column(length = 150)
    private String rua;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 100)
    private String estado;

    @Column(length = 2)
    private String uf;

    @Column(length = 80)
    private String pais;

    @Column(length = 30)
    private String celular;

    private LocalDate dataNascimento;

    @Email(message = "E-mail do analista invalido")
    @NotBlank(message = "E-mail do analista e obrigatorio")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 50)
    private String nivel = "Nível 1";

    @Column(length = 500)
    private String fotoUrl;

    @JsonIgnore
    @Column(length = 100)
    private String senha;

    @JsonIgnore
    @Column(name = "auth_token", length = 64)
    private String authToken;

    @JsonIgnore
    @Column(name = "auth_token_expira_em")
    private LocalDateTime authTokenExpiraEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOperador statusOperador = StatusOperador.OFFLINE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PerfilAcesso perfilAcesso;

    @Column(nullable = false)
    private Boolean online = false;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    void prePersist() {
        normalizarCadastro();
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        normalizarCadastro();
    }

    private void normalizarCadastro() {
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            nomeCompleto = nome;
        }
        if ((nome == null || nome.isBlank()) && nomeCompleto != null) {
            nome = nomeCompleto;
        }
        if (online == null) {
            online = false;
        }
        if (statusOperador == null) {
            statusOperador = Boolean.TRUE.equals(online) ? StatusOperador.ONLINE : StatusOperador.OFFLINE;
        }
        if (nivel == null || nivel.isBlank()) {
            nivel = "Nível 1";
        }
        if (ativo == null) {
            ativo = true;
        }
    }
}
