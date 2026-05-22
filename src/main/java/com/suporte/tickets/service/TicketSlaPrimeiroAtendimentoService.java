package com.suporte.tickets.service;

import com.suporte.tickets.entity.PrioridadeTicket;
import com.suporte.tickets.entity.SlaMeta;
import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketSlaPrimeiroAtendimentoService {

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
            long minutos = meta.getPrazoPrimeiroAtendimentoMinutos();
            LocalDateTime vencimento = slaTempoUtilService.adicionarMinutosUteis(ticket.getDataAbertura(), minutos);
            ticket.setSlaPrimeiroAtendimentoVencimento(vencimento);
            ticket.setSlaPrimeiroAtendimentoCalculadoEm(LocalDateTime.now());
            ticket.setSlaPrimeiroAtendimentoCumprido(null);
        } catch (Exception ex) {
            ticket.setSlaPrimeiroAtendimentoVencimento(null);
            ticket.setSlaPrimeiroAtendimentoCalculadoEm(null);
            ticket.setSlaPrimeiroAtendimentoCumprido(null);
        }
    }

    public void avaliarPrimeiroAtendimento(Ticket ticket, LocalDateTime dataPrimeiroAtendimento) {
        if (ticket == null || dataPrimeiroAtendimento == null) {
            return;
        }
        if (ticket.getSlaPrimeiroAtendimentoVencimento() == null) {
            return;
        }
        boolean cumprido = !dataPrimeiroAtendimento.isAfter(ticket.getSlaPrimeiroAtendimentoVencimento());
        ticket.setSlaPrimeiroAtendimentoCumprido(cumprido);
    }

    public SlaStatus calcularStatusPrimeiroAtendimento(Ticket ticket) {
        if (ticket == null || ticket.getSlaPrimeiroAtendimentoVencimento() == null) {
            return SlaStatus.NAO_CALCULADO;
        }
        if (ticket.getDataPrimeiroAtendimento() != null) {
            if (Boolean.TRUE.equals(ticket.getSlaPrimeiroAtendimentoCumprido())) {
                return SlaStatus.CUMPRIDO;
            }
            if (Boolean.FALSE.equals(ticket.getSlaPrimeiroAtendimentoCumprido())) {
                return SlaStatus.VIOLADO;
            }
            boolean cumprido = !ticket.getDataPrimeiroAtendimento().isAfter(ticket.getSlaPrimeiroAtendimentoVencimento());
            return cumprido ? SlaStatus.CUMPRIDO : SlaStatus.VIOLADO;
        }
        LocalDateTime agora = LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
        if (agora.isAfter(ticket.getSlaPrimeiroAtendimentoVencimento())) {
            return SlaStatus.VENCIDO;
        }
        if (slaVisualStatusHelper.isProximoVencimento(ticket.getSlaPrimeiroAtendimentoVencimento())) {
            return SlaStatus.PROXIMO_DO_VENCIMENTO;
        }
        return SlaStatus.DENTRO_DO_PRAZO;
    }

    public String calcularStatusLabel(Ticket ticket) {
        return calcularStatusPrimeiroAtendimento(ticket).name();
    }

    private PrioridadeTicket resolverPrioridade(PrioridadeTicket prioridade) {
        return prioridade != null ? prioridade : PrioridadeTicket.MEDIA;
    }
}
