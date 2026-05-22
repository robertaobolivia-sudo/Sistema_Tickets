package com.suporte.tickets.service;

import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketEtiqueta;
import com.suporte.tickets.repository.EtiquetaRepository;
import com.suporte.tickets.repository.TicketEtiquetaRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketEtiquetaService {

    private static final int MAX_ETIQUETAS_POR_TICKET = 50;

    private final TicketRepository ticketRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final TicketEtiquetaRepository ticketEtiquetaRepository;

    @Transactional(readOnly = true)
    public List<EtiquetaResponseDTO> listarPorNumeroTicket(String numeroTicket) {
        Ticket ticket = buscarTicket(numeroTicket);
        return ticketEtiquetaRepository.findByTicketOrderByEtiqueta_NomeAsc(ticket)
                .stream()
                .map(TicketEtiqueta::getEtiqueta)
                .map(EtiquetaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EtiquetaResponseDTO> substituirVinculosAtivos(String numeroTicket, List<Long> etiquetaIds) {
        Ticket ticket = buscarTicket(numeroTicket);
        List<Long> idsAtivosSolicitados = normalizarIdsEtiqueta(etiquetaIds);

        List<TicketEtiqueta> atuais = ticketEtiquetaRepository.findByTicketOrderByEtiqueta_NomeAsc(ticket);
        Set<Long> legadoInativos = atuais.stream()
                .filter(v -> v.getEtiqueta() != null && Boolean.FALSE.equals(v.getEtiqueta().getAtivo()))
                .map(v -> v.getEtiqueta().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        ticketEtiquetaRepository.deleteByTicket(ticket);
        ticketEtiquetaRepository.flush();

        Set<Long> idsFinais = new LinkedHashSet<>(legadoInativos);
        for (Long id : idsAtivosSolicitados) {
            Etiqueta etiqueta = etiquetaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Etiqueta nao encontrada: " + id));
            if (!Boolean.TRUE.equals(etiqueta.getAtivo())) {
                throw new IllegalArgumentException("Etiqueta inativa nao pode ser vinculada: " + id);
            }
            idsFinais.add(id);
        }

        for (Long id : idsFinais) {
            Etiqueta etiqueta = etiquetaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Etiqueta nao encontrada: " + id));
            TicketEtiqueta vinculo = new TicketEtiqueta();
            vinculo.setTicket(ticket);
            vinculo.setEtiqueta(etiqueta);
            ticketEtiquetaRepository.save(vinculo);
        }

        return listarPorNumeroTicket(numeroTicket);
    }

    static List<Long> normalizarIdsEtiqueta(List<Long> etiquetaIds) {
        if (etiquetaIds == null || etiquetaIds.isEmpty()) {
            return List.of();
        }
        Set<Long> unicos = new LinkedHashSet<>();
        for (Long id : etiquetaIds) {
            if (id == null) {
                continue;
            }
            unicos.add(id);
        }
        List<Long> lista = new ArrayList<>(unicos);
        if (lista.size() > MAX_ETIQUETAS_POR_TICKET) {
            throw new IllegalArgumentException(
                    "Um ticket pode ter no maximo " + MAX_ETIQUETAS_POR_TICKET + " etiquetas ativas.");
        }
        return lista;
    }

    private Ticket buscarTicket(String numeroTicket) {
        return ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
    }
}
