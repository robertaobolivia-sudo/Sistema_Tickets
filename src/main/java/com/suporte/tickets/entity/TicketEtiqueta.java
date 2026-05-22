package com.suporte.tickets.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vínculo N:N entre ticket e etiqueta (tabela ticket_etiquetas).
 */
@Entity
@Table(
        name = "ticket_etiquetas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ticket_id", "etiqueta_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;
}
