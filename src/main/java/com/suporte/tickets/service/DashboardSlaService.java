package com.suporte.tickets.service;

import com.suporte.tickets.dto.DashboardSlaBlocoDTO;
import com.suporte.tickets.dto.DashboardSlaDTO;
import com.suporte.tickets.dto.DashboardSlaPrioridadeDTO;
import com.suporte.tickets.dto.DashboardSlaTicketCriticoDTO;
import com.suporte.tickets.dto.DashboardSlaVivoResumoDTO;
import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardSlaService {

    private static final int LIMITE_TICKETS_CRITICOS = 25;

    private final TicketRepository ticketRepository;
    private final TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    private final TicketSlaResolucaoService ticketSlaResolucaoService;

    @Transactional(readOnly = true)
    public DashboardSlaDTO obterResumoSla() {
        List<Ticket> tickets = ticketRepository.findAll();
        DashboardSlaDTO dto = new DashboardSlaDTO();
        DashboardSlaBlocoDTO primeiro = new DashboardSlaBlocoDTO();
        DashboardSlaBlocoDTO resolucao = new DashboardSlaBlocoDTO();

        Map<PrioridadeTicket, DashboardSlaPrioridadeDTO> porPrioridadeMap = new EnumMap<>(PrioridadeTicket.class);
        for (PrioridadeTicket prioridade : PrioridadeTicket.values()) {
            porPrioridadeMap.put(prioridade, novoResumoPrioridade(prioridade.name()));
        }

        List<DashboardSlaTicketCriticoDTO> criticos = new ArrayList<>();
        long ticketsEscalonados = 0;
        long vivoDentro = 0;
        long vivoProximo = 0;
        long vivoVencido = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.INDEVIDO) {
                continue;
            }
            if (!TicketAtivoService.isStatusAtivo(ticket.getStatus())) {
                continue;
            }
            if (Boolean.TRUE.equals(ticket.getEscalonado())) {
                ticketsEscalonados++;
            }
            SlaStatus statusPrimeiro = ticketSlaPrimeiroAtendimentoService.calcularStatusPrimeiroAtendimento(ticket);
            SlaStatus statusResolucao = ticketSlaResolucaoService.calcularStatusResolucao(ticket);

            incrementarBloco(primeiro, statusPrimeiro);
            incrementarBloco(resolucao, statusResolucao);

            PrioridadeTicket prioridade = ticket.getPrioridade() != null ? ticket.getPrioridade() : PrioridadeTicket.MEDIA;
            DashboardSlaPrioridadeDTO resumoPrioridade = porPrioridadeMap.get(prioridade);
            resumoPrioridade.setTotal(resumoPrioridade.getTotal() + 1);
            if (statusPrimeiro == SlaStatus.VIOLADO) {
                resumoPrioridade.setSlaPrimeiroViolado(resumoPrioridade.getSlaPrimeiroViolado() + 1);
            }
            if (statusResolucao == SlaStatus.VIOLADO) {
                resumoPrioridade.setSlaResolucaoViolado(resumoPrioridade.getSlaResolucaoViolado() + 1);
            }
            if (statusResolucao == SlaStatus.VENCIDO) {
                resumoPrioridade.setSlaResolucaoVencido(resumoPrioridade.getSlaResolucaoVencido() + 1);
            }

            if (isTicketCriticoSla(ticket, statusPrimeiro, statusResolucao)) {
                criticos.add(mapearTicketCritico(ticket, statusPrimeiro, statusResolucao));
            }

            int pressao = piorPressaoSla(statusPrimeiro, statusResolucao);
            if (pressao >= 3) {
                vivoVencido++;
            } else if (pressao >= 2) {
                vivoProximo++;
            } else {
                vivoDentro++;
            }
        }

        criticos.sort(Comparator
                .comparingInt(this::rankTicketCriticoEscalonamento)
                .thenComparingInt((DashboardSlaTicketCriticoDTO t) -> rankPrioridade(t.getPrioridade()))
                .thenComparing(t -> t.getVencimentoMaisCritico() == null ? LocalDateTime.MAX : t.getVencimentoMaisCritico()));

        if (criticos.size() > LIMITE_TICKETS_CRITICOS) {
            criticos = criticos.subList(0, LIMITE_TICKETS_CRITICOS);
        }

        dto.setPrimeiroAtendimento(primeiro);
        dto.setResolucao(resolucao);
        dto.setPorPrioridade(List.of(
                porPrioridadeMap.get(PrioridadeTicket.CRITICA),
                porPrioridadeMap.get(PrioridadeTicket.ALTA),
                porPrioridadeMap.get(PrioridadeTicket.MEDIA),
                porPrioridadeMap.get(PrioridadeTicket.BAIXA)
        ));
        dto.setTicketsCriticosSla(criticos);
        dto.setTicketsEscalonados(ticketsEscalonados);

        DashboardSlaVivoResumoDTO vivo = new DashboardSlaVivoResumoDTO();
        vivo.setDentroDoPrazo(vivoDentro);
        vivo.setProximosDoLimite(vivoProximo);
        vivo.setVencidos(vivoVencido);
        vivo.setTicketMaisCritico(criticos.isEmpty() ? null : criticos.get(0));
        dto.setVivo(vivo);
        return dto;
    }

    private static int piorPressaoSla(SlaStatus primeiro, SlaStatus resolucao) {
        return Math.max(rankPressao(primeiro), rankPressao(resolucao));
    }

    private static int rankPressao(SlaStatus status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case VENCIDO, VIOLADO -> 3;
            case PROXIMO_DO_VENCIMENTO, PAUSADO -> 2;
            case DENTRO_DO_PRAZO -> 1;
            default -> 0;
        };
    }

    private int rankTicketCriticoEscalonamento(DashboardSlaTicketCriticoDTO ticket) {
        if (Boolean.TRUE.equals(ticket.getEscalonado())) {
            return 3;
        }
        String resolucao = ticket.getSlaResolucaoStatus();
        String primeiro = ticket.getSlaPrimeiroAtendimentoStatus();
        if (isStatusCriticoAlto(resolucao) || isStatusCriticoAlto(primeiro)) {
            return 0;
        }
        if ("PROXIMO_DO_VENCIMENTO".equals(resolucao) || "PROXIMO_DO_VENCIMENTO".equals(primeiro)) {
            return 1;
        }
        if ("PAUSADO".equals(resolucao)) {
            return 2;
        }
        return 4;
    }

    private boolean isStatusCriticoAlto(String status) {
        return "VENCIDO".equals(status) || "VIOLADO".equals(status);
    }

    private boolean isTicketCriticoSla(
            Ticket ticket,
            SlaStatus statusPrimeiro,
            SlaStatus statusResolucao) {
        if (statusResolucao == SlaStatus.PAUSADO) {
            return true;
        }
        if (statusPrimeiro == SlaStatus.VENCIDO || statusResolucao == SlaStatus.VENCIDO) {
            return true;
        }
        if (statusPrimeiro == SlaStatus.PROXIMO_DO_VENCIMENTO || statusResolucao == SlaStatus.PROXIMO_DO_VENCIMENTO) {
            return true;
        }
        return false;
    }

    private DashboardSlaTicketCriticoDTO mapearTicketCritico(
            Ticket ticket,
            SlaStatus statusPrimeiro,
            SlaStatus statusResolucao) {
        LocalDateTime vencimentoCritico = menorVencimento(
                ticket.getSlaPrimeiroAtendimentoVencimento(),
                ticket.getSlaResolucaoVencimento());
        String cliente = ticket.getCliente() != null ? ticket.getCliente().getNome() : "-";
        return new DashboardSlaTicketCriticoDTO(
                ticket.getNumeroTicket(),
                cliente,
                ticket.getPrioridade() != null ? ticket.getPrioridade().name() : "MEDIA",
                ticket.getStatus() != null ? ticket.getStatus().name() : "-",
                statusPrimeiro.name(),
                statusResolucao.name(),
                ticket.getSlaPrimeiroAtendimentoVencimento(),
                ticket.getSlaResolucaoVencimento(),
                vencimentoCritico,
                Boolean.TRUE.equals(ticket.getSlaPausado()),
                Boolean.TRUE.equals(ticket.getEscalonado())
        );
    }

    private LocalDateTime menorVencimento(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }

    private void incrementarBloco(DashboardSlaBlocoDTO bloco, SlaStatus status) {
        switch (status) {
            case DENTRO_DO_PRAZO -> bloco.setDentroDoPrazo(bloco.getDentroDoPrazo() + 1);
            case PROXIMO_DO_VENCIMENTO -> bloco.setProximoVencimento(bloco.getProximoVencimento() + 1);
            case VENCIDO -> bloco.setVencido(bloco.getVencido() + 1);
            case PAUSADO -> bloco.setPausado(bloco.getPausado() + 1);
            case CUMPRIDO -> bloco.setCumprido(bloco.getCumprido() + 1);
            case VIOLADO -> bloco.setViolado(bloco.getViolado() + 1);
            default -> bloco.setNaoCalculado(bloco.getNaoCalculado() + 1);
        }
    }

    private DashboardSlaPrioridadeDTO novoResumoPrioridade(String nome) {
        return new DashboardSlaPrioridadeDTO(nome, 0, 0, 0, 0);
    }

    private int rankPrioridade(String prioridade) {
        if (prioridade == null) {
            return 9;
        }
        return switch (prioridade) {
            case "CRITICA" -> 0;
            case "ALTA" -> 1;
            case "MEDIA" -> 2;
            case "BAIXA" -> 3;
            default -> 9;
        };
    }
}
