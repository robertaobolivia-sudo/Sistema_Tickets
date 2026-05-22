package com.suporte.tickets.service;

import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.SlaMeta;
import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketSlaResolucaoService {

    private final SlaMetaService slaMetaService;
    private final SlaTempoUtilService slaTempoUtilService;
    private final SlaVisualStatusHelper slaVisualStatusHelper;

    public void aplicarCalculoNaCriacao(Ticket ticket) {
        if (ticket == null || ticket.getDataAbertura() == null) {
            return;
        }
        try {
            PrioridadeTicket prioridade = resolverPrioridade(ticket.getPrioridade());
            SlaMeta meta = slaMetaService.buscarMetaAtivaPorPrioridade(prioridade);
            long minutos = meta.getPrazoResolucaoMinutos();
            LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(ticket.getDataAbertura(), minutos);
            ticket.setSlaResolucaoVencimento(vencimento);
            ticket.setSlaResolucaoCalculadoEm(LocalDateTime.now());
            ticket.setSlaResolucaoCumprido(null);
        } catch (Exception ex) {
            ticket.setSlaResolucaoVencimento(null);
            ticket.setSlaResolucaoCalculadoEm(null);
            ticket.setSlaResolucaoCumprido(null);
        }
    }

    public void avaliarResolucao(Ticket ticket, LocalDateTime dataEncerramento) {
        if (ticket == null || dataEncerramento == null) {
            return;
        }
        if (ticket.getSlaResolucaoVencimento() == null) {
            return;
        }
        boolean cumprido = !dataEncerramento.isAfter(ticket.getSlaResolucaoVencimento());
        ticket.setSlaResolucaoCumprido(cumprido);
    }

    public SlaStatus calcularStatusResolucao(Ticket ticket) {
        if (ticket == null || ticket.getSlaResolucaoVencimento() == null) {
            return SlaStatus.NAO_CALCULADO;
        }
        if (isTicketFinalizado(ticket)) {
            if (Boolean.TRUE.equals(ticket.getSlaResolucaoCumprido())) {
                return SlaStatus.CUMPRIDO;
            }
            if (Boolean.FALSE.equals(ticket.getSlaResolucaoCumprido())) {
                return SlaStatus.VIOLADO;
            }
            if (ticket.getDataEncerramento() != null) {
                boolean cumprido = !ticket.getDataEncerramento().isAfter(ticket.getSlaResolucaoVencimento());
                return cumprido ? SlaStatus.CUMPRIDO : SlaStatus.VIOLADO;
            }
            return SlaStatus.NAO_CALCULADO;
        }
        if (Boolean.TRUE.equals(ticket.getSlaPausado())) {
            return SlaStatus.PAUSADO;
        }
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        if (agora.isAfter(ticket.getSlaResolucaoVencimento())) {
            return SlaStatus.VENCIDO;
        }
        if (slaVisualStatusHelper.isProximoVencimento(ticket.getSlaResolucaoVencimento())) {
            return SlaStatus.PROXIMO_DO_VENCIMENTO;
        }
        return SlaStatus.DENTRO_DO_PRAZO;
    }

    public String calcularStatusLabel(Ticket ticket) {
        return calcularStatusResolucao(ticket).name();
    }

    private boolean isTicketFinalizado(Ticket ticket) {
        return ticket.getStatus() == TicketStatus.RESOLVIDO
                || ticket.getStatus() == TicketStatus.CANCELADO;
    }

    private PrioridadeTicket resolverPrioridade(PrioridadeTicket prioridade) {
        return prioridade != null ? prioridade : PrioridadeTicket.MEDIA;
    }
}
