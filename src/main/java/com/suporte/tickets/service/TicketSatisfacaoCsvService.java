package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketSatisfacaoFiltros;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketSatisfacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketSatisfacaoCsvService {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final TicketSatisfacaoConsultaService ticketSatisfacaoConsultaService;

    @Transactional(readOnly = true)
    public byte[] gerarCsv(
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
        String csv = montarConteudoCsv(avaliacoes);
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] comBom = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, comBom, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, comBom, UTF8_BOM.length, body.length);
        return comBom;
    }

    private String montarConteudoCsv(List<TicketSatisfacao> avaliacoes) {
        StringBuilder builder = new StringBuilder();
        builder.append(linha(
                "Data avaliacao",
                "Numero ticket",
                "Cliente",
                "Status ticket",
                "Prioridade",
                "Nota",
                "Comentario"));
        for (TicketSatisfacao s : avaliacoes) {
            Ticket ticket = s.getTicket();
            Cliente cliente = ticket != null ? ticket.getCliente() : null;
            builder.append(linha(
                    formatarData(s.getCriadoEm()),
                    valor(ticket != null ? ticket.getNumeroTicket() : null),
                    valor(cliente != null ? cliente.getNome() : null),
                    valor(ticket != null && ticket.getStatus() != null ? ticket.getStatus().name() : null),
                    valor(ticket != null && ticket.getPrioridade() != null ? ticket.getPrioridade().name() : null),
                    valor(s.getNota() != null ? String.valueOf(s.getNota()) : null),
                    valor(s.getComentario())));
        }
        return builder.toString();
    }

    private String linha(String... colunas) {
        List<String> celulas = new ArrayList<>();
        for (String coluna : colunas) {
            celulas.add(escaparCelula(coluna));
        }
        return String.join(SEPARATOR, celulas) + "\r\n";
    }

    private String valor(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim().replace('\r', ' ').replace('\n', ' ');
    }

    private String formatarData(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(value);
    }

    private String escaparCelula(String value) {
        String texto = value == null ? "-" : value;
        if (texto.contains("\"")) {
            texto = texto.replace("\"", "\"\"");
        }
        if (texto.contains(SEPARATOR) || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            return "\"" + texto + "\"";
        }
        return texto;
    }
}
