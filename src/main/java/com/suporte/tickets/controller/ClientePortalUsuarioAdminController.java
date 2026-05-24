package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ClientePortalUsuarioRequestDTO;
import com.suporte.tickets.dto.ClientePortalUsuarioResponseDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.service.ClientePortalService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cliente-portal/usuarios")
@RequiredArgsConstructor
public class ClientePortalUsuarioAdminController {

    private final ClientePortalService portalService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping
    public ResponseEntity<List<ClientePortalUsuarioResponseDTO>> listar(
            @RequestParam(required = false) Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        List<ClientePortalUsuarioResponseDTO> lista = clienteId != null
                ? portalService.listarPorCliente(clienteId)
                : portalService.listarUsuarios();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<ClientePortalUsuarioResponseDTO> criar(
            @Valid @RequestBody ClientePortalUsuarioRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        Analista executor = perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portalService.criar(request, executor.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientePortalUsuarioResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClientePortalUsuarioRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(portalService.atualizar(id, request));
    }
}
