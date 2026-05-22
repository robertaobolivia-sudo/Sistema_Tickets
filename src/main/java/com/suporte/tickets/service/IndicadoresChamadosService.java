package com.suporte.tickets.service;

import com.suporte.tickets.dto.IndicadorContagemDTO;
import com.suporte.tickets.dto.IndicadoresChamadosDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndicadoresChamadosService {

    private static final int TOP_RANKING = 10;
    private static final int TOP_CATEGORIAS = 5;

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public IndicadoresChamadosDTO obterChamados(LocalDate dataInicio, LocalDate dataFim, String classificacaoCliente) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }

        List<Ticket> tickets = ticketRepository.findByDataAberturaPeriodo(inicio, fim);
        // classificacaoCliente: parâmetro legado ignorado (Sprint 130 — etiquetas flexíveis no futuro).

        IndicadoresChamadosDTO dto = new IndicadoresChamadosDTO();
        dto.setTotalChamados(tickets.size());
        dto.setTotalClientes(contarClientesDistintos(tickets));
        dto.setChamadosPorAtendente(agruparPorAtendente(tickets));
        dto.setChamadosPorGrupo(agruparTopGrupos(tickets));
        dto.setChamadosPorSubgrupo(agruparTopSubgrupos(tickets));
        dto.setChamadosPorPrioridade(agruparPorPrioridade(tickets));
        dto.setChamadosPorStatus(agruparPorStatus(tickets));
        dto.setChamadosPorClassificacaoCliente(List.of());
        return dto;
    }

    private static long contarClientesDistintos(List<Ticket> tickets) {
        Set<Integer> ids = new HashSet<>();
        for (Ticket ticket : tickets) {
            if (ticket.getCliente() != null && ticket.getCliente().getId() != null) {
                ids.add(ticket.getCliente().getId());
            }
        }
        return ids.size();
    }

    private static List<IndicadorContagemDTO> agruparPorAtendente(List<Ticket> tickets) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Ticket ticket : tickets) {
            Analista analista = ticket.getAnalistaResponsavel();
            String nome = analista != null && analista.getNome() != null && !analista.getNome().isBlank()
                    ? analista.getNome().trim()
                    : "Sem atendente";
            mapa.merge(nome, 1L, Long::sum);
        }
        return ordenarTop(mapa, TOP_RANKING);
    }

    private static List<IndicadorContagemDTO> agruparTopGrupos(List<Ticket> tickets) {
        Map<String, Long> mapa = tickets.stream()
                .filter(t -> t.getGrupoCategoria() != null
                        && t.getGrupoCategoria().getNome() != null
                        && !t.getGrupoCategoria().getNome().isBlank())
                .collect(Collectors.groupingBy(
                        t -> t.getGrupoCategoria().getNome().trim(),
                        Collectors.counting()));
        return ordenarTop(mapa, TOP_CATEGORIAS);
    }

    private static List<IndicadorContagemDTO> agruparTopSubgrupos(List<Ticket> tickets) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Ticket ticket : tickets) {
            if (ticket.getSubgrupoCategoria() == null
                    || ticket.getSubgrupoCategoria().getNome() == null
                    || ticket.getSubgrupoCategoria().getNome().isBlank()) {
                continue;
            }
            String sub = ticket.getSubgrupoCategoria().getNome().trim();
            String grupo = ticket.getGrupoCategoria() != null
                    && ticket.getGrupoCategoria().getNome() != null
                    && !ticket.getGrupoCategoria().getNome().isBlank()
                    ? ticket.getGrupoCategoria().getNome().trim()
                    : "-";
            String rotulo = sub + " (" + grupo + ")";
            mapa.merge(rotulo, 1L, Long::sum);
        }
        return ordenarTop(mapa, TOP_CATEGORIAS);
    }

    private static List<IndicadorContagemDTO> agruparPorPrioridade(List<Ticket> tickets) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        mapa.put("CRITICA", 0L);
        mapa.put("ALTA", 0L);
        mapa.put("MEDIA", 0L);
        mapa.put("BAIXA", 0L);
        mapa.put("SEM_PRIORIDADE", 0L);
        for (Ticket ticket : tickets) {
            PrioridadeTicket p = ticket.getPrioridade();
            if (p == null) {
                mapa.merge("SEM_PRIORIDADE", 1L, Long::sum);
            } else {
                mapa.merge(p.name(), 1L, Long::sum);
            }
        }
        List<IndicadorContagemDTO> lista = new ArrayList<>();
        for (Map.Entry<String, Long> e : mapa.entrySet()) {
            if (e.getValue() > 0) {
                lista.add(new IndicadorContagemDTO(e.getKey(), e.getValue()));
            }
        }
        return lista;
    }

    private static List<IndicadorContagemDTO> agruparPorStatus(List<Ticket> tickets) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Ticket ticket : tickets) {
            String status = ticket.getStatus() != null ? ticket.getStatus().name() : "SEM_STATUS";
            mapa.merge(status, 1L, Long::sum);
        }
        return mapa.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new IndicadorContagemDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private static List<IndicadorContagemDTO> ordenarTop(Map<String, Long> mapa, int limite) {
        return mapa.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limite)
                .map(e -> new IndicadorContagemDTO(e.getKey(), e.getValue()))
                .toList();
    }
}
