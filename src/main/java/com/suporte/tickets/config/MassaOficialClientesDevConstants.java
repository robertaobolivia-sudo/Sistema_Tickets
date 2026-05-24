package com.suporte.tickets.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contratantes B2B oficiais da massa DEV (Sprint 253+).
 */
public final class MassaOficialClientesDevConstants {

    public static final String ROCHA_RAZAO = "Rocha Mendes Comercio LTDA";
    public static final String STATUS_RAZAO = "Status Automacao Industria ME";
    public static final String FAST_RAZAO = "Fast Comercio Varejo SA";
    public static final String FENIX_RAZAO = "Fenix Servicos Digitais LTDA";

    public static final Set<String> RAZOES_SOCIAIS_OFICIAIS = Set.of(
            ROCHA_RAZAO,
            STATUS_RAZAO,
            FAST_RAZAO,
            FENIX_RAZAO
    );

    public static final List<String> CNPJS_OFICIAIS = List.of(
            "11.222.333/0001-41",
            "22.333.444/0001-52",
            "33.444.555/0001-63",
            "44.555.666/0001-74"
    );

    private MassaOficialClientesDevConstants() {
    }

    /**
     * Palavras-chave no nome/razão social legado → razão social oficial.
     */
    public static String resolverRazaoOficialPorLegado(ClienteLegadoRef ref) {
        String blob = (ref.nome() + " " + ref.razaoSocial() + " " + ref.empresa()).toLowerCase();
        if (blob.contains("rocha")) {
            return ROCHA_RAZAO;
        }
        if (blob.contains("status") || blob.contains("automacao") || blob.contains("automa")) {
            return STATUS_RAZAO;
        }
        if (blob.contains("fast")) {
            return FAST_RAZAO;
        }
        if (blob.contains("fenix") || blob.contains("fênix")) {
            return FENIX_RAZAO;
        }
        return null;
    }

    public record ClienteLegadoRef(String nome, String razaoSocial, String empresa) {
    }
}
