package com.suporte.tickets.controller;

import com.suporte.tickets.dto.TicketInteracaoRequestDTO;
import com.suporte.tickets.dto.TicketInteracaoResponseDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketInteracaoService;
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
@RequestMapping("/api/tickets/{numeroTicket}/interacoes")
@RequiredArgsConstructor
public class TicketInteracaoController {

    private final TicketInteracaoService ticketInteracaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @PostMapping
    public ResponseEntity<TicketInteracaoResponseDTO> criarInteracaoManual(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken,
            @Valid @RequestBody TicketInteracaoRequestDTO request) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketInteracaoResponseDTO interacao = ticketInteracaoService.criarInteracaoManual(numeroTicket, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(interacao);
    }

    @GetMapping
    public ResponseEntity<List<TicketInteracaoResponseDTO>> listarPorTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        return ResponseEntity.ok(ticketInteracaoService.listarPorTicket(numeroTicket));
    }
}
