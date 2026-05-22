package com.suporte.tickets.controller;

import com.suporte.tickets.dto.EtiquetaRequestDTO;
import com.suporte.tickets.dto.EtiquetaResponseDTO;
import com.suporte.tickets.service.EtiquetaService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping
    public ResponseEntity<List<EtiquetaResponseDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(etiquetaService.listarTodos());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<EtiquetaResponseDTO>> listarAtivas(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(etiquetaService.listarAtivas());
    }

    @PostMapping
    public ResponseEntity<EtiquetaResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody EtiquetaRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(etiquetaService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EtiquetaResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody EtiquetaRequestDTO dto) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(etiquetaService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<EtiquetaResponseDTO> ativar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(etiquetaService.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<EtiquetaResponseDTO> inativar(
            @PathVariable Long id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirAdminOuSupervisor(analistaId, analistaToken);
        return ResponseEntity.ok(etiquetaService.inativar(id));
    }
}
