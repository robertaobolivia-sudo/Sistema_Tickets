package com.suporte.tickets.service.whatsapp;

/**
 * Envio de mensagem WhatsApp (provedor real ou simulado).
 */
public interface WhatsAppMessageSender {

    /**
     * @param whatsappDestino número normalizado (somente dígitos)
     * @param textoMensagem corpo da mensagem
     * @return true se aceito para envio (simulado ou real)
     */
    boolean enviar(String whatsappDestino, String textoMensagem);
}
