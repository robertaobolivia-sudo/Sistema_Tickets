package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardSatisfacaoResumoDTO;
import com.suporte.tickets.dto.IndicadorMotivoItemDTO;
import com.suporte.tickets.dto.IndicadoresEncerramentoAvaliacaoDTO;
import com.suporte.tickets.dto.IndicadoresEnvioResumoDTO;
import com.suporte.tickets.dto.IndicadoresPesquisaResumoDTO;

import java.util.List;

/**
 * Mapeia agregação de indicadores (Sprint 202) para cards do Dashboard (Sprint 203).
 */
public final class DashboardEncerramentoSatisfacaoResumoMapper {

    private DashboardEncerramentoSatisfacaoResumoMapper() {
    }

    public static DashboardSatisfacaoResumoDTO fromIndicadores(
            IndicadoresEncerramentoAvaliacaoDTO origem, String dataInicioIso, String dataFimIso) {
        DashboardSatisfacaoResumoDTO dto = new DashboardSatisfacaoResumoDTO();
        dto.setDataInicio(dataInicioIso);
        dto.setDataFim(dataFimIso);

        List<IndicadorMotivoItemDTO> top = origem != null ? origem.getTopMotivos() : null;
        if (top != null && !top.isEmpty()) {
            IndicadorMotivoItemDTO primeiro = top.get(0);
            dto.setMotivoMaisRecorrente(primeiro.getMotivoNome());
            dto.setTotalMotivoMaisRecorrente(primeiro.getTotalTickets());
        }

        IndicadoresPesquisaResumoDTO p = origem != null ? origem.getPesquisa() : null;
        if (p != null) {
            dto.setTotalPesquisas(p.getTotalPesquisas());
            dto.setTotalRespondidas(p.getRespondidas());
            dto.setTotalPendentes(p.getPendentes());
            dto.setTotalExpiradas(p.getExpiradas());
            dto.setMediaNota(p.getMediaNota());
        }

        IndicadoresEnvioResumoDTO e = origem != null ? origem.getEnvio() : null;
        if (e != null) {
            dto.setTotalFalhasEnvio(e.getFalhas());
            dto.setTotalSimuladas(e.getSimuladas());
        }
        return dto;
    }
}
