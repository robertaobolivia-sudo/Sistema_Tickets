package com.suporte.tickets.service;

import org.springframework.stereotype.Service;

/**
 * Politica minima de senha para cadastro e alteracao de analistas (Sprint 70).
 * Nao se aplica ao login nem a senhas ja armazenadas ate serem alteradas.
 */
@Service
public class AnalistaSenhaPoliticaService {

    public static final int TAMANHO_MINIMO = 8;

    public static final String MENSAGEM_REGRA =
            "Senha deve ter no minimo 8 caracteres, pelo menos 1 letra e pelo menos 1 numero.";

    /**
     * Valida senha informada em cadastro ou troca. Lança IllegalArgumentException se invalida.
     */
    public void validarSenhaInformada(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha e obrigatoria.");
        }
        if (!senhaAtendePolitica(senha)) {
            throw new IllegalArgumentException(MENSAGEM_REGRA);
        }
    }

    static boolean senhaAtendePolitica(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            return false;
        }
        boolean temLetra = false;
        boolean temNumero = false;
        for (int i = 0; i < senha.length(); i++) {
            char c = senha.charAt(i);
            if (Character.isLetter(c)) {
                temLetra = true;
            } else if (Character.isDigit(c)) {
                temNumero = true;
            }
            if (temLetra && temNumero) {
                return true;
            }
        }
        return temLetra && temNumero;
    }
}
