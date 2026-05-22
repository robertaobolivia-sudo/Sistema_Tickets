package com.suporte.tickets.entity;

/**
 * Status da tentativa de envio da pesquisa por WhatsApp (Sprint 200).
 */
public enum TicketSatisfacaoEnvioStatus {
    /** Enfileirado / enviado via implementação simulada (sem provedor real). */
    SIMULADO,
    /** Falha controlada (ex.: WhatsApp ausente, erro ao montar mensagem). */
    FALHA
}
