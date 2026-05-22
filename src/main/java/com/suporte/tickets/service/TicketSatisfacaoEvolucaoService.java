package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoEvolucaoDiaDTO;
import com.suporte.tickets.dto.TicketSatisfacaoFiltros;
import com.suporte.tickets.entity.TicketSatisfacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketSatisfacaoEvolucaoService {

    private final TicketSatisfacaoConsultaService ticketSatisfacaoConsultaService;

    @Transactional(readOnly = true)
    public List<TicketSatisfacaoEvolucaoDiaDTO> calcularEvolucao(
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer nota,
            String statusTicket,
            String termoCliente,
            Integer clienteId) {
        TicketSatisfacaoFiltros filtros =
                ticketSatisfacaoConsultaService.resolverFiltros(
                        dataInicio, dataFim, nota, statusTicket, termoCliente, clienteId);
        List<TicketSatisfacao> avaliacoes = ticketSatisfacaoConsultaService.listarAvaliacoes(filtros);
        return agruparPorDia(avaliacoes);
    }

    static List<TicketSatisfacaoEvolucaoDiaDTO> agruparPorDia(List<TicketSatisfacao> avaliacoes) {
        Map<LocalDate, List<TicketSatisfacao>> porDia = new LinkedHashMap<>();
        for (TicketSatisfacao s : avaliacoes) {
            if (s.getCriadoEm() == null) {
                continue;
            }
            LocalDate dia = s.getCriadoEm().toLocalDate();
            porDia.computeIfAbsent(dia, k -> new ArrayList<>()).add(s);
        }
        return porDia.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> agregarDia(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static TicketSatisfacaoEvolucaoDiaDTO agregarDia(LocalDate data, List<TicketSatisfacao> doDia) {
        long total = doDia.size();
        long soma = 0;
        long comNota = 0;
        long positivas = 0;
        long negativas = 0;
        for (TicketSatisfacao s : doDia) {
            if (s.getNota() == null) {
                continue;
            }
            comNota++;
            int n = s.getNota();
            soma += n;
            if (n >= 4) {
                positivas++;
            }
            if (n <= 2) {
                negativas++;
            }
        }
        double media = comNota == 0 ? 0.0 : Math.round((soma * 10.0) / comNota) / 10.0;
        return new TicketSatisfacaoEvolucaoDiaDTO(data, total, media, positivas, negativas);
    }
}
