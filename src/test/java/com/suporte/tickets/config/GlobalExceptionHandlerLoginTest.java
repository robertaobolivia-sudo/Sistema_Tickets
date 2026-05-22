package com.suporte.tickets.config;

import com.suporte.tickets.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerLoginTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void credenciaisInvalidas_retorna401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleCredenciaisInvalidasException(new CredenciaisInvalidasException());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(CredenciaisInvalidasException.MENSAGEM_PADRAO, response.getBody().getErro());
    }
}
