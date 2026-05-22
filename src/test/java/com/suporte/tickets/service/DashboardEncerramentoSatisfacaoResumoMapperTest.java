package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardSatisfacaoResumoDTO;
import com.suporte.tickets.dto.IndicadorMotivoItemDTO;
import com.suporte.tickets.dto.IndicadoresEncerramentoAvaliacaoDTO;
import com.suporte.tickets.dto.IndicadoresEnvioResumoDTO;
import com.suporte.tickets.dto.IndicadoresPesquisaResumoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DashboardEncerramentoSatisfacaoResumoMapperTest {

    @Test
    void fromIndicadores_mapeiaMotivoEMedia() {
        IndicadorMotivoItemDTO m = new IndicadorMotivoItemDTO();
        m.setMotivoNome("Treinamento");
        m.setTotalTickets(4L);

        IndicadoresPesquisaResumoDTO p = new IndicadoresPesquisaResumoDTO();
        p.setTotalPesquisas(10);
        p.setRespondidas(6);
        p.setPendentes(2);
        p.setExpiradas(1);
        p.setMediaNota(4.2);

        IndicadoresEnvioResumoDTO e = new IndicadoresEnvioResumoDTO();
        e.setFalhas(1);
        e.setSimuladas(3);

        IndicadoresEncerramentoAvaliacaoDTO origem = new IndicadoresEncerramentoAvaliacaoDTO();
        origem.setTopMotivos(List.of(m));
        origem.setPesquisa(p);
        origem.setEnvio(e);

        DashboardSatisfacaoResumoDTO dto = DashboardEncerramentoSatisfacaoResumoMapper.fromIndicadores(
                origem, "2026-04-21", "2026-05-21");

        assertEquals("Treinamento", dto.getMotivoMaisRecorrente());
        assertEquals(4L, dto.getTotalMotivoMaisRecorrente());
        assertEquals(6, dto.getTotalRespondidas());
        assertEquals(4.2, dto.getMediaNota());
        assertEquals(1, dto.getTotalFalhasEnvio());
        assertEquals(3, dto.getTotalSimuladas());
    }

    @Test
    void fromIndicadores_vazio() {
        DashboardSatisfacaoResumoDTO dto = DashboardEncerramentoSatisfacaoResumoMapper.fromIndicadores(
                new IndicadoresEncerramentoAvaliacaoDTO(), "2026-05-01", "2026-05-21");
        assertNull(dto.getMotivoMaisRecorrente());
        assertEquals(0, dto.getTotalPesquisas());
        assertNull(dto.getMediaNota());
    }
}
