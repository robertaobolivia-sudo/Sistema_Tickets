package com.suporte.tickets.service;

import com.suporte.tickets.dto.ChatsHistoricoResumoDTO;
import com.suporte.tickets.dto.ChatsHistoricoTicketResumoDTO;
import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatsHistoricoResumoService {

    private static final int LIMITE_RECENTES = 3;
    private static final List<TicketStatus> STATUS_ENCERRADO =
            List.of(TicketStatus.RESOLVIDO, TicketStatus.CANCELADO);

    private final TicketRepository ticketRepository;

    public ChatsHistoricoResumoDTO buscarPorNumeroTicket(String numeroTicket) {
        Ticket atual = ticketRepository.findByNumeroTicket(numeroTicket)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + numeroTicket));
        if (atual.getCliente() == null || atual.getCliente().getId() == null) {
            return resumoVazio();
        }
        Integer clienteId = atual.getCliente().getId();
        String exclude = atual.getNumeroTicket();

        long total = ticketRepository.countByCliente_Id(clienteId);
        ChatsHistoricoResumoDTO dto = new ChatsHistoricoResumoDTO();
        dto.setTotalTicketsCliente(total);

        List<Ticket> encerrados = ticketRepository.findEncerradosByClienteExcluindoNumero(
                clienteId,
                STATUS_ENCERRADO,
                exclude,
                PageRequest.of(0, 1));
        if (!encerrados.isEmpty()) {
            dto.setUltimoTicketEncerrado(toItem(encerrados.get(0)));
        }

        List<Ticket> recentes = ticketRepository.findRecentesByClienteExcluindoNumero(
                clienteId,
                exclude,
                PageRequest.of(0, LIMITE_RECENTES));
        List<ChatsHistoricoTicketResumoDTO> itens = new ArrayList<>();
        for (Ticket t : recentes) {
            itens.add(toItem(t));
        }
        dto.setTicketsRecentes(itens);
        return dto;
    }

    private static ChatsHistoricoResumoDTO resumoVazio() {
        ChatsHistoricoResumoDTO dto = new ChatsHistoricoResumoDTO();
        dto.setTotalTicketsCliente(0);
        dto.setTicketsRecentes(new ArrayList<>());
        return dto;
    }

    private static ChatsHistoricoTicketResumoDTO toItem(Ticket ticket) {
        ChatsHistoricoTicketResumoDTO item = new ChatsHistoricoTicketResumoDTO();
        item.setNumeroTicket(ticket.getNumeroTicket());
        item.setStatus(ticket.getStatus() != null ? ticket.getStatus().name() : null);
        item.setDataAbertura(ticket.getDataAbertura());
        item.setDataEncerramento(ticket.getDataEncerramento());
        GrupoCategoria grupo = ticket.getGrupoCategoria();
        item.setGrupoCategoriaNome(grupo != null ? grupo.getNome() : null);
        return item;
    }
}
