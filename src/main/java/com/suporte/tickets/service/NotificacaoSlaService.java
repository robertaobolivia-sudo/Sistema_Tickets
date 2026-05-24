package com.suporte.tickets.service;

import com.suporte.tickets.dto.NotificacaoSlaVerificacaoResultadoDTO;
import com.suporte.tickets.entity.NotificacaoTipo;
import com.suporte.tickets.entity.SlaStatus;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoSlaService {

    private final TicketRepository ticketRepository;
    private final TicketSlaPrimeiroAtendimentoService ticketSlaPrimeiroAtendimentoService;
    private final TicketSlaResolucaoService ticketSlaResolucaoService;
    private final NotificacaoInternaService notificacaoInternaService;

    @Transactional
    public NotificacaoSlaVerificacaoResultadoDTO verificarTicketsCriticosSla() {
        List<Ticket> tickets = ticketRepository.findAll();
        long verificados = 0;
        long notificacoesCriadas = 0;
        long ignoradasDuplicadas = 0;

        for (Ticket ticket : tickets) {
            if (isTicketFinalizado(ticket)) {
                continue;
            }
            verificados++;
            String numero = ticket.getNumeroTicket();
            if (numero == null || numero.isBlank()) {
                continue;
            }

            SlaStatus statusPrimeiro = ticketSlaPrimeiroAtendimentoService.calcularStatusPrimeiroAtendimento(ticket);
            SlaStatus statusResolucao = ticketSlaResolucaoService.calcularStatusResolucao(ticket);
            boolean slaPausado = Boolean.TRUE.equals(ticket.getSlaPausado());

            var r1 = registrarSeAplicavel(numero, statusPrimeiro, SlaStatus.VENCIDO,
                    NotificacaoTipo.SLA_PRIMEIRO_ATENDIMENTO_VENCIDO,
                    "SLA 1º atendimento vencido",
                    "O ticket " + numero + " está com SLA de 1º atendimento vencido.");
            notificacoesCriadas += r1.criadas();
            ignoradasDuplicadas += r1.ignoradas();

            var r2 = registrarSeAplicavel(numero, statusPrimeiro, SlaStatus.PROXIMO_DO_VENCIMENTO,
                    NotificacaoTipo.SLA_PRIMEIRO_ATENDIMENTO_PROXIMO,
                    "SLA 1º atendimento próximo do vencimento",
                    "O ticket " + numero + " está próximo do vencimento do SLA de 1º atendimento.");
            notificacoesCriadas += r2.criadas();
            ignoradasDuplicadas += r2.ignoradas();

            if (!slaPausado) {
                var r3 = registrarSeAplicavel(numero, statusResolucao, SlaStatus.VENCIDO,
                        NotificacaoTipo.SLA_RESOLUCAO_VENCIDA,
                        "SLA resolução vencida",
                        "O ticket " + numero + " está com SLA de resolução vencida.");
                notificacoesCriadas += r3.criadas();
                ignoradasDuplicadas += r3.ignoradas();

                var r4 = registrarSeAplicavel(numero, statusResolucao, SlaStatus.PROXIMO_DO_VENCIMENTO,
                        NotificacaoTipo.SLA_RESOLUCAO_PROXIMA,
                        "SLA resolução próxima do vencimento",
                        "O ticket " + numero + " está próximo do vencimento do SLA de resolução.");
                notificacoesCriadas += r4.criadas();
                ignoradasDuplicadas += r4.ignoradas();
            }
        }

        return new NotificacaoSlaVerificacaoResultadoDTO(verificados, notificacoesCriadas, ignoradasDuplicadas);
    }

    private record ResultadoRegistro(long criadas, long ignoradas) {
    }

    private ResultadoRegistro registrarSeAplicavel(
            String numeroTicket,
            SlaStatus statusAtual,
            SlaStatus statusEsperado,
            NotificacaoTipo tipo,
            String titulo,
            String mensagem
    ) {
        if (statusAtual != statusEsperado) {
            return new ResultadoRegistro(0, 0);
        }
        boolean criada = notificacaoInternaService.criarSeNaoExistirNaoLida(tipo, titulo, mensagem, numeroTicket);
        if (criada) {
            return new ResultadoRegistro(1, 0);
        }
        return new ResultadoRegistro(0, 1);
    }

    private boolean isTicketFinalizado(Ticket ticket) {
        return TicketAtivoService.STATUS_FORA_ATENDIMENTO_OPERACIONAL.contains(ticket.getStatus());
    }
}
