package com.suporte.tickets.domain;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Etiquetas globais com papel operacional na gestão de Contatos (Sprint 271).
 * Não aplica regras automáticas em tickets.
 */
public final class EtiquetaOperacionalCatalog {

    public record DefinicaoOperacional(String nome, String descricao, String cor) {}

    public static final List<DefinicaoOperacional> DEFINICOES = List.of(
            new DefinicaoOperacional(
                    "Indevido",
                    "Mensagem ou contato fora do escopo de atendimento. Uso operacional; sem invalidar tickets automaticamente.",
                    "#b91c1c"),
            new DefinicaoOperacional(
                    "Contato Pessoal",
                    "Contato pessoal do analista ou contratante, não comercial. Uso operacional na gestão de Contatos.",
                    "#d97706"),
            new DefinicaoOperacional(
                    "Propaganda",
                    "Propaganda, spam ou divulgação não relacionada ao suporte. Uso operacional na gestão de Contatos.",
                    "#7c3aed"));

    private EtiquetaOperacionalCatalog() {}

    public static String normalizarNome(String nome) {
        if (nome == null) {
            return "";
        }
        return nome.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isNomeOperacional(String nome) {
        String n = normalizarNome(nome);
        if (n.isEmpty()) {
            return false;
        }
        return DEFINICOES.stream().anyMatch(d -> normalizarNome(d.nome()).equals(n));
    }

    public static boolean temAlgumaOperacional(Iterable<String> nomes) {
        if (nomes == null) {
            return false;
        }
        for (String nome : nomes) {
            if (isNomeOperacional(nome)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> nomesCanonicos() {
        return DEFINICOES.stream().map(DefinicaoOperacional::nome).collect(Collectors.toList());
    }
}
