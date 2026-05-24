package com.suporte.tickets.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Normalização de IDs de etiqueta (compartilhado Contato/Ticket legado). Sprint F32. */
public final class EtiquetaVinculoIdsNormalizer {

    private static final int MAX_ETIQUETAS_POR_VINCULO = 50;

    private EtiquetaVinculoIdsNormalizer() {
    }

    public static List<Long> normalizarIdsEtiqueta(List<Long> etiquetaIds) {
        if (etiquetaIds == null || etiquetaIds.isEmpty()) {
            return List.of();
        }
        Set<Long> unicos = new LinkedHashSet<>();
        for (Long id : etiquetaIds) {
            if (id == null) {
                continue;
            }
            unicos.add(id);
        }
        List<Long> lista = new ArrayList<>(unicos);
        if (lista.size() > MAX_ETIQUETAS_POR_VINCULO) {
            throw new IllegalArgumentException(
                    "Maximo de " + MAX_ETIQUETAS_POR_VINCULO + " etiquetas ativas por vinculo.");
        }
        return lista;
    }
}
