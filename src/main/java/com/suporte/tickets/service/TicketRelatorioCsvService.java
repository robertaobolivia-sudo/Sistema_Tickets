package com.suporte.tickets.service;

import com.suporte.tickets.dto.TicketFiltroDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketRelatorioCsvService {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final TicketBuscaService ticketBuscaService;

    @Transactional(readOnly = true)
    public byte[] gerarCsv(TicketFiltroDTO filtro) {
        List<TicketResponseDTO> tickets = ticketBuscaService.buscar(filtro);
        String csv = montarConteudoCsv(tickets);
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] comBom = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, comBom, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, comBom, UTF8_BOM.length, body.length);
        return comBom;
    }

    private String montarConteudoCsv(List<TicketResponseDTO> tickets) {
        StringBuilder builder = new StringBuilder();
        builder.append(linha(
                "Número",
                "Cliente",
                "Origem ticket",
                "Status",
                "Classificação operacional",
                "Prioridade",
                "Canal",
                "Analista responsável",
                "Grupo",
                "Subgrupo",
                "Motivo",
                "Status pesquisa",
                "Nota avaliação",
                "Comentário avaliação",
                "Status envio pesquisa",
                "Pesquisa enviada em",
                "Pesquisa respondida em",
                "Pesquisa expira em",
                "Data de abertura",
                "Data de encerramento",
                "Vencimento 1º Atendimento",
                "SLA 1º Atendimento",
                "Vencimento Resolução",
                "SLA Resolução",
                "SLA pausado",
                "Minutos pausados resolução",
                "Escalonado",
                "Escalonado em",
                "Observação escalonamento"
        ));

        for (TicketResponseDTO ticket : tickets) {
            builder.append(linha(
                    valor(ticket.getNumeroTicket()),
                    valor(ticket.getCliente()),
                    valor(ticket.getOrigemTicket()),
                    valor(formatarStatusRelatorio(ticket.getStatus())),
                    valor(formatarClassificacaoOperacional(ticket)),
                    valor(ticket.getPrioridade()),
                    valor(ticket.getCanal()),
                    valor(ticket.getAnalistaResponsavelNome()),
                    valor(ticket.getGrupoCategoriaNome()),
                    valor(ticket.getSubgrupoCategoriaNome()),
                    valor(ticket.getMotivoNome()),
                    formatarStatusPesquisa(ticket.getSatisfacaoStatus()),
                    formatarNota(ticket.getSatisfacaoNota()),
                    valorComentario(ticket.getSatisfacaoComentario()),
                    valor(ticket.getSatisfacaoEnvioStatus()),
                    formatarData(ticket.getSatisfacaoEnviadaEm()),
                    formatarData(ticket.getSatisfacaoRespondidaEm()),
                    formatarData(ticket.getSatisfacaoExpiraEm()),
                    formatarData(ticket.getDataAbertura()),
                    formatarData(ticket.getDataEncerramento()),
                    formatarData(ticket.getSlaPrimeiroAtendimentoVencimento()),
                    formatarSlaStatus(ticket.getSlaPrimeiroAtendimentoStatus()),
                    formatarData(ticket.getSlaResolucaoVencimento()),
                    formatarSlaStatus(ticket.getSlaResolucaoStatus()),
                    formatarSimNao(ticket.getSlaPausado()),
                    formatarMinutosPausados(ticket.getSlaResolucaoMinutosPausados()),
                    formatarSimNao(ticket.getEscalonado()),
                    formatarData(ticket.getEscalonadoEm()),
                    valorObservacao(ticket.getEscalonamentoObservacao())
            ));
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

    private String formatarStatusRelatorio(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        if ("INDEVIDO".equalsIgnoreCase(status.trim())) {
            return "Não atendimento (Indevido)";
        }
        return status.trim();
    }

    private String formatarClassificacaoOperacional(TicketResponseDTO ticket) {
        if (ticket.getClassificacaoOperacional() != null && !ticket.getClassificacaoOperacional().isBlank()) {
            return switch (ticket.getClassificacaoOperacional().trim()) {
                case "INDEVIDO" -> "Indevido";
                case "CONTATO_PESSOAL" -> "Contato Pessoal";
                case "PROPAGANDA" -> "Propaganda";
                default -> ticket.getClassificacaoOperacional().trim();
            };
        }
        if (ticket.getStatus() != null && "INDEVIDO".equalsIgnoreCase(ticket.getStatus())) {
            return "—";
        }
        return "-";
    }

    private String valor(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.trim();
    }

    private String formatarData(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(value);
    }

    private String formatarSlaStatus(String status) {
        if (status == null || status.isBlank() || "NAO_CALCULADO".equals(status)) {
            return "-";
        }
        return switch (status) {
            case "DENTRO_DO_PRAZO" -> "Dentro do prazo";
            case "PROXIMO_DO_VENCIMENTO" -> "Próximo do vencimento";
            case "VENCIDO" -> "Vencido";
            case "PAUSADO" -> "Pausado";
            case "CUMPRIDO" -> "Cumprido";
            case "VIOLADO" -> "Violado";
            default -> status;
        };
    }

    private String formatarSimNao(Boolean valor) {
        return Boolean.TRUE.equals(valor) ? "Sim" : "Não";
    }

    private String formatarStatusPesquisa(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return switch (status) {
            case "NAO_ENVIADA" -> "Não enviada";
            case "PENDENTE" -> "Pendente";
            case "RESPONDIDA" -> "Respondida";
            case "EXPIRADA" -> "Expirada";
            case "REGISTRADA_MANUALMENTE" -> "Registrada manualmente";
            default -> status;
        };
    }

    private String formatarNota(Integer nota) {
        if (nota == null || nota < 1 || nota > 5) {
            return "-";
        }
        return String.valueOf(nota);
    }

    private String valorComentario(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String valorObservacao(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        return valor.trim();
    }

    private String formatarMinutosPausados(Long minutos) {
        if (minutos == null || minutos <= 0) {
            return "0";
        }
        return String.valueOf(minutos);
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
