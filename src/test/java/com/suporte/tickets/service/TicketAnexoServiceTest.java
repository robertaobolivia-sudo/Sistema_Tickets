package com.suporte.tickets.service;

import com.suporte.tickets.config.UploadStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketAnexoServiceTest {

    @Test
    void sanitizarNome_removeCaracteresInvalidos() {
        assertEquals("teste.pdf", TicketAnexoService.sanitizarNomeArquivo("doc/teste.pdf"));
    }

    @Test
    void sanitizeNumeroTicket() {
        assertEquals("TK-100", UploadStorageProperties.sanitizeNumeroTicket("TK-100"));
        assertEquals("sem-numero", UploadStorageProperties.sanitizeNumeroTicket("   "));
    }

    @Test
    void validar_arquivoVazio() {
        MockMultipartFile vazio = new MockMultipartFile("arquivo", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> TicketAnexoService.validarArquivoUpload(vazio));
    }
}
