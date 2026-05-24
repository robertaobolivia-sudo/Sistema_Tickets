package com.suporte.tickets.controller;

import com.suporte.tickets.dto.TicketResponseDTO;
import com.suporte.tickets.dto.TicketWebhookRequestDTO;
import com.suporte.tickets.service.PerfilAcessoAutorizacaoService;
import com.suporte.tickets.service.TicketService;
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

/**
 * Controlador REST para endpoints de webhook de tickets
 * 
 * Responsável por receber requisições HTTP e delegar o processamento
 * ao serviço de tickets.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final TicketService ticketService;
    private final PerfilAcessoAutorizacaoService perfilAcessoAutorizacaoService;

    /**
     * Endpoint para recebimento de tickets via webhook
     * 
     * Recebe um JSON com dados de um novo ticket e o processa,
     * criando automaticamente o ticket e o cliente se necessário.
     * 
     * URL: POST /api/webhooks/tickets
     * 
     * @param request DTO com dados do ticket
     * @return ResponseEntity com dados do ticket criado
     */
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponseDTO> receberTicketWebhook(
            @Valid @RequestBody TicketWebhookRequestDTO request,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);

        TicketResponseDTO ticketCriado = ticketService.criarTicketPorWebhook(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketCriado);
    }

    /**
     * Endpoint para buscar um ticket pelo número
     * 
     * URL: GET /api/webhooks/tickets/{numeroTicket}
     * 
     * @param numeroTicket Número do ticket (ex: TK-000001)
     * @return ResponseEntity com dados do ticket se encontrado
     */
    @GetMapping("/tickets/{numeroTicket}")
    public ResponseEntity<TicketResponseDTO> buscarTicket(
            @PathVariable String numeroTicket,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_ID, required = false) Long analistaId,
            @RequestHeader(value = PerfilAcessoAutorizacaoService.HEADER_ANALISTA_TOKEN, required = false) String analistaToken) {
        perfilAcessoAutorizacaoService.exigirSessaoValida(analistaId, analistaToken);
        TicketResponseDTO ticket = ticketService.buscarPorNumero(numeroTicket);
        return ResponseEntity.ok(ticket);
    }

}
