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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interacao_pendente_decisao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoPendenteDecisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contato_id", nullable = false)
    private Contato contato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_anterior_id", nullable = false)
    private Ticket ticketAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whatsapp_matriz_id")
    private WhatsappMatriz whatsappMatriz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_gerado_id")
    private Ticket ticketGerado;

    @Column(nullable = false, length = 4000)
    private String mensagem;

    @Column(length = 64)
    private String canal;

    @Column(name = "origem_externa_id", length = 128)
    private String origemExternaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InteracaoPendenteDecisaoStatus status = InteracaoPendenteDecisaoStatus.PENDENTE;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "decidida_em")
    private LocalDateTime decididaEm;

    @Column(name = "decidida_por_analista_id")
    private Long decididaPorAnalistaId;

    @PrePersist
    void prePersist() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
        if (status == null) {
            status = InteracaoPendenteDecisaoStatus.PENDENTE;
        }
    }
}
