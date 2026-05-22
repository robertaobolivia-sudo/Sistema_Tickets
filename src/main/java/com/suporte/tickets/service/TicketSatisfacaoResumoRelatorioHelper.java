package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.TicketSatisfacao;
import com.suporte.tickets.entity.TicketSatisfacaoEnvioStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Preenche campos de satisfação em DTOs de relatório (sem token/hash).
 */
public final class TicketSatisfacaoResumoRelatorioHelper {

    private TicketSatisfacaoResumoRelatorioHelper() {
    }

    public static Map<Integer, TicketSatisfacao> indexarPorTicketId(List<TicketSatisfacao> lista) {
        return lista.stream()
                .filter(s -> s.getTicket() != null && s.getTicket().getId() != null)
                .collect(Collectors.toMap(s -> s.getTicket().getId(), s -> s, (a, b) -> a));
    }

    public static void aplicarEmDto(TicketResponseDTO dto, TicketSatisfacao satisfacao) {
        if (dto == null || satisfacao == null) {
            return;
        }
        dto.setSatisfacaoStatus(TicketSatisfacaoService.resolverStatusExibicao(satisfacao));
        dto.setSatisfacaoNota(satisfacao.getNota());
        dto.setSatisfacaoComentario(satisfacao.getComentario());
        dto.setSatisfacaoEnvioStatus(formatarEnvioStatus(satisfacao.getEnvioStatus()));
        dto.setSatisfacaoEnviadaEm(satisfacao.getEnviadaEm());
        dto.setSatisfacaoRespondidaEm(satisfacao.getRespondidaEm());
        dto.setSatisfacaoExpiraEm(satisfacao.getExpiraEm());
    }

    static String formatarEnvioStatus(TicketSatisfacaoEnvioStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case SIMULADO -> "Simulado";
            case FALHA -> "Falha";
        };
    }
}
