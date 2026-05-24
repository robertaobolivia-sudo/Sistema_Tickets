package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardAvaliacaoTempoRealDTO;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sprint 285 — avaliação operacional em tempo real (sem INDEVIDO, sem filtro de período).
 */
@Service
@RequiredArgsConstructor
public class DashboardAvaliacaoTempoRealService {

    private static final int NOTA_RUIM_MAX = 2;

    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @Transactional(readOnly = true)
    public DashboardAvaliacaoTempoRealDTO obter() {
        List<TicketSatisfacao> lista =
                ticketSatisfacaoRepository.findAllOperacionalExcluindoIndevido(TicketStatus.INDEVIDO);

        long respondidas = 0;
        long pendentes = 0;
        long expiradas = 0;
        long ruins = 0;
        long somaNotas = 0;
        long comNota = 0;

        for (TicketSatisfacao s : lista) {
            TicketSatisfacaoStatus st = s.getStatus();
            if (st == TicketSatisfacaoStatus.PENDENTE) {
                pendentes++;
            } else if (st == TicketSatisfacaoStatus.EXPIRADA) {
                expiradas++;
            } else if (st == TicketSatisfacaoStatus.RESPONDIDA
                    || st == TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE) {
                respondidas++;
                if (s.getNota() != null) {
                    int nota = s.getNota();
                    somaNotas += nota;
                    comNota++;
                    if (nota <= NOTA_RUIM_MAX) {
                        ruins++;
                    }
                }
            }
        }

        DashboardAvaliacaoTempoRealDTO dto = new DashboardAvaliacaoTempoRealDTO();
        dto.setPesquisasRespondidas(respondidas);
        dto.setPesquisasPendentes(pendentes);
        dto.setPesquisasExpiradas(expiradas);
        dto.setAvaliacoesRuins(ruins);
        if (comNota == 0) {
            dto.setMediaAtual(null);
        } else {
            dto.setMediaAtual(Math.round((somaNotas * 10.0) / comNota) / 10.0);
        }
        return dto;
    }
}
