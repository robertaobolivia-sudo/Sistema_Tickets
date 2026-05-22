package com.suporte.tickets.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sla_metas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private PrioridadeTicket prioridade;

    @Column(name = "prazo_primeiro_atendimento_minutos", nullable = false)
    private Integer prazoPrimeiroAtendimentoMinutos;

    @Column(name = "prazo_resolucao_minutos", nullable = false)
    private Integer prazoResolucaoMinutos;

    @Column(nullable = false)
    private Boolean ativo = true;
}
