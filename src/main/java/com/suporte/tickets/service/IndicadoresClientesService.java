package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorClienteItemDTO;
import com.suporte.tickets.dto.IndicadoresClientesDTO;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndicadoresClientesService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public IndicadoresClientesDTO obter(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }

        List<Ticket> tickets = ticketRepository.findByDataAberturaPeriodo(inicio, fim);
        List<Ticket> validos = tickets.stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus()))
                .toList();

        Map<Integer, List<Ticket>> porCliente = new LinkedHashMap<>();
        for (Ticket t : validos) {
            Cliente c = t.getCliente();
            if (c != null && c.getId() != null) {
                porCliente.computeIfAbsent(c.getId(), k -> new ArrayList<>()).add(t);
            }
        }

        List<IndicadorClienteItemDTO> ranking = new ArrayList<>();
        for (Map.Entry<Integer, List<Ticket>> entry : porCliente.entrySet()) {
            List<Ticket> grupo = entry.getValue();
            Cliente c = grupo.get(0).getCliente();
            String nome = c.getNome() != null && !c.getNome().isBlank() ? c.getNome().trim() : "Cliente " + c.getId();

            double tme = grupo.stream()
                    .filter(t -> t.getTmeMinutosUteis() != null)
                    .mapToInt(Ticket::getTmeMinutosUteis)
                    .average().orElse(0.0);

            double tma = grupo.stream()
                    .filter(t -> t.getTmaMinutosUteis() != null)
                    .mapToInt(Ticket::getTmaMinutosUteis)
                    .average().orElse(0.0);

            long comSla = grupo.stream().filter(t -> t.getSlaResolucaoCumprido() != null).count();
            long cumpridos = grupo.stream().filter(t -> Boolean.TRUE.equals(t.getSlaResolucaoCumprido())).count();
            double pct = comSla > 0 ? (double) cumpridos / comSla * 100.0 : -1.0;

            ranking.add(new IndicadorClienteItemDTO(
                    c.getId(), nome, grupo.size(),
                    roundOne(tme), roundOne(tma),
                    pct < 0 ? null : roundOne(pct)));
        }

        ranking.sort(Comparator.comparingLong(IndicadorClienteItemDTO::getTotalTickets).reversed());

        IndicadoresClientesDTO dto = new IndicadoresClientesDTO();
        dto.setTotalTickets(validos.size());
        dto.setTotalClientes(porCliente.size());
        dto.setRankingClientes(ranking);
        return dto;
    }

    private static Double roundOne(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
