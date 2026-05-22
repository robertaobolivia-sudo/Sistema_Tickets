package com.suporte.tickets.entity;

public enum TicketInteracaoTipo {
    ABERTURA,
    COMENTARIO,
    NOTA_INTERNA,
    ENCERRAMENTO,
    /** Mensagem recebida do cliente (WhatsApp/API). */
    MENSAGEM_CLIENTE
}
