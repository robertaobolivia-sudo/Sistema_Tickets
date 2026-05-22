package com.suporte.tickets.controller;

import com.suporte.tickets.dto.WhatsappMatrizRequestDTO;
import com.suporte.tickets.dto.WhatsappMatrizResponseDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.WhatsappMatrizService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp-matrizes")
@RequiredArgsConstructor
public class WhatsappMatrizController {

    private final WhatsappMatrizService whatsappMatrizService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @PostMapping
    public ResponseEntity<WhatsappMatrizResponseDTO> criar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody WhatsappMatrizRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(whatsappMatrizService.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<WhatsappMatrizResponseDTO>> listar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @RequestParam Integer clienteId) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(whatsappMatrizService.listarPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WhatsappMatrizResponseDTO> buscarPorId(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(whatsappMatrizService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WhatsappMatrizResponseDTO> atualizar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id,
            @Valid @RequestBody WhatsappMatrizRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(whatsappMatrizService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<WhatsappMatrizResponseDTO> ativar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(whatsappMatrizService.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<WhatsappMatrizResponseDTO> inativar(
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @PathVariable Integer id) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(whatsappMatrizService.inativar(id));
    }
}
