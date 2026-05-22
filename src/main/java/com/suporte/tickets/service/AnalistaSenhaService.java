package com.suporte.tickets.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AnalistaSenhaService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean senhaArmazenadaEhHash(String armazenada) {
        if (armazenada == null || armazenada.isBlank()) {
            return false;
        }
        return armazenada.startsWith("$2a$")
                || armazenada.startsWith("$2b$")
                || armazenada.startsWith("$2y$");
    }

    public String hashSenha(String senhaEmTexto) {
        return passwordEncoder.encode(senhaEmTexto);
    }

    public boolean senhaConfere(String senhaInformada, String senhaArmazenada) {
        if (senhaInformada == null || senhaArmazenada == null) {
            return false;
        }
        if (senhaArmazenadaEhHash(senhaArmazenada)) {
            return passwordEncoder.matches(senhaInformada, senhaArmazenada);
        }
        return senhaArmazenada.equals(senhaInformada);
    }

    public boolean deveMigrarParaHash(String senhaArmazenada) {
        return senhaArmazenada != null && !senhaArmazenada.isBlank() && !senhaArmazenadaEhHash(senhaArmazenada);
    }
}
