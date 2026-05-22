package com.suporte.tickets.service.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Envio simulado — registra em log até integração com provedor real.
 */
@Component
@Slf4j
public class NoopWhatsAppMessageSender implements WhatsAppMessageSender {

    @Override
    public boolean enviar(String whatsappDestino, String textoMensagem) {
        log.info(
                "WhatsApp simulado (pesquisa satisfacao): destino={}, tamanhoMensagem={}",
                mascararDestino(whatsappDestino),
                textoMensagem != null ? textoMensagem.length() : 0);
        if (log.isDebugEnabled() && textoMensagem != null) {
            log.debug("Corpo simulado: {}", textoMensagem);
        }
        return true;
    }

    private static String mascararDestino(String destino) {
        if (destino == null || destino.length() < 4) {
            return "****";
        }
        return "****" + destino.substring(destino.length() - 4);
    }
}
