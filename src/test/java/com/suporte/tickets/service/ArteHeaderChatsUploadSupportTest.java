package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArteHeaderChatsUploadSupportTest {

    @Test
    void validarArteMultipart_aceitaWebp() {
        MockMultipartFile file = new MockMultipartFile(
                "arte",
                "banner.webp",
                "image/webp",
                new byte[] { 1, 2, 3 });
        assertDoesNotThrow(() -> ArteHeaderChatsUploadSupport.validarArteMultipart(file));
    }

    @Test
    void validarArteMultipart_rejeitaVazio() {
        MockMultipartFile file = new MockMultipartFile("arte", "x.png", "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> ArteHeaderChatsUploadSupport.validarArteMultipart(file));
    }
}
