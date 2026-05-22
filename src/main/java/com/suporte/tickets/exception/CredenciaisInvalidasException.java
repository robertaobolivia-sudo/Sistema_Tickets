package com.suporte.tickets.exception;

/**
 * Falha de autenticação no login (credencial inválida, usuário inativo, etc.).
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public static final String MENSAGEM_PADRAO = "E-mail ou senha inválidos";

    public CredenciaisInvalidasException() {
        super(MENSAGEM_PADRAO);
    }

    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}
