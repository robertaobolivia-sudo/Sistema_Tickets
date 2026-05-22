package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoFiltros;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSatisfacaoConsultaService {

    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;

    public TicketSatisfacaoFiltros resolverFiltros(
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer nota,
            String statusTicket,
            String termoCliente,
            Integer clienteId) {
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(LocalTime.MAX) : null;
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data inicial nao pode ser posterior a data final.");
        }
        if (nota != null && (nota < 1 || nota > 5)) {
            throw new IllegalArgumentException("Nota deve ser entre 1 e 5.");
        }
        TicketStatus status = null;
        if (statusTicket != null && !statusTicket.isBlank()) {
            String valor = statusTicket.trim();
            if (!TicketStatus.isValido(valor)) {
                throw new IllegalArgumentException("Status do ticket invalido.");
            }
            status = TicketStatus.valueOf(valor);
        }
        String termo = termoCliente != null ? termoCliente.trim() : null;
        if (termo != null && termo.isEmpty()) {
            termo = null;
        }
        if (clienteId != null) {
            termo = null;
        }
        return new TicketSatisfacaoFiltros(inicio, fim, nota, status, termo, clienteId);
    }

    @Transactional(readOnly = true)
    public List<TicketSatisfacao> listarAvaliacoes(TicketSatisfacaoFiltros filtros) {
        return ticketSatisfacaoRepository.findByFiltros(
                filtros.inicio(),
                filtros.fim(),
                filtros.nota(),
                filtros.statusTicket(),
                filtros.termoCliente(),
                filtros.clienteId());
    }
}
