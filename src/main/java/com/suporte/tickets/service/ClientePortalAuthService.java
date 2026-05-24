package com.suporte.tickets.service;

import com.suporte.tickets.dto.ClientePortalLoginRequestDTO;
import com.suporte.tickets.dto.ClientePortalLoginResponseDTO;
import com.suporte.tickets.entity.ClientePortalUsuario;
import com.suporte.tickets.exception.AcessoNegadoException;
import com.suporte.tickets.exception.CredenciaisInvalidasException;
import com.suporte.tickets.repository.ClientePortalUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientePortalAuthService {

    public static final String HEADER_PORTAL_ID = "X-Portal-Id";
    public static final String HEADER_PORTAL_TOKEN = "X-Portal-Token";
    private static final int SESSAO_HORAS = 24;

    private final ClientePortalUsuarioRepository usuarioRepository;
    private final AnalistaSenhaService senhaService;

    @Transactional
    public ClientePortalLoginResponseDTO login(ClientePortalLoginRequestDTO request) {
        ClientePortalUsuario usuario = usuarioRepository
                .findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!Boolean.TRUE.equals(usuario.getAtivo())
                || !senhaService.senhaConfere(request.getSenha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        if (senhaService.deveMigrarParaHash(usuario.getSenha())) {
            usuario.setSenha(senhaService.hashSenha(request.getSenha()));
        }

        String token = gerarToken();
        usuario.setAuthToken(token);
        usuario.setAuthTokenExpiraEm(LocalDateTime.now().plusHours(SESSAO_HORAS));
        usuarioRepository.save(usuario);

        return new ClientePortalLoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCliente().getId(),
                usuario.getCliente().getNome(),
                token);
    }

    @Transactional
    public void logout(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(u -> {
            u.setAuthToken(null);
            u.setAuthTokenExpiraEm(null);
            usuarioRepository.save(u);
        });
    }

    public ClientePortalUsuario validarSessao(Long usuarioId, String token) {
        if (usuarioId == null) {
            throw new AcessoNegadoException("Id do usuario do portal obrigatorio (header " + HEADER_PORTAL_ID + ").");
        }
        if (token == null || token.isBlank()) {
            throw new AcessoNegadoException("Token de sessao obrigatorio (header " + HEADER_PORTAL_TOKEN + ").");
        }
        ClientePortalUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new AcessoNegadoException("Sessao invalida."));
        String armazenado = usuario.getAuthToken();
        if (armazenado == null || !tokensIguais(armazenado, token.trim())) {
            throw new AcessoNegadoException("Sessao invalida ou expirada.");
        }
        LocalDateTime expiraEm = usuario.getAuthTokenExpiraEm();
        if (expiraEm != null && LocalDateTime.now().isAfter(expiraEm)) {
            throw new AcessoNegadoException("Sessao expirada. Faca login novamente.");
        }
        return usuario;
    }

    private static String gerarToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private static boolean tokensIguais(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
