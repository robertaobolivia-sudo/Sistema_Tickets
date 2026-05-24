package com.suporte.tickets.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Contratante F5.
 *
 * <p>Modelo operacional: Cliente → Contato → Ticket. Arte Chats no cadastro do Cliente.</p>
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O nome do cliente é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Column(name = "responsavel", length = 150)
    private String responsavel;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String whatsapp;

    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @Column(length = 150)
    private String email;

    @Column(length = 150)
    private String empresa;

    @Column(length = 18)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 255)
    private String endereco;

    @Column(length = 12)
    private String cep;

    @Column(length = 255)
    private String site;

    @Column(name = "horario_funcionamento", length = 120)
    private String horarioFuncionamento;

    @Column(length = 30)
    private String status = "ATIVO";

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_cliente", length = 30)
    private ClassificacaoCliente classificacaoCliente = ClassificacaoCliente.SEM_CLASSIFICACAO;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    /** URL pública da arte do header Chats (Sprint 180). */
    @Column(name = "arte_header_chats_url", length = 512)
    private String arteHeaderChatsUrl;

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        sincronizarStatusEAtivo();
        normalizarClassificacao();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        sincronizarStatusEAtivo();
        normalizarClassificacao();
        dataAtualizacao = LocalDateTime.now();
    }

    private void normalizarClassificacao() {
        classificacaoCliente = ClassificacaoCliente.effective(classificacaoCliente);
    }

    private void sincronizarStatusEAtivo() {
        if (status == null || status.isBlank()) {
            status = Boolean.FALSE.equals(ativo) ? "INATIVO" : "ATIVO";
        }
        ativo = !"INATIVO".equalsIgnoreCase(status);
        if (ativo) {
            status = "ATIVO";
        }
    }

}
