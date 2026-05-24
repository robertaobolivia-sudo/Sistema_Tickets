package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ClientePortalDashboardDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.entity.ClientePortalUsuario;
import com.suporte.tickets.service.ClientePortalAuthService;
import com.suporte.tickets.service.ClientePortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cliente-portal")
@RequiredArgsConstructor
public class ClientePortalController {

    private final ClientePortalAuthService authService;
    private final ClientePortalService portalService;

    @GetMapping("/dashboard")
    public ResponseEntity<ClientePortalDashboardDTO> dashboard(
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_ID, required = false) Long usuarioId,
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_TOKEN, required = false) String token) {
        ClientePortalUsuario usuario = authService.validarSessao(usuarioId, token);
        return ResponseEntity.ok(portalService.getDashboard(usuario.getCliente().getId()));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponseDTO>> tickets(
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_ID, required = false) Long usuarioId,
            @RequestHeader(value = ClientePortalAuthService.HEADER_PORTAL_TOKEN, required = false) String token) {
        ClientePortalUsuario usuario = authService.validarSessao(usuarioId, token);
        return ResponseEntity.ok(portalService.getTickets(usuario.getCliente().getId()));
    }
}
