package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dev.seeds.enabled", havingValue = "true", matchIfMissing = true)
public class Sprint951LimpezaAtendentesSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(Sprint951LimpezaAtendentesSeedConfig.class);

    private final AnalistaRepository analistaRepository;
    private final TicketRepository ticketRepository;

    @Bean
    @Order(300)
    CommandLineRunner limpezaAtendentesOficiais() {
        return args -> {
            List<Analista> oficiais = resolverOficiaisAtivos();
            if (oficiais.size() != 4) {
                log.warn("Sprint 9.5.1: esperados 4 atendentes oficiais ativos, encontrados {}", oficiais.size());
            }

            Set<Long> idsOficiais = oficiais.stream().map(Analista::getId).collect(Collectors.toSet());
            int ticketsRemanejados = remanejarTicketsParaOficiais(oficiais, idsOficiais);
            int inativados = inativarAnalistasNaoOficiais(idsOficiais);

            log.info("Sprint 9.5.1: tickets remanejados={}, analistas inativados={}", ticketsRemanejados, inativados);
        };
    }

    private List<Analista> resolverOficiaisAtivos() {
        List<Analista> encontrados = new ArrayList<>();
        for (String email : AnalistasOficiaisConstants.EMAILS_OFICIAIS) {
            analistaRepository.findByEmailIgnoreCase(email).ifPresent(encontrados::add);
        }
        return encontrados;
    }

    private int remanejarTicketsParaOficiais(List<Analista> oficiais, Set<Long> idsOficiais) {
        if (oficiais.isEmpty()) {
            return 0;
        }
        List<Ticket> tickets = new ArrayList<>(ticketRepository.findAll());
        Collections.shuffle(tickets, new Random(9511));
        int remapeados = 0;
        int indice = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getAnalistaResponsavel() == null) {
                continue;
            }
            if (idsOficiais.contains(ticket.getAnalistaResponsavel().getId())) {
                continue;
            }
            ticket.setAnalistaResponsavel(oficiais.get(indice % oficiais.size()));
            ticketRepository.save(ticket);
            remapeados++;
            indice++;
        }
        return remapeados;
    }

    private int inativarAnalistasNaoOficiais(Set<Long> idsOficiais) {
        int count = 0;
        for (Analista analista : analistaRepository.findAll()) {
            if (idsOficiais.contains(analista.getId())) {
                if (!Boolean.TRUE.equals(analista.getAtivo())) {
                    analista.setAtivo(true);
                    analistaRepository.save(analista);
                }
                continue;
            }
            if (Boolean.FALSE.equals(analista.getAtivo())
                    && !Boolean.TRUE.equals(analista.getOnline())
                    && analista.getStatusOperador() == StatusOperador.OFFLINE) {
                continue;
            }
            analista.setAtivo(false);
            analista.setOnline(false);
            analista.setStatusOperador(StatusOperador.OFFLINE);
            analistaRepository.save(analista);
            count++;
        }
        return count;
    }
}
