package com.suporte.tickets.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Expira avaliações PENDENTE após {@code expiraEm} (Sprint 198).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketSatisfacaoExpiracaoScheduler {

    private final TicketSatisfacaoService ticketSatisfacaoService;

    @Scheduled(fixedRate = 1_800_000)
    public void expirarAvaliacoesPendentes() {
        int quantidade = ticketSatisfacaoService.marcarPendentesExpiradas();
        if (quantidade > 0) {
            log.info("Avaliacoes de satisfacao expiradas: {}", quantidade);
        }
    }
}
