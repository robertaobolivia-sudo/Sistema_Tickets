package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoFiltros;
import com.suporte.tickets.dto.TicketSatisfacaoResumoDTO;
import com.suporte.tickets.entity.TicketSatisfacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSatisfacaoResumoService {

    private final TicketSatisfacaoConsultaService ticketSatisfacaoConsultaService;

    @Transactional(readOnly = true)
    public TicketSatisfacaoResumoDTO calcularResumo(
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer nota,
            String statusTicket,
            String termoCliente,
            Integer clienteId) {
        TicketSatisfacaoFiltros filtros =
                ticketSatisfacaoConsultaService.resolverFiltros(
                        dataInicio, dataFim, nota, statusTicket, termoCliente, clienteId);
        List<TicketSatisfacao> avaliacoes = ticketSatisfacaoConsultaService.listarAvaliacoes(filtros);
        return agregar(avaliacoes);
    }

    static TicketSatisfacaoResumoDTO agregar(List<TicketSatisfacao> avaliacoes) {
        TicketSatisfacaoResumoDTO dto = new TicketSatisfacaoResumoDTO();
        long total = avaliacoes.size();
        dto.setTotalAvaliacoes(total);
        if (total == 0) {
            dto.setMediaGeral(null);
            dto.setPercentualPositivas(0);
            dto.setPercentualNegativas(0);
            return dto;
        }
        long n1 = 0;
        long n2 = 0;
        long n3 = 0;
        long n4 = 0;
        long n5 = 0;
        long soma = 0;
        long positivas = 0;
        long negativas = 0;
        for (TicketSatisfacao s : avaliacoes) {
            if (s.getNota() == null) {
                continue;
            }
            int nota = s.getNota();
            soma += nota;
            switch (nota) {
                case 1 -> n1++;
                case 2 -> n2++;
                case 3 -> n3++;
                case 4 -> n4++;
                case 5 -> n5++;
                default -> { }
            }
            if (nota >= 4) {
                positivas++;
            }
            if (nota <= 2) {
                negativas++;
            }
        }
        dto.setQuantidadeNota1(n1);
        dto.setQuantidadeNota2(n2);
        dto.setQuantidadeNota3(n3);
        dto.setQuantidadeNota4(n4);
        dto.setQuantidadeNota5(n5);
        long comNota = n1 + n2 + n3 + n4 + n5;
        if (comNota == 0) {
            dto.setMediaGeral(null);
            dto.setPercentualPositivas(0);
            dto.setPercentualNegativas(0);
            return dto;
        }
        dto.setMediaGeral(Math.round((soma * 10.0) / comNota) / 10.0);
        dto.setPercentualPositivas(arredondarPercentual(positivas, comNota));
        dto.setPercentualNegativas(arredondarPercentual(negativas, comNota));
        return dto;
    }

    private static double arredondarPercentual(long parte, long total) {
        return Math.round((parte * 1000.0) / total) / 10.0;
    }
}
