package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorAtendenteItemDTO;
import com.suporte.tickets.dto.IndicadoresAtendentesDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndicadoresAtendentesService {

    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @Transactional(readOnly = true)
    public IndicadoresAtendentesDTO obter(LocalDate dataInicio, LocalDate dataFim, Analista executor) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }

        List<Ticket> tickets = ticketRepository.findByDataAberturaPeriodo(inicio, fim);
        List<Ticket> validos = tickets.stream()
                .filter(t -> TicketAtivoService.isAtendimentoOperacionalValido(t.getStatus()))
                .toList();

        PerfilAcesso perfil = AnalistaService.resolverPerfilAcesso(executor);
        boolean somenteProprioAnalista = perfil == PerfilAcesso.ANALISTA;

        Map<Long, List<Ticket>> porAnalista = new LinkedHashMap<>();
        for (Ticket t : validos) {
            Analista a = t.getAnalistaResponsavel();
            if (a == null || a.getId() == null) {
                continue;
            }
            if (somenteProprioAnalista && !a.getId().equals(executor.getId())) {
                continue;
            }
            porAnalista.computeIfAbsent(a.getId(), k -> new ArrayList<>()).add(t);
        }

        Set<Integer> ticketIds = validos.stream().map(Ticket::getId).collect(Collectors.toSet());
        Map<Integer, Integer> notaPorTicketId = ticketSatisfacaoRepository.findByTicket_IdIn(ticketIds)
                .stream()
                .filter(s -> s.getNota() != null)
                .collect(Collectors.toMap(s -> s.getTicket().getId(), TicketSatisfacao::getNota));

        List<IndicadorAtendenteItemDTO> lista = new ArrayList<>();
        for (Map.Entry<Long, List<Ticket>> entry : porAnalista.entrySet()) {
            List<Ticket> grupo = entry.getValue();
            Analista a = grupo.get(0).getAnalistaResponsavel();
            String nome = a.getNome() != null && !a.getNome().isBlank() ? a.getNome().trim() : "Analista " + a.getId();

            double tma = grupo.stream()
                    .filter(t -> t.getTmaMinutosUteis() != null)
                    .mapToInt(Ticket::getTmaMinutosUteis)
                    .average().orElse(0.0);

            long comSla = grupo.stream().filter(t -> t.getSlaResolucaoCumprido() != null).count();
            long cumpridos = grupo.stream().filter(t -> Boolean.TRUE.equals(t.getSlaResolucaoCumprido())).count();
            double pctSla = comSla > 0 ? (double) cumpridos / comSla * 100.0 : -1.0;

            List<Integer> notas = grupo.stream()
                    .map(t -> notaPorTicketId.get(t.getId()))
                    .filter(n -> n != null)
                    .toList();
            double mediaAvaliacao = notas.isEmpty() ? -1.0
                    : notas.stream().mapToInt(Integer::intValue).average().orElse(0.0);

            lista.add(new IndicadorAtendenteItemDTO(
                    a.getId(), nome, grupo.size(),
                    roundOne(tma),
                    pctSla < 0 ? null : roundOne(pctSla),
                    mediaAvaliacao < 0 ? null : roundOne(mediaAvaliacao),
                    notas.size()));
        }

        lista.sort(Comparator.comparingLong(IndicadorAtendenteItemDTO::getTotalTickets).reversed());

        IndicadoresAtendentesDTO dto = new IndicadoresAtendentesDTO();
        dto.setTotalTickets(validos.size());
        dto.setAtendentes(lista);
        return dto;
    }

    private static Double roundOne(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
