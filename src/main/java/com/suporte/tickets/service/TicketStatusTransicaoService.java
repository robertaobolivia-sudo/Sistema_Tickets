package com.suporte.tickets.service;

import com.suporte.tickets.entity.TicketStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados do Ticket (Sprint 295) — transições permitidas por contexto operacional.
 */
@Service
public class TicketStatusTransicaoService {

    public enum MotivoTransicao {
        /** PUT /api/tickets/{numero}/status (Chats). */
        ATUALIZACAO_MANUAL,
        /** PUT encerrar com categoria/motivo. */
        ENCERRAMENTO,
        /** Reabertura explícita. */
        REABERTURA,
        /** Classificação indevido com confirmação. */
        CLASSIFICACAO_INDEVIDO,
        /** Reversão de indevido pelo analista no mesmo dia (Sprint 305). */
        REVERTER_INDEVIDO
    }

    private static final Set<TicketStatus> ATIVOS_OPERACIONAIS = EnumSet.of(
            TicketStatus.ABERTO,
            TicketStatus.EM_ATENDIMENTO,
            TicketStatus.AGUARDANDO_CLIENTE);

    private static final Set<TicketStatus> TERMINAIS_REABERTURA = EnumSet.of(
            TicketStatus.RESOLVIDO,
            TicketStatus.CANCELADO);

    private static final Map<TicketStatus, Set<TicketStatus>> MANUAL_DE_PARA = Map.of(
            TicketStatus.ABERTO,
            EnumSet.of(TicketStatus.EM_ATENDIMENTO, TicketStatus.AGUARDANDO_CLIENTE, TicketStatus.CANCELADO),
            TicketStatus.EM_ATENDIMENTO,
            EnumSet.of(TicketStatus.AGUARDANDO_CLIENTE, TicketStatus.CANCELADO),
            TicketStatus.AGUARDANDO_CLIENTE,
            EnumSet.of(TicketStatus.EM_ATENDIMENTO, TicketStatus.CANCELADO));

    /**
     * Valida transição; lança {@link IllegalArgumentException} se proibida.
     */
    public void validarTransicao(TicketStatus statusAtual, TicketStatus novoStatus, MotivoTransicao motivo) {
        if (statusAtual == null) {
            throw new IllegalArgumentException("Status atual do ticket e obrigatorio.");
        }
        if (novoStatus == null) {
            throw new IllegalArgumentException("Novo status e obrigatorio.");
        }
        if (statusAtual == novoStatus) {
            if (motivo == MotivoTransicao.ENCERRAMENTO && !ATIVOS_OPERACIONAIS.contains(statusAtual)) {
                throw new IllegalArgumentException(
                        "Ticket ja esta encerrado ou nao pode ser encerrado neste status.");
            }
            return;
        }
        if (!isTransicaoPermitida(statusAtual, novoStatus, motivo)) {
            throw new IllegalArgumentException(mensagemTransicaoNegada(statusAtual, novoStatus, motivo));
        }
    }

    public boolean isTransicaoPermitida(
            TicketStatus statusAtual, TicketStatus novoStatus, MotivoTransicao motivo) {
        if (statusAtual == novoStatus) {
            if (motivo == MotivoTransicao.ENCERRAMENTO && !ATIVOS_OPERACIONAIS.contains(statusAtual)) {
                return false;
            }
            return true;
        }
        return switch (motivo) {
            case ATUALIZACAO_MANUAL -> permitidoAtualizacaoManual(statusAtual, novoStatus);
            case ENCERRAMENTO -> permitidoEncerramento(statusAtual, novoStatus);
            case REABERTURA -> permitidoReabertura(statusAtual, novoStatus);
            case CLASSIFICACAO_INDEVIDO -> permitidoClassificacaoIndevido(statusAtual, novoStatus);
            case REVERTER_INDEVIDO -> permitidoReverterIndevido(statusAtual, novoStatus);
        };
    }

    /** Ticket em fila operacional (ativo). */
    public boolean isStatusAtivoOperacional(TicketStatus status) {
        return status != null && ATIVOS_OPERACIONAIS.contains(status);
    }

    /** Terminal indevido — não retorna ao ativo por fluxos padrão. */
    public boolean isTerminalIndevido(TicketStatus status) {
        return status == TicketStatus.INDEVIDO;
    }

    private static boolean permitidoAtualizacaoManual(TicketStatus de, TicketStatus para) {
        if (para == TicketStatus.INDEVIDO || para == TicketStatus.RESOLVIDO) {
            return false;
        }
        if (TERMINAIS_REABERTURA.contains(de) || de == TicketStatus.INDEVIDO) {
            return false;
        }
        Set<TicketStatus> destinos = MANUAL_DE_PARA.get(de);
        return destinos != null && destinos.contains(para);
    }

    private static boolean permitidoEncerramento(TicketStatus de, TicketStatus para) {
        return ATIVOS_OPERACIONAIS.contains(de) && para == TicketStatus.RESOLVIDO;
    }

    private static boolean permitidoReabertura(TicketStatus de, TicketStatus para) {
        return TERMINAIS_REABERTURA.contains(de) && para == TicketStatus.ABERTO;
    }

    private static boolean permitidoClassificacaoIndevido(TicketStatus de, TicketStatus para) {
        return ATIVOS_OPERACIONAIS.contains(de) && para == TicketStatus.INDEVIDO;
    }

    private static boolean permitidoReverterIndevido(TicketStatus de, TicketStatus para) {
        return de == TicketStatus.INDEVIDO && para == TicketStatus.ABERTO;
    }

    private static String mensagemTransicaoNegada(
            TicketStatus de, TicketStatus para, MotivoTransicao motivo) {
        return "Transicao de status nao permitida: "
                + de.name()
                + " -> "
                + para.name()
                + " (contexto: "
                + motivo.name()
                + ").";
    }
}
