package com.suporte.tickets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_satisfacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSatisfacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", length = 32)
    private TicketSatisfacaoStatus status;

    /** Nullable até resposta (PENDENTE / NAO_ENVIADA). Sprint 211: alinha com MySQL/Hibernate. */
    @Column(nullable = true)
    private Integer nota;

    @Column(length = 500)
    private String comentario;

    @Column(name = "enviada_em")
    private LocalDateTime enviadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(name = "solicitada_por_analista_id")
    private Long solicitadaPorAnalistaId;

    @Column(name = "token_resposta_hash", length = 64, unique = true)
    private String tokenRespostaHash;

    @Column(name = "token_criado_em")
    private LocalDateTime tokenCriadoEm;

    @Column(name = "token_usado_em")
    private LocalDateTime tokenUsadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "envio_status", length = 24)
    private TicketSatisfacaoEnvioStatus envioStatus;

    @Column(name = "ultima_tentativa_envio_em")
    private LocalDateTime ultimaTentativaEnvioEm;

    @Column(name = "erro_envio", length = 500)
    private String erroEnvio;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
