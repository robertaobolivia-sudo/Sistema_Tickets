package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorSlaPrioridadeItemDTO;
import com.suporte.tickets.dto.IndicadoresSlaDTO;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndicadoresSlaService {

    /** Meta fixa Sprint 308 — 60 min para todos. */
    public static final int META_SLA_MINUTOS = 60;

    private static final String[] ORDEM_PRIORIDADE = {
            "CRITICA", "ALTA", "MEDIA", "BAIXA", "SEM_PRIORIDADE"
    };

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public IndicadoresSlaDTO obter(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }

        List<Ticket> tickets = ticketRepository.findByDataAberturaPeriodo(inicio, fim);
        List<Ticket> encerrados = tickets.stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus())
                        && t.getDataEncerramento() != null)
                .toList();

        Map<String, List<Ticket>> porPrioridade = new LinkedHashMap<>();
        for (String p : ORDEM_PRIORIDADE) {
            porPrioridade.put(p, new ArrayList<>());
        }
        for (Ticket t : encerrados) {
            PrioridadeTicket p = t.getPrioridade();
            String key = p != null ? p.name() : "SEM_PRIORIDADE";
            porPrioridade.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        long totalCumpridos = 0;
        List<IndicadorSlaPrioridadeItemDTO> lista = new ArrayList<>();
        for (Map.Entry<String, List<Ticket>> entry : porPrioridade.entrySet()) {
            List<Ticket> grupo = entry.getValue();
            if (grupo.isEmpty()) {
                continue;
            }
            long cumpridos = grupo.stream().filter(t -> Boolean.TRUE.equals(t.getSlaResolucaoCumprido())).count();
            long naoAvaliados = grupo.stream().filter(t -> t.getSlaResolucaoCumprido() == null).count();
            long avaliados = grupo.size() - naoAvaliados;
            double pct = avaliados > 0 ? (double) cumpridos / avaliados * 100.0 : -1.0;

            double tma = grupo.stream()
                    .filter(t -> t.getTmaMinutosUteis() != null)
                    .mapToInt(Ticket::getTmaMinutosUteis)
                    .average().orElse(0.0);

            lista.add(new IndicadorSlaPrioridadeItemDTO(
                    entry.getKey(), grupo.size(), cumpridos, naoAvaliados,
                    pct < 0 ? null : roundOne(pct),
                    roundOne(tma)));

            totalCumpridos += cumpridos;
        }

        long totalAvaliados = encerrados.stream().filter(t -> t.getSlaResolucaoCumprido() != null).count();
        double pctGeral = totalAvaliados > 0 ? (double) totalCumpridos / totalAvaliados * 100.0 : -1.0;

        IndicadoresSlaDTO dto = new IndicadoresSlaDTO();
        dto.setTotalTicketsEncerrados(encerrados.size());
        dto.setTotalCumpridos(totalCumpridos);
        dto.setPercentualGeralCumprimento(pctGeral < 0 ? null : roundOne(pctGeral));
        dto.setMetaMinutos(META_SLA_MINUTOS);
        dto.setPorPrioridade(lista);
        return dto;
    }

    private static Double roundOne(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
