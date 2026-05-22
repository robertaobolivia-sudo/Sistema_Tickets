package com.suporte.tickets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "horarios_uteis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(nullable = false)
    private Boolean segunda = true;

    @Column(nullable = false)
    private Boolean terca = true;

    @Column(nullable = false)
    private Boolean quarta = true;

    @Column(nullable = false)
    private Boolean quinta = true;

    @Column(nullable = false)
    private Boolean sexta = true;

    @Column(nullable = false)
    private Boolean sabado = false;

    @Column(nullable = false)
    private Boolean domingo = false;

    @Column(nullable = false)
    private Boolean ativo = true;
}
