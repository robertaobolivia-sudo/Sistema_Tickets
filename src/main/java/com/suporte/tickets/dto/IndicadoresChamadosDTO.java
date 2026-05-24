package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresChamadosDTO {

    private long totalChamados;
    /** Tickets INDEVIDO no período (grupo Não atendimento). */
    private long totalNaoAtendimento;
    private long totalClientes;
    private List<IndicadorContagemDTO> chamadosPorAtendente = new ArrayList<>();
    private List<IndicadorContagemDTO> chamadosPorGrupo = new ArrayList<>();
    private List<IndicadorContagemDTO> chamadosPorSubgrupo = new ArrayList<>();
    private List<IndicadorContagemDTO> chamadosPorPrioridade = new ArrayList<>();
    private List<IndicadorContagemDTO> chamadosPorStatus = new ArrayList<>();
    private List<IndicadorContagemDTO> chamadosPorClassificacaoCliente = new ArrayList<>();
}
