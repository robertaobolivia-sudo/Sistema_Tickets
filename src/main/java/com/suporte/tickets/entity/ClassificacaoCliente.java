package com.suporte.tickets.entity;

/**
 * Campo legado no cliente até existir modelo de etiquetas flexíveis.
 * Valores N1/N2 permanecem apenas para leitura de dados antigos no banco; não usar em novas regras de UI/Indicadores.
 */
public enum ClassificacaoCliente {
    /** @deprecated Substituir por etiquetas flexíveis; não expor na UI. */
    @Deprecated
    N1,
    /** @deprecated Substituir por etiquetas flexíveis; não expor na UI. */
    @Deprecated
    N2,
    /** Sem etiqueta (equivalente conceitual a SEM_ETIQUETA). */
    SEM_CLASSIFICACAO;

    public static ClassificacaoCliente effective(ClassificacaoCliente value) {
        return value != null ? value : SEM_CLASSIFICACAO;
    }

    public static ClassificacaoCliente parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return SEM_CLASSIFICACAO;
        }
        String normalized = raw.trim().toUpperCase();
        for (ClassificacaoCliente item : values()) {
            if (item.name().equals(normalized)) {
                return item;
            }
        }
        return SEM_CLASSIFICACAO;
    }
}
