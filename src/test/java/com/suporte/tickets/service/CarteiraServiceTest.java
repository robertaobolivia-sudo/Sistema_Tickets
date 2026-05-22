package com.suporte.tickets.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CarteiraServiceTest {

    @Test
    void validarArteMultipart_aceitaPng() {
        MockMultipartFile file = new MockMultipartFile(
                "arte",
                "banner.png",
                "image/png",
                new byte[] { 1, 2, 3 });
        assertDoesNotThrow(() -> ArteHeaderChatsUploadSupport.validarArteMultipart(file));
    }

    @Test
    void validarArteMultipart_rejeitaTipoInvalido() {
        MockMultipartFile file = new MockMultipartFile(
                "arte",
                "doc.pdf",
                "application/pdf",
                new byte[] { 1 });
        assertThrows(IllegalArgumentException.class, () -> ArteHeaderChatsUploadSupport.validarArteMultipart(file));
    }

    @Test
    void validarArteMultipart_rejeitaArquivoGrande() {
        byte[] grande = new byte[(int) (5L * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "arte",
                "banner.jpg",
                "image/jpeg",
                grande);
        assertThrows(IllegalArgumentException.class, () -> ArteHeaderChatsUploadSupport.validarArteMultipart(file));
    }
}
