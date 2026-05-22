package com.suporte.tickets.service;

import com.suporte.tickets.dto.AuditoriaEventoFiltroDTO;
import com.suporte.tickets.dto.AuditoriaEventoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuditoriaEventoCsvService {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final byte[] UTF8_BOM = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final Pattern SENSIVEL =
            Pattern.compile("(?i)(senha|password|authToken|auth_token|token\\s*[:=])");

    private final AuditoriaConsultaService auditoriaConsultaService;

    @Transactional(readOnly = true)
    public byte[] gerarCsv(AuditoriaEventoFiltroDTO filtro) {
        List<AuditoriaEventoResponseDTO> eventos = auditoriaConsultaService.listarParaExportacao(filtro);
        String csv = montarConteudoCsv(eventos);
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] comBom = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, comBom, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, comBom, UTF8_BOM.length, body.length);
        return comBom;
    }

    private String montarConteudoCsv(List<AuditoriaEventoResponseDTO> eventos) {
        StringBuilder builder = new StringBuilder();
        builder.append(linha(
                "Data/Hora",
                "Analista ID",
                "Analista",
                "Perfil",
                "Ação",
                "Entidade",
                "Entidade ID",
                "Descrição",
                "IP",
                "User-Agent"));
        for (AuditoriaEventoResponseDTO evento : eventos) {
            builder.append(linha(
                    formatarData(evento.getDataHora()),
                    formatarId(evento.getAnalistaId()),
                    valor(evento.getAnalistaNome()),
                    valor(evento.getPerfilAcesso()),
                    valor(evento.getAcao()),
                    valor(evento.getEntidade()),
                    valor(evento.getEntidadeId()),
                    valorSanitizado(evento.getDescricao()),
                    valor(evento.getIpOrigem()),
                    valor(evento.getUserAgent())));
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
        return sanitizarTexto(value.trim());
    }

    private String valorSanitizado(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String texto = sanitizarTexto(value.trim());
        if (SENSIVEL.matcher(texto).find()) {
            return "[conteúdo omitido]";
        }
        return texto;
    }

    private String sanitizarTexto(String texto) {
        return texto.replace('\r', ' ').replace('\n', ' ');
    }

    private String formatarId(Long id) {
        return id == null ? "-" : String.valueOf(id);
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
