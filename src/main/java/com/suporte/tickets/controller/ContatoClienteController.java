package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ContatoClienteRequestDTO;
import com.suporte.tickets.dto.ContatoClienteResponseDTO;
import com.suporte.tickets.service.ContatoClienteService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contatos-clientes")
@RequiredArgsConstructor
public class ContatoClienteController {

    private final ContatoClienteService contatoClienteService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping("/busca")
    public ResponseEntity<List<ContatoClienteResponseDTO>> pesquisar(
            @RequestParam(required = false) String termo,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.pesquisar(termo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContatoClienteResponseDTO> buscar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContatoClienteResponseDTO> atualizar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody ContatoClienteRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ContatoClienteResponseDTO> ativar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.ativar(id));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ContatoClienteResponseDTO> inativar(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.inativar(id));
    }

    @PatchMapping("/{id}/principal")
    public ResponseEntity<ContatoClienteResponseDTO> definirPrincipal(
            @PathVariable Integer id,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.definirPrincipal(id));
    }
}
