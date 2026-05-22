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
 * Vínculo N:N entre contato e etiqueta (tabela contato_etiquetas).
 */
@Entity
@Table(
        name = "contato_etiquetas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contato_id", "etiqueta_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContatoEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contato_id", nullable = false)
    private Contato contato;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etiqueta_id", nullable = false)
    private Etiqueta etiqueta;
}
