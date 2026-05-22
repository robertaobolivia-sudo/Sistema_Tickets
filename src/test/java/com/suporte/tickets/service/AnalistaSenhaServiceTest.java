package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalistaSenhaServiceTest {

    private final AnalistaSenhaService service = new AnalistaSenhaService();

    @Test
    void hashSenha_naoRetornaTextoPlano() {
        String hash = service.hashSenha("Teste@1234");
        assertTrue(service.senhaArmazenadaEhHash(hash));
        assertNotEquals("Teste@1234", hash);
    }

    @Test
    void senhaConfere_comHash() {
        String hash = service.hashSenha("segredo");
        assertTrue(service.senhaConfere("segredo", hash));
        assertFalse(service.senhaConfere("outra", hash));
    }

    @Test
    void senhaConfere_comTextoLegado() {
        assertTrue(service.senhaConfere("legado", "legado"));
        assertTrue(service.deveMigrarParaHash("legado"));
    }
}
