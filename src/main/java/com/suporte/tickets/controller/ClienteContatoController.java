package com.suporte.tickets.controller;

import com.suporte.tickets.dto.ContatoClienteRequestDTO;
import com.suporte.tickets.dto.ContatoClienteResponseDTO;
import com.suporte.tickets.service.ContatoClienteService;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteContatoController {

    private final ContatoClienteService contatoClienteService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @PostMapping("/{clienteId}/contatos")
    public ResponseEntity<ContatoClienteResponseDTO> criar(
            @PathVariable Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody ContatoClienteRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        request.setClienteId(clienteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(contatoClienteService.criar(clienteId, request));
    }

    @GetMapping("/{clienteId}/contatos")
    public ResponseEntity<List<ContatoClienteResponseDTO>> listar(
            @PathVariable Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.listarPorCliente(clienteId));
    }

    @GetMapping("/{clienteId}/contatos/ativos")
    public ResponseEntity<List<ContatoClienteResponseDTO>> listarAtivos(
            @PathVariable Integer clienteId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(contatoClienteService.listarAtivosPorCliente(clienteId));
    }
}
