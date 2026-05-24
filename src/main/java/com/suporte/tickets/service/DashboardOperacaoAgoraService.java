package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardOperacaoAgoraDTO;
import com.suporte.tickets.dto.DashboardOperacaoCardDTO;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 280 — métricas operacionais em tempo real (Operação Agora).
 */
@Service
@RequiredArgsConstructor
public class DashboardOperacaoAgoraService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public DashboardOperacaoAgoraDTO obter() {
        List<Ticket> emAtendimento = ticketRepository.findByStatusOrderByDataAberturaDesc(TicketStatus.EM_ATENDIMENTO);
        List<Ticket> emFila = ticketRepository.findByStatusOrderByDataAberturaDesc(TicketStatus.ABERTO);

        DashboardOperacaoAgoraDTO dto = new DashboardOperacaoAgoraDTO();
        dto.setEmAtendimento(montarCardAtendimento(emAtendimento));
        dto.setEmFila(montarCardFila(emFila));
        return dto;
    }

    private static DashboardOperacaoCardDTO montarCardAtendimento(List<Ticket> tickets) {
        LocalDateTime agora = LocalDateTime.now();
        List<Long> segundos = new ArrayList<>();
        for (Ticket t : tickets) {
            LocalDateTime inicio = t.getDataPrimeiroAtendimento() != null
                    ? t.getDataPrimeiroAtendimento()
                    : t.getDataAbertura();
            if (inicio == null) {
                continue;
            }
            long s = Duration.between(inicio, agora).getSeconds();
            if (s >= 0) {
                segundos.add(s);
            }
        }
        return new DashboardOperacaoCardDTO(
                tickets.size(),
                formatarMedia(segundos),
                "TMA");
    }

    private static DashboardOperacaoCardDTO montarCardFila(List<Ticket> tickets) {
        LocalDateTime agora = LocalDateTime.now();
        List<Long> segundos = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getDataAbertura() == null) {
                continue;
            }
            long s = Duration.between(t.getDataAbertura(), agora).getSeconds();
            if (s >= 0) {
                segundos.add(s);
            }
        }
        return new DashboardOperacaoCardDTO(
                tickets.size(),
                formatarMedia(segundos),
                "TME");
    }

    static String formatarMedia(List<Long> segundosLista) {
        if (segundosLista == null || segundosLista.isEmpty()) {
            return "-";
        }
        long media = Math.round(segundosLista.stream().mapToLong(Long::longValue).average().orElse(0));
        Duration d = Duration.ofSeconds(media);
        return String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }
}
