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
@Table(name = "ticket_anexos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "tipo_conteudo", length = 120)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    /** Caminho relativo dentro de uploads/ (ex.: tickets/TK-1/uuid.pdf). */
    @Column(name = "identificador_arquivo", nullable = false, length = 500)
    private String identificadorArquivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketAnexoOrigem origem = TicketAnexoOrigem.MANUAL;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "criado_por_analista_id")
    private Long criadoPorAnalistaId;

    @Column(name = "criado_por_nome", length = 120)
    private String criadoPorNome;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (origem == null) {
            origem = TicketAnexoOrigem.MANUAL;
        }
    }
}
