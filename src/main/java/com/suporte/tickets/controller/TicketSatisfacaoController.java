package com.suporte.tickets.controller;

import com.suporte.tickets.dto.TicketSatisfacaoRequestDTO;
import com.suporte.tickets.dto.TicketSatisfacaoRespostaRequestDTO;
import com.suporte.tickets.dto.TicketSatisfacaoResponseDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketSatisfacaoService;
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

@RestController
@RequestMapping("/api/tickets/{numeroTicket}/satisfacao")
@RequiredArgsConstructor
public class TicketSatisfacaoController {

    private final TicketSatisfacaoService ticketSatisfacaoService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    @GetMapping
    public ResponseEntity<TicketSatisfacaoResponseDTO> consultar(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketSatisfacaoResponseDTO dto = ticketSatisfacaoService.consultarPorNumeroTicket(numeroTicket);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<TicketSatisfacaoResponseDTO> registrar(
            @PathVariable String numeroTicket,
            @Valid @RequestBody TicketSatisfacaoRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketSatisfacaoResponseDTO criado = ticketSatisfacaoService.registrar(numeroTicket, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Resposta interna da pesquisa (sessão analista). Link/token público: sprint futura.
     */
    @PostMapping("/responder")
    public ResponseEntity<TicketSatisfacaoResponseDTO> responder(
            @PathVariable String numeroTicket,
            @Valid @RequestBody TicketSatisfacaoRespostaRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketSatisfacaoResponseDTO atualizado =
                ticketSatisfacaoService.responderAvaliacao(numeroTicket, request);
        return ResponseEntity.ok(atualizado);
    }
}
