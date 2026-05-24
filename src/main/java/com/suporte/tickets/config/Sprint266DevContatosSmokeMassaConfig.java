package com.suporte.tickets.config;

import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoStatus;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Massa mínima DEV para smoke Sprint 266 (avaliação ruim em contato 63 / Rocha).
 * Uso: {@code --app.sprint266.smoke-massa=true} na subida, depois desligar.
 */
@Component
@Order(257)
@ConditionalOnProperty(name = "app.sprint266.smoke-massa", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class Sprint266DevContatosSmokeMassaConfig implements ApplicationRunner {

    private static final int CONTATO_ID_SMOKE = 63;

    private final TicketRepository ticketRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Ticket> tickets =
                ticketRepository.findHistoricoByContatoIdOrderByDataAberturaDesc(CONTATO_ID_SMOKE);
        if (tickets.isEmpty()) {
            log.warn("Sprint 266 smoke-massa: sem ticket para contato {}", CONTATO_ID_SMOKE);
            return;
        }
        Ticket ticket = tickets.get(0);
        if (ticketSatisfacaoRepository.existsByTicket_Id(ticket.getId())) {
            ticketSatisfacaoRepository
                    .findByTicket_Id(ticket.getId())
                    .ifPresent(s -> {
                        s.setNota(2);
                        s.setStatus(TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE);
                        s.setRespondidaEm(LocalDateTime.now());
                        ticketSatisfacaoRepository.save(s);
                        log.info(
                                "Sprint 266 smoke-massa: satisfacao ticket {} atualizada nota=2",
                                ticket.getNumeroTicket());
                    });
            return;
        }
        TicketSatisfacao satisfacao = new TicketSatisfacao();
        satisfacao.setTicket(ticket);
        satisfacao.setStatus(TicketSatisfacaoStatus.REGISTRADA_MANUALMENTE);
        satisfacao.setNota(2);
        satisfacao.setRespondidaEm(LocalDateTime.now());
        satisfacao.setComentario("Smoke Sprint 266 avaliacao ruim");
        ticketSatisfacaoRepository.save(satisfacao);
        log.info(
                "Sprint 266 smoke-massa: satisfacao nota=2 criada para ticket {}",
                ticket.getNumeroTicket());
    }
}
