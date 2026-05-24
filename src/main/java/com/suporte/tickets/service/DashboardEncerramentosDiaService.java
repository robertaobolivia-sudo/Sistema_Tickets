package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardEncerramentosDiaDTO;
import com.suporte.tickets.dto.DashboardRecorrenciaDiaDTO;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprint 284 — encerramentos do dia corrente (operacional, sem INDEVIDO).
 */
@Service
@RequiredArgsConstructor
public class DashboardEncerramentosDiaService {

    private static final int TOP_RECORRENCIAS = 5;
    private static final ZoneId FUSO = CalendarioSlaHelper.FUSO_SLA;

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public DashboardEncerramentosDiaDTO obter() {
        LocalDate hoje = LocalDate.now(FUSO);
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fimExclusivo = hoje.plusDays(1).atStartOfDay();

        List<Ticket> encerrados =
                ticketRepository.findEncerradosOperacionaisNoPeriodo(inicio, fimExclusivo, TicketStatus.INDEVIDO);

        long finalizados = 0;
        long naoResolvidos = 0;
        long escalonados = 0;
        long abandonados = 0;
        Map<String, Long> recorrenciaPorRotulo = new LinkedHashMap<>();

        for (Ticket ticket : encerrados) {
            TicketStatus status = ticket.getStatus();
            if (status == TicketStatus.RESOLVIDO) {
                finalizados++;
            } else if (status == TicketStatus.CANCELADO) {
                naoResolvidos++;
            } else {
                continue;
            }

            if (Boolean.TRUE.equals(ticket.getEscalonado())) {
                escalonados++;
            }
            if (ticket.getDataPrimeiroAtendimento() == null) {
                abandonados++;
            }

            String rotulo = rotuloRecorrencia(ticket);
            recorrenciaPorRotulo.merge(rotulo, 1L, Long::sum);
        }

        List<DashboardRecorrenciaDiaDTO> recorrencias = recorrenciaPorRotulo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_RECORRENCIAS)
                .map(e -> new DashboardRecorrenciaDiaDTO(e.getKey(), e.getValue()))
                .toList();

        DashboardEncerramentosDiaDTO dto = new DashboardEncerramentosDiaDTO();
        dto.setFinalizados(finalizados);
        dto.setNaoResolvidos(naoResolvidos);
        dto.setEscalonados(escalonados);
        dto.setAbandonados(abandonados);
        dto.setRecorrencias(new ArrayList<>(recorrencias));
        return dto;
    }

    static String rotuloRecorrencia(Ticket ticket) {
        Motivo motivo = ticket.getMotivo();
        if (motivo != null && motivo.getNome() != null && !motivo.getNome().isBlank()) {
            return motivo.getNome().trim();
        }
        SubgrupoCategoria sub = ticket.getSubgrupoCategoria();
        if (sub != null && sub.getNome() != null && !sub.getNome().isBlank()) {
            return sub.getNome().trim();
        }
        return "Sem motivo";
    }
}
