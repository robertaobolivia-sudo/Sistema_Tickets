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
 * Número WhatsApp do Cliente contratante conectado à API (Sprint 191).
 */
@Entity
@Table(
        name = "whatsapp_matriz",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_whatsapp_matriz_numero_norm",
                columnNames = "numero_normalizado"
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatsappMatriz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(length = 120)
    private String nome;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String numero;

    @NotBlank
    @Column(name = "numero_normalizado", nullable = false, length = 20)
    private String numeroNormalizado;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 80)
    private String provedor;

    @Column(name = "identificador_externo", length = 120)
    private String identificadorExterno;

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
