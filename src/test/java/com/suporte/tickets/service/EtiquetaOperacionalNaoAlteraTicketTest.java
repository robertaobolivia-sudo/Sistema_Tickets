package com.suporte.tickets.service;

import com.suporte.tickets.repository.TicketRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Garante que vínculo de etiqueta no Contato não aciona classificação de ticket (Sprint 274).
 */
class EtiquetaOperacionalNaoAlteraTicketTest {

    @Test
    void contatoEtiquetaService_naoDependeDeTicketIndevido() {
        assertFalse(dependeDe(ContatoEtiquetaService.class, TicketIndevidoService.class));
        assertFalse(dependeDe(ContatoEtiquetaService.class, TicketRepository.class));
    }

    private static boolean dependeDe(Class<?> service, Class<?> dependency) {
        return Arrays.stream(service.getDeclaredFields())
                .map(Field::getType)
                .anyMatch(t -> t.equals(dependency));
    }
}
