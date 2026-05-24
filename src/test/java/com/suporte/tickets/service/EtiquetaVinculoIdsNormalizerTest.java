package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EtiquetaVinculoIdsNormalizerTest {

    @Test
    void normalizar_removeNulosDuplicadosOrdenaInsercao() {
        List<Long> ids = EtiquetaVinculoIdsNormalizer.normalizarIdsEtiqueta(Arrays.asList(3L, null, 1L, 3L, 2L));
        assertEquals(List.of(3L, 1L, 2L), ids);
    }

    @Test
    void normalizar_vazio() {
        assertEquals(List.of(), EtiquetaVinculoIdsNormalizer.normalizarIdsEtiqueta(null));
        assertEquals(List.of(), EtiquetaVinculoIdsNormalizer.normalizarIdsEtiqueta(List.of()));
    }

    @Test
    void normalizar_limiteMaximo() {
        Long[] arr = new Long[51];
        for (int i = 0; i < 51; i++) {
            arr[i] = (long) i;
        }
        assertThrows(IllegalArgumentException.class, () ->
                EtiquetaVinculoIdsNormalizer.normalizarIdsEtiqueta(Arrays.asList(arr)));
    }
}
