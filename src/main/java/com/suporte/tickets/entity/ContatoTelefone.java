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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Telefone adicional vinculado a um {@link Contato} (Sprint 288).
 * O WhatsApp principal permanece em {@link Contato#whatsappNormalizado} (imutável).
 */
@Entity
@Table(
        name = "contato_telefones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contato_tel_cliente_norm",
                        columnNames = {"cliente_id", "telefone_normalizado"}
                ),
                @UniqueConstraint(
                        name = "uk_contato_tel_contato_norm",
                        columnNames = {"contato_id", "telefone_normalizado"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoTelefone {

    public static final String ORIGEM_ADICIONAL = "ADICIONAL";
    public static final String ORIGEM_CADASTRO_MANUAL = "CADASTRO_MANUAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contato_id", nullable = false)
    private Contato contato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String telefone;

    @NotBlank
    @Column(name = "telefone_normalizado", nullable = false, length = 20)
    private String telefoneNormalizado;

    /**
     * Sempre {@code false} nesta tabela: o principal é o WhatsApp do {@link Contato}.
     */
    @Column(nullable = false)
    private Boolean principal = false;

    @NotBlank
    @Column(nullable = false, length = 40)
    private String origem = ORIGEM_ADICIONAL;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (principal == null) {
            principal = false;
        }
        if (origem == null || origem.isBlank()) {
            origem = ORIGEM_ADICIONAL;
        }
    }
}
