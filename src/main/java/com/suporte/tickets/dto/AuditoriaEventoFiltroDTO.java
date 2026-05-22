package com.suporte.tickets.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditoriaEventoFiltroDTO {

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Long analistaId;
    private String acao;
    private String entidade;
    private String entidadeId;
}
