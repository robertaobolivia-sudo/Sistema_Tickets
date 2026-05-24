package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ClientePortalLoginRequestDTO;
import com.suporte.tickets.dto.ClientePortalLoginResponseDTO;
import com.suporte.tickets.service.ClientePortalAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente-portal/auth")
@RequiredArgsConstructor
public class ClientePortalLoginController {

    private final ClientePortalAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ClientePortalLoginResponseDTO> login(
            @Valid @RequestBody ClientePortalLoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_ID, required = false) Long usuarioId,
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_TOKEN, required = false) String token) {
        authService.validarSessao(usuarioId, token);
        authService.logout(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
