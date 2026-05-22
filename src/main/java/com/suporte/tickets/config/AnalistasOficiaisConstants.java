package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;

import java.util.Set;

/**
 * Atendentes oficiais mantidos no sistema (Sprint 9.5.1).
 */
public final class AnalistasOficiaisConstants {

    public static final String JOAO_EMAIL = "robertaobolivia@gmail.com";
    public static final String WESLEY_EMAIL = "wesley.silva@suporte.local";
    public static final String GUSTAVO_EMAIL = "gustavo.silva@suporte.local";
    public static final String MICHELLE_EMAIL = "michelle.falcone@suporte.local";

    public static final Set<String> EMAILS_OFICIAIS = Set.of(
            JOAO_EMAIL,
            WESLEY_EMAIL,
            GUSTAVO_EMAIL,
            MICHELLE_EMAIL
    );

    private AnalistasOficiaisConstants() {
    }

    public static boolean isOficial(Analista analista) {
        if (analista == null) {
            return false;
        }
        if (analista.getEmail() != null && EMAILS_OFICIAIS.contains(analista.getEmail().toLowerCase())) {
            return true;
        }
        String nome = analista.getNomeCompleto() != null ? analista.getNomeCompleto() : analista.getNome();
        if (nome == null) {
            return false;
        }
        String n = nome.trim().toLowerCase();
        return n.equals("joão falcone") || n.equals("joao falcone")
                || n.equals("wesley silva")
                || n.equals("gustavo silva")
                || n.equals("michelle falcone");
    }
}
