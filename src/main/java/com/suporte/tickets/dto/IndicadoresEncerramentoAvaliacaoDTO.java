package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresEncerramentoAvaliacaoDTO {

    private List<IndicadorMotivoItemDTO> topMotivos = new ArrayList<>();
    private IndicadoresPesquisaResumoDTO pesquisa = new IndicadoresPesquisaResumoDTO();
    private IndicadoresEnvioResumoDTO envio = new IndicadoresEnvioResumoDTO();
}
