package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketEtiquetaServiceTest {

    @Test
    void normalizar_removeDuplicadosENulos() {
        List<Long> ids = TicketEtiquetaService.normalizarIdsEtiqueta(Arrays.asList(3L, null, 1L, 3L, 2L));
        assertEquals(List.of(3L, 1L, 2L), ids);
    }

    @Test
    void normalizar_listaVazia() {
        assertEquals(List.of(), TicketEtiquetaService.normalizarIdsEtiqueta(null));
        assertEquals(List.of(), TicketEtiquetaService.normalizarIdsEtiqueta(List.of()));
    }

    @Test
    void normalizar_excedeLimite() {
        Long[] arr = new Long[51];
        for (int i = 0; i < 51; i++) {
            arr[i] = (long) i + 1;
        }
        assertThrows(IllegalArgumentException.class, () ->
                TicketEtiquetaService.normalizarIdsEtiqueta(Arrays.asList(arr)));
    }
}
