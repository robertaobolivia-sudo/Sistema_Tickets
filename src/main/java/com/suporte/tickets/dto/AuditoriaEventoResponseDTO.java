package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaEventoResponseDTO {

    private Long id;
    private LocalDateTime dataHora;
    private Long analistaId;
    private String analistaNome;
    private String perfilAcesso;
    private String acao;
    private String entidade;
    private String entidadeId;
    private String descricao;
    private String ipOrigem;
    private String userAgent;
}
