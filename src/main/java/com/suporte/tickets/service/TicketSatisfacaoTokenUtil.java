package com.suporte.tickets.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token opaco para resposta pública (armazena-se apenas o hash SHA-256 em hex).
 */
public final class TicketSatisfacaoTokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TicketSatisfacaoTokenUtil() {
    }

    public static String gerarTokenOpaco() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token invalido");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
