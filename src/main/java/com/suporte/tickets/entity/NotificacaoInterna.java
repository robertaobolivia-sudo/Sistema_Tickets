package com.suporte.tickets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_internas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoInterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificacaoTipo tipo;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Lob
    @Column(nullable = true)
    private String mensagem;

    @Column(name = "ticket_numero", nullable = false, length = 20)
    private String ticketNumero;

    @Column(nullable = false)
    private Boolean lida = false;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "lida_em", nullable = true)
    private LocalDateTime lidaEm;
}
