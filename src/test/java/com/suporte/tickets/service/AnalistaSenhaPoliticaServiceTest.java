package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalistaSenhaPoliticaServiceTest {

    private final AnalistaSenhaPoliticaService service = new AnalistaSenhaPoliticaService();

    @Test
    void senhaAtendePolitica_aceitaValida() {
        assertTrue(AnalistaSenhaPoliticaService.senhaAtendePolitica("Abcdef12"));
    }

    @Test
    void senhaAtendePolitica_rejeitaCurtaSemNumero() {
        assertFalse(AnalistaSenhaPoliticaService.senhaAtendePolitica("abcdefgh"));
        assertFalse(AnalistaSenhaPoliticaService.senhaAtendePolitica("abc1"));
    }

    @Test
    void validarSenhaInformada_rejeitaFraca() {
        assertThrows(IllegalArgumentException.class, () -> service.validarSenhaInformada("fraca"));
    }
}
