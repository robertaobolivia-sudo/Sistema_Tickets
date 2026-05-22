package com.suporte.tickets.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/** Validação compartilhada de upload da arte do header Chats (Sprint 174/180). */
public final class ArteHeaderChatsUploadSupport {

    public static final long MAX_ARTE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp");

    private ArteHeaderChatsUploadSupport() {
    }

    public static void validarArteMultipart(MultipartFile arte) {
        if (arte == null || arte.isEmpty()) {
            throw new IllegalArgumentException("Selecione uma imagem para a arte do header.");
        }
        if (arte.getSize() > MAX_ARTE_BYTES) {
            throw new IllegalArgumentException("A imagem deve ter no máximo 5 MB.");
        }
        String contentType = arte.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Use imagem PNG, JPG/JPEG ou WEBP.");
        }
    }

    public static String obterExtensaoImagem(MultipartFile arte) {
        String contentType = arte.getContentType() != null ? arte.getContentType().toLowerCase(Locale.ROOT) : "";
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
