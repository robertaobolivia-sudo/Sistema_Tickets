package com.suporte.tickets.util;

import com.suporte.tickets.entity.Cliente;

import java.util.regex.Pattern;

/**
 * Leitura de campos que ficaram temporariamente em {@link Cliente#getObservacoes()} (Sprint 252).
 */
public final class ClienteCamposLegadoSupport {

    private static final Pattern LINE_PREFIX = Pattern.compile("^(IE|CEP|Site|Horário|Horario)\\s*:\\s*(.*)$", Pattern.CASE_INSENSITIVE);

    private ClienteCamposLegadoSupport() {
    }

    public record CamposLegadoObservacoes(
            String inscricaoEstadual,
            String cep,
            String site,
            String horarioFuncionamento,
            String observacoesLivres
    ) {
    }

    public static CamposLegadoObservacoes parseObservacoes(String observacoesRaw) {
        if (observacoesRaw == null || observacoesRaw.isBlank()) {
            return new CamposLegadoObservacoes(null, null, null, null, null);
        }
        String ie = null;
        String cep = null;
        String site = null;
        String horario = null;
        StringBuilder free = new StringBuilder();
        for (String line : observacoesRaw.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            var m = LINE_PREFIX.matcher(trimmed);
            if (m.matches()) {
                String key = m.group(1).toLowerCase();
                String val = m.group(2).trim();
                switch (key) {
                    case "ie" -> ie = val;
                    case "cep" -> cep = val;
                    case "site" -> site = val;
                    case "horário", "horario" -> horario = val;
                    default -> appendFreeLine(free, trimmed);
                }
            } else {
                appendFreeLine(free, trimmed);
            }
        }
        String freeText = free.length() == 0 ? null : free.toString().trim();
        return new CamposLegadoObservacoes(ie, cep, site, horario, freeText);
    }

    private static void appendFreeLine(StringBuilder free, String line) {
        if (free.length() > 0) {
            free.append('\n');
        }
        free.append(line);
    }

    public static void aplicarFallbackLeitura(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        if (isBlank(cliente.getRazaoSocial()) && !isBlank(cliente.getEmpresa())) {
            cliente.setRazaoSocial(cliente.getEmpresa().trim());
        }
        if (isBlank(cliente.getWhatsapp()) && !isBlank(cliente.getTelefone())) {
            cliente.setWhatsapp(cliente.getTelefone().trim());
        }
        if (isBlank(cliente.getResponsavel()) && !isBlank(cliente.getNome())) {
            cliente.setResponsavel(cliente.getNome().trim());
        }
        CamposLegadoObservacoes legado = parseObservacoes(cliente.getObservacoes());
        if (isBlank(cliente.getInscricaoEstadual()) && !isBlank(legado.inscricaoEstadual())) {
            cliente.setInscricaoEstadual(legado.inscricaoEstadual());
        }
        if (isBlank(cliente.getCep()) && !isBlank(legado.cep())) {
            cliente.setCep(legado.cep());
        }
        if (isBlank(cliente.getSite()) && !isBlank(legado.site())) {
            cliente.setSite(legado.site());
        }
        if (isBlank(cliente.getHorarioFuncionamento()) && !isBlank(legado.horarioFuncionamento())) {
            cliente.setHorarioFuncionamento(legado.horarioFuncionamento());
        }
        if (!isBlank(legado.observacoesLivres())) {
            cliente.setObservacoes(legado.observacoesLivres());
        } else if (observacoesSoPrefixos(cliente.getObservacoes())) {
            cliente.setObservacoes(null);
        }
    }

    private static boolean observacoesSoPrefixos(String observacoes) {
        if (observacoes == null || observacoes.isBlank()) {
            return false;
        }
        CamposLegadoObservacoes p = parseObservacoes(observacoes);
        return p.observacoesLivres() == null || p.observacoesLivres().isBlank();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
