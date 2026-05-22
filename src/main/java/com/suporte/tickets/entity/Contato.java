package com.suporte.tickets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Pessoa final atendida pelo WhatsApp (modelo oficial Sprint 189+).
 * Chave lógica: {@link #cliente} + {@link #whatsappNormalizado}.
 */
@Entity
@Table(
        name = "contatos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_contatos_cliente_whatsapp_norm",
                columnNames = {"cliente_id", "whatsapp_normalizado"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nome;

    /** Número informado na criação (exibição). */
    @NotBlank
    @Column(nullable = false, length = 30)
    private String whatsapp;

    /** Apenas dígitos; imutável após persistência. */
    @NotBlank
    @Column(name = "whatsapp_normalizado", nullable = false, length = 20, updatable = false)
    private String whatsappNormalizado;

    @Column(length = 150)
    private String email;

    @Column(name = "empresa_local", length = 150)
    private String empresaLocal;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_automaticamente", nullable = false)
    private Boolean criadoAutomaticamente = false;

    @Column(name = "primeira_interacao_em")
    private LocalDateTime primeiraInteracaoEm;

    @Column(name = "ultima_interacao_em")
    private LocalDateTime ultimaInteracaoEm;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
