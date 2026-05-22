package com.suporte.tickets.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UploadStorageProperties {

    private final Path uploadRoot;
    private final Path analistasDir;
    private final Path conexoesHeaderChatsDir;
    private final Path clientesHeaderChatsDir;
    private final Path ticketsRoot;

    public UploadStorageProperties() {
        this.uploadRoot = Path.of("uploads").toAbsolutePath().normalize();
        this.analistasDir = uploadRoot.resolve("analistas");
        this.conexoesHeaderChatsDir = uploadRoot.resolve("conexoes").resolve("header-chats");
        this.clientesHeaderChatsDir = uploadRoot.resolve("clientes").resolve("header-chats");
        this.ticketsRoot = uploadRoot.resolve("tickets");
    }

    @PostConstruct
    public void garantirPastas() throws IOException {
        Files.createDirectories(analistasDir);
        Files.createDirectories(conexoesHeaderChatsDir);
        Files.createDirectories(clientesHeaderChatsDir);
        Files.createDirectories(ticketsRoot);
    }

    public Path getUploadRoot() {
        return uploadRoot;
    }

    public Path getAnalistasDir() {
        return analistasDir;
    }

    public Path getConexoesHeaderChatsDir() {
        return conexoesHeaderChatsDir;
    }

    public Path getClientesHeaderChatsDir() {
        return clientesHeaderChatsDir;
    }

    public Path getTicketsRoot() {
        return ticketsRoot;
    }

    public Path getTicketDir(String numeroTicket) {
        return ticketsRoot.resolve(sanitizeNumeroTicket(numeroTicket));
    }

    public String toTicketIdentificador(String numeroTicket, String storedFileName) {
        return "tickets/" + sanitizeNumeroTicket(numeroTicket) + "/" + storedFileName;
    }

    public Path resolveTicketAnexo(String identificadorArquivo) {
        if (identificadorArquivo == null || identificadorArquivo.isBlank()) {
            return null;
        }
        String rel = identificadorArquivo.replace('\\', '/').trim();
        if (rel.contains("..") || !rel.startsWith("tickets/")) {
            return null;
        }
        Path resolved = uploadRoot.resolve(rel).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            return null;
        }
        return resolved;
    }

    public static String sanitizeNumeroTicket(String numeroTicket) {
        if (numeroTicket == null) {
            return "sem-numero";
        }
        String limpo = numeroTicket.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        if (limpo.isEmpty()) {
            return "sem-numero";
        }
        return limpo.length() > 80 ? limpo.substring(0, 80) : limpo;
    }

    public String toPublicUrl(String fileName) {
        return "/uploads/analistas/" + fileName;
    }

    public String toConexaoHeaderChatsPublicUrl(String fileName) {
        return "/uploads/conexoes/header-chats/" + fileName;
    }

    public String toClienteHeaderChatsPublicUrl(String fileName) {
        return "/uploads/clientes/header-chats/" + fileName;
    }

    public Path resolveFromPublicUrl(String fotoUrl) {
        if (fotoUrl == null || !fotoUrl.startsWith("/uploads/analistas/")) {
            return null;
        }
        String fileName = fotoUrl.substring("/uploads/analistas/".length());
        return analistasDir.resolve(fileName);
    }

    public Path resolveConexaoHeaderChatsPublicUrl(String arteUrl) {
        return resolveHeaderChatsPublicUrl(arteUrl, "/uploads/conexoes/header-chats/", conexoesHeaderChatsDir);
    }

    public Path resolveClienteHeaderChatsPublicUrl(String arteUrl) {
        return resolveHeaderChatsPublicUrl(arteUrl, "/uploads/clientes/header-chats/", clientesHeaderChatsDir);
    }

    private static Path resolveHeaderChatsPublicUrl(String arteUrl, String prefix, Path baseDir) {
        if (arteUrl == null || !arteUrl.startsWith(prefix)) {
            return null;
        }
        String fileName = arteUrl.substring(prefix.length());
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return null;
        }
        Path resolved = baseDir.resolve(fileName).normalize();
        if (!resolved.startsWith(baseDir)) {
            return null;
        }
        return resolved;
    }
}
