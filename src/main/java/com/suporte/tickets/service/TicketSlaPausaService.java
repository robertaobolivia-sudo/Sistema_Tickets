package com.suporte.tickets.service;

import com.suporte.tickets.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketSlaPausaService {

    private final SlaTempoUtilService slaTempoUtilService;

    public void iniciarPausa(Ticket ticket) {
        if (ticket == null || Boolean.TRUE.equals(ticket.getSlaPausado())) {
            return;
        }
        ticket.setSlaPausado(true);
        ticket.setSlaPausaInicio(agoraNoFusoSla());
    }

    public void finalizarPausa(Ticket ticket) {
        if (ticket == null || !Boolean.TRUE.equals(ticket.getSlaPausado())) {
            return;
        }
        LocalDateTime inicioPausa = ticket.getSlaPausaInicio();
        LocalDateTime fimPausa = agoraNoFusoSla();
        if (inicioPausa != null) {
            long minutosPausados = slaTempoUtilService.calcularMinutosUteisEntre(inicioPausa, fimPausa);
            if (minutosPausados > 0) {
                long acumulado = ticket.getSlaResolucaoMinutosPausados() != null
                        ? ticket.getSlaResolucaoMinutosPausados()
                        : 0L;
                ticket.setSlaResolucaoMinutosPausados(acumulado + minutosPausados);
                if (ticket.getSlaResolucaoVencimento() != null) {
                    LocalDateTime vencimentoEstendido = slaTempoUtilService.adicionarMinutosUteis(
                            ticket.getSlaResolucaoVencimento(),
                            minutosPausados
                    );
                    ticket.setSlaResolucaoVencimento(vencimentoEstendido);
                }
            }
        }
        ticket.setSlaPausaInicio(null);
        ticket.setSlaPausado(false);
    }

    public void finalizarPausaSeNecessario(Ticket ticket) {
        if (ticket != null && Boolean.TRUE.equals(ticket.getSlaPausado())) {
            finalizarPausa(ticket);
        }
    }

    private LocalDateTime agoraNoFusoSla() {
        return LocalDateTime.now(CalendarioSlaHelper.FUSO_SLA);
    }
}
